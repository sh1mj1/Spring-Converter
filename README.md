# Spring-Converter

# 1. 프로젝트 생성 & 스프링 타입 컨버터 소개

### 프로젝트 생성

스프링 부트 스타터 사이트로 이동해서 스프링 프로젝트를 생성합니다.

https://start.spring.io

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/4667292e-89b5-4740-8cb8-1f0fc969343e/Untitled.png)

이렇게 프로젝트를 생성하고 나서 생성된 프로젝트의 build.gradle 을 IntelliJ 로 프로젝트로 엽니다.

그 후 `build.gradle` 이 아래처럼 정상적으로 Dependencies 가 적용되었는지 확인합니다.

```java
plugins {
	id 'java'
	id 'org.springframework.boot' version '2.7.8'
	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

마지막으로 동작을 확인하면 됩니다.

기본 메인 클래스(`TypeconverterApplication.main()`) 을 실행합니다. 그 후 [http://localhost:8080](http://localhost:8080) 을 호출해서 Whitelabel Error Page 가 나오면 정상적으로 동작하는 것입니다. 아래 그림처럼 말이죠.

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/65450c08-8d2f-4fe0-9420-67663126f7c4/Untitled.png)

### 스프링 타입 컨버터 소개

문자를 숫자로 변환하거나 반대로 숫자를 문자로 변환해야 하는 것처럼 애플리케이션을 개발하다 보면 타입을 변환해야 하는 경우가 상당히 많습니다.

아래 예를 봅시다.

`HelloController` - 문자 타입을 숫자 타입으로 변경

```java
package hello.typeconverter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HelloController {
    @GetMapping("/hello-v1")
    public String helloV1(HttpServletRequest request) {
        String data = request.getParameter("data"); // 문자 타입 조회
        Integer intValue = Integer.valueOf(data); // 숫자 타입으로 변경
        System.out.println("intValue = " + intValue);
        return "ok";
    }
}
```

실행

http://localhost:8080/hello-v1?data=10

분석

`String data = request.getParameter(”data”)`

**HTTP 요청 파라미터는 모두 문자로 처리됩니다.** 따라서 요청 파라미터를 자바에서 다른 타입으로 변환해서 사용하고 싶으면 아래처럼 숫자 타입으로 변환하는 과정을 거쳐야 합니다.

`Integer intValue = Integer.valueOf(data)`

이번에는 스프링 MVC 가 제공하는 `@RequestParam` 을 사용해봅시다.

`HelloController` - 추가

```java
@GetMapping("/hello-v2")
public String helloV2(@RequestParam Integer data) {
    System.out.println("data =" + data);
    return "ok";
}
```

http://localhost:8080/hello-v2?data=10

앞서 보았듯이 HTTP 쿼리 스트링으로 전달하는 `data=10` 부분에서 10은 숫자가 아닌 문자 10입니다. 스프링이 제공하는 `@RequestParam` 을 사용하면 이 문자 10 을 Integer 타입의 숫자 10으로 편리하게 받을 수 있습니다. 

이것은 스프링이 중간에서 타입을 변환해주었기 때문입니다.

이러한 예는 `@ModelAttribute`, `@PathVariable` 에서도 확인할 수 있습니다.

`@ModelAttribute` 타입 변환 예시

```java
@ModelAttribute UserData data

class UserData {
		Integer data;
}
```

`@RequestParam` 와 같이, 문자 `data=10` 을 숫자 10으로 받을 수 있습니다.

`@PathVariable` 타입 변환 예시

```java
/users/{userId}
@PathVariable("userId") Integer data
```

URL 경로는 문자입니다. `/users/10` → 여기서 10도 숫자 10이 아니라 그냥 문자 “10”입니다. `data` 를 `Integer` 타입으로 받을 수 있는 것도 스프링이 타입변환을 해주기 때문입니다.

스프링의 타입 변환 적용 예

    스프링 MVC 요청 파라미터 

            `@RequestParam`, `@ModelAttribute`, `@PathVariable`

    `@Value` 등으로 YML 정보 읽기

    XML 에 넣은 스프링 빈 정보를 변환

    뷰를 렌더링 할 때

스프링과 타입 변환

이렇게 타입을 변환해야 하는 경우는 상당히 많습니다. 개발자가 직접 하나하나 타입 변환을 하는 것은 너무나도 번거롭지요…

스프링이 중간에 타입 변환기를 사용해서 타입을 `String` → `Integer` 로 변환해주었기 때문에 개발자는 편리하게 해당 타입을 바로 받을 수 있습니다. 앞에서는 문자를 숫자로 변경하는 예시를 들었지만 반대로 숫자를 문자로 변경하는 것도 가능하고, `Boolean` 타입을 숫자로 변경하는 것도 가능합니다. 만약 개발자가 새로운 타입을 만들어서 변환하고 싶으면 어떻게 해야 할까요?

### 컨버터 인터페이스

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/9bae95ee-bbab-43c3-8c65-e62ada43f91f/Untitled.png)

스프링은 확장 가능한 컨버터 인터페이스를 제공합니다.

개발자는 스프링에 추가적인 타입 변환이 필요하면 이 컨버터 인터페이스를 구현해서 등록하면 됩니다. 이 컨버터 인터페이스는 모든 타입에 적용할 수 있습니다. 필요하다면 X → Y 타입으로 변환하는 컨버터 인터페이스를 만들고, 또 Y → X 타입으로 변환하는 컨버터 인터페이스를 만들어서 등록하면 됩니다.

예를 들어서 문자로 `“true”` 가 오면 `Boolean` 타입으로 받고 싶으면 `Boolean` → `String` 타입으로 변환되도록 컨버터 인터페이스를 만들어서 등록하고, 반대로 적용하고 싶으면 `Boolean` → `String` 타입으로 변환되도록 컨버터를 추가로 만들어서 등록하면 됩니다.

참고

과거에는 `PropertyEditor` 라는 것으로 타입을 변환했습니다. `PropertyEditor` 는 동시성 문제가 있어서 타입을 변환할때 마다 객체를 새로 생성해야 했지만 현재는 `Converter` 의 등장으로 해당 문제들이 해결되었고, 기능 확장이 필요하면 `Converter` 을 사용하면 됩니다.

다음 챕터에서 실제 코드를 통해 타입 컨버터를 이해할 것입니다.

