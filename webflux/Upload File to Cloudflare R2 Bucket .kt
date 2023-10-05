
/*

    Koltin method using AWS S3 SDK

    Upload a file to CloudFlare R2 bucket


    Providing this function because CF is incapable of documentation
    and they also stole the aws api - had to dig this up from their Postman collection


    Dependencies:
        implementation("software.amazon.awssdk:s3:2.20.160")
        rest of imports below



    Using AWS SDK for CloudFlare API, uploading file with PUT request.
    Tried to add auth header manually, but unfortunately i puked and also accidentally jumped out of my window,
    so didnt finish that


 */


/**
 * Credentials - System variable
 */
private val credentials = mapOf(
    "bucketUrl" to System.getenv("HOST_CF_BUCKET_URL"), // https://your-account-id--present-in-bucket-settings.r2.cloudflarestorage.com/bucket-name
    "bucketToken" to System.getenv("HOST_CF_BUCKET_APIKEY"), // api TOKEN  |  Unused
    "accessKeyId" to System.getenv("HOST_CF_BUCKET_APIKEYID"), // api key ID
    "accessKeySecret" to System.getenv("HOST_CF_BUCKET_APIKEYSECRET") // api key SECRET
)


/**
 * ### Upload function
 * Uploading an example image to cloudflare using AWS SDK
 */
suspend fun uploadImageToBucket(
    image: BufferedImage,
    fileName: String,
    mimeType: String,
): ResponseEntity<String> {

    val awsCredentials = AwsBasicCredentials.create(
        credentials["accessKeyId"] ?: "",
        credentials["accessKeySecret"] ?: ""
    )

    val r2bucket = S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .region(Region.of("auto"))
        .endpointOverride(URI.create(credentials["bucketUrl"]?.substringBeforeLast("/") ?: ""))
        .build()

    val baos = ByteArrayOutputStream()
    withContext(Dispatchers.IO) {
        ImageIO.write(image, mimeType.substringAfterLast("/"), baos)
    }
    val byteArray = baos.toByteArray()

    val request = PutObjectRequest.builder()
        .bucket(credentials["bucketUrl"]?.substringAfterLast("/"))
        .key(fileName)
        .contentType(mimeType)
        .build()

    val requestBody = RequestBody.fromBytes(byteArray)

    val putObjectResponse: PutObjectResponse = r2bucket.putObject(request, requestBody)

    return ResponseEntity.ok(putObjectResponse.eTag())
}




/*      IMPORTS


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URI
import javax.imageio.ImageIO


 */




 

/*
    Johnson Guzzler (c) 2027
    busta 3000
    2023/10/05T21:20
    Webinary Fluxary Flussary
    This article has been sponsored by the snakeyaml cve, 2023
    kt > ts
 */
