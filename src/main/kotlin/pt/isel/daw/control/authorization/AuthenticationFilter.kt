package pt.isel.daw.control.authorization

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pt.isel.daw.model.Profile

typealias CredentialsVerifier = (String, Jdbi) -> Profile?

const val PROFILE_ATTRIBUTE = "Profile-Attribute" // Doesn't matter what the value is
const val BASIC_SCHEME = "Basic"
const val CHALLENGE_HEADER = "WWW-Authenticate"

@Component
class AuthenticationFilter(private val credentialsVerifier: CredentialsVerifier, private val jdbi: Jdbi) : Filter {

    private val logger = LoggerFactory.getLogger(AuthenticationFilter::class.java)

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {

        val httpRequest = request as HttpServletRequest
        val authorizationHeader: String = httpRequest.getHeader("authorization") ?: ""

        val userInfo = credentialsVerifier(authorizationHeader, jdbi)
        if (userInfo != null || httpRequest.method == "GET") {
            logger.info("User credentials are valid. Proceeding.")
            httpRequest.setAttribute(PROFILE_ATTRIBUTE, userInfo)
            chain?.doFilter(request, response)
        } else {
            logger.info("User credentials are invalid or were not provided. Issuing challenge.")
            val httpResponse = response as HttpServletResponse
            httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            httpResponse.addHeader(CHALLENGE_HEADER, "$BASIC_SCHEME realm=\"daw\"")
        }
    }
}