package pt.isel.ls.webApi

import org.http4k.core.Status

data class RequestErrorMem(val status: Status, val message: String)

fun getErrorMem(e: Exception): RequestErrorMem {
    return when (e) {
        is BadRequestException -> RequestErrorMem(Status.BAD_REQUEST, e.message ?: "")
        is UnauthorizedException -> RequestErrorMem(Status.UNAUTHORIZED, e.message ?: "")
        is ForbiddenRequestException -> RequestErrorMem(Status.FORBIDDEN, e.message ?: "")
        is NotFoundException -> RequestErrorMem(Status.NOT_FOUND, e.message ?: "")
        else -> RequestErrorMem(Status.INTERNAL_SERVER_ERROR, "Something went wrong")
    }
}
