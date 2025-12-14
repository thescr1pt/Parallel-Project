package phase2;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BankServiceImpl extends UnicastRemoteObject implements BankService {
    private Map<String, Account> accounts;
    private TransactionServiceImpl transactionService;

    public BankServiceImpl(TransactionServiceImpl transactionService) throws RemoteException {
        this.accounts = new HashMap<>();
        this.transactionService = transactionService;
        initializeAccounts();
    }

    private void initializeAccounts() {
        accounts.put("moataz", new Account("moataz", "123", 15000.0));
        accounts.put("wael", new Account("wael", "123", 1000.0));
        accounts.put("omar", new Account("omar", "123", 5000.0));
        accounts.put("khouly", new Account("khouly", "123", 5000.0));
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        Account account = accounts.get(username.toLowerCase());
        if (account != null && account.getPassword().equals(password)) {
            System.out.println("Client " + username + " logged in successfully.");
            return true;
        }
        System.out.println("Failed login attempt for: " + username);
        return false;
    }

    @Override
    public String createAccount(String username, String password, double initialBalance) throws RemoteException {
        String key = username.toLowerCase();
        if (accounts.containsKey(key)) {
            return "ERROR|Account already exists";
        }
        if (initialBalance < 0) {
            return "ERROR|Initial balance cannot be negative";
        }

        accounts.put(key, new Account(username, password, initialBalance));
        System.out.println("New account created: " + username + " with balance $" + initialBalance);
        return "SUCCESS|Account created successfully. Balance: $" + initialBalance;
    }

    @Override
    public double checkBalance(String username) throws RemoteException {
        Account account = accounts.get(username.toLowerCase());
        if (account != null) {
            return account.getBalance();
        }
        return -1;
    }

    @Override
    public String deposit(String username, double amount) throws RemoteException {
        if (amount <= 0) {
            return "ERROR|Deposit amount must be positive";
        }

        Account account = accounts.get(username.toLowerCase());
        if (account == null) {
            return "ERROR|Account not found";
        }

        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);

        String timestamp = getTimestamp();
        String transaction = timestamp + " - Deposit: $" + amount + ". New Balance: $" + newBalance;
        account.addTransaction(transaction);

        System.out.println(username + " - " + transaction);

        transactionService.broadcastUpdate(username + " deposited $" + amount, username);

        return "SUCCESS|Deposit successful. New Balance: $" + newBalance;
    }

    @Override
    public String withdraw(String username, double amount) throws RemoteException {
        if (amount <= 0) {
            return "ERROR|Withdrawal amount must be positive";
        }

        Account account = accounts.get(username.toLowerCase());
        if (account == null) {
            return "ERROR|Account not found";
        }

        if (account.getBalance() < amount) {
            return "ERROR|Insufficient funds";
        }

        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance);

        String timestamp = getTimestamp();
        String transaction = timestamp + " - Withdrawal: $" + amount + ". New Balance: $" + newBalance;
        account.addTransaction(transaction);

        System.out.println(username + " - " + transaction);

        transactionService.broadcastUpdate(username + " withdrew $" + amount, username);

        return "SUCCESS|Withdrawal successful. New Balance: $" + newBalance;
    }

    @Override
    public String transfer(String fromUsername, String toUsername, double amount) throws RemoteException {
        if (amount <= 0) {
            return "ERROR|Transfer amount must be positive";
        }

        Account fromAccount = accounts.get(fromUsername.toLowerCase());
        Account toAccount = accounts.get(toUsername.toLowerCase());

        if (fromAccount == null) {
            return "ERROR|Source account not found";
        }
        if (toAccount == null) {
            return "ERROR|Recipient account not found";
        }
        if (fromUsername.equalsIgnoreCase(toUsername)) {
            return "ERROR|Cannot transfer to yourself";
        }
        if (fromAccount.getBalance() < amount) {
            return "ERROR|Insufficient funds";
        }

        double newFromBalance = fromAccount.getBalance() - amount;
        double newToBalance = toAccount.getBalance() + amount;

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        String timestamp = getTimestamp();
        String fromTransaction = timestamp + " - Transfer: $" + amount + " to " + toUsername + ". New Balance: $" + newFromBalance;
        String toTransaction = timestamp + " - Transfer: $" + amount + " from " + fromUsername + ". New Balance: $" + newToBalance;

        fromAccount.addTransaction(fromTransaction);
        toAccount.addTransaction(toTransaction);

        System.out.println("Transfer: $" + amount + " from " + fromUsername + " to " + toUsername +
                ". " + fromUsername + " Balance: $" + newFromBalance +
                ", " + toUsername + " Balance: $" + newToBalance);

        transactionService.broadcastUpdate(fromUsername + " transferred $" + amount + " to " + toUsername, fromUsername);

        return "SUCCESS|Transfer successful. New Balance: $" + newFromBalance;
    }

    public Account getAccount(String username) {
        return accounts.get(username.toLowerCase());
    }
}
