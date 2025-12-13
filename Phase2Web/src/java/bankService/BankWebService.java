/*
 * Bank Web Service for Phase 2 Bonus Task
 * Exposes banking functionality through web services for external applications
 */
package bankService;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web Service for Banking Operations
 * Allows external clients to check balances, deposit, withdraw, or transfer funds
 */
@WebService(serviceName = "BankWebService")
public class BankWebService {

    // In-memory storage for accounts (simulating database)
    private static Map<String, Account> accounts = new HashMap<>();
    private static Map<String, List<String>> transactionHistory = new HashMap<>();

    // Static initializer to create default accounts
    static {
        accounts.put("moataz", new Account("moataz", "123", 15000.0));
        accounts.put("wael", new Account("wael", "123", 1000.0));
        accounts.put("omar", new Account("omar", "123", 5000.0));
        accounts.put("khouly", new Account("khouly", "123", 5000.0));
        
        // Initialize transaction history
        transactionHistory.put("moataz", new ArrayList<>());
        transactionHistory.put("wael", new ArrayList<>());
        transactionHistory.put("omar", new ArrayList<>());
        transactionHistory.put("khouly", new ArrayList<>());
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * Web service operation - Hello test
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String name) {
        return "Hello " + name + "! Welcome to the Bank Web Service.";
    }

    /**
     * Web service operation - Login authentication
     */
    @WebMethod(operationName = "login")
    public String login(@WebParam(name = "username") String username, 
                        @WebParam(name = "password") String password) {
        Account account = accounts.get(username.toLowerCase());
        if (account != null && account.getPassword().equals(password)) {
            return "SUCCESS|Login successful. Welcome " + username + "!";
        }
        return "ERROR|Invalid username or password";
    }

    /**
     * Web service operation - Create new account
     */
    @WebMethod(operationName = "createAccount")
    public String createAccount(@WebParam(name = "username") String username,
                                @WebParam(name = "password") String password,
                                @WebParam(name = "initialBalance") double initialBalance) {
        String key = username.toLowerCase();
        if (accounts.containsKey(key)) {
            return "ERROR|Account already exists";
        }
        if (initialBalance < 0) {
            return "ERROR|Initial balance cannot be negative";
        }
        
        accounts.put(key, new Account(username, password, initialBalance));
        transactionHistory.put(key, new ArrayList<>());
        
        String timestamp = getTimestamp();
        transactionHistory.get(key).add(timestamp + " - Account created with initial balance: $" + initialBalance);
        
        return "SUCCESS|Account created successfully. Balance: $" + initialBalance;
    }

    /**
     * Web service operation - Check account balance
     */
    @WebMethod(operationName = "checkBalance")
    public String checkBalance(@WebParam(name = "username") String username,
                               @WebParam(name = "password") String password) {
        Account account = accounts.get(username.toLowerCase());
        if (account == null) {
            return "ERROR|Account not found";
        }
        if (!account.getPassword().equals(password)) {
            return "ERROR|Invalid password";
        }
        return "SUCCESS|Balance: $" + account.getBalance();
    }

    /**
     * Web service operation - Deposit funds
     */
    @WebMethod(operationName = "deposit")
    public String deposit(@WebParam(name = "username") String username,
                          @WebParam(name = "password") String password,
                          @WebParam(name = "amount") double amount) {
        if (amount <= 0) {
            return "ERROR|Deposit amount must be positive";
        }

        String key = username.toLowerCase();
        Account account = accounts.get(key);
        if (account == null) {
            return "ERROR|Account not found";
        }
        if (!account.getPassword().equals(password)) {
            return "ERROR|Invalid password";
        }

        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);

        String timestamp = getTimestamp();
        String transaction = timestamp + " - Deposit: $" + amount + ". New Balance: $" + newBalance;
        transactionHistory.get(key).add(transaction);

        return "SUCCESS|Deposit successful. New Balance: $" + newBalance;
    }

    /**
     * Web service operation - Withdraw funds
     */
    @WebMethod(operationName = "withdraw")
    public String withdraw(@WebParam(name = "username") String username,
                           @WebParam(name = "password") String password,
                           @WebParam(name = "amount") double amount) {
        if (amount <= 0) {
            return "ERROR|Withdrawal amount must be positive";
        }

        String key = username.toLowerCase();
        Account account = accounts.get(key);
        if (account == null) {
            return "ERROR|Account not found";
        }
        if (!account.getPassword().equals(password)) {
            return "ERROR|Invalid password";
        }
        if (account.getBalance() < amount) {
            return "ERROR|Insufficient funds. Current balance: $" + account.getBalance();
        }

        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance);

        String timestamp = getTimestamp();
        String transaction = timestamp + " - Withdrawal: $" + amount + ". New Balance: $" + newBalance;
        transactionHistory.get(key).add(transaction);

        return "SUCCESS|Withdrawal successful. New Balance: $" + newBalance;
    }

    /**
     * Web service operation - Transfer funds between accounts
     */
    @WebMethod(operationName = "transfer")
    public String transfer(@WebParam(name = "fromUsername") String fromUsername,
                           @WebParam(name = "password") String password,
                           @WebParam(name = "toUsername") String toUsername,
                           @WebParam(name = "amount") double amount) {
        if (amount <= 0) {
            return "ERROR|Transfer amount must be positive";
        }

        String fromKey = fromUsername.toLowerCase();
        String toKey = toUsername.toLowerCase();

        Account fromAccount = accounts.get(fromKey);
        Account toAccount = accounts.get(toKey);

        if (fromAccount == null) {
            return "ERROR|Source account not found";
        }
        if (!fromAccount.getPassword().equals(password)) {
            return "ERROR|Invalid password";
        }
        if (toAccount == null) {
            return "ERROR|Recipient account not found";
        }
        if (fromKey.equals(toKey)) {
            return "ERROR|Cannot transfer to yourself";
        }
        if (fromAccount.getBalance() < amount) {
            return "ERROR|Insufficient funds. Current balance: $" + fromAccount.getBalance();
        }

        double newFromBalance = fromAccount.getBalance() - amount;
        double newToBalance = toAccount.getBalance() + amount;

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        String timestamp = getTimestamp();
        String fromTransaction = timestamp + " - Transfer: $" + amount + " to " + toUsername + ". New Balance: $" + newFromBalance;
        String toTransaction = timestamp + " - Transfer: $" + amount + " from " + fromUsername + ". New Balance: $" + newToBalance;

        transactionHistory.get(fromKey).add(fromTransaction);
        transactionHistory.get(toKey).add(toTransaction);

        return "SUCCESS|Transfer successful. $" + amount + " transferred to " + toUsername + ". New Balance: $" + newFromBalance;
    }

    /**
     * Web service operation - Get transaction history
     */
    @WebMethod(operationName = "getTransactionHistory")
    public String getTransactionHistory(@WebParam(name = "username") String username,
                                        @WebParam(name = "password") String password) {
        String key = username.toLowerCase();
        Account account = accounts.get(key);
        
        if (account == null) {
            return "ERROR|Account not found";
        }
        if (!account.getPassword().equals(password)) {
            return "ERROR|Invalid password";
        }

        List<String> history = transactionHistory.get(key);
        if (history == null || history.isEmpty()) {
            return "SUCCESS|No transactions found";
        }

        StringBuilder sb = new StringBuilder("SUCCESS|");
        for (int i = 0; i < history.size(); i++) {
            sb.append(history.get(i));
            if (i < history.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Inner class to represent a bank account
     */
    private static class Account {
        private String username;
        private String password;
        private double balance;

        public Account(String username, String password, double balance) {
            this.username = username;
            this.password = password;
            this.balance = balance;
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
    }
}
