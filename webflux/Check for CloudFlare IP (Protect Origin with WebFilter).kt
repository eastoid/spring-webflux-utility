


/* 

	security measure to protect origin server by only allowing CloudFlare requests through (Cache 100 last CF ips)
	CloudFlare IP list: https://www.cloudflare.com/ips/
	(Imports under code)


	Updated:
 		A maybe slightly better version of the filter that may or may not be reactive
		  but a working filter regardless



    TODO: Optimize
    TODO TODO: This method literally breaks the chain why tf did i upload it

*/



private var ipCache = ConcurrentHashMap<String, Boolean>()

private val IP_RANGES = setOf(
    "173.245.48.0/20",
    "103.21.244.0/22",
    "103.22.200.0/22",
    "103.31.4.0/22",
    "141.101.64.0/18",
    "108.162.192.0/18",
    "190.93.240.0/20",
    "188.114.96.0/20",
    "197.234.240.0/22",
    "198.41.128.0/17",
    "162.158.0.0/15",
    "104.16.0.0/13",
    "104.24.0.0/14",
    "172.64.0.0/13",
    "131.0.72.0/22"
)


@OptIn(DelicateCoroutinesApi::class)
@Component
class WebfluxFilter : WebFilter {


    // filter function
    override fun filter(
        serverWebExchange: ServerWebExchange,
        webFilterChain: WebFilterChain
    ): Mono<Void> {
        var result: Mono<Void> = Mono.empty()
        runBlocking {
            val requestIP = serverWebExchange.request.remoteAddress?.address?.hostAddress ?: ""
            if (ipCache.count() >= 50) {
                unCache()
            }
            val cloudflareIP = ipCache.computeIfAbsent(requestIP) { ip ->
                runBlocking {
                    checkIPInRange(ip).awaitFirstOrNull() ?: false
                }
            }

            println("checking for")
            if (cloudflareIP) {
                println("Allowing CloudFlare connection")

                val ip = serverWebExchange.request.headers.getFirst("CF-Connecting-IP").toString()
                if (!ip.contains(".")) {
                    result = Mono.empty()
                    
                }

		result = webFilterChain.filter(serverWebExchange)
            } else {


                println("Refusing direct connection")

		result = Mono.empty()

            }
        }

        return result
    }



    // function to remove half of cached ip
    suspend fun unCache() {
        try {
            val sortedEntries = ipCache.entries.toList().sortedBy { it.key }
            val splitIndex = ipCache.size / 2
            val newHashMap = ConcurrentHashMap<String, Boolean>()
            for (i in splitIndex until sortedEntries.size) {
                val entry = sortedEntries[i]
                newHashMap[entry.key] = entry.value
            }
            ipCache.clear()
            ipCache.putAll(newHashMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // check if ip is in ip (CF) range
    suspend fun checkIPInRange(ipAddress: String?): Mono<Boolean> {
        try {
            val address = InetAddress.getByName(ipAddress)
            for (range in IP_RANGES) {
                val subnetUtils = SubnetUtils(range)
                if (subnetUtils.info.isInRange(address.hostAddress)) {
                    return Mono.just(true)
                }
            }
        } catch (e: Exception) {
            // Log or handle the exception if required
            e.printStackTrace()
        }
        return Mono.just(false)
    }


}






/*
import org.apache.commons.net.util.SubnetUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
*/
