<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bankService.BankWebService" %>
<!DOCTYPE html>
<html>
<head>
    <title>Bank Web Service Client</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        h1 {
            color: #2c3e50;
            text-align: center;
        }
        h2 {
            color: #34495e;
            border-bottom: 2px solid #3498db;
            padding-bottom: 10px;
        }
        .section {
            background-color: white;
            padding: 20px;
            margin: 20px 0;
            border-radius: 8px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        label {
            display: block;
            margin: 10px 0 5px;
            font-weight: bold;
            color: #555;
        }
        input[type="text"], input[type="password"], input[type="number"] {
            width: 100%;
            padding: 10px;
            margin-bottom: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        button {
            background-color: #3498db;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            margin: 5px;
            font-size: 14px;
        }
        button:hover {
            background-color: #2980b9;
        }
        .result {
            margin-top: 20px;
            padding: 15px;
            border-radius: 4px;
        }
        .success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .transaction-history {
            background-color: #e9ecef;
            padding: 10px;
            border-radius: 4px;
            white-space: pre-line;
            font-family: monospace;
            font-size: 12px;
        }
        .nav {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            justify-content: center;
            margin-bottom: 20px;
        }
        .nav a {
            text-decoration: none;
        }
        .nav button {
            background-color: #6c757d;
        }
        .nav button:hover {
            background-color: #5a6268;
        }
    </style>
</head>
<body>

<h1>🏦 Bank Web Service Client</h1>
<p style="text-align: center; color: #666;">Phase 2 Bonus Task - Web Services Integration</p>

<div class="nav">
    <a href="#login"><button>Login</button></a>
    <a href="#balance"><button>Check Balance</button></a>
    <a href="#deposit"><button>Deposit</button></a>
    <a href="#withdraw"><button>Withdraw</button></a>
    <a href="#transfer"><button>Transfer</button></a>
    <a href="#history"><button>History</button></a>
    <a href="#create"><button>Create Account</button></a>
</div>

<!-- Login Section -->
<div class="section" id="login">
    <h2>🔐 Login</h2>
    <form action="bankClient.jsp" method="POST">
        <label>Username:</label>
        <input type="text" name="username" required>
        <label>Password:</label>
        <input type="password" name="password" required>
        <button type="submit" name="action" value="login">Login</button>
    </form>
</div>

<!-- Check Balance Section -->
<div class="section" id="balance">
    <h2>💰 Check Balance</h2>
    <form action="bankClient.jsp" method="POST">
        <label>Username:</label>
        <input type="text" name="username" required>
        <label>Password:</label>
        <input type="password" name="password" required>
        <button type="submit" name="action" value="checkBalance">Check Balance</button>
    </form>
</div>

<!-- Deposit Section -->
<div class="section" id="deposit">
    <h2>📥 Deposit</h2>
    <form action="bankClient.jsp" method="POST">
        <label>Username:</label>
        <input type="text" name="username" required>
        <label>Password:</label>
        <input type="password" name="password" required>
        <label>Amount ($):</label>
        <input type="number" name="amount" step="0.01" min="0.01" required>
        <button type="submit" name="action" value="deposit">Deposit</button>
    </form>
</div>

<!-- Withdraw Section -->
<div class="section" id="withdraw">
    <h2>📤 Withdraw</h2>
    <form action="bankClient.jsp" method="POST">
        <label>Username:</label>
        <input type="text" name="username" required>
        <label>Password:</label>
        <input type="password" name="password" required>
        <label>Amount ($):</label>
        <input type="number" name="amount" step="0.01" min="0.01" required>
        <button type="submit" name="action" value="withdraw">Withdraw</button>
    </form>
</div>

<!-- Transfer Section -->
<div class="section" id="transfer">
    <h2>🔄 Transfer</h2>
    <form action="bankClient.jsp" method="POST">
        <label>Your Username:</label>
        <input type="text" name="username" required>
        <label>Your Password:</label>
        <input type="password" name="password" required>
        <label>Recipient Username:</label>
        <input type="text" name="toUsername" required>
        <label>Amount ($):</label>
        <input type="number" name="amount" step="0.01" min="0.01" required>
        <button type="submit" name="action" value="transfer">Transfer</button>
    </form>
</div>

<!-- Transaction History Section -->
<div class="section" id="history">
    <h2>📋 Transaction History</h2>
    <form action="bankClient.jsp" method="POST">
        <label>Username:</label>
        <input type="text" name="username" required>
        <label>Password:</label>
        <input type="password" name="password" required>
        <button type="submit" name="action" value="getHistory">Get History</button>
    </form>
</div>

<!-- Create Account Section -->
<div class="section" id="create">
    <h2>➕ Create New Account</h2>
    <form action="bankClient.jsp" method="POST">
        <label>Username:</label>
        <input type="text" name="username" required>
        <label>Password:</label>
        <input type="password" name="password" required>
        <label>Initial Balance ($):</label>
        <input type="number" name="initialBalance" step="0.01" min="0" required>
        <button type="submit" name="action" value="createAccount">Create Account</button>
    </form>
</div>

<%
    String result = null;
    String resultType = null;
    
    // Process form submission
    String action = request.getParameter("action");
    if (action != null) {
        try {
            // Directly use the BankWebService class
            BankWebService bankService = new BankWebService();
            
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            
            if (action.equals("login")) {
                result = bankService.login(username, password);
            } else if (action.equals("checkBalance")) {
                result = bankService.checkBalance(username, password);
            } else if (action.equals("deposit")) {
                double amount = Double.parseDouble(request.getParameter("amount"));
                result = bankService.deposit(username, password, amount);
            } else if (action.equals("withdraw")) {
                double amount = Double.parseDouble(request.getParameter("amount"));
                result = bankService.withdraw(username, password, amount);
            } else if (action.equals("transfer")) {
                String toUsername = request.getParameter("toUsername");
                double amount = Double.parseDouble(request.getParameter("amount"));
                result = bankService.transfer(username, password, toUsername, amount);
            } else if (action.equals("getHistory")) {
                result = bankService.getTransactionHistory(username, password);
            } else if (action.equals("createAccount")) {
                double initialBalance = Double.parseDouble(request.getParameter("initialBalance"));
                result = bankService.createAccount(username, password, initialBalance);
            }
            
            if (result != null && result.startsWith("SUCCESS")) {
                resultType = "success";
                result = result.substring(8); // Remove "SUCCESS|"
            } else if (result != null && result.startsWith("ERROR")) {
                resultType = "error";
                result = result.substring(6); // Remove "ERROR|"
            }
            
        } catch (Exception ex) {
            result = "Error: " + ex.getMessage();
            resultType = "error";
        }
    }
    
    if (result != null && !result.isEmpty()) {
%>
    <div class="section">
        <h2>📊 Result</h2>
        <div class="result <%= "success".equals(resultType) ? "success" : "error" %>">
            <pre><%= result %></pre>
        </div>
    </div>
<%
    }
%>

<div class="section">
    <h2>ℹ️ Default Test Accounts</h2>
    <table style="width: 100%; border-collapse: collapse;">
        <tr style="background-color: #3498db; color: white;">
            <th style="padding: 10px; text-align: left;">Username</th>
            <th style="padding: 10px; text-align: left;">Password</th>
            <th style="padding: 10px; text-align: left;">Initial Balance</th>
        </tr>
        <tr style="background-color: #f8f9fa;">
            <td style="padding: 10px;">moataz</td>
            <td style="padding: 10px;">123</td>
            <td style="padding: 10px;">$15,000.00</td>
        </tr>
        <tr>
            <td style="padding: 10px;">wael</td>
            <td style="padding: 10px;">123</td>
            <td style="padding: 10px;">$1,000.00</td>
        </tr>
        <tr style="background-color: #f8f9fa;">
            <td style="padding: 10px;">omar</td>
            <td style="padding: 10px;">123</td>
            <td style="padding: 10px;">$5,000.00</td>
        </tr>
        <tr>
            <td style="padding: 10px;">khouly</td>
            <td style="padding: 10px;">123</td>
            <td style="padding: 10px;">$5,000.00</td>
        </tr>
    </table>
</div>

</body>
</html>
