package pt.isel.daw.control.authorization

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.daw.model.Profile

class AccessControlInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(AccessControlInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val userInfo = request.getAttribute(PROFILE_ATTRIBUTE) as? Profile
        logger.info("SampleInterceptor - preHandle with handler ${handler.javaClass.name} and user is ${userInfo?.name}")

        return when {
            request.method.equals("GET") -> true
            userInfo != null -> true
            else -> {
                // We could have used 404 (Not Found) instead
                response.status = HttpServletResponse.SC_FORBIDDEN
                false
            }
        }
    }

}