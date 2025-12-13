import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class BankClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 1099;
    
    private BankService bankService;
    private TransactionService transactionService;
    private Scanner scanner;
    private String username;
    private boolean loggedIn;

    public BankClient() {
        scanner = new Scanner(System.in);
        loggedIn = false;
    }

    public void start() {
        try {
            // Connect to RMI registry
            Registry registry = LocateRegistry.getRegistry(SERVER_ADDRESS, SERVER_PORT);
            
            // Lookup services
            bankService = (BankService) registry.lookup("BankService");
            transactionService = (TransactionService) registry.lookup("TransactionService");
            
            System.out.println("Connected to Bank RMI Server at " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Authenticate
            if (!authenticate()) {
                System.out.println("Authentication failed. Exiting...");
                return;
            }

            // Register for updates
            ClientCallback callback = new ClientCallbackImpl();
            transactionService.registerClient(username, callback);

            // Main menu loop
            showMenu();

            // Cleanup
            transactionService.unregisterClient(username);
            System.out.println("Disconnected from server.");

        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean authenticate() {
        try {
            System.out.print("Enter username: ");
            username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (bankService.login(username, password)) {
                double balance = bankService.checkBalance(username);
                System.out.println("\n=================================");
                System.out.println("Login successful!");
                System.out.println("Current Balance: $" + balance);
                System.out.println("=================================\n");
                loggedIn = true;
                return true;
            } else {
                System.out.println("Login failed. Invalid credentials.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            return false;
        }
    }

    private void showMenu() {
        while (loggedIn) {
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
                        System.out.println("Thank you for using our banking system. Goodbye!");
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void checkBalance() throws Exception {
        double balance = bankService.checkBalance(username);
        if (balance >= 0) {
            System.out.println("\nCurrent Balance: $" + balance);
        } else {
            System.out.println("\nError: Could not retrieve balance");
        }
    }

    private void deposit() throws Exception {
        System.out.print("Enter deposit amount: $");
        String amountStr = scanner.nextLine();

        try {
            double amount = Double.parseDouble(amountStr);
            String response = bankService.deposit(username, amount);
            String[] parts = response.split("\\|", 2);

            if (parts[0].equals("SUCCESS")) {
                System.out.println("\n" + parts[1]);
            } else {
                System.out.println("\nError: " + parts[1]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        }
    }

    private void withdraw() throws Exception {
        System.out.print("Enter withdrawal amount: $");
        String amountStr = scanner.nextLine();

        try {
            double amount = Double.parseDouble(amountStr);
            String response = bankService.withdraw(username, amount);
            String[] parts = response.split("\\|", 2);

            if (parts[0].equals("SUCCESS")) {
                System.out.println("\n" + parts[1]);
            } else {
                System.out.println("\nError: " + parts[1]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        }
    }

    private void transfer() throws Exception {
        System.out.print("Enter recipient username: ");
        String recipient = scanner.nextLine();

        System.out.print("Enter transfer amount: $");
        String amountStr = scanner.nextLine();

        try {
            double amount = Double.parseDouble(amountStr);
            String response = bankService.transfer(username, recipient, amount);
            String[] parts = response.split("\\|", 2);

            if (parts[0].equals("SUCCESS")) {
                System.out.println("\n" + parts[1]);
            } else {
                System.out.println("\nError: " + parts[1]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        }
    }

    private void viewHistory() throws Exception {
        List<String> history = transactionService.getTransactionHistory(username);
        
        System.out.println("\n--- Transaction History ---");
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (String transaction : history) {
                System.out.println(transaction);
            }
        }
        System.out.println("---------------------------");
    }

    public static void main(String[] args) {
        BankClient client = new BankClient();
        client.start();
    }
}
