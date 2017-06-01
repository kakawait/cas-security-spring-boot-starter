# Spring Security CAS starter

> A Spring boot starter that will help you configure [Spring Security Cas](http://docs.spring.io/spring-security/site/docs/current/reference/html/cas.html) within the application security context.

## Features

- Configures CAS authentication and authorization
- Support dynamic service resolution based on current `HttpServletRequest`
- Advance configuration through [CasSecurityConfigurerAdapter](https://github.com/kakawait/cas-security-spring-boot-starter/blob/master/cas-security-spring-boot-autoconfigure/src/main/java/com/kakawait/spring/boot/security/cas/CasSecurityConfigurerAdapter.java)
- Integration with _Basic authentication_ if `security.basic.enabled=true` that allow you to authenticate using header `Authorization: Basic ...` in addition to _CAS_

## Setup

Artifact is not yet upload in maven central, you need to install it locally before adding dependency on your project (will be soon uploaded to maven)

```bash
./mvnw clean install
```

Add the Spring boot starter to your project

```xml
<dependency>
  <groupId>com.kakawait</groupId>
  <artifactId>cas-security-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Usage

In order to trigger auto-configuration you must fill, at least, the following properties regarding the resolution mode you want to use

### _static_ (_classic_) resolution mode

_static_ resolution mode is _classic_ and default mode that you could find if you're using plain old [Apereo Java client](https://github.com/apereo/java-cas-client) or [Spring Security CAS](http://docs.spring.io/spring-security/site/docs/current/reference/html/cas.html).

Thus you have to fill at least the following mandatory properties:

```yml
security:
  cas:
    server:
      base-url: http://your.cas.server/cas
    service:
      base-url: http://localhost:8080
```

| Property                        | [Apereo Java client](https://github.com/apereo/java-cas-client) equivalent | Description                                                                                                                                                                                                              |
|---------------------------------|----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `security.cas.server.base-url`  | `casServerUrlPrefix`                                                       | The start of the CAS server url, i.e. https://localhost:8443/cas                                                                                                                                                         |
| `security.cas.service.base-url` | `serviceName`                                                              | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port). |

### _dynamic_ resolution mode:

_dynamic_ resolution mode is a novel mode from that starter that will allow you to do not hard-code service url in your configuration. Thereby your configuration will be more portable and easy to use.

**ATTENTION** _dynamic_ resolution mode use information from `HttpServletRequest` to build service url, that can be a security breach if you do not control headers like `Host` or `X-Forwarded-*` that why _dynamic_ resolution mode **is not the default mode** and you must activate it as describe on above properties.

```yml
security:
  cas:
    server:
      base-url: http://your.cas.server/cas
    service:
      resolution-mode: dynamic
```

| Property                               | [Apereo Java client](https://github.com/apereo/java-cas-client) equivalent | Description                                                                                                                                                                                                         |
|----------------------------------------|----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `security.cas.server.base-url`         | `casServerUrlPrefix`                                                       | the start of the CAS server url, i.e. https://localhost:8443/cas                                                                                                                                                    |
| `security.cas.service.resolution-mode` | **Not implemented**                                                        | Resolution modes can be `static` or `dynamic`, by default is `static` and you must fill `security.cas.service.base-url` whereas in `dynamic` mode service url will be generated from receiving `HttpServletRequest` |

if you're using `X-Forwarding-Prefix` header I will strongly recommend you to use [ForwardedHeaderFilter](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/filter/ForwardedHeaderFilter.html) since _Tomcat_ [`RemoteIpValve`](https://tomcat.apache.org/tomcat-8.5-doc/api/org/apache/catalina/valves/RemoteIpValve.html) used when setting up `server.use-forward-headers=true` does not support _prefix_/_context-path_.

```java
@Bean
FilterRegistrationBean forwardedHeaderFilter() {
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
    filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return filterRegistrationBean;
}
```

## Properties

The supported properties are:

| Property                                    | [Apereo Java client](https://github.com/apereo/java-cas-client) equivalent | Default value                  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|---------------------------------------------|----------------------------------------------------------------------------|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `security.cas.enabled`                      | _N/A_                                                                      | `true`                         | Enable CAS security                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `security.cas.key`                          | _N/A_                                                                      | `UUID.randomUUID().toString()` | An id used by the [`CasAuthenticationProvider`](https://docs.spring.io/spring-security/site/docs/current/apidocs/org/springframework/security/cas/authentication/CasAuthenticationProvider.html#setKey-java.lang.String-)                                                                                                                                                                                                                                                                                                                                         |
| `security.cas.paths`                        | _N/A_                                                                      | `/**`                          | Comma-separated list of paths to secure (work as same way as `security.basic.path`)                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `security.cas.user.roles-attributes`        | _N/A_                                                                      |                                | Comma-separated list of CAS attributes to be used to determine user roles, see [GrantedAuthorityFromAssertionAttributesUserDetailsService](https://docs.spring.io/spring-security/site/docs/3.2.4.RELEASE/apidocs/org/springframework/security/cas/userdetails/GrantedAuthorityFromAssertionAttributesUserDetailsService.html#GrantedAuthorityFromAssertionAttributesUserDetailsService(java.lang.String[])) for more details.  If `security.cas.user.roles-attributes` is _null_ or _empty_ user default roles will be those defined in `cas.security.user.role` |
| `security.cas.server.protocol-version`      | _N/A_                                                                      | 3                              | Determine which CAS protocol version to be used, only protocol version 1, 2 or 3 is supported.                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `security.cas.server.base-url`              | `casServerUrlPrefix`                                                       |                                | The start of the CAS server url, i.e. https://localhost:8443/cas                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `security.cas.server.paths.login`           | `casServerLoginUrl`                                                        | `/login`                       | Defines the location of the CAS server login path that will be append to the existing `security.cas.server.base-url` url                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `security.cas.server.paths.login`           | _N/A_                                                                      | `/logout`                      | Defines the location of the CAS server logout path that will be append to the existing `security.cas.server.base-url` url                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `security.cas.service.resolution-mode`      | **Not implemented**                                                        | `static`                       | Resolution modes can be `static` or `dynamic`, by default is `static` and you must fill `security.cas.service.base-url` whereas in `dynamic` mode service url will be generated from receiving `HttpServletRequest`                                                                                                                                                                                                                                                                                                                                               |
| `security.cas.service.base-url`             | `serviceName`                                                              |                                | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port).  Skipped if resolution mode is `dynamic`.                                                                                                                                                                                                                                                                                                |
| `security.cas.service.paths.login`          | _N/A_                                                                      | `/login`                       | Defines the application login path that will be append to the existing `security.cas.service.base-url` url                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `security.cas.service.paths.logout`         | _N/A_                                                                      | `/logout`                      | Defines the application logout path that will be append to the existing `security.cas.service.base-url` url                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `security.cas.service.paths.proxy-callback` | `proxyCallbackUrl`                                                         | `/cas/proxy-callback`          | The callback url to provide the CAS server to accept Proxy Granting Tickets.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |

Otherwise you can checkout [CasSecurityProperties](https://github.com/kakawait/cas-security-spring-boot-starter/blob/master/cas-security-spring-boot-autoconfigure/src/main/java/com/kakawait/spring/boot/security/cas/CasSecurityProperties.java) class.

## Additional configuration

If you need to set additional configuration options simply register within Spring application context instance of [`CasSecurityConfigurerAdapter`](https://github.com/kakawait/cas-security-spring-boot-starter/blob/master/cas-security-spring-boot-autoconfigure/src/main/java/com/kakawait/spring/boot/security/cas/CasSecurityConfigurerAdapter.java)

```java
@Configuration
public class CustomCasSecurityConfiguration extends CasSecurityConfigurerAdapter {
    @Override
    public void configure(CasAuthenticationFilterConfigurer filter) {
        // Here you can configure CasAuthenticationFilter
    }

    @Override
    public void configure(CasAuthenticationProviderSecurityBuilder provider) {
        // Here  you can configure CasAuthenticationProvider
    }
}
```

Otherwise every beans defined in that starter are annotated with `@ConditionOnMissingBean` thus you can override default bean definitions.

## License

MIT License
