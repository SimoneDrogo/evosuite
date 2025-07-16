package org.evosuite.samples;

public class ProvaBanca {

	private double balance;
    private final double interestRate;

    public ProvaBanca(double initialBalance, double interestRate) {
        this.balance = initialBalance;
        this.interestRate = interestRate;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        }
    }

    public void applyInterest() {
        balance += balance * interestRate;
    }

    public double getBalance() {
        return balance;
    }

    public static boolean isPositive(double value) {
        return value >= 0;
    }
}