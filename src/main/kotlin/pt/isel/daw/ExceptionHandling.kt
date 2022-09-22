package pt.isel.daw

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import java.sql.SQLException
import org.postgresql.util.PSQLException
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import pt.isel.daw.view.problem.notFound
import pt.isel.daw.view.problem.problemResponse

@ControllerAdvice
class ExceptionHandling : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        //TODO: When is this ever thrown?
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {
        logger.info("Handling MethodArgumentNotValidException")
        return problemResponse("about:blank", "Method Argument Not Valid", 400, "Method Argument Not Valid Exception")
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {
        logger.info("Handling HttpMessageNotReadableException")
        if (ex.cause is MissingKotlinParameterException)
            return problemResponse(
                "about:blank", "Bad request body", 400,
                "Required property missing: " + (ex.cause as MissingKotlinParameterException).parameter.name
            )
        else if (ex.cause is InvalidFormatException)
            return problemResponse(
                "about:blank", "Bad request body", 400,
                "Unexpected value in JSON body, check documentation for correct usage"
            )
        return problemResponse(
            "about:blank", "Bad Request", 400,
            "Read documentation for correct request info"
        )
    }

    override fun handleMissingPathVariable(
        ex: MissingPathVariableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {
        logger.info("Handling MethodArgumentNotValidException")
        return problemResponse("about:blank", "Missing Parameters", 400, "Missing Parameters: " + ex.parameter)
    }

    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {
        logger.info("Handling handleTypeMismatch")
        if (ex is MethodArgumentTypeMismatchException)
            return problemResponse(
                "about:blank", "Argument Not Valid Exception", 400,
                "Expected: " + ex.requiredType + " on " + ex.name
            )
        return problemResponse("about:blank", "Argument Not Valid Exception", 400, ex.message)
    }

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {

        logger.info("Handling HttpRequestMethodNotSupportedException")
        return problemResponse("about:blank", "Bad HTTP Request", 400, ex.message)
    }

    @ExceptionHandler(SQLException::class)
    fun handlePSQLException(ex: PSQLException): ResponseEntity<Any> {
        logger.info("Exception thrown, state: ${ex.sqlState}@${ex.serverErrorMessage?.line} > $ex")
        return when (ex.sqlState) {
            "28P01" -> problemResponse("about:blank", "Database Unresponsive", 500, "Couldn't connect to the database")
            "23503" -> if (ex.serverErrorMessage?.line == 2490)
                problemResponse(
                    "about:blank",
                    "Resource in use",
                    403,
                    "The value you are trying to alter is being referenced by another resource"
                )
            else notFound("Parent resource not found", "One of the specified resources does not exist")
            "23505" -> problemResponse(
                "about:blank",
                "Duplicate Value error",
                403,
                "Value already taken by another instance"
            )
            else -> problemResponse("about:blank", "Error", 500, "Something went wrong")
        }
    }


}