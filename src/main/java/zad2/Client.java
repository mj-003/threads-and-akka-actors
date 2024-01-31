package zad2;

import java.util.Random;

class Client extends Thread {
        private static int nextId = 1;
        private final int clientId;
        private final Bank bank;
        private boolean running;

        public Client(Bank bank)
        {
            clientId = nextId++;
            this.bank = bank;
            running = true;
        }

        public void stopClient() {
            running = false;
        }

        public int getClientId() {
            return clientId;
        }

        public int getBalance() {
            return bank.getBalance(clientId % bank.getNumAccounts());
        }

        @Override
        public void run()
        {
            Random random = new Random();
            while (running)
            {
                int fromAccount = clientId % bank.getNumAccounts();
                int toAccount = random.nextInt(bank.getNumAccounts());
                int amount = random.nextInt(100) + 1;

                switch (random.nextInt(4)) {
                    case 0 -> bank.accounts.get(fromAccount).deposit(amount);
                    case 1 -> bank.accounts.get(fromAccount).withdraw(amount);
                    case 3 -> bank.accounts.get(fromAccount).transfer(bank.accounts.get(toAccount), amount);
                }

                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }