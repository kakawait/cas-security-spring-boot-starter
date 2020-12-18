package com.kakawait.spring.boot.security.cas.integration.tests;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static java.lang.String.format;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.boot.test.util.TestPropertyValues.of;

/**
 * @author Thibaud Leprêtre
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"security.cas.service.resolution-mode=dynamic", "logging.level.org.springframework.security=debug"})
@ContextConfiguration(initializers = {CasIntegrationTest.Initializer.class})
public class CasIntegrationTest extends AbstractCasIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Override
    protected TestRestTemplate getTestRestTemplate(TestRestTemplate.HttpClientOption... httpClientOptions) {
        TestRestTemplate testRestTemplate = new TestRestTemplate(restTemplateBuilder, null, null, httpClientOptions);
        LocalHostUriTemplateHandler handler = new LocalHostUriTemplateHandler(environment);
        testRestTemplate.setUriTemplateHandler(handler);
        return testRestTemplate;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = of(property("security.cas.server.base-url", getCasBaseUrl()));
            values.applyTo(configurableApplicationContext);
        }

        private String property(String key, String value) {
            return format("%s=%s", key, value);
        }
    }

}
