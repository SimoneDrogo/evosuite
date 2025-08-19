package org.evosuite.samples;



import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Classe progettata per generare branch e stati interessanti per EvoSuite
 * senza mai lanciare eccezioni (Runtime o Checked).
 *
 * Linee guida anti-eccezione:
 * - Tutti i metodi accettano null e input malformati, restituendo valori di default.
 * - Nessuna divisione per zero: controlli sempre presenti.
 * - Nessun accesso fuori dai limiti: tutte le collezioni sono verificate.
 * - Nessuna NPE: ogni accesso a riferimento è preceduto da verifica.
 * - Nessun throw in tutto il file.
 *
 * Funzionalità:
 * - Ingest di misurazioni per utente (valore, timestamp).
 * - Scoring con decadimento e finestra scorrevole.
 * - Predizione “next” tramite trend semplice + smoothing.
 * - Spiegazione testuale deterministica.
 * - Parser di configurazioni "k=v;..." tollerante agli errori.
 * - Raccomandazioni top-K con tie-breaking deterministico.
 * - Cache LRU dei punteggi per esercitare eviction.
 * - Macchina a stati dell'utente (IDLE, WARMUP, ACTIVE, COOLING).
 */
public final class EvoComplex {

    // ---- configurazione runtime, modificabile via configure(...) ----
    private volatile double decay = 0.96;   // in [0.8, 0.999]
    private volatile int window = 8;        // in [3, 64]
    private volatile Mode mode = Mode.MEAN; // MEAN | MEDIAN | ROBUST

    // ---- stato per utente ----
    private final Map<String, UserState> users = new ConcurrentHashMap<>();

    // ---- cache LRU per score calcolati ----
    private final ScoreCache cache;

    // ---- PRNG deterministico per tie-breaking, seed stabile ----
    private final XorShift64 prng;

    // contatore globale per differenziare seeds in istanze diverse
    private static final AtomicLong GLOBAL_COUNTER = new AtomicLong(1);

    public EvoComplex() {
        this(256, null);
    }

    public EvoComplex(int cacheCapacity, String seedHint) {
        int cap = clamp(cacheCapacity, 16, 4096);
        this.cache = new ScoreCache(cap);
        long base = 0x9E3779B97F4A7C15L ^ System.identityHashCode(this);
        long hint = (seedHint == null ? 0L : mix64(seedHint.hashCode()));
        long counter = GLOBAL_COUNTER.getAndIncrement();
        long seed = mix64(base ^ hint ^ counter);
        this.prng = new XorShift64(seed == 0 ? 0xCAFEBABEDEADBEEFL : seed);
    }

    // --------------------- API PUBBLICA ------------------------

    /**
     * Ingerisce una misura per un utente.
     * Accetta tutti i valori; NaN/infiniti vengono normalizzati in 0.0.
     * timestamp<=0 viene sostituito da contatore crescente locale.
     */
    public synchronized void ingest(String userId, Double value, Long timestamp) {
        String id = safe(userId);
        double v = normalize(value);
        long t = (timestamp == null || timestamp <= 0) ? nextLogicalTime() : timestamp;

        UserState st = users.computeIfAbsent(id, k -> new UserState());
        st.addSample(v, t, window);

        // Aggiorna macchina a stati in base all'attività
        double activity = st.activityLevel();
        if (activity < 0.15) st.phase = Phase.IDLE;
        else if (activity < 0.35) st.phase = Phase.WARMUP;
        else if (activity < 0.75) st.phase = Phase.ACTIVE;
        else st.phase = Phase.COOLING;

        // invalidiamo cache per questo utente
        cache.remove(id);
    }

    /**
     * Calcola uno score per l'utente, dipendente da mode/decay/window.
     * Non lancia eccezioni; se utente sconosciuto -> 0.0.
     */
    public synchronized double score(String userId) {
        String id = safe(userId);
        Double cached = cache.get(id);
        if (cached != null && Double.isFinite(cached)) return cached;

        UserState st = users.get(id);
        double result;
        if (st == null || st.values.isEmpty()) {
            result = 0.0;
        } else {
            List<Double> recent = st.lastN(window);
            if (recent.isEmpty()) {
                result = 0.0;
            } else {
                switch (mode) {
                    case MEDIAN:
                        result = median(recent);
                        break;
                    case ROBUST:
                        result = trimmedMean(recent, 0.2);
                        break;
                    case MEAN:
                    default:
                        result = ema(recent, decay);
                        break;
                }
                // leggera modulazione per fase utente
                result *= (1.0 + st.phase.modMultiplier);
                // clamp finale per evitare infiniti/NaN
                result = clamp(result, -1e12, 1e12);
                if (!Double.isFinite(result)) result = 0.0;
            }
        }
        cache.put(id, result);
        return result;
    }

    /**
     * Predice un "next value" semplice combinando trend e smoothing.
     * Restituisce Optional.empty() se non ci sono dati.
     */
    public synchronized Optional<Double> predictNext(String userId) {
        String id = safe(userId);
        UserState st = users.get(id);
        if (st == null || st.values.size() < 2) return Optional.empty();
        List<Double> recent = st.lastN(Math.max(3, Math.min(window, st.values.size())));
        double smoothed = ema(recent, decay);

        // trend lineare robusto: differenze recenti mediana
        List<Double> diffs = new ArrayList<>();
        for (int i = 1; i < recent.size(); i++) {
            double d = safeMinus(recent.get(i), recent.get(i - 1));
            diffs.add(d);
        }
        double trend = diffs.isEmpty() ? 0.0 : median(diffs);

        double next = smoothed + 0.5 * trend;
        next = clamp(next, -1e12, 1e12);
        if (!Double.isFinite(next)) next = 0.0;
        return Optional.of(next);
    }

    /**
     * Fornisce una spiegazione testuale deterministica dello score.
     * Mai null, mai eccezioni.
     */
    public synchronized String explain(String userId) {
        String id = safe(userId);
        UserState st = users.get(id);
        double sc = score(id);
        StringBuilder sb = new StringBuilder(128);
        sb.append("User=").append(id.isEmpty() ? "<anon>" : id)
          .append(" phase=").append(st == null ? "NONE" : st.phase.name())
          .append(" mode=").append(mode.name())
          .append(" decay=").append(to3(decay))
          .append(" window=").append(window)
          .append(" score=").append(to3(sc));

        if (st == null || st.values.isEmpty()) {
            sb.append(" | no-data");
        } else {
            List<Double> r = st.lastN(Math.min(window, st.values.size()));
            double vol = robustStd(r);
            sb.append(" | n=").append(st.values.size())
              .append(" recentVol=").append(to3(vol));
            predictNext(id).ifPresent(nx -> sb.append(" next≈").append(to3(nx)));
        }
        return sb.toString();
    }

    /**
     * Configura la classe con una mini-DSL "k=v; k=v; ..."
     * Chiavi supportate:
     *  - decay ∈ [0.8, 0.999]
     *  - window ∈ [3, 64]
     *  - mode ∈ {mean, median, robust}
     * Input malformato viene ignorato senza errori.
     * Ritorna una mappa con i valori effettivamente applicati.
     */
    public synchronized Map<String, String> configure(String config) {
        Map<String, String> applied = new LinkedHashMap<>();
        String cfg = config == null ? "" : config;
        String[] parts = cfg.split("[;\\n]");
        for (String raw : parts) {
            String line = raw == null ? "" : raw.trim();
            if (line.isEmpty()) continue;
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            String key = line.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            String val = line.substring(eq + 1).trim();

            switch (key) {
                case "decay": {
                    Double d = parseDoubleSafe(val);
                    if (d != null) {
                        double nd = clamp(d, 0.8, 0.999);
                        this.decay = nd;
                        applied.put("decay", String.valueOf(nd));
                    }
                    break;
                }
                case "window": {
                    Integer w = parseIntSafe(val);
                    if (w != null) {
                        int nw = clamp(w, 3, 64);
                        this.window = nw;
                        applied.put("window", String.valueOf(nw));
                    }
                    break;
                }
                case "mode": {
                    Mode m = Mode.fromString(val);
                    if (m != null) {
                        this.mode = m;
                        applied.put("mode", m.name());
                    }
                    break;
                }
                default:
                    // chiave sconosciuta: ignoriamo senza eccezioni
                    break;
            }
        }
        // invalida tutta la cache, poiché i parametri influiscono sul calcolo
        cache.clear();
        return applied;
    }

    /**
     * Restituisce la classifica top-K degli userId in base a score().
     * k fuori range viene clippato; set nullo o vuoto -> lista vuota.
     * Tie-breaking deterministico basato su PRNG seedato.
     */
    public synchronized List<String> recommend(Set<String> userIds, int k) {
        List<String> result = new ArrayList<>();
        if (userIds == null || userIds.isEmpty()) return result;

        int kk = clamp(k, 1, userIds.size());
        List<String> ids = new ArrayList<>(userIds.size());
        for (String u : userIds) {
            ids.add(safe(u));
        }

        // calcola gli score in anticipo
        Map<String, Double> scores = new HashMap<>();
        for (String id : ids) {
            scores.put(id, score(id));
        }

        // ordina per score desc, tie-break con prng deterministico
        ids.sort((a, b) -> {
            double sa = safeGet(scores.get(a));
            double sb = safeGet(scores.get(b));
            if (sa == sb) {
                long ra = prng.mixForKey(a);
                long rb = prng.mixForKey(b);
                // attenzione a overflow: confrontiamo come long
                return Long.compare(rb, ra);
            }
            return Double.compare(sb, sa);
        });

        // sublist sicura
        for (int i = 0; i < kk && i < ids.size(); i++) {
            result.add(ids.get(i));
        }
        return result;
    }

    /**
     * Analisi “what-if”: applica in modo non distruttivo una sequenza ipotetica
     * e restituisce score e prossima fase attesa.
     */
    public synchronized Map<String, Object> whatIf(String userId, List<Double> hypothetical) {
        String id = safe(userId);
        List<Double> hs = hypothetical == null ? Collections.emptyList() : hypothetical;

        UserState st = users.get(id);
        UserState tmp = (st == null ? new UserState() : st.copy());
        long t = nextLogicalTime();
        for (Double d : hs) {
            tmp.addSample(normalize(d), t++, window);
        }
        double s;
        if (tmp.values.isEmpty()) s = 0.0;
        else {
            List<Double> recent = tmp.lastN(window);
            s = (mode == Mode.MEDIAN) ? median(recent)
                    : (mode == Mode.ROBUST) ? trimmedMean(recent, 0.2)
                    : ema(recent, decay);
            s *= (1.0 + tmp.phase.modMultiplier);
            if (!Double.isFinite(s)) s = 0.0;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("user", id);
        out.put("score", s);
        out.put("phase", tmp.phase.name());
        out.put("n", tmp.values.size());
        return out;
    }

    // --------------------- SUPPORTO E UTILITIES ------------------------

    private static String safe(String s) { return s == null ? "" : s; }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        if (!Double.isFinite(v)) return 0.0;
        return Math.max(lo, Math.min(hi, v));
    }

    private static Double parseDoubleSafe(String s) {
        if (s == null) return null;
        try {
            double v = Double.parseDouble(s.trim());
            if (!Double.isFinite(v)) return null;
            return v;
        } catch (Exception ignored) { return null; }
    }

    private static Integer parseIntSafe(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception ignored) { return null; }
    }

    private static double normalize(Double v) {
        if (v == null || !Double.isFinite(v)) return 0.0;
        // clamp dolce per evitare estremi patologici
        double x = clamp(v, -1e9, 1e9);
        // leggera saturazione non lineare per creare branch
        double ax = Math.abs(x);
        if (ax > 1e6) x = Math.signum(x) * (1e6 + Math.log1p(ax - 1e6));
        return x;
    }

    private static double ema(List<Double> xs, double decay) {
        if (xs == null || xs.isEmpty()) return 0.0;
        double d = clamp(decay, 0.8, 0.999);
        double a = 1.0 - d;
        double s = 0.0;
        for (double x : xs) {
            double xv = Double.isFinite(x) ? x : 0.0;
            s = d * s + a * xv;
        }
        return s;
    }

    private static double median(List<Double> xs) {
        if (xs == null || xs.isEmpty()) return 0.0;
        List<Double> copy = new ArrayList<>(xs.size());
        for (Double d : xs) copy.add(Double.isFinite(d) ? d : 0.0);
        Collections.sort(copy);
        int n = copy.size();
        if ((n & 1) == 1) return copy.get(n / 2);
        return 0.5 * (copy.get(n / 2 - 1) + copy.get(n / 2));
    }

    private static double trimmedMean(List<Double> xs, double alpha) {
        if (xs == null || xs.isEmpty()) return 0.0;
        int n = xs.size();
        if (n < 3) return ema(xs, 0.9);
        List<Double> copy = new ArrayList<>(n);
        for (Double d : xs) copy.add(Double.isFinite(d) ? d : 0.0);
        Collections.sort(copy);
        int k = (int) Math.floor(alpha * n);
        int lo = Math.min(Math.max(0, k), n - 1);
        int hi = Math.max(lo + 1, n - lo);
        double sum = 0.0;
        int cnt = 0;
        for (int i = lo; i < hi; i++) {
            sum += copy.get(i);
            cnt++;
        }
        if (cnt <= 0) return 0.0;
        return sum / cnt;
    }

    private static double robustStd(List<Double> xs) {
        if (xs == null || xs.isEmpty()) return 0.0;
        double m = median(xs);
        List<Double> dev = new ArrayList<>(xs.size());
        for (Double d : xs) dev.add(Math.abs((Double.isFinite(d) ? d : 0.0) - m));
        return median(dev) * 1.4826; // MAD->std approx
    }

    private static String to3(double v) {
        if (!Double.isFinite(v)) return "0.000";
        return String.format(Locale.ROOT, "%.3f", v);
    }

    private static double safeMinus(Double a, Double b) {
        double x = (a == null || !Double.isFinite(a)) ? 0.0 : a;
        double y = (b == null || !Double.isFinite(b)) ? 0.0 : b;
        return x - y;
    }

    private static long nextLogicalTime() {
        // contatore pseudo-temporale (non dipende dall'orologio di sistema)
        return GLOBAL_COUNTER.getAndIncrement();
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    }

    // --------------------- CLASSI INTERNE ------------------------

    private enum Mode {
        MEAN, MEDIAN, ROBUST;

        static Mode fromString(String s) {
            if (s == null) return null;
            String k = s.trim().toLowerCase(Locale.ROOT);
            switch (k) {
                case "mean": return MEAN;
                case "median": return MEDIAN;
                case "robust": return ROBUST;
                default: return null;
            }
        }
    }

    private enum Phase {
        IDLE( -0.05),
        WARMUP(0.00),
        ACTIVE(0.05),
        COOLING(-0.02);

        final double modMultiplier;
        Phase(double m) { this.modMultiplier = m; }
    }

    private static final class UserState {
        final Deque<Double> values = new ArrayDeque<>(64);
        final Deque<Long> times = new ArrayDeque<>(64);
        Phase phase = Phase.IDLE;

        void addSample(double v, long t, int maxWindow) {
            // manteniamo una cronologia moderata per favorire branch eviction
            values.addLast(v);
            times.addLast(t);
            int limit = Math.max(4, Math.min(256, maxWindow * 3));
            while (values.size() > limit) {
                values.removeFirst();
                times.removeFirst();
            }
        }

        List<Double> lastN(int n) {
            int need = Math.max(1, n);
            List<Double> out = new ArrayList<>(need);
            Iterator<Double> it = values.descendingIterator();
            int c = 0;
            while (it.hasNext() && c < need) {
                out.add(it.next());
                c++;
            }
            Collections.reverse(out);
            return out;
        }

        double activityLevel() {
            if (values.isEmpty()) return 0.0;
            List<Double> r = lastN(Math.min(8, values.size()));
            double vol = robustStd(r);
            double mag = Math.min(1.0, Math.abs(ema(r, 0.9)) / (1.0 + vol));
            return clamp(mag, 0.0, 1.0);
        }

        UserState copy() {
            UserState c = new UserState();
            c.values.addAll(this.values);
            c.times.addAll(this.times);
            c.phase = this.phase;
            return c;
        }
    }

    private static final class ScoreCache extends LinkedHashMap<String, Double> {
        private final int capacity;
        ScoreCache(int capacity) {
            super(Math.max(16, capacity), 0.75f, true);
            this.capacity = Math.max(16, capacity);
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Double> eldest) {
            return size() > capacity;
        }
        public void clear() { super.clear(); }
    }

    /**
     * PRNG semplice (xorshift64*) + funzione di mix per chiavi stringa,
     * usata per tie-breaking deterministico.
     */
    private static final class XorShift64 {
        private long x;
        XorShift64(long seed) { this.x = (seed == 0 ? 0x2545F4914F6CDD1DL : seed); }
        long nextLong() {
            long z = x += 0x9E3779B97F4A7C15L;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            return z ^ (z >>> 31);
        }
        long mixForKey(String key) {
            if (key == null) return nextLong();
            long h = 1469598103934665603L;
            for (int i = 0; i < key.length(); i++) {
                h ^= key.charAt(i);
                h *= 1099511628211L;
            }
            // combina con stato corrente per evitare collisioni banali
            return mix64(h ^ x ^ 0xD6E8FEB86659FD93L);
        }
    }

    private static double safeGet(Double d) {
        return (d == null || !Double.isFinite(d)) ? 0.0 : d;
    }
}