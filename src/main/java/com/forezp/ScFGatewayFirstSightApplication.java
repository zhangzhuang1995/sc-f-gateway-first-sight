package com.forezp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class ScFGatewayFirstSightApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScFGatewayFirstSightApplication.class, args);
    }

    /**
     * 在下面的myRoutes方法中，使用了一个RouteLocatorBuilder的bean去创建路由，除了创建路由RouteLocatorBuilder
     * 可以让你添加各种predicates和filters，predicates断言的意思，顾名思义就是根据具体的请求的规则，
     * 由具体的route去处理，filters是各种过滤器，用来对请求做各种判断和修改。
     * <p>
     * 创建的route可以让请求"/get"请求都转发到"http://httpbin.org/get"
     * 在route配置上，我们添加了一个filter，该filter会将请求添加一个header,key为hello，value为world。
     * <p>
     * http://localhost:8080/get 转发到了 http://httpbin.org/get，并加上了header
     * <p>
     * 在spring cloud gateway中可以使用Hystrix。Hystrix在spring cloud gateway中是以filter的形式使用的。
     * 该router使用host去断言请求是否进入该路由，当请求的host有“*.hystrix.com”，都会进入该router，
     * 该router中有一个hystrix的filter,该filter可以配置名称、和指向性fallback的逻辑的地址，比如本案例中重定向到了“/fallback”。
     * <p>
     * <p>
     * <p>
     * curl --dump-header - --header Host:www.hystrix.com http://localhost:8080/delay/3 拦截
     * curl --dump-header - --header Host:www.baicu.com http://localhost:8080/get 不拦截
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        String httpUri = "http://httpbin.org:80";
        return builder.routes()
                .route(p -> p
                        .path("/get") //拦截/get路径，转发到httpUri路径，并加上header
                        .filters(f -> f.addRequestHeader("hello", "world"))
                        .uri(httpUri))
                .route(p -> p
                        .host("*.hystrix.com") //拦截/.hystrix.com路径，转发到fallback路径
                        .filters(f -> f
                                .hystrix(config -> config
                                        .setName("mycmd")
                                        .setFallbackUri("forward:/fallback")))
                        .uri(httpUri))
                .build();
    }

    /**
     * spring5响应式编程。Flux和Mono是Reactor中的两个基本概念。
     * Flux表示的是包含0到N个元素的异步序列。Mono表示的是包含0或者1个元素的异步序列。
     *
     * @return
     */
    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }
}
