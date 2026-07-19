package com.nandra.framework;

import mg.itu.test.annotation.UrlMapping;
import com.nandra.framework.outils.Mapping;
import com.nandra.framework.outils.UrlMethod;
import com.nandra.framework.constant.HttpMethod;
import com.nandra.framework.model.ModelAndView;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.annotation.Annotation;

public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Mapping> mappingUrls = new HashMap<>();
    private String prefix;
    private String suffix;

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext context = getServletContext();
        this.mappingUrls = (Map<UrlMethod, Mapping>) context.getAttribute("mappingUrls");
        this.prefix = (String) context.getAttribute("prefix");
        this.suffix = (String) context.getAttribute("suffix");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
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
            public Class<? extends Annotation> annotationType() {
                return UrlMapping.class;
            }
        };

        Mapping matchedMapping = mappingUrls.get(new UrlMethod(urlMapping));

        if (matchedMapping != null) {
            try {
                Object controllerInstance = matchedMapping.getControllerClass().getDeclaredConstructor().newInstance();
                Object returnValue = matchedMapping.getMethod().invoke(controllerInstance);

                if (returnValue instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) returnValue;
                    
                    if (mv.getModel() != null) {
                        for (Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }

                    String viewPath = this.prefix + mv.getView() + this.suffix;

                    RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                    dispatcher.forward(req, res);
                    
                } else if (returnValue instanceof String) {
                    res.setContentType("text/plain;charset=UTF-8");
                    res.getWriter().println(returnValue);
                } else {
                    res.setContentType("text/html;charset=UTF-8");
                    res.getWriter().println("<p>La méthode s'est exécutée mais n'a retourné aucune vue.</p>");
                }

            } catch(Exception e) {
                res.setContentType("text/html;charset=UTF-8");
                PrintWriter out = res.getWriter();
                out.println("<!DOCTYPE html><html><body>");
                out.println("<h2 style='color: red;'>Erreur lors de l'exécution de la méthode :</h2>");
                out.println("<pre>");
                e.printStackTrace(out);
                out.println("</pre>");
                out.println("</body></html>");
            }

        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.println("<!DOCTYPE html><html><head><title>404 - Introuvable</title></head><body>");
            out.println("<h1 style='color: red;'>Erreur 404 - Route introuvable</h1>");
            out.println("<p>L'URL <strong>" + relativeUrl + "</strong> ne correspond à aucun mapping.</p><hr/>");
            out.println("<h3>Liste de toutes les routes disponibles dans l'application :</h3>");

            if (mappingUrls.isEmpty()) {
                out.println("<p>Aucune route n'a été configurée.</p>");
            } else {
                out.println("<table border='1' cellpadding='10' style='border-collapse: collapse;'>");
                out.println("<tr style='background-color: #f2f2f2;'><th>Méthode & URL</th><th>Contrôleur</th><th>Méthode Java</th></tr>");
                for (Map.Entry<UrlMethod, Mapping> entry : mappingUrls.entrySet()) {
                    out.println("<tr>");
                    out.println("  <td><code>" + entry.getKey().getMethod() + " " + entry.getKey().getUrl() + "</code></td>");
                    out.println("  <td>" + entry.getValue().getControllerClass().getName() + "</td>");
                    out.println("  <td><code>" + entry.getValue().getMethod().getName() + "()</code></td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
            out.println("</body></html>");
        }
    }
}