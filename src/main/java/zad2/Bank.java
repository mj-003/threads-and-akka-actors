package zad2;

import java.util.ArrayList;
class Bank {
    final ArrayList<Account> accounts;

    public Bank(int numAccounts)
    {
        accounts = new ArrayList<>();
        for (int i = 0; i < numAccounts; i++) {
            accounts.add(new Account(i));
        }
    }

    public int getBalance(int account) {
        return accounts.get(account).getBalance();
    }

    public int getNumAccounts() {
        return accounts.size();
    }
}
