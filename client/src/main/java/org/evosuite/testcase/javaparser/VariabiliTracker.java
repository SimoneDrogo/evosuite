package org.evosuite.testcase.javaparser;

import java.util.HashMap;

import org.evosuite.testcase.variable.VariableReference;

public class VariabiliTracker {
	private HashMap<String, VariableReference> variabili;

	public VariabiliTracker() {
		variabili = new HashMap<>();
	}

	
	public boolean aggiungiVariabile(String nome, VariableReference variable) {
		if (variabili.containsKey(nome)) {
			return false;
		}
		variabili.put(nome, variable);
		return true;
	}

	public VariableReference getReference(String nome) {
		return variabili.get(nome);
	}

	public void stampaVariabili() {
		for (String nome : variabili.keySet()) {
			System.out.println(nome + " -> " + variabili.get(nome));
		}
	}

	public boolean contiene(String nome) {

		return variabili.containsKey(nome);
	}

}