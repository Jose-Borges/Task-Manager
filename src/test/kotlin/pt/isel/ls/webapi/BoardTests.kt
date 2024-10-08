package pt.isel.ls.webapi

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import pt.isel.ls.management.BoardMem
import pt.isel.ls.management.mem.DataMem
import pt.isel.ls.services.ServicesMem
import pt.isel.ls.webApi.WebApiMem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardTests {
    private val userId = 1
    private val userName = "Antonieta"
    private val userEmail = "antonieta@gmail.com"
    private val password1 = "password1"
    private val userId2 = 2
    private val userName2 = "Esdrubal"
    private val userEmail2 = "esdrubal@gmail.com"
    private val password2 = "2"
    private val boardId = 1
    private val boardName = "Board 1"
    private val boardName2 = "Board 2"
    private val boardDescription = "1st board"
    private lateinit var userToken: String
    private lateinit var userToken2: String
    private lateinit var body: Map<String, String>
    private lateinit var request: Request
    private lateinit var response: Response
    private val dB = DataMem()
    private val s = ServicesMem(dB)
    private val w = WebApiMem(s)
    private val app = w.getRoutes()

    @BeforeTest
    fun setUpTest() {
        userToken = dB.UserData().createUser(userName, userEmail, password1).first
        userToken2 = dB.UserData().createUser(userName2, userEmail2, password2).first
    }

    @AfterTest
    fun finishTest() {
        dB.UserData().deleteUser(userId)
    }

    @Test
    fun createBoardTest() {
        body = mapOf("boardName" to boardName, "boardDescription" to boardDescription)
        request = Request(POST, "http://localhost:9000/boards")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
        response = app(request)
        assertEquals(Status.CREATED, response.status)
        assertEquals("application/json", response.header("content-type"))
        val board = Json.decodeFromString<Int>(response.bodyString())
        assertEquals(boardId, board)
        assertEquals(dB.userBoards.first { it.first == userId }.first, board)
        dB.BoardData().deleteBoard(board)
        dB.BoardData().deleteUserBoard(userId, board)
    }

    @Test
    fun createBoardWithInvalidParameterTest() {
        body = mapOf("boardDescription" to boardDescription)
        request = Request(POST, "http://localhost:9000/boards")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid Name", response.bodyString())
    }

    @Test
    fun addUserToBoardTest() {
        val board = dB.BoardData().createBoard(boardName, boardDescription)
        dB.BoardData().addUserToBoard(board, userId)
        body = mapOf("userId" to "$userId2")
        request = Request(PUT, "http://localhost:9000/boards/$boardId")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
        response = app(request)
        println(response.bodyString())
        println(dB.BoardData().getAllUserBoard())
        assertEquals(Status.OK, response.status)
        val uBoard = Json.decodeFromString<Pair<Int, Int>>(response.bodyString())
        assertEquals(userId2, uBoard.first)
        assertEquals(boardId, uBoard.second)
        dB.BoardData().deleteUserBoard(userId2, boardId)
        dB.BoardData().deleteUserBoard(userId, boardId)
        dB.BoardData().deleteBoard(boardId)
    }

    @Test
    fun addUserToBoardWithInvalidUserIdTest() {
        val board = dB.BoardData().createBoard(boardName, boardDescription)
        body = mapOf("userId" to "${-1}")
        request = Request(PUT, "http://localhost:9000/boards/$boardId")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no user with the given id", response.bodyString())
        dB.BoardData().deleteBoard(board)
    }

    @Test
    fun getBoardsTest() {
        val board = dB.BoardData().createBoard(boardName, boardDescription)
        val board2 = dB.BoardData().createBoard("board2", "boardDescription2")
        val board3 = dB.BoardData().createBoard("board3", "boardDescription3")
        val board4 = dB.BoardData().createBoard("board4", "boardDescription4")
        dB.BoardData().addUserToBoard(userId, board)
        dB.BoardData().addUserToBoard(userId, board3)
        request = Request(GET, "http://localhost:9000/boards")
            .query("userId", "$userId")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val boards = Json.decodeFromString<List<BoardMem>>(response.bodyString())
        assertTrue(boards.any { it.name == boardName })
        assertTrue(!boards.any { it.name == "board2" })
        assertTrue(boards.any { it.name == "board3" })
        assertTrue(!boards.any { it.name == "board4" })
        dB.BoardData().deleteUserBoard(userId, board)
        dB.BoardData().deleteUserBoard(userId, board3)
        dB.BoardData().deleteBoard(board)
        dB.BoardData().deleteBoard(board2)
        dB.BoardData().deleteBoard(board3)
        dB.BoardData().deleteBoard(board4)
    }

    @Test
    fun getBoardsWithoutUserIdTest() {
        val board = dB.BoardData().createBoard(boardName, boardDescription)
        val board2 = dB.BoardData().createBoard("board2", "boardDescription2")
        val board3 = dB.BoardData().createBoard("board3", "boardDescription3")
        val board4 = dB.BoardData().createBoard("board4", "boardDescription4")
        dB.BoardData().addUserToBoard(userId, board)
        dB.BoardData().addUserToBoard(userId, board3)
        request = Request(GET, "http://localhost:9000/boards")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val boards = Json.decodeFromString<List<BoardMem>>(response.bodyString())
        assertTrue(boards.any { it.name == boardName })
        assertTrue(boards.any { it.name == "board2" })
        assertTrue(boards.any { it.name == "board3" })
        assertTrue(boards.any { it.name == "board4" })
        dB.BoardData().deleteUserBoard(userId, board)
        dB.BoardData().deleteUserBoard(userId, board3)
        dB.BoardData().deleteBoard(board)
        dB.BoardData().deleteBoard(board2)
        dB.BoardData().deleteBoard(board3)
        dB.BoardData().deleteBoard(board4)
    }

    @Test
    fun getBoardTest() {
        val boardId = dB.BoardData().createBoard(boardName, boardDescription)
        request = Request(GET, "http://localhost:9000/boards/$boardId")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val board = Json.decodeFromString<BoardMem>(response.bodyString())
        assertEquals(boardName, board.name)
        assertEquals(boardDescription, board.description)
        dB.BoardData().deleteBoard(board.id)
    }

    @Test
    fun getBoardWithNonExistentIdTest() {
        val boardId = dB.BoardData().createBoard(boardName, boardDescription)
        request = Request(GET, "http://localhost:9000/boards/-1")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no board with the given boardId", response.bodyString())
        dB.BoardData().deleteBoard(boardId)
    }

    @Test
    fun getBoardUsers() {
        val board = dB.BoardData().createBoard(boardName, boardDescription)
        dB.BoardData().addUserToBoard(userId, board)
        dB.BoardData().addUserToBoard(userId2, board)
        request = Request(GET, "boards/$board/users")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val users = Json.decodeFromString<List<Pair<Int, Int>>>(response.bodyString())
        assertTrue { users.any { it.first == userId } }
        assertTrue { users.any { it.first == userId2 } }
        dB.BoardData().deleteUserBoard(userId, boardId)
        dB.BoardData().deleteUserBoard(userId2, boardId)
        dB.BoardData().deleteBoard(boardId)
    }

    @Test
    fun getBoardUsersWithNonExistentBoardId() {
        val board = dB.BoardData().createBoard(boardName, boardDescription)
        dB.BoardData().addUserToBoard(userId, board)
        dB.BoardData().addUserToBoard(userId2, board)
        request = Request(GET, "boards/-1/users")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no board with the given boardId", response.bodyString())
    }

    @Test
    fun searchBoardByNameTest() {
        val board1 = dB.BoardData().createBoard(boardName, boardDescription)
        request = Request(GET, "boards")
            .query("name", "1")
        response = app(request)
        val boards = Json.decodeFromString<List<BoardMem>>(response.bodyString())
        assertEquals(Status.OK, response.status)
        assertTrue(boards.contains(BoardMem(board1, boardName, boardDescription, mutableListOf())))
        dB.BoardData().deleteBoard(board1)
    }
}
