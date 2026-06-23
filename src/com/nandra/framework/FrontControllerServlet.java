package com.nandra.framework;

import com.nandra.framework.outils.Utils;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllerList = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        
        String basePackage = this.getInitParameter("base-package");
        
        if (basePackage != null && !basePackage.trim().isEmpty()) {
            this.controllerList = Utils.getControllerClasses(basePackage.trim());
        } else {
            System.out.println("Attention : Aucun 'base-package' n'a été spécifié.");
        }
    }

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
        out.println("<title>Sprint-1: Objets Class</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Mon Framework IoC</h1>");
        
        String basePackage = this.getInitParameter("base-package");
        out.println("<p><strong>Package scanné :</strong> " + (basePackage != null ? basePackage : "Aucun") + "</p>");
        out.println("<h3>Classes @Controller chargées en mémoire :</h3>");
        
        if (controllerList.isEmpty()) {
            out.println("<p style='color: orange;'>Aucune classe trouvée.</p>");
        } else {
            out.println("<ul>");
            for (Class<?> clazz : controllerList) {
                out.println("<li>Nom complet : <strong>" + clazz.getName() + "</strong> (Nom simple : " + clazz.getSimpleName() + ")</li>");
            }
            out.println("</ul>");
        }
        
        out.println("</body>");
        out.println("</html>");

        out.close();
    }
}