<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bankService.BankWebService" %>
<%
    // Check if logged in
    String username = (String) session.getAttribute("username");
    String password = (String) session.getAttribute("password");
    
    if (username == null || password == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    
    BankWebService bankService = new BankWebService();
    
    String message = null;
    String messageType = null;
    String currentBalance = "0.00";
    
    // Get current balance
    String balanceResult = bankService.checkBalance(username, password);
    if (balanceResult.startsWith("SUCCESS")) {
        currentBalance = balanceResult.substring(balanceResult.lastIndexOf("$") + 1);
    }
    
    // Process form submission
    String action = request.getParameter("action");
    if (action != null) {
        try {
            String result = null;
            
            if (action.equals("deposit")) {
                double amount = Double.parseDouble(request.getParameter("amount"));
                result = bankService.deposit(username, password, amount);
            } else if (action.equals("withdraw")) {
                double amount = Double.parseDouble(request.getParameter("amount"));
                result = bankService.withdraw(username, password, amount);
            } else if (action.equals("transfer")) {
                String toUsername = request.getParameter("toUsername");
                double amount = Double.parseDouble(request.getParameter("amount"));
                result = bankService.transfer(username, password, toUsername, amount);
            }
            
            if (result != null) {
                if (result.startsWith("SUCCESS")) {
                    message = result.substring(8);
                    messageType = "success";
                    // Refresh balance
                    balanceResult = bankService.checkBalance(username, password);
                    if (balanceResult.startsWith("SUCCESS")) {
                        currentBalance = balanceResult.substring(balanceResult.lastIndexOf("$") + 1);
                    }
                } else {
                    message = result.substring(6);
                    messageType = "error";
                }
            }
        } catch (NumberFormatException ex) {
            message = "Please enter a valid amount";
            messageType = "error";
        } catch (Exception ex) {
            message = "Error: " + ex.getMessage();
            messageType = "error";
        }
    }
    
    // Get transaction history
    String historyResult = bankService.getTransactionHistory(username, password);
    String[] transactions = new String[0];
    if (historyResult.startsWith("SUCCESS")) {
        String historyData = historyResult.substring(8);
        if (!historyData.equals("No transactions found")) {
            transactions = historyData.split("\n");
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard - Bank</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; background: #f0f0f0; min-height: 100vh; }
        .navbar {
            background: #333;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .navbar h1 { color: #fff; font-size: 18px; }
        .navbar .user-info { display: flex; align-items: center; gap: 15px; }
        .navbar .user-info span { color: #ccc; font-size: 14px; }
        .logout-btn {
            background: #dc3545;
            color: #fff;
            padding: 8px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            font-size: 13px;
        }
        .logout-btn:hover { background: #c82333; }
        .container { max-width: 1000px; margin: 0 auto; padding: 20px; }
        .balance-card {
            background: #0066cc;
            border-radius: 8px;
            padding: 25px;
            color: #fff;
            margin-bottom: 20px;
        }
        .balance-card h2 { font-size: 12px; text-transform: uppercase; opacity: 0.8; margin-bottom: 5px; }
        .balance-card .amount { font-size: 36px; font-weight: bold; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 20px; }
        .card {
            background: #fff;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        .card h3 { color: #333; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #eee; font-size: 16px; }
        .form-group { margin-bottom: 12px; }
        .form-group label { display: block; margin-bottom: 5px; color: #555; font-size: 13px; }
        .form-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
        }
        .form-group input:focus { outline: none; border-color: #0066cc; }
        .btn {
            width: 100%;
            padding: 10px;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
            color: #fff;
        }
        .btn:hover { opacity: 0.9; }
        .btn-deposit { background: #28a745; }
        .btn-withdraw { background: #ffc107; color: #333; }
        .btn-transfer { background: #6f42c1; }
        .message { padding: 12px; border-radius: 4px; margin-bottom: 20px; font-size: 14px; }
        .message.success { background: #d4edda; color: #155724; }
        .message.error { background: #f8d7da; color: #721c24; }
        .history-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .history-card h3 { color: #333; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #eee; font-size: 16px; }
        .transaction-list { max-height: 250px; overflow-y: auto; }
        .transaction-item {
            padding: 10px;
            background: #f9f9f9;
            margin-bottom: 8px;
            border-radius: 4px;
            font-size: 13px;
            color: #555;
            border-left: 3px solid #0066cc;
        }
        .no-transactions { text-align: center; padding: 30px; color: #999; }
    </style>
</head>
<body>
    <nav class="navbar">
        <h1>Bank Web Service</h1>
        <div class="user-info">
            <span>Welcome, <strong><%= username %></strong></span>
            <a href="logout.jsp" class="logout-btn">Logout</a>
        </div>
    </nav>
    
    <div class="container">
        <% if (message != null) { %>
            <div class="message <%= messageType %>"><%= message %></div>
        <% } %>
        
        <div class="balance-card">
            <h2>Current Balance</h2>
            <div class="amount">$<%= currentBalance %></div>
        </div>
        
        <div class="grid">
            <div class="card">
                <h3>Deposit</h3>
                <form action="dashboard.jsp" method="POST">
                    <input type="hidden" name="action" value="deposit">
                    <div class="form-group">
                        <label>Amount ($)</label>
                        <input type="number" name="amount" step="0.01" min="0.01" required>
                    </div>
                    <button type="submit" class="btn btn-deposit">Deposit</button>
                </form>
            </div>
            
            <div class="card">
                <h3>Withdraw</h3>
                <form action="dashboard.jsp" method="POST">
                    <input type="hidden" name="action" value="withdraw">
                    <div class="form-group">
                        <label>Amount ($)</label>
                        <input type="number" name="amount" step="0.01" min="0.01" required>
                    </div>
                    <button type="submit" class="btn btn-withdraw">Withdraw</button>
                </form>
            </div>
            
            <div class="card">
                <h3>Transfer</h3>
                <form action="dashboard.jsp" method="POST">
                    <input type="hidden" name="action" value="transfer">
                    <div class="form-group">
                        <label>Recipient Username</label>
                        <input type="text" name="toUsername" required>
                    </div>
                    <div class="form-group">
                        <label>Amount ($)</label>
                        <input type="number" name="amount" step="0.01" min="0.01" required>
                    </div>
                    <button type="submit" class="btn btn-transfer">Transfer</button>
                </form>
            </div>
        </div>
        
        <div class="history-card">
            <h3>Transaction History</h3>
            <div class="transaction-list">
                <% if (transactions.length == 0) { %>
                    <div class="no-transactions">No transactions yet</div>
                <% } else { 
                    for (int i = transactions.length - 1; i >= 0; i--) { %>
                        <div class="transaction-item"><%= transactions[i] %></div>
                    <% } 
                } %>
            </div>
        </div>
    </div>
</body>
</html>
