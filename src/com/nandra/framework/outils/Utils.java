package com.nandra.framework.outils;

import mg.itu.test.annotation.Controller;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<Class<?>> getControllerClasses(String basePackage) {
        List<Class<?>> controllerClasses = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            
            String packagePath = basePackage.replace('.', '/');
            URL packageUrl = classLoader.getResource(packagePath);
            
            if (packageUrl == null) {
                System.out.println("Le package spécifié n'existe pas : " + basePackage);
                return controllerClasses;
            }

            File directory = new File(packageUrl.toURI());
            scanDirectory(directory, basePackage, controllerClasses);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return controllerClasses;
    }

    private static void scanDirectory(File directory, String currentPackage, List<Class<?>> controllerClasses) {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                String nextPackage = currentPackage.isEmpty() ? file.getName() : currentPackage + "." + file.getName();
                scanDirectory(file, nextPackage, controllerClasses);
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName().substring(0, file.getName().length() - 6);
                String fullClassName = currentPackage.isEmpty() ? className : currentPackage + "." + className;

                try {
                    Class<?> clazz = Class.forName(fullClassName);
                    
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        controllerClasses.add(clazz);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    System.err.println("Impossible de charger la classe : " + fullClassName);
                }
            }
        }
    }
}