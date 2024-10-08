package pt.isel.ls.mem

import org.testng.annotations.BeforeClass
import pt.isel.ls.management.mem.DataMem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UserMemTests {
    private val name = "Antonieta"
    private val email = "antonieta@gmail.com"
    private val password = "password"
    private val dB = DataMem()

    @BeforeClass
    fun clearUserMem() {
        dB.users = emptyArray()
    }

    @Test
    fun createUserMem() {
        val user = dB.UserData().createUser(name, email, password)
        assertTrue(user.first.isNotEmpty())
        assertEquals(1, user.second)
        dB.UserData().deleteUser(user.second)
    }

    @Test
    fun getUserMem() {
        val a = dB.UserData().createUser(name, email, password)
        val user = dB.UserData().getUser(1)
        assertEquals(a.second, user.id)
        assertEquals(name, user.name)
        assertEquals(email, user.email)
        assertEquals(a.first, user.token)
        dB.UserData().deleteUser(a.second)
    }

    @Test
    fun getUserWithInvalidIdMem() {
        val a = dB.UserData().createUser(name, email, password)
        assertFailsWith<NoSuchElementException> {
            dB.UserData().getUser(a.second + 1)
        }
        dB.UserData().deleteUser(a.second)
    }
}
