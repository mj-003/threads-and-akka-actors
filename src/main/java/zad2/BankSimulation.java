package zad2;

import java.util.ArrayList;

public class BankSimulation {
    private static final int NUM_CLIENTS = 5;
    private static final int NUM_ACCOUNTS = 10;

    public static void main(String[] args)
    {
        Bank bank = new Bank(NUM_ACCOUNTS);
        ArrayList<Client> clients = new ArrayList<>();

        for (int i = 0; i < NUM_CLIENTS; i++) {
            clients.add(new Client(bank));
        }

        for (Client client : clients) {
            client.start();
        }

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Client client : clients) {
            client.stopClient();
        }

        System.out.println("Final balances:");
        for (Client client : clients) {
            System.out.println("Client " + client.getClientId() + ": Balance: " + client.getBalance());
        }
    }
}