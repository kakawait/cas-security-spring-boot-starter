package com.kakawait.sample;

import com.kakawait.spring.security.cas.client.CasAuthorizationInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static com.kakawait.sample.CasSecuritySpringBootSampleApplication.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
abstract class CasSecuritySpringBootSampleApplicationIT {

    @Autowired
    protected ApplicationContext applicationContext;

    @Test
    void contextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();
    }

    @Test
    void hasForwardedHeaderFilterBeanConfigured(ApplicationContext context) {
        FilterRegistrationBean forwardedHeaderFilter = (FilterRegistrationBean) context.getBean("forwardedHeaderFilter");
        assertThat(forwardedHeaderFilter).isNotNull();

        assertThat(forwardedHeaderFilter.getFilter()).isInstanceOf(ForwardedHeaderFilter.class);
        assertThat(forwardedHeaderFilter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void hasCasRestTemplateBeanConfigured(ApplicationContext context) {
        RestTemplate casRestTemplate = (RestTemplate) context.getBean("casRestTemplate");
        assertThat(casRestTemplate).isNotNull();

        assertThat(casRestTemplate.getInterceptors()).hasSize(1);
        assertThat(casRestTemplate.getInterceptors().get(0)).isInstanceOf(CasAuthorizationInterceptor.class);
    }

    @DisplayName("has static bean configured")
    @ParameterizedTest
    @ValueSource(classes = {SecurityConfiguration.class
            , OverrideDefaultCasSecurity.class
            , LogoutConfiguration.class
            , ApiSecurityConfiguration.class
            //, CustomLogoutConfiguration.class // In comment as custom-logout profile is NOT active
            //, WebMvcConfiguration.class // // In comment as custom-logout profile NOT active
            , BackwardSpringBoot1CasSecurityConfiguration.class
            , IndexController.class
            , HelloWorldController.class
    })
    void hasStaticBeanConfigured(Class clazz) {
        var staticBean = applicationContext.getBean(clazz);
        assertThat(staticBean).isNotNull();
    }

    @DisplayName("has noSuchBeanDefinitionException thrown")
    @ParameterizedTest
    @ValueSource(classes = {CustomLogoutConfiguration.class
            , WebMvcConfiguration.class
    })
    void noSuchBeanDefinitionExceptionIsThrown(Class clazz) {
        assertThatThrownBy(() -> applicationContext.getBean(clazz))
                .isInstanceOf(NoSuchBeanDefinitionException.class);
    }
}
