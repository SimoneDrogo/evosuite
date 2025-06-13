package org.evosuite.testcase.javapareser;

import java.util.HashMap;

public class VariabiliTracker {
    private HashMap<String, String> variabili;

    public VariabiliTracker() {
        variabili = new HashMap<>();
    }

    // Aggiunge una variabile, ma segnala errore se il nome esiste già
    public void aggiungiVariabile(String nome, String tipo) {
        if (variabili.containsKey(nome)) {
            throw new IllegalArgumentException("La variabile '" + nome + "' è già stata dichiarata.");
        }
        variabili.put(nome, tipo);
    }

    public String getTipo(String nome) {
        return variabili.get(nome);
    }

    public void stampaVariabili() {
        for (String nome : variabili.keySet()) {
            System.out.println(nome + " -> " + variabili.get(nome));
        }
    }
}