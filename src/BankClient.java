import java.io.*;
import java.net.*;
import java.util.Scanner;

public class BankClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8000;
    private Socket socket;
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    private Scanner scanner;
    private String username;
    private volatile boolean waitingForResponse = false;

    public BankClient() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        try {
            // Connect to server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
            
            System.out.println("Connected to Bank Server at " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Authenticate
            if (!authenticate()) {
                System.out.println("Authentication failed. Disconnecting...");
                socket.close();
                return;
            }

            // Start listening for server updates in a separate thread
            ServerListener listenerThread = new ServerListener();
            listenerThread.start();

            // Main menu loop
            showMenu();

            listenerThread.interrupt();
            socket.close();

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private boolean authenticate() throws IOException {
        // Get username
        String prompt = fromServer.readUTF();
        System.out.print(prompt + " ");
        username = scanner.nextLine();
        toServer.writeUTF(username);
        toServer.flush();

        // Get password
        prompt = fromServer.readUTF();
        System.out.print(prompt + " ");
        String password = scanner.nextLine();
        toServer.writeUTF(password);
        toServer.flush();

        // Check authentication result
        String result = fromServer.readUTF();
        if (result.equals("SUCCESS")) {
            double balance = fromServer.readDouble();
            System.out.println("\n=================================");
            System.out.println("Login successful!");
            System.out.println("Current Balance: $" + balance);
            System.out.println("=================================\n");
            return true;
        } else {
            System.out.println("Login failed. Invalid credentials.");
            return false;
        }
    }

    private void showMenu() {
        while (true) {
            System.out.println("\n--- Bank Menu ---");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Transaction History");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        checkBalance();
                        break;
                    case "2":
                        deposit();
                        break;
                    case "3":
                        withdraw();
                        break;
                    case "4":
                        transfer();
                        break;
                    case "5":
                        viewHistory();
                        break;
                    case "6":
                        toServer.writeUTF("exit");
                        toServer.flush();
                        System.out.println("Thank you for using our banking system. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (IOException e) {
                System.err.println("Error communicating with server: " + e.getMessage());
                break;
            }
        }
    }

    private void checkBalance() throws IOException {
        waitingForResponse = true;
        toServer.writeUTF("BALANCE");
        toServer.flush();

        String response = fromServer.readUTF();
        String[] parts = response.split("\\|");
        
        if (parts[0].equals("BALANCE")) {
            System.out.println("\nCurrent Balance: $" + parts[1]);
        }
        waitingForResponse = false;
    }

    private void deposit() throws IOException {
        System.out.print("Enter deposit amount: $");
        String amountStr = scanner.nextLine();
        
        try {
            double amount = Double.parseDouble(amountStr);
            waitingForResponse = true;
            toServer.writeUTF("DEPOSIT|" + amount);
            toServer.flush();

            String response = fromServer.readUTF();
            String[] parts = response.split("\\|", 2);
            
            if (parts[0].equals("SUCCESS")) {
                System.out.println("\n" + parts[1]);
            } else {
                System.out.println("\nError: " + parts[1]);
            }
            waitingForResponse = false;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        }
    }

    private void withdraw() throws IOException {
        System.out.print("Enter withdrawal amount: $");
        String amountStr = scanner.nextLine();
        
        try {
            double amount = Double.parseDouble(amountStr);
            waitingForResponse = true;
            toServer.writeUTF("WITHDRAW|" + amount);
            toServer.flush();

            String response = fromServer.readUTF();
            String[] parts = response.split("\\|", 2);
            
            if (parts[0].equals("SUCCESS")) {
                System.out.println("\n" + parts[1]);
            } else {
                System.out.println("\nError: " + parts[1]);
            }
            waitingForResponse = false;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        }
    }

    private void transfer() throws IOException {
        System.out.print("Enter recipient username: ");
        String recipient = scanner.nextLine();
        
        System.out.print("Enter transfer amount: $");
        String amountStr = scanner.nextLine();
        
        try {
            double amount = Double.parseDouble(amountStr);
            waitingForResponse = true;
            toServer.writeUTF("TRANSFER|" + recipient + "|" + amount);
            toServer.flush();

            String response = fromServer.readUTF();
            String[] parts = response.split("\\|", 2);
            
            if (parts[0].equals("SUCCESS")) {
                System.out.println("\n" + parts[1]);
            } else {
                System.out.println("\nError: " + parts[1]);
            }
            waitingForResponse = false;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        }
    }

    private void viewHistory() throws IOException {
        waitingForResponse = true;
        toServer.writeUTF("HISTORY");
        toServer.flush();

        String response = fromServer.readUTF();
        String[] parts = response.split("\\|", 2);
        
        if (parts[0].equals("HISTORY")) {
            System.out.println("\n--- Transaction History ---");
            System.out.println(parts[1]);
            System.out.println("---------------------------");
        }
        waitingForResponse = false;
    }

    class ServerListener extends Thread {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    // Only read if we're not waiting for a command response and data is available
                    if (!waitingForResponse && socket.getInputStream().available() > 0) {
                        String message = fromServer.readUTF();
                        if (message.startsWith("UPDATE|")) {
                            String update = message.substring(7);
                            System.out.println("\n[Server Update] " + update);
                            System.out.print("Choose an option: ");
                        }
                    } else {
                        Thread.sleep(50);
                    }
                }
            } catch (IOException | InterruptedException e) {
                // Connection closed or interrupted
            }
        }
    }

    public static void main(String[] args) {
        BankClient client = new BankClient();
        client.start();
    }
}
