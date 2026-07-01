package com.nandra.framework.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.nandra.framework.outils.Mapping;
import com.nandra.framework.outils.Utils;
import com.nandra.framework.outils.UrlMethod;
import mg.itu.test.annotation.UrlMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Map<UrlMethod, Mapping> mappingUrls = new HashMap<>();

        String basePackage = context.getInitParameter("base-package");
        
        if (basePackage != null && !basePackage.trim().isEmpty()) {
            try {
                List<Class<?>> controllerList = Utils.getControllerClasses(basePackage.trim());
            
                for (Class<?> controllerClass : controllerList) {
                    Method[] methods = controllerClass.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(UrlMapping.class)) {
                            UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                            
                            Mapping mapping = new Mapping(controllerClass, method);
                            mappingUrls.put(new UrlMethod(urlMapping), mapping);
                        }
                    }
                }
                System.out.println("Scan des contrôleurs terminé avec succès.");
            } catch(Exception e) {
                context.log("Erreur lors du scan des contrôleurs", e);
            }
        } else {
            System.out.println("Attention : Aucun 'base-package' n'a été spécifié.");
        }

        context.setAttribute("mappingUrls", mappingUrls);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("mappingUrls");
    }
}