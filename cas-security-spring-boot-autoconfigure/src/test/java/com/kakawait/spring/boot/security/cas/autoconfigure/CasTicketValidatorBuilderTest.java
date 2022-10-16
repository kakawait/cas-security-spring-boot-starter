package com.kakawait.spring.boot.security.cas.autoconfigure;

import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.validation.Cas10TicketValidator;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.ProxyList;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Lepretre
 */
@ExtendWith(OutputCaptureExtension.class)
public class CasTicketValidatorBuilderTest {

    private static final String CAS_SERVER_URL_PREFIX = "http://my.cas.server.base.url/";

    private static final String V1_WARN_MESSAGE_TEMPLATE =
            "WARN com.kakawait.spring.boot.security.cas.autoconfigure.CasTicketValidatorBuilder - " +
                    "Configuration \"%s\" isn't possible using protocol version 1, will be omitted!";

    private static final String SERVICE_VALIDATOR_WARN_MESSAGE_TEMPLATE =
            "WARN com.kakawait.spring.boot.security.cas.autoconfigure.CasTicketValidatorBuilder - " +
                    "Configuration \"%s\" isn't possible using service ticket validator " +
                    "(please consider proxy ticket validator), will be omitted!";

    private CasTicketValidatorBuilder builder;

    @BeforeEach
    public void setUp() {
        builder = new CasTicketValidatorBuilder(CAS_SERVER_URL_PREFIX);
    }

    @Test
    public void build_DefaultTicketValidator_Cas30ProxyTicketValidator() {
        assertThat(builder.build()).isInstanceOf(Cas30ProxyTicketValidator.class);
    }

    @Test
    public void build_UnsupportedProtocolVersion_FallbackToProtocolVersion3() {
        builder.protocolVersion(42);

        assertThat(builder.build()).isInstanceOf(Cas30ProxyTicketValidator.class);
    }

    @Test
    public void build_WithProtocolVersion1_Cas10TicketValidator() {
        builder.protocolVersion(1);

        assertThat(builder.build()).isInstanceOf(Cas10TicketValidator.class);
    }

    @Test
    public void build_WithProtocolVersion2_Cas20ProxyTicketValidator() {
        builder.protocolVersion(2);

        assertThat(builder.build()).isInstanceOf(Cas20ProxyTicketValidator.class);
    }

    @Test
    public void build_WithoutProxyTicketValidator_CasX0ServiceTicketValidator() {
        builder.protocolVersion(3).proxyTicketValidator(false);

        assertThat(builder.build()).isInstanceOf(Cas30ServiceTicketValidator.class);

        builder.protocolVersion(2).proxyTicketValidator(false);

        assertThat(builder.build()).isInstanceOf(Cas20ServiceTicketValidator.class);
    }

    @Test
    public void build_WithProxyTicketValidator_IgnoredWithProtocolVersion1() {
        builder.protocolVersion(1).proxyTicketValidator(true);

        assertThat(builder.build()).isInstanceOf(Cas10TicketValidator.class);

        builder.protocolVersion(1).proxyTicketValidator(false);

        assertThat(builder.build()).isInstanceOf(Cas10TicketValidator.class);
    }

    @Test
    public void build_UsingProtocolVersion3AndAnyParameters_InjectInsideTicketValidator() {
        testBuilder(3);
    }

    @Test
    public void build_UsingProtocolVersion2AndAnyParameters_InjectInsideTicketValidator() {
        testBuilder(2);
    }

    @Test
    public void build_UsingProtocolVersion1AndAnyParameters_InjectInsideTicketValidator() {
        testBuilder(1);
    }

    @Test
    public void build_ProtocolVersion1WithIncompatibleParameter_LogWarnMessage(CapturedOutput output) {
        int protocolVersion = 1;

        CasTicketValidatorBuilder builder = fulfilledBuilder();
        builder.protocolVersion(protocolVersion);

        builder.build();

        List<String> warns = new ArrayList<>();
        warns.add(String.format(V1_WARN_MESSAGE_TEMPLATE, "proxyCallbackUrl"));
        warns.add(String.format(V1_WARN_MESSAGE_TEMPLATE, "proxyGrantingTicketStorage"));
        warns.add(String.format(V1_WARN_MESSAGE_TEMPLATE, "proxyRetriever"));
        warns.add(String.format(V1_WARN_MESSAGE_TEMPLATE, "proxyChainsValidation"));
        warns.add(String.format(V1_WARN_MESSAGE_TEMPLATE, "proxyChains"));
        warns.add(String.format(V1_WARN_MESSAGE_TEMPLATE, "allowEmptyProxyChain"));

        for (String warn : warns) {
            assertThat(output).contains(warn);
        }
    }

    @Test
    public void build_ServiceValidatorProtocolWithIncompatibleParameter_LogWarnMessage(CapturedOutput output) {
        int protocolVersion = 2;

        CasTicketValidatorBuilder builder = fulfilledBuilder();
        builder.protocolVersion(protocolVersion).proxyTicketValidator(false);

        builder.build();

        List<String> warns = new ArrayList<>();
        warns.add(String.format(SERVICE_VALIDATOR_WARN_MESSAGE_TEMPLATE, "proxyChainsValidation"));
        warns.add(String.format(SERVICE_VALIDATOR_WARN_MESSAGE_TEMPLATE, "proxyChains"));
        warns.add(String.format(SERVICE_VALIDATOR_WARN_MESSAGE_TEMPLATE, "allowEmptyProxyChain"));

        for (String warn : warns) {
            assertThat(output).contains(warn);
        }

        protocolVersion = 3;

        builder.protocolVersion(protocolVersion).proxyTicketValidator(false);
        builder.build();

        for (String warn : warns) {
            assertThat(output).contains(warn);
        }
    }

    private CasTicketValidatorBuilder fulfilledBuilder() {
        HttpURLConnectionFactory urlConnectionFactory = Mockito.mock(HttpURLConnectionFactory.class);
        ProxyList proxyList = new ProxyList();
        boolean proxyChainsValidation = true;
        ProxyGrantingTicketStorage proxyGrantingTicketStorage = Mockito.mock(ProxyGrantingTicketStorage.class);
        String proxyCallbackUrl = "http://my.client/proxy/callback";
        boolean allowEmptyProxyChain = false;
        Map<String, String> customParameters = Collections.singletonMap("test", "value");
        ProxyRetriever proxyRetriever = Mockito.mock(ProxyRetriever.class);
        boolean renew = false;

        builder.urlConnectionFactory(urlConnectionFactory)
               .proxyChains(proxyList)
               .proxyChainsValidation(proxyChainsValidation)
               .proxyGrantingTicketStorage(proxyGrantingTicketStorage)
               .proxyCallbackUrl(proxyCallbackUrl)
               .allowEmptyProxyChain(allowEmptyProxyChain)
               .customParameters(customParameters)
               .proxyRetriever(proxyRetriever)
               .renew(renew);
        return builder;
    }

    private void testBuilder(int protocolVersion) {
        HttpURLConnectionFactory urlConnectionFactory = Mockito.mock(HttpURLConnectionFactory.class);
        ProxyList proxyList = new ProxyList();
        boolean proxyChainsValidation = true;
        ProxyGrantingTicketStorage proxyGrantingTicketStorage = Mockito.mock(ProxyGrantingTicketStorage.class);
        String proxyCallbackUrl = "http://my.client/proxy/callback";
        boolean allowEmptyProxyChain = false;
        Map<String, String> customParameters = Collections.singletonMap("test", "value");
        ProxyRetriever proxyRetriever = Mockito.mock(ProxyRetriever.class);
        boolean renew = false;


        builder.protocolVersion(protocolVersion)
               .urlConnectionFactory(urlConnectionFactory)
               .proxyChains(proxyList)
               .proxyChainsValidation(proxyChainsValidation)
               .proxyGrantingTicketStorage(proxyGrantingTicketStorage)
               .proxyCallbackUrl(proxyCallbackUrl)
               .allowEmptyProxyChain(allowEmptyProxyChain)
               .customParameters(customParameters)
               .proxyRetriever(proxyRetriever)
               .renew(renew);

        List<String> fields = new ArrayList<>();
        fields.add("urlConnectionFactory");
        fields.add("allowedProxyChains");
        fields.add("proxyGrantingTicketStorage");
        fields.add("proxyCallbackUrl");
        fields.add("customParameters");
        fields.add("proxyRetriever");
        List<Object> values = new ArrayList<>();
        values.add(urlConnectionFactory);
        values.add(proxyList);
        values.add(proxyGrantingTicketStorage);
        values.add(proxyCallbackUrl);
        values.add(customParameters);
        values.add(proxyRetriever);

        if (protocolVersion == 1) {
            fields.remove("allowedProxyChains");
            values.remove(proxyList);
            fields.remove("proxyCallbackUrl");
            values.remove(proxyCallbackUrl);
            fields.remove("proxyGrantingTicketStorage");
            values.remove(proxyGrantingTicketStorage);
            fields.remove("proxyRetriever");
            values.remove(proxyRetriever);
        }

        TicketValidator ticketValidator = builder.build();
        assertThat(ticketValidator)
                .extracting(fields.toArray(new String[0]))
                .usingElementComparator((Comparator<Object>) (o1, o2) -> (o1 == o2) ? 0 : -1)
                .containsExactly(values.toArray());

        if (protocolVersion > 1) {
            assertThat(ticketValidator).hasFieldOrPropertyWithValue("allowEmptyProxyChain", allowEmptyProxyChain);
            assertThat(ticketValidator).hasFieldOrPropertyWithValue("acceptAnyProxy", !proxyChainsValidation);
        }
        assertThat(ticketValidator).hasFieldOrPropertyWithValue("renew", renew);
    }

}
