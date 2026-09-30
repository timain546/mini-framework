package mg.nandra.framework.outils;

import java.util.Objects;
import mg.nandra.framework.annotation.UrlMapping;
import mg.nandra.framework.enumeration.HttpMethod;

public class UrlMethod {

    private String url;
    private HttpMethod method;

    public UrlMethod(UrlMapping urlMapping) {
        this.url = urlMapping.url();
        this.method = urlMapping.method();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method,url);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UrlMethod urlMethod) {
            return urlMethod.getMethod().equals(this.method) && urlMethod.getUrl().equals(this.url);
        }
        return false;
    }
}