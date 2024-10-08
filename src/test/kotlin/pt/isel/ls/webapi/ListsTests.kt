package pt.isel.ls.webapi

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import pt.isel.ls.management.ListsMem
import pt.isel.ls.management.mem.DataMem
import pt.isel.ls.services.ServicesMem
import pt.isel.ls.webApi.WebApiMem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListsTests {
    private val userName = "Antonieta"
    private val userEmail = "antonieta@gmail.com"
    private val password1 = "password1"
    private val userId = 1
    private val userName2 = "Esdrubal"
    private val userEmail2 = "Esdrubal@gmail.com"
    private val password2 = "password2"
    private val userId2 = 2
    private val boardName = "Board 1"
    private val boardDescription = "1st board"
    private val boardName2 = "Board 2"
    private val boardDescription2 = "2nd board"
    private val boardId = 1
    private val listId = 1
    private val listName = "List 1"
    private val listName2 = "List 2"
    private lateinit var userToken: String
    private lateinit var body: Map<String, String?>
    private lateinit var request: Request
    private lateinit var response: Response
    private val dB = DataMem()
    private val s = ServicesMem(dB)
    private val w = WebApiMem(s)
    private val app = w.getRoutes()

    @BeforeTest
    fun setUpTest() {
        userToken = dB.UserData().createUser(userName, userEmail, password1).first
        dB.BoardData().createBoard(boardName, boardDescription)
        dB.BoardData().addUserToBoard(userId, boardId)
    }

    @AfterTest
    fun finishTest() {
        dB.UserData().deleteUser(userId)
        dB.BoardData().deleteUserBoard(userId, boardId)
        dB.BoardData().deleteBoard(boardId)
    }

    @Test
    fun createListTest() {
        body = mapOf("listName" to listName)
        request = Request(Method.POST, "http://localhost:9000/lists")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.CREATED, response.status)
        assertEquals(response.header("content-type"), "application/json")
        val list = Json.decodeFromString<Int>(response.bodyString())
        assertEquals(listId, list)
        val list1 = dB.lists.first { it.id == listId }
        assertEquals(boardId, list1.boardId)
        assertEquals(listName, list1.name)
    }

    @Test
    fun createListInvalidBodyParametersTest() {
        body = mapOf("listName" to null)
        request = Request(Method.POST, "http://localhost:9000/lists")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid Name", response.bodyString())
    }

    @Test
    fun createListInvalidQueryParametersTest() {
        body = mapOf("listName" to listName)
        request = Request(Method.POST, "http://localhost:9000/lists")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
            .query("boardId", null)
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid BoardId", response.bodyString())
    }

    @Test
    fun createListWithoutAccessToTheBoardTest() {
        val userToken2 = dB.UserData().createUser(userName2, userEmail2, password2).first
        body = mapOf("listName" to listName)
        request = Request(Method.POST, "http://localhost:9000/lists")
            .header("Authorization", "Bearer $userToken2")
            .body(Json.encodeToString(body))
            .query("boardId", "$boardId")
        response = app(request)
        println(response.bodyString())
        assertEquals(Status.FORBIDDEN, response.status)
        assertEquals("User $userId2 doesn't have access to that Board", response.bodyString())
        dB.UserData().deleteUser(userId2)
    }

    @Test
    fun createListWithNonExistentBoardIdTest() {
        body = mapOf("listName" to listName)
        request = Request(Method.POST, "http://localhost:9000/lists")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
            .query("boardId", "-1")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no board with the given boardId", response.bodyString())
    }

    @Test
    fun getListsTest() {
        val board2 = dB.BoardData().createBoard(boardName2, boardDescription2)
        dB.BoardData().addUserToBoard(userId, board2)
        val list1 = dB.ListData().createList(listName, boardId)
        val list2 = dB.ListData().createList(listName2, board2)
        request = Request(Method.GET, "http://localhost:9000/lists")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val lists = Json.decodeFromString<List<ListsMem>>(response.bodyString())
        assertTrue(lists.any { it.name == listName })
        assertTrue(!lists.any { it.name == listName2 })
        dB.ListData().deleteList(list2)
        dB.ListData().deleteList(list1)
        dB.BoardData().deleteUserBoard(userId, board2)
        dB.BoardData().deleteBoard(board2)
    }

    @Test
    fun getListsWithSkipAndLimitTest() {
        val list1 = dB.ListData().createList(listName, boardId)
        val list2 = dB.ListData().createList(listName2, boardId)
        request = Request(Method.GET, "http://localhost:9000/lists")
            .query("boardId", "$boardId")
            .query("skip", "1")
            .query("limit", "1")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val lists = Json.decodeFromString<List<ListsMem>>(response.bodyString())
        assertEquals(1, lists.size)
        assertTrue(lists.any { it.name == listName2 })
        dB.ListData().deleteList(list2)
        dB.ListData().deleteList(list1)
    }

    @Test
    fun getListsInvalidParametersTest() {
        val list1 = dB.ListData().createList(listName, boardId)
        val list2 = dB.ListData().createList(listName2, boardId)
        request = Request(Method.GET, "http://localhost:9000/lists")
            .query("boardId", null)
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        dB.ListData().deleteList(list2)
        dB.ListData().deleteList(list1)
    }

    @Test
    fun getListsNonExistentBoardIdTest() {
        val list1 = dB.ListData().createList(listName, boardId)
        val list2 = dB.ListData().createList(listName2, boardId)
        request = Request(Method.GET, "http://localhost:9000/lists")
            .query("boardId", "-1")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no board with the given boardId", response.bodyString())
        dB.ListData().deleteList(list2)
        dB.ListData().deleteList(list1)
    }

    @Test
    fun getListTest() {
        val list1 = dB.ListData().createList(listName, boardId)
        request = Request(Method.GET, "http://localhost:9000/lists/$list1")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val list = Json.decodeFromString<ListsMem>(response.bodyString())
        assertEquals(boardId, list.boardId)
        assertEquals(listName, list.name)
        dB.ListData().deleteList(list1)
    }

    @Test
    fun getListInvalidBodyParametersTest() {
        val list = dB.ListData().createList(listName, boardId)
        request = Request(Method.GET, "http://localhost:9000/lists/$list")
            .header("Authorization", "Bearer $userToken")
            .query("boardId", null)
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid BoardId", response.bodyString())
        dB.ListData().deleteList(list)
    }

    @Test
    fun getListWithNonExistentListIdTest() {
        val list = dB.ListData().createList(listName, boardId)
        request = Request(Method.GET, "http://localhost:9000/lists/-1")
            .header("Authorization", "Bearer $userToken")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no list with the given listId", response.bodyString())
        dB.ListData().deleteList(list)
    }

    @Test
    fun getListWrongListIdTest() {
        val boardId2 = dB.BoardData().createBoard(boardName2, boardDescription2)
        val list = dB.ListData().createList(listName, boardId2)
        request = Request(Method.GET, "http://localhost:9000/lists/$list")
            .header("Authorization", "Bearer $userToken")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("Board $boardId doesn't have a list $list", response.bodyString())
        dB.ListData().deleteList(list)
        dB.BoardData().deleteBoard(boardId2)
    }

    @Test
    fun deleteListTest() {
        val list = dB.ListData().createList(listName, boardId)
        request = Request(Method.DELETE, "http://localhost:9000/lists/$list/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val lists = dB.ListData().getLists(boardId)
        assertFalse(lists.any { it.id == list })
    }

    @Test
    fun deleteListTestNonExistentBoardId() {
        val list = dB.ListData().createList(listName, boardId)
        request = Request(Method.DELETE, "http://localhost:9000/lists/$list/-1")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no board with the given boardId", response.bodyString())
        dB.ListData().deleteList(list)
    }

    @Test
    fun deleteListTestUnauthorizedUser() {
        val userToken2 = dB.UserData().createUser(userName2, userEmail2, password2).first
        val list = dB.ListData().createList(listName, boardId)
        request = Request(Method.DELETE, "http://localhost:9000/lists/$list/$boardId")
            .header("Authorization", "Bearer $userToken2")
        response = app(request)
        assertEquals(Status.FORBIDDEN, response.status)
        assertEquals("User $userId2 doesn't have access to that Board", response.bodyString())
        dB.UserData().deleteUser(userId2)
        dB.ListData().deleteList(list)
    }

    @Test
    fun deleteListTestNonExistentListId() {
        request = Request(Method.DELETE, "http://localhost:9000/lists/-1/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no list with the given listId", response.bodyString())
    }
}
