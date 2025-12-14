package phase2;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String username;
    private String password;
    private double balance;
    private List<String> transactionHistory;

    public Account(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void addTransaction(String transaction) {
        transactionHistory.add(transaction);
    }

    public List<String> getTransactionHistory() {
        return transactionHistory;
    }
}
