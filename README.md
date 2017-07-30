# Spring Security CAS starter

[![Maven Central](https://img.shields.io/maven-central/v/com.kakawait/cas-security-spring-boot-starter.svg)](https://search.maven.org/#artifactdetails%7Ccom.kakawait%7Ccas-security-spring-boot-starter%7C0.4.4%7Cjar)
[![license](https://img.shields.io/github/license/kakawait/cas-security-spring-boot-starter.svg)](https://github.com/kakawait/cas-security-spring-boot-starter/blob/master/LICENSE.md)

> A Spring boot starter that will help you configure [Spring Security Cas](http://docs.spring.io/spring-security/site/docs/current/reference/html/cas.html) within the application security context.

## Features

- Configures CAS authentication and authorization
- Support dynamic service resolution based on current `HttpServletRequest`
- Advance configuration through [CasSecurityConfigurerAdapter](https://github.com/kakawait/cas-security-spring-boot-starter/blob/master/cas-security-spring-boot-autoconfigure/src/main/java/com/kakawait/spring/boot/security/cas/CasSecurityConfigurerAdapter.java)
- Integration with _Basic authentication_ if `security.basic.enabled=true` that allow you to authenticate using header `Authorization: Basic ...` in addition to _CAS_

## Setup

Add the Spring boot starter to your project

```xml
<dependency>
  <groupId>com.kakawait</groupId>
  <artifactId>cas-security-spring-boot-starter</artifactId>
  <version>0.4.4</version>
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

**ATTENTION** _dynamic_ resolution mode use information from `HttpServletRequest` to build service url, that can be a security breach if you do not control headers like `Host` or `X-Forwarded-*` that why _dynamic_ resolution mode **is not the default mode** and you must activate it as describe on below properties.

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

| Property                                    | Default value                  | Description                                                                                                                                                                                                                                                        |
|---------------------------------------------|--------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `security.cas.enabled`                      | `true`                         | Enable CAS security                                                                                                                                                                                                                                                |
| `security.cas.key`                          | `UUID.randomUUID().toString()` | An id used by the [`CasAuthenticationProvider`](https://docs.spring.io/spring-security/site/docs/current/apidocs/org/springframework/security/cas/authentication/CasAuthenticationProvider.html#setKey-java.lang.String-)                                          |
| `security.cas.paths`                        | `/**`                          | Comma-separated list of paths to secure (work as same way as `security.basic.path`)                                                                                                                                                                                |
| `security.cas.user.default-roles`           | `USER`                         | Comma-separated list of default user roles. If roles have been found from `security.cas.user.roles-attributes` default roles will be append to the list of users roles                                                                                             |
| `security.cas.user.roles-attributes`        |                                | Comma-separated list of CAS attributes to be used to determine user roles                                                                                                                                                                                          |
| `security.cas.proxy-validation.enabled`     | `true`                         | Defines if proxy should be checked again chains `security.cas.proxy-validation.chains`                                                                                                                                                                             |
| `security.cas.proxy-validation.chains`      |                                | Defines proxy chains. Each acceptable proxy chain should include a comma-separated list of URLs (for exact match) or regular expressions of URLs (starting by the ^ character)                                                                                     |
| `security.cas.server.protocol-version`      | `3`                            | Determine which CAS protocol version to be used, only protocol version 1, 2 or 3 is supported.                                                                                                                                                                     |
| `security.cas.server.base-url`              |                                | The start of the CAS server url, i.e. https://localhost:8443/cas                                                                                                                                                                                                   |
| `security.cas.server.paths.login`           | `/login`                       | Defines the location of the CAS server login path that will be append to the existing `security.cas.server.base-url` url                                                                                                                                           |
| `security.cas.server.paths.logout`          | `/logout`                      | Defines the location of the CAS server logout path that will be append to the existing `security.cas.server.base-url` url                                                                                                                                          |
| `security.cas.service.resolution-mode`      | `static`                       | Resolution modes can be `static` or `dynamic`, by default is `static` and you must fill `security.cas.service.base-url` whereas in `dynamic` mode service url will be generated from receiving `HttpServletRequest`                                                |
| `security.cas.service.base-url`             |                                | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port).  Skipped if resolution mode is `dynamic`. |
| `security.cas.service.paths.login`          | `/login`                       | Defines the application login path that will be append to the existing `security.cas.service.base-url` url                                                                                                                                                         |
| `security.cas.service.paths.logout`         | `/logout`                      | Defines the application logout path that will be append to the existing `security.cas.service.base-url` url                                                                                                                                                        |
| `security.cas.service.paths.proxy-callback` |                                | The callback path that will be, if present, append to the `security.cas.service.base-url` and add to as parameter inside request validation. **It must be set if you want to receive _Proxy Granting Ticket_ `PGT`**.                                              |

Otherwise you can checkout [CasSecurityProperties](https://github.com/kakawait/cas-security-spring-boot-starter/blob/master/cas-security-spring-boot-autoconfigure/src/main/java/com/kakawait/spring/boot/security/cas/CasSecurityProperties.java) class.

## Additional configuration

If you need to set additional configuration options simply register within Spring application context instance of [`CasSecurityConfigurerAdapter`](https://github.com/kakawait/cas-security-spring-boot-starter/blob/master/cas-security-spring-boot-autoconfigure/src/main/java/com/kakawait/spring/boot/security/cas/CasSecurityConfigurerAdapter.java)

```java
@Configuration
class CustomCasSecurityConfiguration extends CasSecurityConfigurerAdapter {
    @Override
    public void configure(CasAuthenticationFilterConfigurer filter) {
        // Here you can configure CasAuthenticationFilter
    }
    
    @Override
    public void configure(CasSingleSignOutFilterConfigurer filter) {
        // Here you can configure SingleSignOutFilter
    }

    @Override
    public void configure(CasAuthenticationProviderSecurityBuilder provider) {
        // Here  you can configure CasAuthenticationProvider
    }
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // Here you can configure Spring Security HttpSecurity object during init configure
    }
    
    @Override
    public void configure(CasTicketValidatorBuilder ticketValidator) {
        // Here you can configure CasTicketValidator
    }
}
```

Otherwise many beans defined in that starter are annotated with `@ConditionOnMissingBean` thus you can override default bean definitions.

## Proxy granting storage

Starter does not provide any additional _proxy granting storage_ (yet), by default an _in memory_ storage is used [`ProxyGrantingTicketStorageImpl`](https://github.com/apereo/java-cas-client/blob/master/cas-client-core/src/main/java/org/jasig/cas/client/proxy/ProxyGrantingTicketStorageImpl.java).

To override it you can expose a `ProxyGrantingTicketStorage` beans like following:

```java
@Bean
ProxyGrantingTicketStorage proxyGrantingTicketStorage() {
    return new MyCustomProxyGrantingTicketStorage();
}
```

**Or** use `configurer` but a bit longer since you must report `ProxyGrantingTicketStorage` in both `CasAuthenticationFilter` and `TicketValidator`

```java
@Configuration
class CustomCasSecurityConfiguration extends CasSecurityConfigurerAdapter {
    @Override
    public void configure(CasAuthenticationFilterConfigurer filter) {
        filter.proxyGrantingTicketStorage(new MyCustomProxyGrantingStorage());
    }
    
    @Override
    public void configure(CasTicketValidatorBuilder ticketValidator) {
        ticketValidator.proxyGrantingTicketStorage(new MyCustomProxyGrantingStorage());
    }
}
```

## Logout & SLO

By default starter will configure both _logout_ and _single logout (SLO)_.

**ATTENTION** default _logout_ (on `/logout`) behavior will:
 
1. Logout from application and also logout from CAS server that will logout any other applications.
2. Keep default Spring security behavior concerning _CSRF_ and _logging out_ to summarize if _CSRF_ is enabled logout will only mapped on `POST`, see https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#csrf-logout for more details 

If you want to change those behaviors, for example by adding a logout page that will propose user to logout from other application, you may configure like following:

```java
@Configuration
class CasCustomLogoutConfiguration extends CasSecurityConfigurerAdapter {
    private final CasSecurityProperties casSecurityProperties;

    public CasCustomLogoutConfiguration(CasSecurityProperties casSecurityProperties) {
        this.casSecurityProperties = casSecurityProperties;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.logout()
            .permitAll()
            .logoutSuccessUrl("/logout.html")
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
        String logoutUrl = UriComponentsBuilder
                .fromUri(casSecurityProperties.getServer().getBaseUrl())
                .path(casSecurityProperties.getServer().getPaths().getLogout())
                .toUriString();
        LogoutFilter filter = new LogoutFilter(logoutUrl, new SecurityContextLogoutHandler());
        filter.setFilterProcessesUrl("/cas/logout");
        http.addFilterBefore(filter, LogoutFilter.class);
    }
}

@Configuration
class WebMvcConfiguration extends WebMvcConfigurerAdapter {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/logout.html").setViewName("logout");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
```

With possible `logout.html` like following
 
 ```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8" />
    <title>Logout page</title>
</head>
<body>
    <h2>Do you want to log out of CAS?</h2>
    <p>You have logged out of this application, but may still have an active single-sign on session with CAS.</p>
    <p><a href="/cas/logout" th:href="@{/cas/logout}">Logout of CAS</a></p>
</body>
</html>
```

You can checkout & run sample module [`cas-security-spring-boot-sample`](https://github.com/kakawait/cas-security-spring-boot-starter/tree/master/cas-security-spring-boot-sample) with _profile_ `custom-logout`.

## Proxy chains validation

By default client configuration is `security.cas.proxy-validation.enabled = true` with empty proxy chains (`security.cas.proxy-validation.chains`). That mean you will not be able to validate proxy ticket since proxy chains is empty.

You should disable proxy validation using:

```yml
security:
  cas:
    proxy-validation:
      enabled: false
```

**But is not recommended for production environment**, or define your own proxy chains:

```yml
security
  cas:
    proxy-validation:
      chains:
        - http://localhost:8180, http://localhost:8181
        - - http://localhost:8280
          - http://localhost:8281
        - ^http://my\\.domain\\..*
```

As you can see there is multiple syntaxes for `yml` format to define _collection of collection_:

1. Using _comma-separated_ list
2. Using double `- -` syntax

If you are using `properties` format you could translate like following:

```properties
security.cas.proxy-validation.chains[0] = http://localhost:8180, http://localhost:8181
security.cas.proxy-validation.chains[1] = http://localhost:8280, http://localhost:8281
security.cas.proxy-validation.chains[2] = ^http://my\\.domain\\..*
```

## License

MIT License
