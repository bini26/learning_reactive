package nchl.fellow.learning_reactive.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HomeController  {

    @GetMapping("/hello")
    public Mono<String> hello(Mono<Authentication> auth){
        Mono<String> message = auth.map(a-> "Hello " +a.getName());
//        return Mono.just("Hello World");
        return message;
    }

    @GetMapping("/test")
    public Mono<String> test(){
        Mono<String> message = ReactiveSecurityContextHolder.getContext()
                .map(cxt->cxt.getAuthentication())
                .map(auth-> "Hello "  + auth.getName());

        return message;

    }
}
