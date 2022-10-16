package com.kakawait.spring.security.cas.client.ticket;

/**
 * Proxy ticker provider is simple interface that provides a way to get a CAS proxy ticket for a given service
 * and current bounded user.
 *
 * @author Jonathan Coueraud
 * @author Thibaud Lepretre
 * @since 0.7.0
 */
public interface ProxyTicketProvider {

    /**
     * Ask proxy ticket for a given service to CAS server.
     *
     * @param service service name or (mostly) service URL
     * @return the proxy ticket or {@code null} if CAS server won't be able to return us a proxy ticket for given
     * {@code service}
     */
    String getProxyTicket(String service);
}
