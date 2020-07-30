# Change Log

All notable changes to this project will be documented in this file.

## [1.0.3](https://github.com/kakawait/cas-security-spring-boot-starter/milestone/26) - 30 jul 2020

- Add new interface `ProxyCallbackUrlAwareTicketValidator` if you'd like to create your own `TicketValidator` but do not
want to extends `Cas20ServiceTicketValidator` (or others) because they added `final` everywhere and so is impossible
to extends...

## [1.0.2](https://github.com/kakawait/cas-security-spring-boot-starter/milestone/25) - 12 mar 2020

- Update dependencies for security fixes

## [1.0.1](https://github.com/kakawait/cas-security-spring-boot-starter/milestone/24) - 31 jan 2020

- Happy new year :tada:
- Move some method to become `public` in order to facilate customization (https://github.com/kakawait/cas-security-spring-boot-starter/pull/135/commits/02e1e805998f92216753cc87c6f3e99d6f6ff17e)

## [1.0.0](https://github.com/kakawait/cas-security-spring-boot-starter/milestone/9) - 5 sept 2019

- First release that supporting Spring boot 2 ([#32](https://github.com/kakawait/cas-security-spring-boot-starter/issues/32))
- Remove deprecated module `cas-security-dynamic-service-resolver` ([#35](https://github.com/kakawait/cas-security-spring-boot-starter/issues/35))
- Open some api to facilitate extensibility ([#122](https://github.com/kakawait/cas-security-spring-boot-starter/pull/122), [#109](https://github.com/kakawait/cas-security-spring-boot-starter/pull/109))

### Breaking changes

- Related to [#35 Remove cas-security-dynamic-service-resolver module](https://github.com/kakawait/cas-security-spring-boot-starter/issues/35), you must use [spring-security-cas-extension](https://github.com/kakawait/cas-security-spring-boot-starter/tree/master/spring-security-cas-extension) instead.
- Related to [#33
Rename package com.kakawait.spring.boot.security.cas to com.kakawait.spring.boot.security.cas.autoconfigure](https://github.com/kakawait/cas-security-spring-boot-starter/issues/33), you must rewrite your `import` statements to append `.autoconfigure.`.
- Property `security.cas.authorize-mode` has been renamed `security.cas.authorization.mode`
- `CasHttpSecurityConfigurer.cas().configure(http)` will no secure paths, only configure CAS authentication
- Autoconfiguration java class package was moved from `com.kakawait.spring.boot.security.cas` to `com.kakawait.spring.boot.security.cas.autoconfigure`

#### Spring boot 2 support breaking changes

By supporting _Spring Boot 2_, you should understand that some security features has been removed on _Spring Boot 2_ regarding _Spring Boot 1_ (see official documentations [Spring boot 2 migration guide - security](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide#security) and [Spring boot 2 security migration](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-Security-2.0)).

##### No more basic auth by default on Spring boot 2

Some behaviors will not be able on _Spring Boot 2_ for example excepting getting `Basic Auth` support in addition to `Cas Auth` if you put `security.basic.enabled=true` (which is default). You must re-add `Basic Auth` by yourself like on [`cas-security-spring-boot-sample`](https://github.com/kakawait/cas-security-spring-boot-starter/blob/develop/cas-security-spring-boot-sample/src/main/java/com/kakawait/sample/CasSecuritySpringBootSampleApplication.java#L157).

##### No more default `ROLE_USER` and `ROLE` based authentication by default

In addition, no more default role `ROLE_USER` will be added to any authenticated user.

Thus default `security.cas.authorization.mode` (formerly `security.cas.authorization.mode`) value is now `authenticated` instead of `role`.

However if you want to re-add default `ROLE_` on every authenticated user you could use:

```yml
security:
  cas:
    user:
      default-roles: USER
```

Where `security.cas.user.default-roles` accepts _list_ of roles.

And if you comes back to `ROLE` based authentication instead of _just authenticated_, you should:

```yml
security:
  cas:
    user:
      default-roles: USER
    authorization:
      mode: role
      roles: USER
```

Where `security.cas.authorization.roles` (which only useful when using `security.cas.authorization.mode=ROLE`) is _list_ of roles that use must have to be accepted.
