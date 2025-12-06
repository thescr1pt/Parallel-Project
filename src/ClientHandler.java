import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler extends Thread {
    private Socket socket;
    private DataInputStream fromClient;
    private DataOutputStream toClient;
    private String currentUser;
    private boolean authenticated;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.authenticated = false;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void sendMessage(String message) {
        try {
            toClient.writeUTF(message);
            toClient.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to " + currentUser + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            fromClient = new DataInputStream(socket.getInputStream());
            toClient = new DataOutputStream(socket.getOutputStream());

            if (!handleAuthentication()) {
                socket.close();
                return;
            }

            System.out.println("Client " + currentUser + " logged in successfully.");

            while (true) {
                String request = fromClient.readUTF();
                System.out.println("Request from " + currentUser + ": " + request);

                if (request.equalsIgnoreCase("exit")) {
                    break;
                }

                processRequest(request);
            }

        } catch (EOFException e) {
            System.out.println("Client " + currentUser + " disconnected.");
        } catch (IOException e) {
            System.err.println("Error handling client " + currentUser + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private boolean handleAuthentication() throws IOException {
        toClient.writeUTF("Enter username:");
        toClient.flush();
        String username = fromClient.readUTF();

        toClient.writeUTF("Enter password:");
        toClient.flush();
        String password = fromClient.readUTF();

        if (BankServer.authenticateUser(username, password)) {
            currentUser = username;
            authenticated = true;
            Account account = BankServer.getAccount(username);
            toClient.writeUTF("SUCCESS");
            toClient.writeDouble(account.getBalance());
            toClient.flush();
            return true;
        } else {
            toClient.writeUTF("FAILED");
            toClient.flush();
            return false;
        }
    }

    private void processRequest(String request) throws IOException {
        String[] parts = request.split("\\|");
        String action = parts[0];

        Account account = BankServer.getAccount(currentUser);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        switch (action.toUpperCase()) {
            case "BALANCE":
                handleBalanceCheck(account);
                break;

            case "DEPOSIT":
                if (parts.length < 2) {
                    toClient.writeUTF("ERROR|Invalid deposit format");
                    toClient.flush();
                    return;
                }
                handleDeposit(account, Double.parseDouble(parts[1]), timestamp);
                break;

            case "WITHDRAW":
                if (parts.length < 2) {
                    toClient.writeUTF("ERROR|Invalid withdrawal format");
                    toClient.flush();
                    return;
                }
                handleWithdrawal(account, Double.parseDouble(parts[1]), timestamp);
                break;

            case "TRANSFER":
                if (parts.length < 3) {
                    toClient.writeUTF("ERROR|Invalid transfer format");
                    toClient.flush();
                    return;
                }
                handleTransfer(account, parts[1], Double.parseDouble(parts[2]), timestamp);
                break;

            case "HISTORY":
                handleHistory(account);
                break;

            default:
                toClient.writeUTF("ERROR|Unknown command");
                toClient.flush();
        }
    }

    private void handleBalanceCheck(Account account) throws IOException {
        toClient.writeUTF("BALANCE|" + account.getBalance());
        toClient.flush();
    }

    private void handleDeposit(Account account, double amount, String timestamp) throws IOException {
        if (amount <= 0) {
            toClient.writeUTF("ERROR|Deposit amount must be positive");
            toClient.flush();
            return;
        }

        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);
        String transaction = timestamp + " - Deposit: $" + amount + ". New Balance: $" + newBalance;
        account.addTransaction(transaction);

        System.out.println(currentUser + " - " + transaction);

        toClient.writeUTF("SUCCESS|Deposit successful. New Balance: $" + newBalance);
        toClient.flush();

        BankServer.broadcastUpdate("UPDATE|" + currentUser + " deposited $" + amount);
    }

    private void handleWithdrawal(Account account, double amount, String timestamp) throws IOException {
        if (amount <= 0) {
            toClient.writeUTF("ERROR|Withdrawal amount must be positive");
            toClient.flush();
            return;
        }

        if (account.getBalance() < amount) {
            toClient.writeUTF("ERROR|Insufficient funds");
            toClient.flush();
            return;
        }

        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance);
        String transaction = timestamp + " - Withdrawal: $" + amount + ". New Balance: $" + newBalance;
        account.addTransaction(transaction);

        System.out.println(currentUser + " - " + transaction);

        toClient.writeUTF("SUCCESS|Withdrawal successful. New Balance: $" + newBalance);
        toClient.flush();

        BankServer.broadcastUpdate("UPDATE|" + currentUser + " withdrew $" + amount);
    }

    private void handleTransfer(Account fromAccount, String toUsername, double amount, String timestamp) throws IOException {
        if (amount <= 0) {
            toClient.writeUTF("ERROR|Transfer amount must be positive");
            toClient.flush();
            return;
        }

        Account toAccount = BankServer.getAccount(toUsername);
        if (toAccount == null) {
            toClient.writeUTF("ERROR|Recipient account not found");
            toClient.flush();
            return;
        }

        if (currentUser.equals(toUsername)) {
            toClient.writeUTF("ERROR|Cannot transfer to yourself");
            toClient.flush();
            return;
        }

        if (fromAccount.getBalance() < amount) {
            toClient.writeUTF("ERROR|Insufficient funds");
            toClient.flush();
            return;
        }

        double newFromBalance = fromAccount.getBalance() - amount;
        double newToBalance = toAccount.getBalance() + amount;
        
        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        String fromTransaction = timestamp + " - Transfer: $" + amount + " to " + toUsername + ". New Balance: $" + newFromBalance;
        String toTransaction = timestamp + " - Transfer: $" + amount + " from " + currentUser + ". New Balance: $" + newToBalance;
        
        fromAccount.addTransaction(fromTransaction);
        toAccount.addTransaction(toTransaction);

        System.out.println("Transfer: $" + amount + " from " + currentUser + " to " + toUsername + 
                           ". " + currentUser + " Balance: $" + newFromBalance + 
                           ", " + toUsername + " Balance: $" + newToBalance);

        toClient.writeUTF("SUCCESS|Transfer successful. New Balance: $" + newFromBalance);
        toClient.flush();

        BankServer.broadcastUpdate("UPDATE|" + currentUser + " transferred $" + amount + " to " + toUsername);
    }

    private void handleHistory(Account account) throws IOException {
        StringBuilder history = new StringBuilder("HISTORY|");
        for (String transaction : account.getTransactionHistory()) {
            history.append(transaction).append("\n");
        }
        if (account.getTransactionHistory().isEmpty()) {
            history.append("No transactions yet.");
        }
        toClient.writeUTF(history.toString());
        toClient.flush();
    }

    private void cleanup() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            BankServer.removeClient(this);
            System.out.println("Client " + currentUser + " connection closed.");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
