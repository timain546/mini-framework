package com.nandra.framework;

import mg.itu.test.annotation.UrlMapping;
import com.nandra.framework.outils.Mapping;
import com.nandra.framework.outils.Utils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontControllerServlet extends HttpServlet {

    private Map<String, Mapping> mappingUrls = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        
        String basePackage = this.getInitParameter("base-package");
        
        if (basePackage != null && !basePackage.trim().isEmpty()) {
            List<Class<?>> controllerList = Utils.getControllerClasses(basePackage.trim());
            
            for (Class<?> controllerClass : controllerList) {
                Method[] methods = controllerClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                        String url = urlMapping.value();
                        
                        Mapping mapping = new Mapping(controllerClass, method);
                        this.mappingUrls.put(url, mapping);
                    }
                }
            }
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

        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        
        String relativeUrl = requestURI.substring(contextPath.length());

        Mapping matchedMapping = mappingUrls.get(relativeUrl);

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Sprint-2: Routage</title></head>");
        out.println("<body>");

        if (matchedMapping != null) {
            out.println("<h1>Route trouvée !</h1>");
            out.println("<p><strong>URL demandée :</strong> " + relativeUrl + "</p>");
            out.println("<p><strong>Contrôleur ciblé :</strong> " + matchedMapping.getControllerClass().getName() + "</p>");
            out.println("<p><strong>Méthode à exécuter :</strong> " + matchedMapping.getMethod().getName() + "()</p>");
        } else {
            out.println("<h1 style='color: red;'>Erreur 404 - Route introuvable</h1>");
            out.println("<p>L'URL <strong>" + relativeUrl + "</strong> ne correspond à aucun mapping.</p>");
            out.println("<hr/>");
            out.println("<h3>Liste de toutes les routes disponibles dans l'application :</h3>");

            if (mappingUrls.isEmpty()) {
                out.println("<p>Aucune route n'a été configurée dans l'application.</p>");
            } else {
                out.println("<table border='1' cellpadding='10' style='border-collapse: collapse; text-align: left;'>");
                out.println("<tr style='background-color: #f2f2f2;'><th>URL</th><th>Contrôleur</th><th>Méthode</th></tr>");
                
                for (Map.Entry<String, Mapping> entry : mappingUrls.entrySet()) {
                    String url = entry.getKey();
                    Mapping mapping = entry.getValue();
                    
                    out.println("<tr>");
                    out.println("  <td><code>" + url + "</code></td>");
                    out.println("  <td>" + mapping.getControllerClass().getName() + "</td>");
                    out.println("  <td><code>" + mapping.getMethod().getName() + "()</code></td>");
                    out.println("</tr>");
                }
                
                out.println("</table>");
            }
        }

        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}