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


# 2. 타입 컨버터(Converter)

타입 컨버터를 어떻게 사용하는지 코드로 알아봅시다.

타입 컨버터를 사용하려면 `org.springframework.core.convert.converter.Converter` 인터페이스를 구현하면 됩니다. 이전 챕터에서 사진으로 미리 보았죠?

먼저 가장 단순한 형태인 문자를 숫자로 바꾸는 타입 컨버터를 만들어봅시다.

`StringToIntegerConverter` - 문자를 숫자로 변환하는 타입 컨버터

```java
package hello.typeconverter.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public class StringToIntegerConverter implements Converter<String, Integer> {
    @Override
    public Integer convert(String source) {
        log.info("convert source={}", source);
        return Integer.valueOf(source);
    }

}
```

`String` →  `Integer` 로 변환하기 때문에 소스가 String 이 된다. 이 문자를 `Integer.valueOf(source)` 를 사용해서 숫자로 변경한 다음에 변경된 숫자를 반환하면 된다

`IntegerToStringConverter` - 숫자를 문자로 변환하는 타입 컨버터

```java
package hello.typeconverter.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public class IntegerToStringConverter implements Converter<Integer, String> {
    @Override
    public String convert(Integer source) {
        log.info("convert source={}", source);
        return String.valueOf(source);
    }
}
```

이번에는 숫자를 문자로 변환하는 타입 컨버터입니다. 앞의 컨버터와 반대의 일을 합니다. 이번에는 숫자가 입력되기 때문에 소스가 `Integer` 가 됩니다. `String.valueOf(source)` 을 사용해서 문자로 변경한 다음 변경된 문자를 반환하면 됩니다.

테스트 코드를 통해서 타입 컨버터가 어떻게 동작하는지 확인해봅시다.

`ConverterTest` - 타입 컨버터 테스트 코드

```java
package hello.typeconverter.converter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ConverterTest {
    @Test
    void stringToInteger() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Integer result = converter.convert("10");
        assertThat(result).isEqualTo(10);
    }
    @Test
    void integerToString() {
        IntegerToStringConverter converter = new IntegerToStringConverter();
        String result = converter.convert(10);
        assertThat(result).isEqualTo("10");
    }
}
```

테스트 결과 정상적으로 수행되는 것을 알 수 있습니다.

### 사용자 정의 타입 컨버터

타입 컨버터 이해를 돕기 위해서 조금 다른 컨버터를 준비해보았습니다.

`127.0.0.1:8080` 과 같은 IP, PORT 를 입력하면 IpPort 객체로 변환하는 컨버터를 만들어봅시다.

`IpPort`

```java
package hello.typeconverter.type;

@Getter
@EqualsAndHashCode
public class IpPort {

    private String Ip;
    private int port;

    public IpPort(String ip, int port) {
        Ip = ip;
        this.port = port;
    }
}
```

롬복의 `@EqualAndHashCode` 을 넣으면 모든 필드를 사용해서 `equals()`, `hashcode()` 을 생성합니다.

따라서 모든 필드의 값이 같다면 `a.equal(b)` 의 결과가 참이 됩니다.

`StringToIpPortConverter` - 컨버터

```java
package hello.typeconverter.converter;

import hello.typeconverter.type.IpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public class StringToIpPortConverter implements Converter<String, IpPort> {

    @Override
    public IpPort convert(String source) {
        log.info("convert source={}", source);
        String[] split = source.split(":");
        String ip = split[0];
        int port = Integer.parseInt(split[1]);

        return new IpPort(ip, port);
    }
}
```

`127.0.0.1:8080` 같은 문자를 입력하면 `IpPort` 객체를 만들어 반환합니다.

`IpPortToStringConverter`

```java
package hello.typeconverter.converter;

import hello.typeconverter.type.IpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public class IpPortToStringConverter implements Converter<IpPort, String> {
    @Override
    public String convert(IpPort source) {
        log.info("convert source={}", source);
        return source.getIp() + ":" + source.getPort();
    }
}
```

`IpPort` 객체를 입력하면 `127.0.0.1:8080` 같은 문자를 반환합니다.

`ConverterTest` - `IpPort` 컨버터 테스트 추가

```java
@Test
void stringToIpPort() {
    StringToIpPortConverter converter = new StringToIpPortConverter();
    String source = "127.0.0.1:8080";
    IpPort result = converter.convert(source);
    assertThat(result).isEqualTo(new IpPort("127.0.0.1", 8080));
}

@Test
void ipPortToString() {
    IpPortToStringConverter converter = new IpPortToStringConverter();
    IpPort source = new IpPort("127.0.0.1", 8080);
    String result = converter.convert(source);
    assertThat(result).isEqualTo("127.0.0.1:8080");
}
```

이번에도 테스트가 성공합니다.

타입 컨버터 인터페이스가 단순해서 이해하기 어렵지 않을 것입니다.

그런데 이렇게 타입 컨버터를 하나하나 직접 사용하면, 개발자가 직접 컨버팅 하는 것과 큰 차이가 없습니다.

**타입 컨버터를 등록하고 관리하면서 편리하게 변환 기능을 제공하는 역할을 하는 무언가가 필요합니다. 이것은 다음 챕터에서 설명합니다.**

> 참고 - 스프링은 용도에 따라 다양한 방식의 타입 컨버터를 제공한다.
Converter →  기본 타입 컨버터
ConverterFactory → 전체 클래스 계층 구조가 필요할 때
GenericConverter → 정교한 구현, 대상 필드의 애노테이션 정보 사용 가능
ConditionalGenericConverter → 특정 조건이 참인 경우에만 실행
자세한 내용은 공식 문서를 참고하면 됩니다.
[https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#coreconvert](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#coreconvert)
> 

> 참고 - 스프링은 문자, 숫자, Boolean, Enum 등 일반적인 타입에 대한 대부분의 컨버터를 기본으로 제공한다.  
IDE에서 Converter , ConverterFactory , GenericConverter 의 구현체를 찾아보면 수 많은 컨버터를 확인할 수 있습니다.
>

# 3. 컨버전 서비스(ConversionService)

이전 챕터처럼 타입 컨버터를 하나하나 직접 찾아서 타입 변환에 사용하는 것은 매우 불편합니다.

그래서 스프링은 개별 컨버터를 모아두고 그것들을 묶어서 편리하게 사용할 수 있는 기능을 제공하는데, 이것이 바로 컨버전 서비스( `ConversionService` )입니다.

`ConversionService` 인터페이스

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/591ee3c7-db9a-442c-b143-d22600a0af8b/Untitled.png)

컨버전 서비스 인터페이스는 단순 컨버팅뿐 아니라 확인하는 기능과 컨버팅 기능 둘을 제공합니다.

사용 예를 확인해봅시다.

`ConversionServiceTest` - 컨버전 서비스 테스트 코드

```java
package hello.typeconverter.converter;

import hello.typeconverter.type.IpPort;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ConversionServiceTest {
    @Test
    void conversionService() {
        //등록
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new StringToIntegerConverter());
        conversionService.addConverter(new IntegerToStringConverter());
        conversionService.addConverter(new StringToIpPortConverter());
        conversionService.addConverter(new IpPortToStringConverter());

        //사용
        assertThat(conversionService.convert("10", Integer.class)).isEqualTo(10);
        assertThat(conversionService.convert(10, String.class)).isEqualTo("10");

        IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
        assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));

        String ipPortString = conversionService.convert(new IpPort("127.0.0.1", 8080), String.class);
        assertThat(ipPortString).isEqualTo("127.0.0.1:8080");

    }

}
```

`DefaultConversionService` 는 `ConversionService` 인터페이스를 구현했는데, 추가로 컨버터를 등록하는 기능도 제공합니다.

### 등록과 사용 분리

컨버터를 등록할 때는 `StringToIntegerConverter` 같은 타입 컨버터를 명확하게 알아야 합니다. 반면에 컨버터를 사용하는 입장에서는 타입 컨버터를 전혀 몰라도 됩니다. 

**타입 컨버터들은 모두 컨버전 서비스 내부에 숨어서 제공**됩니다. 따라서 타입을 변환을 원하는 사용자는 컨버전 서비스 인터페이스에만 의존하면 됩니다. 

물론 컨버전 서비스를 등록하는 부분과 사용하는 부분을 분리하고 의존관계 주입을 사용해야 합니다.

컨버전 서비스 사용

`Integer value = conversionService.convert(”10”, Integer.class)`

### 인터페이스 분리 원칙 - ISP(Interface Segregation Principle)

인터페이스 분리 원칙은 클라이언트가 자신이 이용하지 않는 메서드에 의존하지 않아야 합니다.

`DefaultConversionService` 는 다음 인터페이스를 구현했습니다.

        `ConversionService`: 컨버터 사용에 초점을 둔다.

        `ConvertRegistry`: 컨버터 등록에 초점을 둔다.

이렇게 인터페이스를 분리하면 컨버터를 사용하는 클라이언트와 컨버터를 등록하고 관리하는 클라이언트의 관심사를 명확하게 분리할 수 있습니다. 

특히 컨버터를 사용하는 클라이언트는 `ConversionService` 만 의존하면 되므로, 컨버터를 어떻게 등록하고 관리하는지는 전혀 몰라도 됩니다. 결과적으로 컨버터를 사용하는 클라이언트는 꼭 필요한 메서드만 알게 됩니다. 

이렇게 인터페이스를 분리하는 것을 `ISP` 라고 합니다.

스프링은 내부에서 `ConversionService` 를 사용해서 타입을 변환한다. 예를 들어서 앞서 살펴본 `@RequestParam` 같은 곳에서 이 기능을 사용해서 타입을 변환한다.

이제 컨버전 서비스를 스프링에 적용해봅시다.