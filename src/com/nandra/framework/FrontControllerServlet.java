package com.nandra.framework;

import mg.itu.test.annotation.UrlMapping;
import com.nandra.framework.outils.Mapping;
import com.nandra.framework.outils.Utils;
import com.nandra.framework.outils.UrlMethod;
import com.nandra.framework.constant.HttpMethod;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Mapping> mappingUrls = new HashMap<>();

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
                        
                        Mapping mapping = new Mapping(controllerClass, method);
                        this.mappingUrls.put(new UrlMethod(urlMapping), mapping);
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
        HttpMethod httpMethod = HttpMethod.valueOf(req.getMethod());

        UrlMapping urlMapping = new UrlMapping() {
            @Override
            public String url() {
                return relativeUrl;
            }

            @Override
            public HttpMethod method() {
                return httpMethod;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return UrlMapping.class;
            }
        };

        Mapping matchedMapping = mappingUrls.get(new UrlMethod(urlMapping));

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Sprint-2: Routage</title></head>");
        out.println("<body>");

        if (matchedMapping != null) {
            out.println("<h1>Route trouvée !</h1>");
            
            try {
                Object controllerInstance = matchedMapping.getControllerClass().getDeclaredConstructor().newInstance();
                matchedMapping.getMethod().invoke(controllerInstance);
                out.println("<p>La méthode <strong>" + matchedMapping.getMethod().getName() + "</strong> du contrôleur <strong>" + matchedMapping.getControllerClass().getName() + "</strong> a été exécutée avec succès.</p>");
            } catch(Exception e) {
                out.println("<h2 style='color: red;'>Erreur lors de l'exécution de la méthode :</h2>");
                out.println("<pre>" + e.getMessage() + "</pre>");
            }

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
                
                for (Map.Entry<UrlMethod, Mapping> entry : mappingUrls.entrySet()) {
                    UrlMethod urlMethod = entry.getKey();
                    Mapping mapping = entry.getValue();
                    
                    out.println("<tr>");
                    out.println("  <td><code>" + urlMethod.getMethod() + " " + urlMethod.getUrl() + "</code></td>");
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