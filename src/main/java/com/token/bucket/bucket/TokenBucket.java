package com.token.bucket.bucket;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "token")
public class TokenBucket implements Serializable {

    private Map<String,String> method;

    public Map<String, String> getMethod() {
        return method;
    }

    public void setMethod(Map<String, String> method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "TokenBucket{" +
                "method=" + method +
                '}';
    }
}