package org.evosuite.testcase.javaparser;

import java.util.HashMap;

import org.evosuite.testcase.variable.VariableReference;

public class VariabiliTracker {
    private HashMap<String, VariableReference> variabili;

    public VariabiliTracker() {
        variabili = new HashMap<>();
    }

    // Aggiunge una variabile, ma segnala errore se il nome esiste già
    public void aggiungiVariabile(String nome, VariableReference variable) {
        if (variabili.containsKey(nome)) {
            System.out.println("Attenzione: la variabile '" + nome + "' è già stata dichiarata. Ignorata.");
            return;
        }
        variabili.put(nome, variable);
    }
    public VariableReference getRefernce(String nome) {
        return variabili.get(nome);
    }

    public void stampaVariabili() {
        for (String nome : variabili.keySet()) {
            System.out.println(nome + " -> " + variabili.get(nome));
        }
    }
    
    public boolean contiene (String nome) {
    	
    	
    	return variabili.containsKey(nome);
    }
    
}