package mg.nandra.framework;

import mg.nandra.framework.outils.*;
import mg.nandra.framework.annotation.*;
import mg.nandra.framework.enumeration.HttpMethod;
import mg.nandra.framework.model.ModelAndView;
import mg.nandra.framework.model.MySession;
import mg.nandra.framework.model.Fichier;
import com.google.gson.Gson;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.annotation.Annotation;
import jakarta.servlet.annotation.MultipartConfig;

import java.util.Map;
import java.util.HashMap;

// Multipart est important sinon getPart() levera une exception

@MultipartConfig(
    maxFileSize = 10 * 1024 * 1024,      // 10 Mo par fichier
    maxRequestSize = 50 * 1024 * 1024,   // 50 Mo par requête
    fileSizeThreshold = 1024 * 1024      // au-delà de 1 Mo, écrit sur disque temporairement
)
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
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
    {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
    {
        processRequest(req, res);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
    {
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

                Parameter[] parameters = matchedMapping.getMethod().getParameters();
                Object[] arguments = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {

                    Parameter parameter = parameters[i];
                    Class<?> parameterType = parameter.getType();

                    if (parameterType == MySession.class) {
                        arguments[i] = new MySession(req);

                    } else if (parameterType == HttpSession.class) {
                        arguments[i] = req.getSession(true);

                    } else if (parameterType == HttpServletRequest.class) {
                        arguments[i] = req;

                    } else if (parameterType == HttpServletResponse.class) {
                        arguments[i] = res;

                    } else if (parameter.isAnnotationPresent(Param.class)) {
                        Param param = parameter.getAnnotation(Param.class);

                        if (parameterType == Fichier.class) {
                            arguments[i] = extraireFichier(req, param.value());
                        } else {
                            String value = req.getParameter(param.value());
                            arguments[i] = convertValue(value, parameterType);
                        }
                    } else {
                        arguments[i] = bindObject(parameterType, req, "");
                    }
                }

                Object returnValue =
                        matchedMapping.getMethod().invoke(
                                controllerInstance,
                                arguments
                        );

                boolean jsonResponse = matchedMapping.getMethod().isAnnotationPresent(JsonResponse.class);

                if(jsonResponse) {
                    res.setContentType("application/json;charset=UTF-8");

                    if(returnValue instanceof ModelAndView) {
                        throw new ServletException(
                            "Une méthode annotée avec @JsonResponse ne peut pas retourner ModelAndView."
                        );
                    }

                    if(returnValue instanceof String) {
                        res.getWriter().println(returnValue);
                    } else {
                        Gson gson = new Gson();
                        String json = gson.toJson(returnValue);
                        res.getWriter().println(json);
                    }

                } else {
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

    private Object convertValue(String value, Class<?> type)
    {
        if (type == String.class) {
            return value;
        }

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }

        if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        }

        if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }

        if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        }

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (type == short.class || type == Short.class) {
            return Short.parseShort(value);
        }

        if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(value);
        }

        if (type == char.class || type == Character.class) {
            return value.charAt(0);
        }

        throw new IllegalArgumentException(
            "Type non supporté : " + type.getName()
        );
    }

    private Object bindObject(Class<?> objectClass, HttpServletRequest req, String prefix)
    throws Exception
    {
        Object object = objectClass.getDeclaredConstructor().newInstance();

        for (Method method : objectClass.getMethods()) {
            String methodName = method.getName();

            if (!methodName.startsWith("set") || methodName.length() <= 3) continue;
            if (method.getParameterCount() != 1) continue;

            String fieldName = Character.toLowerCase(methodName.charAt(3))
                    + methodName.substring(4);
            String paramName = prefix + fieldName;
            Class<?> type = method.getParameterTypes()[0];

            if (type == Fichier.class) {
                method.invoke(object, extraireFichier(req, paramName));
            } else if (isSimpleType(type)) {
                String value = req.getParameter(paramName);
                if (value == null || value.isEmpty()) continue;
                method.invoke(object, convertValue(value, type));
            } else if (hasNestedParam(req, paramName + ".")) {
                method.invoke(object, bindObject(type, req, paramName + "."));
            }
        }
        return object;
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Character.class;
    }

    private boolean hasNestedParam(HttpServletRequest req, String prefix) {
        java.util.Enumeration<String> names = req.getParameterNames();
        while (names.hasMoreElements()) {
            if (names.nextElement().startsWith(prefix)) return true;
        }
        return false;
    }

    private Fichier extraireFichier(HttpServletRequest req, String name)
    throws IOException, ServletException
    {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
            return null;
        }

        Part part = req.getPart(name);
        if (part == null) return null;

        String nom = part.getSubmittedFileName();
        if (nom == null || nom.isEmpty() || part.getSize() == 0) {
            return null;
        }

        nom = nom.substring(Math.max(nom.lastIndexOf('/'), nom.lastIndexOf('\\')) + 1);

        try (InputStream in = part.getInputStream()) {
            return new Fichier(nom, part.getContentType(), part.getSize(), in.readAllBytes());
        }
    }

}
