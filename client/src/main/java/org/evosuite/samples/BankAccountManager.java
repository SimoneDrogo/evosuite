package org.evosuite.samples;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccountManager {
    private final Map<String, BankAccount> accounts = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void createAccount(String accountId, String owner, double initialBalance) {
        if (accountId == null || accountId.isEmpty() || initialBalance < 0) {
            throw new IllegalArgumentException("Invalid account details");
        }

        lock.lock();
        try {
            if (accounts.containsKey(accountId)) {
                throw new IllegalStateException("Account already exists");
            }
            accounts.put(accountId, new BankAccount(accountId, owner, initialBalance));
        } finally {
            lock.unlock();
        }
    }

    public void deposit(String accountId, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        getAccount(accountId).deposit(amount);
    }

    public void withdraw(String accountId, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
        getAccount(accountId).withdraw(amount);
    }

    public void transfer(String fromAccountId, String toAccountId, double amount) {
        if (amount <= 0 || fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Invalid transfer");
        }

        // Evita deadlock ordinando i lock per ID
        BankAccount from = getAccount(fromAccountId);
        BankAccount to = getAccount(toAccountId);
        BankAccount first = fromAccountId.compareTo(toAccountId) < 0 ? from : to;
        BankAccount second = fromAccountId.compareTo(toAccountId) < 0 ? to : from;

        synchronized (first) {
            synchronized (second) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }

    public double getBalance(String accountId) {
        return getAccount(accountId).getBalance();
    }

    private BankAccount getAccount(String accountId) {
        lock.lock();
        try {
            BankAccount account = accounts.get(accountId);
            if (account == null) throw new NoSuchElementException("Account not found");
            return account;
        } finally {
            lock.unlock();
        }
    }

    // Classe interna che rappresenta un singolo conto bancario
    private static class BankAccount {
        private final String accountId;
        private final String owner;
        private double balance;

        public BankAccount(String accountId, String owner, double balance) {
            this.accountId = accountId;
            this.owner = owner;
            this.balance = balance;
        }

        public synchronized void deposit(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            balance += amount;
        }

        public synchronized void withdraw(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            if (amount > balance) throw new IllegalStateException("Insufficient funds");
            balance -= amount;
        }

        public synchronized double getBalance() {
            return balance;
        }

        public String getOwner() {
            return owner;
        }
    }
}
