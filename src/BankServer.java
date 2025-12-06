import java.io.*;
import java.net.*;
import java.util.*;

public class BankServer {
    private static final int PORT = 8000;
    private static Map<String, Account> accounts = new HashMap<>();
    private static List<ClientHandler> connectedClients = new ArrayList<>();

    public static void main(String[] args) {
        initializeAccounts();

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Bank Server started on port " + PORT);
            System.out.println("Waiting for clients...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Create a new client handler thread for each client
                ClientHandler handler = new ClientHandler(clientSocket);
                connectedClients.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeAccounts() {
        accounts.put("moataz", new Account("mohamed", "123", 15000.0));
        accounts.put("wael", new Account("mohamed", "123", 1000.0));
        accounts.put("khouly", new Account("khouly", "123", 5000.0));
        accounts.put("omar", new Account("omar", "123", 5000.0));
    }

    public static Account getAccount(String username) {
        return accounts.get(username);
    }

    public static boolean authenticateUser(String username, String password) {
        Account account = accounts.get(username);
        return account != null && account.getPassword().equals(password);
    }

    public static void broadcastUpdate(String message) {
        for (ClientHandler client : connectedClients) {
            if (client.isAuthenticated()) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
    }
}

class Account {
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
