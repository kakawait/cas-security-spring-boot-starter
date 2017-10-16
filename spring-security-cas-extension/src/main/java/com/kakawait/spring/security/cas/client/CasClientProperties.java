package com.kakawait.spring.security.cas.client;

import java.util.Objects;

/**
 * @author Jonathan Coueraud
 */
public class CasClientProperties {

    private String proxyTicketQueryKey = "ticket";

    public String getProxyTicketQueryKey() {
        return proxyTicketQueryKey;
    }

    public CasClientProperties setProxyTicketQueryKey(String proxyTicketQueryKey) {
        this.proxyTicketQueryKey = proxyTicketQueryKey;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CasClientProperties that = (CasClientProperties) o;
        return Objects.equals(proxyTicketQueryKey, that.proxyTicketQueryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxyTicketQueryKey);
    }
}
