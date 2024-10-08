package pt.isel.ls.webapi

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import pt.isel.ls.management.User
import pt.isel.ls.management.mem.DataMem
import pt.isel.ls.services.ServicesMem
import pt.isel.ls.webApi.WebApiMem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserTests {
    private val userName = "Antonieta"
    private val userEmail = "antonieta@gmail.com"
    private val password1 = "password1"
    private val userName2 = "Esdrubal"
    private val userEmail2 = "Esdrubal@gmail.com"
    private val password2 = "password2"
    private lateinit var body: Map<String, String?>
    private lateinit var request: Request
    private lateinit var response: Response
    private val dB = DataMem()
    private val s = ServicesMem(dB)
    private val w = WebApiMem(s)
    private val app = w.getRoutes()

    @Test
    fun createUserTest() {
        body = mapOf("userName" to userName, "email" to userEmail, "password" to password1)
        request = Request(Method.POST, "http://localhost:9000/users")
            .body(Json.encodeToString(body))
        response = app(request)
        assertEquals(Status.CREATED, response.status)
        assertEquals("application/json", response.header("content-type"))
        val user = Json.decodeFromString<Pair<String, Int>>(response.bodyString())
        assertTrue(user.first.isNotEmpty())
        assertEquals(1, user.second)
        dB.UserData().deleteUser(user.second)
    }

    @Test
    fun createUserWithInvalidParameterTest() {
        body = mapOf("userName" to userName, "email" to null, "password" to password1)
        request = Request(Method.POST, "http://localhost:9000/users")
            .body(Json.encodeToString(body))
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid email", response.bodyString())
    }

    @Test
    fun createUserWithRepeatedEmail() {
        val user = dB.UserData().createUser(userName, userEmail, password1)
        body = mapOf("userName" to userName, "email" to userEmail, "password" to password1)
        request = Request(Method.POST, "http://localhost:9000/users")
            .body(Json.encodeToString(body))
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("There's already an user with the given email", response.bodyString())
        dB.UserData().deleteUser(user.second)
    }

    @Test
    fun getUsersTest() {
        val u1 = dB.UserData().createUser(userName, userEmail, password1)
        val u2 = dB.UserData().createUser(userName2, userEmail2, password2)
        val users = dB.UserData().getUsers()
        assertTrue { users.any { it.name == userName } }
        assertTrue { users.any { it.name == userName2 } }
        dB.UserData().deleteUser(u1.second)
        dB.UserData().deleteUser(u2.second)
    }

    @Test
    fun getUserTest() {
        val u = dB.UserData().createUser(userName, userEmail, password1)
        request = Request(Method.GET, "http://localhost:9000/users/${u.second}")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val user = Json.decodeFromString<User>(response.bodyString())
        assertEquals(u.second, user.id)
        assertEquals(userName, user.name)
        assertEquals(userEmail, user.email)
        assertEquals(u.first, user.token)
        dB.UserData().deleteUser(u.second)
    }

    @Test
    fun getUserWithSkipAndLimitTest() {
        val u = dB.UserData().createUser("Test", "userEmail", "password")
        val u2 = dB.UserData().createUser(userName, userEmail, password1)
        request = Request(Method.GET, "http://localhost:9000/users")
            .query("skip", "1")
            .query("limit", "1")
        response = app(request)
        assertEquals(response.status, Status.OK)
        val users = Json.decodeFromString<List<User>>(response.bodyString())
        assertEquals(1, users.size)
        assertEquals(u2.second, users[0].id)
        assertEquals(userName, users[0].name)
        assertEquals(userEmail, users[0].email)
        assertEquals(u2.first, users[0].token)
        dB.UserData().deleteUser(u2.second)
        dB.UserData().deleteUser(u.second)
    }

    @Test
    fun getUserWithInvalidIdTest() {
        val u = dB.UserData().createUser(userName, userEmail, password1)
        request = Request(Method.GET, "http://localhost:9000/users/${u.second + 1}")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no user with the given id", response.bodyString())
        dB.UserData().deleteUser(u.second)
    }
}
