package com.kakawait.spring.boot.security.cas;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.validation.AbstractCasProtocolUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Cas10TicketValidator;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.ProxyList;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author Thibaud Leprêtre
 */
@Accessors(fluent = true)
@Setter
@Slf4j
public class CasTicketValidatorBuilder {

    private int protocolVersion = 3;

    private Boolean proxyTicketValidator;

    final String casServerUrlPrefix;

    String proxyCallbackUrl;

    ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    ProxyRetriever proxyRetriever;

    HttpURLConnectionFactory urlConnectionFactory;

    boolean renew;

    Map<String, String> customParameters;

    Boolean proxyChainsValidation;

    ProxyList proxyChains;

    Boolean allowEmptyProxyChain;

    CasTicketValidatorBuilder(String casServerUrlPrefix) {
        this.casServerUrlPrefix = casServerUrlPrefix;
    }

    public TicketValidator build() {
        CasTicketValidatorBuilder builder;
        if (proxyTicketValidator == null && protocolVersion > 1) {
            logger.debug("\"proxyTicketValidator\" configuration is missing, fallback on proxyTicketValidation = true");
        }
        if (protocolVersion > 3 || protocolVersion < 1) {
            logger.warn("Protocol version {} is not valid protocol, will be fallback to version 3", protocolVersion);
        }
        if (protocolVersion == 1) {
            if (proxyTicketValidator != null) {
                logger.warn("Proxy ticket validator isn't possible using protocol version 1, will be omitted!");
            }
            builder = new Cas10TicketValidatorBuilder(casServerUrlPrefix);
        } else if (protocolVersion == 2) {
            if (proxyTicketValidator != null && !proxyTicketValidator) {
                builder = new Cas20ServiceTicketValidatorBuilder(casServerUrlPrefix);
            } else {
                builder = new Cas20ProxyTicketValidatorBuilder(casServerUrlPrefix);
            }
        } else {
            if (proxyTicketValidator != null && !proxyTicketValidator) {
                builder = new Cas30ServiceTicketValidatorBuilder(casServerUrlPrefix);
            } else {
                builder = new Cas30ProxyTicketValidatorBuilder(casServerUrlPrefix);
            }
        }
        configure(builder);
        return builder.build();
    }

    private void configure(CasTicketValidatorBuilder builder) {
        builder.proxyCallbackUrl(proxyCallbackUrl)
               .proxyGrantingTicketStorage(proxyGrantingTicketStorage)
               .proxyRetriever(proxyRetriever)
               .urlConnectionFactory(urlConnectionFactory)
               .renew(renew)
               .customParameters(customParameters)
               .proxyChainsValidation(proxyChainsValidation)
               .proxyChains(proxyChains)
               .allowEmptyProxyChain(allowEmptyProxyChain);
    }

    private static abstract class AbstractTicketValidatorBuilder<T extends AbstractCasProtocolUrlBasedTicketValidator>
            extends CasTicketValidatorBuilder {

        AbstractTicketValidatorBuilder(String casServerUrlPrefix) {
            super(casServerUrlPrefix);
        }

        protected void configure(T ticketValidator) {
            if (urlConnectionFactory != null) {
                ticketValidator.setURLConnectionFactory(urlConnectionFactory);
            }
            if (customParameters != null) {
                ticketValidator.setCustomParameters(customParameters);
            }
            ticketValidator.setRenew(renew);
        }
    }

    private static class Cas10TicketValidatorBuilder extends AbstractTicketValidatorBuilder<Cas10TicketValidator> {

        private static final String OMISSION_MESSAGE_TEMPLATE =
                "Configuration \"{}\" isn't possible using protocol version 1, will be omitted!";

        Cas10TicketValidatorBuilder(String casServerUrlPrefix) {
            super(casServerUrlPrefix);
        }

        @Override
        public TicketValidator build() {
            Cas10TicketValidator ticketValidator = new Cas10TicketValidator(casServerUrlPrefix);
            if (StringUtils.hasText(proxyCallbackUrl)) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyCallbackUrl");
            }
            if (proxyGrantingTicketStorage !=  null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyGrantingTicketStorage");
            }
            if (proxyRetriever != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyRetriever");
            }
            if (proxyChainsValidation != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyChainsValidation");
            }
            if (proxyChains != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyChains");
            }
            if (allowEmptyProxyChain != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "allowEmptyProxyChain");
            }
            configure(ticketValidator);
            return ticketValidator;
        }
    }

    private static class Cas20ServiceTicketValidatorBuilder
            extends AbstractTicketValidatorBuilder<Cas20ServiceTicketValidator> {

        static final String OMISSION_MESSAGE_TEMPLATE = "Configuration \"{}\" isn't possible using " +
                "service ticket validator (please consider proxy ticket validator), will be omitted!";

        Cas20ServiceTicketValidatorBuilder(String casServerUrlPrefix) {
            super(casServerUrlPrefix);
        }

        @Override
        public TicketValidator build() {
            Cas20ServiceTicketValidator ticketValidator = new Cas20ServiceTicketValidator(casServerUrlPrefix);
            if (proxyChainsValidation != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyChainsValidation");
            }
            if (proxyChains != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyChains");
            }
            if (allowEmptyProxyChain != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "allowEmptyProxyChain");
            }
            configure(ticketValidator);
            return ticketValidator;
        }

        @Override
        protected void configure(Cas20ServiceTicketValidator ticketValidator) {
            super.configure(ticketValidator);
            if (proxyGrantingTicketStorage != null) {
                ticketValidator.setProxyGrantingTicketStorage(proxyGrantingTicketStorage);
            }
            if (proxyRetriever != null) {
                ticketValidator.setProxyRetriever(proxyRetriever);
            }
            if (StringUtils.hasText(proxyCallbackUrl)) {
                ticketValidator.setProxyCallbackUrl(proxyCallbackUrl);
            }
        }
    }

    private static class Cas20ProxyTicketValidatorBuilder extends Cas20ServiceTicketValidatorBuilder {

        Cas20ProxyTicketValidatorBuilder(String casServerUrlPrefix) {
            super(casServerUrlPrefix);
        }

        @Override
        public TicketValidator build() {
            Cas20ProxyTicketValidator ticketValidator = new Cas20ProxyTicketValidator(casServerUrlPrefix);
            configure(ticketValidator);

            if (proxyChainsValidation != null) {
                ticketValidator.setAcceptAnyProxy(!proxyChainsValidation);
            }
            if (allowEmptyProxyChain != null) {
                ticketValidator.setAllowEmptyProxyChain(allowEmptyProxyChain);
            }
            if (proxyChains != null) {
                ticketValidator.setAllowedProxyChains(proxyChains);
            }

            return ticketValidator;
        }
    }

    private static class Cas30ServiceTicketValidatorBuilder extends Cas20ServiceTicketValidatorBuilder {

        Cas30ServiceTicketValidatorBuilder(String casServerUrlPrefix) {
            super(casServerUrlPrefix);
        }

        @Override
        public TicketValidator build() {
            Cas30ServiceTicketValidator ticketValidator = new Cas30ServiceTicketValidator(casServerUrlPrefix);
            if (proxyChainsValidation != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyChainsValidation");
            }
            if (proxyChains != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "proxyChains");
            }
            if (allowEmptyProxyChain != null) {
                logger.warn(OMISSION_MESSAGE_TEMPLATE, "allowEmptyProxyChain");
            }
            configure(ticketValidator);
            return ticketValidator;
        }
    }

    private static class Cas30ProxyTicketValidatorBuilder extends Cas20ServiceTicketValidatorBuilder {

        Cas30ProxyTicketValidatorBuilder(String casServerUrlPrefix) {
            super(casServerUrlPrefix);
        }

        @Override
        public Cas30ProxyTicketValidator build() {
            Cas30ProxyTicketValidator ticketValidator = new Cas30ProxyTicketValidator(casServerUrlPrefix);
            configure(ticketValidator);

            if (proxyChainsValidation != null) {
                ticketValidator.setAcceptAnyProxy(!proxyChainsValidation);
            }
            if (allowEmptyProxyChain != null) {
                ticketValidator.setAllowEmptyProxyChain(allowEmptyProxyChain);
            }
            if (proxyChains != null) {
                ticketValidator.setAllowedProxyChains(proxyChains);
            }

            return ticketValidator;
        }
    }
}
