import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.*




/*
-- JSON Web Token --


Example docs:
https://dev.to/sayf21/security-cloud-with-jwt-and-webflux-2l04


*/






@Service
class JwtService {
    fun generateJwtToken(): String {
        val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
        val now = Date()
        val expiryDate = Date(now.time + Duration.ofMinutes(30).toMillis())
        return Jwts.builder()
            .setIssuer("Tissuer")
            .setIssuedAt(Date(893120400000)) // Unix time ms
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .setAudience("the fbi")
            .setSubject("eiliens")
            .setHeaderParam("header", "big fat fucking value")
            .compact()
    }
}




@RestController
class JwtController @Autowired constructor(private val jwtService: JwtService) {

    @get:GetMapping("/j")
    val jwt: ResponseEntity<String>
        get() {
            // Generate a basic JWT token
            val jwtToken = jwtService.generateJwtToken()

            // Send the JWT as an HTTP header in the response
            return ResponseEntity.status(HttpStatus.OK)
                .header("JWT tokan", jwtToken)
                .body("Jesus Womb Token sent :)")
        }
}