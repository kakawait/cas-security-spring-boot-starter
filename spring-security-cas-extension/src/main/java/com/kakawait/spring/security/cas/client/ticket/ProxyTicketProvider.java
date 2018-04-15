package com.kakawait.spring.security.cas.client.ticket;

/**
 * @author Jonathan Coueraud
 * @author Thibaud Leprêtre
 */
public interface ProxyTicketProvider {

    String getProxyTicket(String service);
}
