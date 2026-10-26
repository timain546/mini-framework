package mg.nandra.framework.model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public class MySession {

    private final HttpServletRequest request;

    public MySession(HttpServletRequest request) {
        this.request = request;
    }

    private HttpSession session() {
        return request.getSession(true);
    }

    public Object get(String key) {
        HttpSession s = request.getSession(false);
        return (s == null) ? null : s.getAttribute(key);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public void set(String key, Object value) {
        session().setAttribute(key, value);
    }

    public void remove(String key) {
        HttpSession s = request.getSession(false);
        if (s != null) s.removeAttribute(key);
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        HttpSession s = request.getSession(false);
        if (s != null) {
            Enumeration<String> names = s.getAttributeNames();
            for (String n : Collections.list(names)) {
                map.put(n, s.getAttribute(n));
            }
        }
        return map;
    }

    public String getId() {
        return session().getId();
    }

    public void setMaxInactiveInterval(int seconds) {
        session().setMaxInactiveInterval(seconds);
    }

    // Déconnexion : détruit la session
    public void invalidate() {
        HttpSession s = request.getSession(false);
        if (s != null) s.invalidate();
    }
}
