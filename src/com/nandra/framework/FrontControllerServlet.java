package com.nandra.framework;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontControllerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        processRequest(req, res);
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Sprint-0</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>Hello world!!</p>");
        out.println("</body>");
        out.println("</html>");

        out.close();
    }
}