import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain



/*
	-- basic Spring Webflux Security chain configuration --


	Documentation can be found on:
	https://docs.spring.io/spring-security/reference/reactive/configuration/webflux.html
	
	But differences in launguages are confusing
	
	
	-> Dependency:
		implementation("org.springframework.boot:spring-boot-starter-security")


	
	(i) Enable authentication on /actuator/**, allow other requests. Disable CSRF, disable CORS
/*






@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .authorizeExchange { exchanges: ServerHttpSecurity.AuthorizeExchangeSpec ->
                exchanges
                    .pathMatchers("/actuator/**").authenticated()
                    .anyExchange().permitAll()
            }
            .csrf(Customizer { csrf -> csrf.disable() })
            .cors { it.disable() }
            .httpBasic(withDefaults())
        return http.build()
    }
}