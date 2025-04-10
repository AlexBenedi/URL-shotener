@file:Suppress("MatchingDeclarationName", "WildcardImport")

package es.unizar.urlshortener

import es.unizar.urlshortener.infrastructure.delivery.ShortUrlDataOut
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URI
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

/**
 * Integration tests for HTTP requests.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HttpRequestTest {
    @LocalServerPort
    private val port = 0

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    /**
     * Sets up the test environment before each test.
     * Configures the HTTP client to disable redirect handling and clears the database tables.
     */
    @BeforeTest
    fun setup() {
        val httpClient = HttpClientBuilder.create()
            .disableRedirectHandling()
            .build()
        (restTemplate.restTemplate.requestFactory as HttpComponentsClientHttpRequestFactory).httpClient = httpClient

        JdbcTestUtils.deleteFromTables(jdbcTemplate, "shorturl", "click")
    }

    /**
     * Cleans up the test environment after each test.
     * Clears the database tables.
     */
    @AfterTest
    fun tearDowns() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "shorturl", "click")
    }

    /**
     * Tests that the main page is accessible and contains the expected content.
     */
    @Test
    fun `main page works`() {
        val response = restTemplate.getForEntity("http://localhost:$port/", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("A front-end example page for the project")
    }

    /**
     * Tests that a redirect is returned when the key exists.
     */
    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        val target = shortUrl("http://example.com/").headers.location
        require(target != null)

        // Wait for Kafka to process the message and update the URL's field in the database
        // this is not the best way to do it, but it works for now 
        // it should be replaced with a better solution in the future! 
        Thread.sleep(5000)
        val response = restTemplate.getForEntity(target, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.TEMPORARY_REDIRECT)
        assertThat(response.headers.location).isEqualTo(URI.create("http://example.com/"))

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(1)
    }

    /**
     * Tests that a forbidden is returned when the key exists but url is unsafe.
     */
    @Test
    fun `redirectTo returns a forbidden when the key exists but url is unsafe`() {
        val target = shortUrl("https://testsafebrowsing.appspot.com/s/malware.html").headers.location
        require(target != null)
        Thread.sleep(5000)
        val response = restTemplate.getForEntity(target, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(0)
    }

    // TODO test unsafety not checked (400): Will be made when async verification is implemented
    @Test
    fun `redirectTo returns a bad request when the key exists but url safety is not checked yet`() {
        val target = shortUrl("https://testsafebrowsing.appspot.com/s/malware.html").headers.location
        require(target != null)
        val response = restTemplate.getForEntity(target, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(0)
    }

    /**
     * Tests that a not found status is returned when the key does not exist.
     */
    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        val response = restTemplate.getForEntity("http://localhost:$port/f684a3c4", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(0)
    }

    /**
     * Tests that a basic redirect is created if a hash can be computed.
     */
    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        val response = shortUrl("http://example.com/")

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.headers.location).isEqualTo(URI.create("http://localhost:$port/f684a3c4"))
        assertThat(response.body?.url).isEqualTo(URI.create("http://localhost:$port/f684a3c4"))

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "shorturl")).isEqualTo(1)
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(0)
    }

    /**
     * Tests that a bad request status is returned if a hash cannot be computed.
     */
    @Test
    fun `creates returns bad request if it can't compute a hash`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val data: MultiValueMap<String, String> = LinkedMultiValueMap()
        data["url"] = "ftp://example.com/"

        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/link",
            HttpEntity(data, headers),
            ShortUrlDataOut::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "shorturl")).isEqualTo(0)
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(0)
    }

    /**
     * Tests that a forbidden status is returned if the URL is not safe.
     */
    @Ignore("This test is not needed")
    @Test
    fun `creates returns forbidden if the URL is not safe`() {
        val response = shortUrl("https://testsafebrowsing.appspot.com/s/phishing.html")

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "shorturl")).isEqualTo(0)
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "click")).isEqualTo(0)
    }
    /** 
     * Tests that accessing the /users/ endpoint redirects to the Google OAuth authentication page.
     */
    @Test
    fun `user endpoint redirects to Google OAuth`() {
        // Realiza una solicitud GET a la ruta /users/
        val response = restTemplate.getForEntity("http://localhost:$port/user", String::class.java)

        // Verifica que la respuesta sea un redireccionamiento
        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND) // 302 Found

        // Verifica que el Location header contenga la URL de Google OAuth
        val location = response.headers.location
        require(location != null) { "Location header should not be null" }

        assertThat(location.toString()).contains("/oauth2/authorization/google")
    }



    /**
     * Creates a short URL for the given URL.
     * @param url The URL to shorten.
     * @return The response entity containing the short URL data.
     */
    private fun shortUrl(url: String): ResponseEntity<ShortUrlDataOut> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val data: MultiValueMap<String, String> = LinkedMultiValueMap()
        data["url"] = url

        return restTemplate.postForEntity(
            "http://localhost:$port/api/link",
            HttpEntity(data, headers),
            ShortUrlDataOut::class.java
        )
    }
}
