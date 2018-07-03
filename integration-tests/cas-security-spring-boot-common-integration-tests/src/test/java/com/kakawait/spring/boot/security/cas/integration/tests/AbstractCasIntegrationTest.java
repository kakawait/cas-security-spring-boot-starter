package com.kakawait.spring.boot.security.cas.integration.tests;

import com.kakawait.spring.boot.security.cas.CasSecurityProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplateHandler;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jasig.cas.client.util.CommonUtils.constructRedirectUrl;
import static org.springframework.boot.test.util.TestPropertyValues.of;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * @author Thibaud Leprêtre
 */
public abstract class AbstractCasIntegrationTest {

    private static final String CAS_BASE_URL_PROPERTY_KEY = "test.cas.base-url";

    private static DockerComposeContainer container;

    @BeforeClass
    public static void startupContainers() throws URISyntaxException {
        if (System.getProperty(CAS_BASE_URL_PROPERTY_KEY) == null) {
            File dockerComposeFile = new File(
                    AbstractCasIntegrationTest.class.getResource("/docker/cas-server/docker-compose.yml").toURI());
            container = new DockerComposeContainer(dockerComposeFile)
                    .withLocalCompose(true)
                    .withExposedService("app", 8080, Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(10)));
            container.starting(null);
        }
    }

    @AfterClass
    public static void stopContainers() {
        if (container != null) {
            container.finished(null);
        }
    }

    static String getCasBaseUrl() {
        if (System.getProperty(CAS_BASE_URL_PROPERTY_KEY) != null) {
            return System.getProperty(CAS_BASE_URL_PROPERTY_KEY);
        } else {
            return format("http://%s:%d/cas", container.getServiceHost("app", 8080),
                    container.getServicePort("app", 8080));
        }
    }

    @Autowired
    private CasSecurityProperties casSecurityProperties;

    @Autowired
    private ServiceProperties serviceProperties;

    protected abstract TestRestTemplate getTestRestTemplate(TestRestTemplate.HttpClientOption... httpClientOptions);

    @Test
    public void security_SecurePath_RedirectToCasLoginPage() {
        TestRestTemplate restTemplate = getTestRestTemplate();
        ResponseEntity<Void> response = restTemplate.exchange("/", GET, null, Void.class);
        assertThat(response.getHeaders().getLocation())
                .isNotNull()
                .isEqualTo(getRedirectUrl(getRootUri(restTemplate) + "/login"));
    }

    @Test
    public void security_WithValidServiceTicket_Ok() {
        TestRestTemplate restTemplate = getTestRestTemplate();

        String ticket = getServiceTicketFor(getRootUri(restTemplate) + "/");
        ResponseEntity<Identity> response = restTemplate
                .exchange("/?" + serviceProperties.getArtifactParameter() + "=" + ticket , GET, null, Identity.class);
        Identity identity = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(identity).hasFieldOrPropertyWithValue("username", "casuser");
    }

    @Test
    public void security_WithInvalidServiceTicket_Unauthorized() {
        TestRestTemplate restTemplate = getTestRestTemplate();

        String ticket = "ST-FAKE";
        ResponseEntity<Identity> response = restTemplate
                .exchange("/?" + serviceProperties.getArtifactParameter() + "=" + ticket , GET, null, Identity.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String getServiceTicketFor(String service) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("username", "casuser");
        data.add("password", "Mellon");
        HttpEntity<Object> entity = new HttpEntity<>(data, headers);

        TestRestTemplate restTemplate = getTestRestTemplate();
        ResponseEntity<String> response = restTemplate
                .exchange(getCasTicketsApi(), POST, entity, String.class);
        String ticketGrantingTicket = response.getBody();

        URI serviceTicketApi = UriComponentsBuilder
                .fromUri(getCasTicketsApi()).path("/" + ticketGrantingTicket).build().toUri();

        data = new LinkedMultiValueMap<>();
        data.add(serviceProperties.getServiceParameter(), service);
        entity = new HttpEntity<>(data, headers);
        response = restTemplate.exchange(serviceTicketApi, POST, entity, String.class);
        return response.getBody();
    }

    private URI getRedirectUrl(String service) {
        String redirectUrl = constructRedirectUrl(getCasLoginUrl().toASCIIString(),
                serviceProperties.getServiceParameter(), service, false, false);
        return URI.create(redirectUrl);
    }

    private URI getCasLoginUrl() {
        return UriComponentsBuilder
                .fromUri(casSecurityProperties.getServer().getBaseUrl())
                .path(casSecurityProperties.getServer().getPaths().getLogin())
                .build().toUri();
    }

    private URI getCasTicketsApi() {
        return UriComponentsBuilder
                .fromUri(casSecurityProperties.getServer().getBaseUrl())
                .path("/v1/tickets")
                .build().toUri();
    }

    private String getRootUri(TestRestTemplate testRestTemplate) {
        UriTemplateHandler uriTemplateHandler = testRestTemplate.getRestTemplate().getUriTemplateHandler();
        if (uriTemplateHandler instanceof RootUriTemplateHandler) {
            return ((RootUriTemplateHandler) uriTemplateHandler).getRootUri();
        }
        return "";
    }

    private static class Identity {
        private String username;

        private String pgt;

        public String getUsername() {
            return username;
        }

        public String getPgt() {
            return pgt;
        }
    }

}
