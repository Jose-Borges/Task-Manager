package pt.isel.ls.webApi

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import pt.isel.ls.http.logRequest
import pt.isel.ls.services.ServicesMem

class WebApiMem(services: ServicesMem) {
    private val s = services
    private var token: String = ""
    private val handler: (Request) -> String? = { request: Request ->
        val authHeader = request.header("Authorization") ?: ""
        val token = if (authHeader.startsWith("Bearer ")) {
            authHeader.substringAfter("Bearer ").trim()
        } else {
            null
        }
        token
    }

    private fun requestHandler(withAuth: Boolean, req: Request, function: (Request) -> Response): Response {
        return try {
            if (withAuth) token = handler(req) ?: throw UnauthorizedException("No token was given")
            function(req)
        } catch (e: Exception) {
            val rsp = getErrorMem(e)
            Response(rsp.status).body(rsp.message)
        }
    }

    @Serializable
    data class BodyFormat(
        val userId: Int? = null,
        val userName: String? = null,
        val email: String? = null,
        val password: String? = null,
        val boardId: Int? = null,
        val boardName: String? = null,
        val boardDescription: String? = null,
        val listId: Int? = null,
        val listName: String? = null,
        val cardName: String? = null,
        val cardDescription: String? = null,
        val creationDate: String? = null,
        val conclusionDate: String? = null
    )

    fun getRoutes() = routes(
        "users" bind Method.POST to { r -> requestHandler(false, r, ::createUser) },
        "users" bind Method.GET to { r -> requestHandler(false, r, ::getUsers) },
        "users/login" bind Method.GET to { r -> requestHandler(false, r, ::login) },
        "users/{id}" bind Method.GET to { r -> requestHandler(false, r, ::getUser) },
        "boards" bind Method.POST to { r -> requestHandler(true, r, ::createBoard) },
        "boards" bind Method.GET to { r -> requestHandler(false, r, ::getBoards) },
        "boards/{bId}" bind Method.GET to { r -> requestHandler(false, r, ::getBoard) },
        "boards/{bId}" bind Method.PUT to { r -> requestHandler(true, r, ::addUserToBoard) },
        "boards/{bId}/users" bind Method.GET to { r -> requestHandler(false, r, ::getUsersOfBoard) },
        "lists" bind Method.POST to { r -> requestHandler(true, r, ::createList) },
        "lists" bind Method.GET to { r -> requestHandler(false, r, ::getLists) },
        "lists/{lId}" bind Method.GET to { r -> requestHandler(false, r, ::getList) },
        "lists/{lId}/{bId}" bind Method.DELETE to { r -> requestHandler(true, r, ::deleteList) },
        "cards" bind Method.POST to { r -> requestHandler(true, r, ::createCard) },
        "cards" bind Method.GET to { r -> requestHandler(false, r, ::getCards) },
        "cards/archived/{bId}" bind Method.GET to { r -> requestHandler(false, r, ::getCardsArchived) },
        "cards/archived/{cId}/{bId}" bind Method.GET to { r -> requestHandler(false, r, ::getCardArchived) },
        "cards/archived/{cId}/{lId}/{bId}" bind Method.PUT to { r -> requestHandler(true, r, ::archiveCard) },
        "cards/unarchive/{cId}/{lId}/{bId}" bind Method.PUT to { r -> requestHandler(true, r, ::unarchiveCard) },
        "cards/{cId}" bind Method.GET to { r -> requestHandler(false, r, ::getCard) },
        "cards/{cId}" bind Method.PUT to { r -> requestHandler(true, r, ::moveCard) },
        "cards/{cId}" bind Method.DELETE to { r -> requestHandler(true, r, ::deleteCard) },
        singlePageApp(ResourceLoader.Directory("static-content"))
    )

    private fun getUsers(request: Request): Response {
        logRequest(request)
        val limit = request.query("limit")
        val skip = request.query("skip")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getUsers(skip, limit)))
    }

    private fun getUser(request: Request): Response {
        logRequest(request)
        val num = request.path("id")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getUser(num)))
    }

    private fun login(request: Request): Response {
        logRequest(request)
        val email = request.query("email")
        val password = request.query("password")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.login(email, password)))
    }

    private fun createUser(request: Request): Response {
        logRequest(request)
        val body = Json.decodeFromString<WebApi.BodyFormat>(request.bodyString())
        val name = body.userName
        val email = body.email
        val password = body.password
        return Response(Status.CREATED)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.createUser(name, email, password)))
    }

    private fun createBoard(request: Request): Response {
        logRequest(request)
        val body = Json.decodeFromString<WebApi.BodyFormat>(request.bodyString())
        val name = body.boardName
        val desc = body.boardDescription
        val userId = s.getUserWithToken(token)
        return Response(Status.CREATED)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.createBoard(userId, name, desc)))
    }

    private fun addUserToBoard(request: Request): Response {
        logRequest(request)
        val body = Json.decodeFromString<WebApi.BodyFormat>(request.bodyString())
        val board = request.path("bId")?.toInt()
        val user = s.getUserWithToken(token)
        val userId = body.userId
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.addUserToBoard(user, userId, board)))
    }

    private fun getBoards(request: Request): Response {
        logRequest(request)
        val limit = request.query("limit")
        val skip = request.query("skip")
        val user = request.query("userId")
        val name = request.query("boardName")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getBoards(user, name, skip, limit)))
    }

    private fun getBoard(request: Request): Response {
        logRequest(request)
        val board = request.path("bId")?.toInt()
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getBoard(board)))
    }

    private fun getUsersOfBoard(request: Request): Response {
        logRequest(request)
        val board = request.path("bId")?.toInt()
        val limit = request.query("limit")
        val skip = request.query("skip")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getUsersOfBoard(board, skip, limit)))
    }

    private fun createList(request: Request): Response {
        logRequest(request)
        val body = Json.decodeFromString<WebApi.BodyFormat>(request.bodyString())
        val name = body.listName
        val board = request.query("boardId")
        val user = s.getUserWithToken(token)
        return Response(Status.CREATED)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.createList(name, board, user)))
    }

    private fun getLists(request: Request): Response {
        logRequest(request)
        val board = request.query("boardId")
        val limit = request.query("limit")
        val skip = request.query("skip")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getLists(board, skip, limit)))
    }

    private fun getList(request: Request): Response {
        logRequest(request)
        val list = request.path("lId")
        val board = request.query("boardId")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getList(list, board)))
    }

    private fun deleteList(request: Request): Response {
        logRequest(request)
        val list = request.path("lId")
        val board = request.path("bId")
        val user = s.getUserWithToken(token)
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.deleteList(list, board, user)))
    }

    private fun createCard(request: Request): Response {
        logRequest(request)
        val body = Json.decodeFromString<WebApi.BodyFormat>(request.bodyString())
        val name = body.cardName
        val desc = body.cardDescription
        val dueDate = body.conclusionDate
        val board = request.query("boardId")
        val list = request.query("listId")
        val user = s.getUserWithToken(token)
        return Response(Status.CREATED)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.createCard(name, desc, dueDate, list, board, user)))
    }

    private fun getCardsArchived(request: Request): Response {
        logRequest(request)
        val board = request.path("bId")
        val limit = request.query("limit")
        val skip = request.query("skip")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getCardsArchived(board, skip, limit)))
    }

    private fun getCards(request: Request): Response {
        logRequest(request)
        val limit = request.query("limit")
        val skip = request.query("skip")
        val list = request.query("listId")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getCards(list, skip, limit)))
    }

    private fun getCard(request: Request): Response {
        logRequest(request)
        val card = request.path("cId")
        val list = request.query("listId")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getCard(card, list)))
    }

    private fun getCardArchived(request: Request): Response {
        logRequest(request)
        val card = request.path("cId")
        val board = request.path("bId")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.getCardArchived(card, board)))
    }

    private fun moveCard(request: Request): Response {
        logRequest(request)
        val card = request.path("cId")
        val newList = request.query("newList")
        val oldList = request.query("oldList")
        val board = request.query("boardId")
        val newCardIdx = request.query("cix")
        val user = s.getUserWithToken(token)
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.moveCard(oldList, newList, card, newCardIdx, board, user)))
    }

    private fun archiveCard(request: Request): Response {
        logRequest(request)
        val user = s.getUserWithToken(token)
        val board = request.path("bId")
        val list = request.path("lId")
        val card = request.path("cId")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.archiveCard(card, list, board, user)))
    }

    private fun unarchiveCard(request: Request): Response {
        logRequest(request)
        val user = s.getUserWithToken(token)
        val board = request.path("bId")
        val list = request.path("lId")
        val card = request.path("cId")
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.unarchiveCard(card, list, board, user)))
    }

    private fun deleteCard(request: Request): Response {
        logRequest(request)
        val card = request.path("cId")
        val list = request.query("listId")
        val board = request.query("boardId")
        val user = s.getUserWithToken(token)
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(s.deleteCard(card, list, board, user)))
    }
}
