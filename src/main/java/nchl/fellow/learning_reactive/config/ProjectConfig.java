package nchl.fellow.learning_reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import java.time.LocalTime;
import java.util.function.Function;

@Configuration
public class ProjectConfig {

    @Bean
    public ReactiveUserDetailsService userDetailsService(){
        var u= User.withUsername("user")
                .password("password")
                .authorities("read")
                .roles("ADMIN")
               .build();

        return new MapReactiveUserDetailsService(u);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
//        http.httpBasic(Customizer.withDefaults());
//        http.authorizeExchange(c->c.pathMatchers(HttpMethod.GET,"/hello","/test")
//                .authenticated()
//                .anyExchange()
//                .permitAll());
//        return http.build();
//    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){

        http.httpBasic(Customizer.withDefaults());
        http.authorizeExchange(c->c.anyExchange()
                .access(this::getAuthorizationDecisionMono));
        return http.build();
    }

    private Mono<AuthorizationDecision> getAuthorizationDecisionMono(Mono<Authentication> a,
                                                                     AuthorizationContext c){

        String path = getRequestPath(c);
        boolean restrictedTime = LocalTime.now().isAfter(LocalTime.NOON);

        if(path.equals("/hello")){
            return a.map(isAdmin())
                    .map(auth->auth && !restrictedTime)
                    .map(AuthorizationDecision::new);
        }
        return Mono.just(new AuthorizationDecision(false));
    }

    private String getRequestPath(AuthorizationContext c){
        return c.getExchange()
                .getRequest()
                .getPath()
                .toString();
    }
    private Function<Authentication,Boolean> isAdmin(){

        return p-> {
            boolean isAdmin = p.getAuthorities().stream()
                    .anyMatch(e -> e.getAuthority().equals("ROLE_ADMIN"));

            System.out.println("isAdmin check: " + isAdmin);
            return isAdmin;

        };

}
}