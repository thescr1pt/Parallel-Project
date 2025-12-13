<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bankService.BankWebService" %>
<%
    // Check if already logged in
    String loggedInUser = (String) session.getAttribute("username");
    if (loggedInUser != null) {
        response.sendRedirect("dashboard.jsp");
        return;
    }

    String message = null;
    String messageType = null;
    
    // Process form submission
    String action = request.getParameter("action");
    if (action != null) {
        BankWebService bankService = new BankWebService();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        try {
            if (action.equals("login")) {
                String result = bankService.login(username, password);
                if (result.startsWith("SUCCESS")) {
                    session.setAttribute("username", username);
                    session.setAttribute("password", password);
                    response.sendRedirect("dashboard.jsp");
                    return;
                } else {
                    message = result.substring(6);
                    messageType = "error";
                }
            } else if (action.equals("createAccount")) {
                double initialBalance = Double.parseDouble(request.getParameter("initialBalance"));
                String result = bankService.createAccount(username, password, initialBalance);
                if (result.startsWith("SUCCESS")) {
                    message = result.substring(8) + " You can now login.";
                    messageType = "success";
                } else {
                    message = result.substring(6);
                    messageType = "error";
                }
            }
        } catch (Exception ex) {
            message = "Error: " + ex.getMessage();
            messageType = "error";
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Bank Login</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: Arial, sans-serif;
            background: #f0f0f0;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        .container { width: 100%; max-width: 400px; }
        .logo { text-align: center; margin-bottom: 20px; }
        .logo h1 { color: #333; font-size: 24px; }
        .logo p { color: #666; font-size: 12px; }
        .card {
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        .tabs { display: flex; background: #f5f5f5; }
        .tab {
            flex: 1;
            padding: 15px;
            text-align: center;
            cursor: pointer;
            border: none;
            background: transparent;
            font-size: 14px;
            font-weight: bold;
            color: #666;
        }
        .tab:hover { color: #333; }
        .tab.active { background: #fff; color: #0066cc; }
        .form-container { padding: 25px; }
        .form { display: none; }
        .form.active { display: block; }
        .form-group { margin-bottom: 15px; }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            color: #333;
            font-size: 14px;
        }
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
            padding: 12px;
            background: #0066cc;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
        }
        .btn:hover { background: #0055aa; }
        .message {
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 15px;
            font-size: 13px;
        }
        .message.success { background: #d4edda; color: #155724; }
        .message.error { background: #f8d7da; color: #721c24; }
        .test-accounts {
            margin-top: 20px;
            padding: 15px;
            background: #f9f9f9;
            border-radius: 4px;
            font-size: 12px;
        }
        .test-accounts h4 { color: #333; margin-bottom: 10px; }
        .test-accounts p { color: #666; margin: 3px 0; }
        .test-accounts code {
            background: #eee;
            padding: 1px 4px;
            border-radius: 2px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">
            <h1>Bank Web Service</h1>
            <p>Phase 2 - Web Services</p>
        </div>
        
        <div class="card">
            <div class="tabs">
                <button class="tab active" onclick="showForm('login')">Sign In</button>
                <button class="tab" onclick="showForm('register')">Create Account</button>
            </div>
            
            <div class="form-container">
                <% if (message != null) { %>
                    <div class="message <%= messageType %>"><%= message %></div>
                <% } %>
                
                <form id="loginForm" class="form active" action="login.jsp" method="POST">
                    <input type="hidden" name="action" value="login">
                    <div class="form-group">
                        <label>Username</label>
                        <input type="text" name="username" required>
                    </div>
                    <div class="form-group">
                        <label>Password</label>
                        <input type="password" name="password" required>
                    </div>
                    <button type="submit" class="btn">Sign In</button>
                </form>
                
                <form id="registerForm" class="form" action="login.jsp" method="POST">
                    <input type="hidden" name="action" value="createAccount">
                    <div class="form-group">
                        <label>Username</label>
                        <input type="text" name="username" required>
                    </div>
                    <div class="form-group">
                        <label>Password</label>
                        <input type="password" name="password" required>
                    </div>
                    <div class="form-group">
                        <label>Initial Deposit ($)</label>
                        <input type="number" name="initialBalance" step="0.01" min="0" value="0" required>
                    </div>
                    <button type="submit" class="btn">Create Account</button>
                </form>
            </div>
        </div>
    </div>
    
    <script>
        function showForm(formType) {
            document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
            event.target.classList.add('active');
            document.querySelectorAll('.form').forEach(form => form.classList.remove('active'));
            document.getElementById(formType === 'login' ? 'loginForm' : 'registerForm').classList.add('active');
        }
    </script>
</body>
</html>
