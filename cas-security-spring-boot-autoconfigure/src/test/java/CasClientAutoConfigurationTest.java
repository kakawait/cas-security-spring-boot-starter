import com.kakawait.spring.boot.security.cas.CasClientAutoConfiguration;
import com.kakawait.spring.security.cas.client.Cas;
import com.kakawait.spring.security.cas.client.CasStatelessInterceptor;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Jonathan Coueraud
 */
public class CasClientAutoConfigurationTest {

    @Test
    public void restTemplateGetsCasInterceptor() {
        ConfigurableApplicationContext context = init(OneRestTemplate.class);
        final Map<String, RestTemplate> restTemplates = context.getBeansOfType(RestTemplate.class);

        Assertions.assertThat(restTemplates).isNotNull();
        Assertions.assertThat(restTemplates.values()).hasSize(1);

        RestTemplate restTemplate = restTemplates.values().iterator().next();

        assertCas(restTemplate);
    }

    @Test
    public void multipleRestTemplates() {
        ConfigurableApplicationContext context = init(TwoRestTemplates.class);
        final Map<String, RestTemplate> restTemplates = context.getBeansOfType(RestTemplate.class);

        Assertions.assertThat(restTemplates).isNotNull();
        Collection<RestTemplate> templates = restTemplates.values();
        Assertions.assertThat(templates).hasSize(2);

        TwoRestTemplates.Two two = context.getBean(TwoRestTemplates.Two.class);

        Assertions.assertThat(two.casRestTemplate).isNotNull();
        assertCas(two.casRestTemplate);

        Assertions.assertThat(two.nonCas).isNotNull();
        Assertions.assertThat(two.nonCas.getInterceptors()).isEmpty();
    }

    private void assertCas(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        Assertions.assertThat(interceptors).hasSize(1);
        ClientHttpRequestInterceptor interceptor = interceptors.get(0);
        Assertions.assertThat(interceptor).isInstanceOf(CasStatelessInterceptor.class);
    }

    private ConfigurableApplicationContext init(Class<?> config) {
        return new SpringApplicationBuilder().web(false)
                .sources(config, CasClientAutoConfiguration.class).run();
    }

    @Configuration
    public static class OneRestTemplate {

        @Cas
        @Bean
        RestTemplate casRestTemplate() {
            return new RestTemplate();
        }
    }

    @Configuration
    public static class TwoRestTemplates {

        @Primary
        @Bean
        RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Cas
        @Bean
        RestTemplate casRestTemplate() {
            return new RestTemplate();
        }

        @Configuration
        protected static class Two {
            @Autowired
            RestTemplate nonCas;

            @Autowired
            @Cas
            RestTemplate casRestTemplate;
        }
    }
}
