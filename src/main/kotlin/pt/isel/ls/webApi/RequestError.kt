package pt.isel.ls.webApi

import kotlinx.serialization.Serializable
import org.http4k.core.Status

class BadRequestException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)
class ForbiddenRequestException(message: String) : RuntimeException(message)
class NotFoundException(message: String) : RuntimeException(message)

@Serializable
data class RequestError(val status: Int, val message: String)

fun getError(e: Exception): RequestError {
    return when (e) {
        is BadRequestException -> RequestError(Status.BAD_REQUEST.code, e.message ?: "")
        is UnauthorizedException -> RequestError(Status.UNAUTHORIZED.code, e.message ?: "")
        is ForbiddenRequestException -> RequestError(Status.FORBIDDEN.code, e.message ?: "")
        is NotFoundException -> RequestError(Status.NOT_FOUND.code, e.message ?: "")
        else -> RequestError(Status.INTERNAL_SERVER_ERROR.code, "Something went wrong")
    }
}
