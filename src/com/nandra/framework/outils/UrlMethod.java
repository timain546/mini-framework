package com.nandra.framework.outils;

import java.util.Objects;
import mg.itu.test.annotation.UrlMapping;
import com.nandra.framework.constant.HttpMethod;

public class UrlMethod {

    private HttpMethod method;
    private String url;

    public UrlMethod(UrlMapping urlMapping) {
        this.method = urlMapping.method();
        this.url = urlMapping.url();
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, url);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UrlMethod urlMethod) {
            return urlMethod.getMethod().equals(this.method) && urlMethod.getUrl().equals(this.url);
        }
        return false;
    }
}