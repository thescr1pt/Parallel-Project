package phase2;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class BankClientGUI extends JFrame {

    private BankService bankService;
    private TransactionService transactionService;
    private String currentUser;

    private JTextField usernameField, amountField, recipientField;
    private JPasswordField passwordField;
    private JTextArea outputArea;
    private JLabel balanceLabel;
    private JPanel loginPanel, bankPanel;
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public BankClientGUI() {
        setTitle("Bank Client");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectToServer();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> login());
        loginPanel.add(loginBtn, gbc);

        bankPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        balanceLabel = new JLabel("Balance: $0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(balanceLabel);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshBalance());
        topPanel.add(refreshBtn);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        topPanel.add(logoutBtn);
        bankPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField(10);
        centerPanel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("Recipient:"), gbc);
        gbc.gridx = 1;
        recipientField = new JTextField(10);
        centerPanel.add(recipientField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JButton depositBtn = new JButton("Deposit");
        depositBtn.addActionListener(e -> deposit());
        centerPanel.add(depositBtn, gbc);

        gbc.gridx = 1;
        JButton withdrawBtn = new JButton("Withdraw");
        withdrawBtn.addActionListener(e -> withdraw());
        centerPanel.add(withdrawBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton transferBtn = new JButton("Transfer");
        transferBtn.addActionListener(e -> transfer());
        centerPanel.add(transferBtn, gbc);

        gbc.gridy = 4;
        JButton historyBtn = new JButton("View History");
        historyBtn.addActionListener(e -> viewHistory());
        centerPanel.add(historyBtn, gbc);

        bankPanel.add(centerPanel, BorderLayout.CENTER);

        outputArea = new JTextArea(8, 40);
        outputArea.setEditable(false);
        bankPanel.add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        mainContainer.add(loginPanel, "login");
        mainContainer.add(bankPanel, "bank");

        add(mainContainer);
        cardLayout.show(mainContainer, "login");
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
            bankService = (BankService) registry.lookup("BankService");
            transactionService = (TransactionService) registry.lookup("TransactionService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + e.getMessage());
        }
    }

    private void login() {
        try {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            if (bankService.login(user, pass)) {
                currentUser = user;

                ClientCallback callback = new ClientCallbackImpl() {
                    @Override
                    public void notifyUpdate(String message) {
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append("[Notification] " + message + "\n");
                            refreshBalance();
                        });
                    }
                };
                transactionService.registerClient(currentUser, callback);

                refreshBalance();
                cardLayout.show(mainContainer, "bank");
                outputArea.setText("Welcome, " + currentUser + "!\n");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Login error: " + e.getMessage());
        }
    }

    private void logout() {
        try {
            transactionService.unregisterClient(currentUser);
        } catch (Exception e) { }
        currentUser = null;
        usernameField.setText("");
        passwordField.setText("");
        cardLayout.show(mainContainer, "login");
    }

    private void refreshBalance() {
        try {
            double balance = bankService.checkBalance(currentUser);
            balanceLabel.setText("Balance: $" + String.format("%.2f", balance));
        } catch (Exception e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void deposit() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String response = bankService.deposit(currentUser, amount);
            String[] parts = response.split("\\|", 2);
            outputArea.append(parts[1] + "\n");
            if (parts[0].equals("SUCCESS")) {
                refreshBalance();
                amountField.setText("");
            }
        } catch (NumberFormatException e) {
            outputArea.append("Invalid amount!\n");
        } catch (Exception e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void withdraw() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String response = bankService.withdraw(currentUser, amount);
            String[] parts = response.split("\\|", 2);
            outputArea.append(parts[1] + "\n");
            if (parts[0].equals("SUCCESS")) {
                refreshBalance();
                amountField.setText("");
            }
        } catch (NumberFormatException e) {
            outputArea.append("Invalid amount!\n");
        } catch (Exception e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void transfer() {
        try {
            String recipient = recipientField.getText();
            double amount = Double.parseDouble(amountField.getText());
            String response = bankService.transfer(currentUser, recipient, amount);
            String[] parts = response.split("\\|", 2);
            outputArea.append(parts[1] + "\n");
            if (parts[0].equals("SUCCESS")) {
                refreshBalance();
                amountField.setText("");
                recipientField.setText("");
            }
        } catch (NumberFormatException e) {
            outputArea.append("Invalid amount!\n");
        } catch (Exception e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void viewHistory() {
        try {
            List<String> history = transactionService.getTransactionHistory(currentUser);
            outputArea.append("\n--- Transaction History ---\n");
            if (history.isEmpty()) {
                outputArea.append("No transactions yet.\n");
            } else {
                for (String t : history) {
                    outputArea.append(t + "\n");
                }
            }
            outputArea.append("---------------------------\n");
        } catch (Exception e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BankClientGUI().setVisible(true);
        });
    }
}
