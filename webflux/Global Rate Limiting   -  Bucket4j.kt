import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Bucket4j
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration



/*

	Code can be found on: 
	https://blog.davidvassallo.me/2020/09/18/rate-limiting-spring-reactive-web-apis-bucket4j/
	
	
	
	Dependency: 
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:+")

	Official project:
	https://bucket4j.com
	https://github.com/bucket4j/bucket4j
	
	
*/











object RateLimitingCache {
    val byIP = HashMap<String, Bucket>()
}

@Component
class ApiFilter() : WebFilter {

    private fun createSessionRateLimitBucket() : Bucket {
        val limit = Bandwidth.simple(30, Duration.ofMinutes(1))
        return Bucket.builder().addLimit(limit).build()
    }

    private fun createIpRateLimitBucket() : Bucket {
        val limit = Bandwidth.simple(30, Duration.ofMinutes(1))
        return Bucket.builder().addLimit(limit).build()
    }


    override fun filter(serverWebExchange: ServerWebExchange,
                        webFilterChain: WebFilterChain
    ): Mono<Void> {

        val sourceIP = serverWebExchange.request.remoteAddress!!.address.hostAddress
        if (RateLimitingCache.byIP.containsKey(sourceIP)){
            if (!RateLimitingCache.byIP[sourceIP]!!.tryConsume(1)){
                serverWebExchange.response.statusCode= HttpStatus.BANDWIDTH_LIMIT_EXCEEDED
                return  Mono.empty()
            }
        } else {
            RateLimitingCache.byIP[sourceIP] = createIpRateLimitBucket()
        }

        return serverWebExchange.session
            // use flatmap to extract the WebSession object from serverWebExchange
            .flatMap { webSession ->
                // check if a bucket already exists for this session
                if (webSession.attributes.containsKey("bucket")){
                    // if it does - extract the bucket from the session
                    val bucket = webSession.attributes["bucket"] as Bucket
                    // consume a token
                    if (bucket.tryConsume(1)){
                        // if allowed - i.e. not over the allocated rate,
                        // then pass request on to the next filter in the chain
                        webFilterChain.filter(serverWebExchange)
                    } else {
                        // if not allowed then modify response code and immediately return to client
                        serverWebExchange.response.statusCode=HttpStatus.BANDWIDTH_LIMIT_EXCEEDED
                        Mono.empty()
                    }
                } else {
                    // if bucket does not exist create a new one
                    val bucket = createSessionRateLimitBucket()
                    // save bucket to session
                    webSession.attributes["bucket"]=bucket
                    bucket.tryConsume(1)
                    // pass on the request to the next filter in the chain
                    webFilterChain.filter(serverWebExchange)
                }
            }

    }
}
