package com.kakawait.spring.security.cas;


import org.junit.jupiter.api.Test;
import org.springframework.security.cas.ServiceProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Thibaud Leprêtre
 */
public class LaxServicePropertiesTest {

    @Test
    public void afterPropertiesSet_WithDynamicServiceResolutionAndNullOrEmptyService_Ok() {
        LaxServiceProperties serviceProperties = new LaxServiceProperties(true);
        serviceProperties.afterPropertiesSet();
        assertThat(serviceProperties.isDynamicServiceResolution()).isTrue();
    }

    @Test
    public void afterPropertiesSet_WithoutDynamicServiceResolutionValueAndNullOrEmptyService_IllegalArgumentException() {
        LaxServiceProperties serviceProperties = new LaxServiceProperties(false);
        assertThrows(IllegalArgumentException.class, serviceProperties::afterPropertiesSet);
    }

    @Test
    public void afterPropertiesSet_WithNullService_NoException() {
        ServiceProperties serviceProperties = new LaxServiceProperties();
        serviceProperties.setService(null);
        serviceProperties.afterPropertiesSet();

        assertThat(serviceProperties.getService()).isNull();
    }

    @Test
    public void afterPropertiesSet_WithNullOrEmptyArtifactParameter_IllegalArgumentException() {
        LaxServiceProperties serviceProperties = new LaxServiceProperties();
        serviceProperties.setArtifactParameter(null);

        assertThatThrownBy(serviceProperties::afterPropertiesSet).isInstanceOf(IllegalArgumentException.class);

        serviceProperties.setArtifactParameter("");
        assertThatThrownBy(serviceProperties::afterPropertiesSet).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void afterPropertiesSet_WithNullOrEmptyServiceParameter_IllegalArgumentException() {
        LaxServiceProperties serviceProperties = new LaxServiceProperties();
        serviceProperties.setServiceParameter(null);

        assertThatThrownBy(serviceProperties::afterPropertiesSet).isInstanceOf(IllegalArgumentException.class);

        serviceProperties.setServiceParameter("");
        assertThatThrownBy(serviceProperties::afterPropertiesSet).isInstanceOf(IllegalArgumentException.class);
    }
}
