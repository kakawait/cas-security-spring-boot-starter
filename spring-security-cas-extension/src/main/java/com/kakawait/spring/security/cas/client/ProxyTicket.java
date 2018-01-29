package com.kakawait.spring.security.cas.client;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author Jonathan Coueraud
 */
public class ProxyTicket implements Ticket {
    private final String value;

    public ProxyTicket(String value) {
        Assert.notNull(value, "Value should not be null");
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyTicket that = (ProxyTicket) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
