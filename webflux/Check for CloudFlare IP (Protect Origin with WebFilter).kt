


/* 

	WebFlux WebFilter component
 
	security measure to protect origin server by only allowing CloudFlare requests through
	CloudFlare IP list: https://www.cloudflare.com/ips/

	Uses external libraries:
 		com.github.ben-manes.caffeine:caffeine
   		commons-net:commons-net


*/



@Component
class WebfluxFilter : WebFilter {


    // Calculated IP address cache (for quick block/allow decisions)
    private val ipCache = Caffeine.newBuilder()
        .maximumSize(2000)
        .build<String, Boolean>()


    // https://www.cloudflare.com/ips-v4/#
    // 	implementation: com.github.ben-manes.caffeine:caffeine 3.1.8
    // https://github.com/ben-manes/caffeine
    private val SubnetUtilsRanges = setOf(
        SubnetUtils("173.245.48.0/20"),
        SubnetUtils("103.21.244.0/22"),
        SubnetUtils("103.22.200.0/22"),
        SubnetUtils("103.31.4.0/22"),
        SubnetUtils("141.101.64.0/18"),
        SubnetUtils("108.162.192.0/18"),
        SubnetUtils("190.93.240.0/20"),
        SubnetUtils("188.114.96.0/20"),
        SubnetUtils("197.234.240.0/22"),
        SubnetUtils("198.41.128.0/17"),
        SubnetUtils("162.158.0.0/15"),
        SubnetUtils("104.16.0.0/13"),
        SubnetUtils("104.24.0.0/14"),
        SubnetUtils("172.64.0.0/13"),
        SubnetUtils("131.0.72.0/22"),
    )


    // filter function
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {


        val requestIP = exchange.request.remoteAddress?.address?.hostAddress ?: return chain.filter(exchange)

        val cloudflareIp = ipCache.getIfPresent(requestIP) ?: let {
            val isCloudflareIp = requestIP.isCloudflareIp()
            ipCache.put(requestIP, isCloudflareIp)
            isCloudflareIp
        }

        if (!cloudflareIp) { // Not CloudFlare request
            return exchange.response.setComplete() // Respond to request with nothing
        }

        val ip = exchange.request.headers.getFirst("CF-Connecting-IP").toString()
        if (!ip.isValidIp()) {
            exchange.response.statusCode = HttpStatusCode.valueOf(500)
            return chain.filter(exchange) // ip from cloudflare header is invalid
        }


        return chain.filter(exchange)
    }


    val ipv4Regex = ("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$").toRegex()
    fun String?.isValidIp(): Boolean {
        if (this == null) return false
        val matches = this.matches(ipv4Regex)
        return matches
    }


    fun String.isCloudflareIp(): Boolean {


        try {
            val address = InetAddress.getByName(this)
            for (range in SubnetUtilsRanges) {
                if (range.info.isInRange(address.hostAddress)) return true
            }
        } catch (e: Exception) {

            e.printStackTrace()
        }
        return false
    }


}




/*

import com.github.benmanes.caffeine.cache.Caffeine
import org.apache.commons.net.util.SubnetUtils

import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.InetAddress

*/
