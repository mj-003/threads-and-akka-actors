package zad2;

import java.util.Random;

class Account {
    private static final int MAX_INITIAL_BALANCE = 1000;
    private int balance;
    private final int id;

    public Account(int id) {
        balance = new Random().nextInt(MAX_INITIAL_BALANCE);
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public synchronized void deposit(int amount)
    {
        balance += amount;
        System.out.println("Deposited " + amount + " on account " + id);
    }

    public synchronized void withdraw(int amount)
    {
        if (balance < amount) {
            System.out.println("Not enough money on account " + id);
        }
        else {
            balance -= amount;
            System.out.println("Withdrew " + amount + " from account " + id);
        }
    }

    public synchronized void transfer(Account toAccount, int amount)
    {
        withdraw(amount);
        toAccount.deposit(amount);
        System.out.println("Transferred " + amount + " from account " + id + " to account " + toAccount.getId());
    }

    public int getBalance() {
        return balance;
    }
}
