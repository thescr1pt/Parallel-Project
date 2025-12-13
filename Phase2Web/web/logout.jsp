<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Invalidate session
    session.invalidate();
    // Redirect to login
    response.sendRedirect("login.jsp");
%>
