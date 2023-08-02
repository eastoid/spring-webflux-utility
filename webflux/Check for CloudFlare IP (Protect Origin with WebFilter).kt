


/* 

	security measure to protect origin server by only allowing CloudFlare requests through (Cache 100 last CF ips)
	CloudFlare IP list: https://www.cloudflare.com/ips/
	(Imports under code)

*/



// --> This configuration (for now) is blocking <--
To do: cache val, nonblocking code (👺)


@Component
class WebfluxFilter : WebFilter {

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

    // cached allowed IPs
    private var ipCache = ConcurrentHashMap<String, Boolean>()

    // filter function
    override fun filter(
        serverWebExchange: ServerWebExchange,
        webFilterChain: WebFilterChain
    ): Mono<Void> {

        // get request remote address
        val requestIP = serverWebExchange.request.remoteAddress?.address?.hostAddress ?: ""

        // check if IP is from Cloudflare using cache
        if (ipCache.count() >= 50) {
            // check if too many ip cached
            unCache()
        }

        val cloudflareIP = ipCache.computeIfAbsent(requestIP) { ip ->
            checkIPInRange(ip)
        }

        var ip = ""

        // action if / if not Cloudflare IP
        if (!cloudflareIP) {
            return Mono.empty()
        } else { //action if ip is from CF
            ip = serverWebExchange.request.headers.getFirst("CF-Connecting-IP").toString()
            if (!ip.contains(".")) return Mono.empty() // Invalid Cloudflare IP in the header
        }


/*																												<== Process request from CloudFlare  						*/


        // Continue filter chain
        return webFilterChain.filter(serverWebExchange)
    }
    
    // function to remove half of cached ip
    fun unCache() {
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
    fun checkIPInRange(ipAddress: String?): Boolean {
        try {
            val address = InetAddress.getByName(ipAddress)
            for (range in IP_RANGES) {
                val subnetUtils = SubnetUtils(range)
                if (subnetUtils.info.isInRange(address.hostAddress)) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Log or handle the exception if required
            e.printStackTrace()
        }
        return false
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
