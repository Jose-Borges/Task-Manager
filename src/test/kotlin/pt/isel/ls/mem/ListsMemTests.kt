package pt.isel.ls.mem

import pt.isel.ls.management.mem.DataMem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ListsMemTests {
    private val userName = "Antonieta"
    private val userEmail = "antonieta@gmail.com"
    private val password = "password"
    private val userId = 1
    private val boardName = "Board 1"
    private val description = "1st board"
    private val boardId = 1
    private val listName = "List 1"
    private val listName2 = "List 2"
    private val dB = DataMem()

    @BeforeTest
    fun setUpNecessities() {
        dB.UserData().createUser(userName, userEmail, password)
        dB.BoardData().createBoard(boardName, description)
    }

    @AfterTest
    fun deleteNecessities() {
        dB.UserData().deleteUser(userId)
        dB.BoardData().deleteBoard(boardId)
    }

    @Test
    fun createListTestMem() {
        val id = dB.ListData().createList(listName, boardId)
        val id2 = dB.ListData().createList(listName2, boardId)
        assertTrue(dB.lists.size == 2)
        assertEquals(1, id)
        assertEquals(2, id2)
        dB.ListData().deleteList(id)
        dB.ListData().deleteList(id2)
    }

    @Test
    fun getListTestMem() {
        val id = dB.ListData().createList(listName, boardId)
        val list = dB.ListData().getList(id)
        assertEquals(listName, list.name)
        assertEquals(boardId, list.boardId)
        dB.ListData().deleteList(id)
    }

    @Test
    fun getListWithInvalidIdMem() {
        val id = dB.ListData().createList(listName, boardId)
        assertFailsWith<NoSuchElementException> {
            dB.ListData().getList(id + 1)
        }
        dB.ListData().deleteList(id)
    }

    @Test
    fun getListsTestMem() {
        val id = dB.ListData().createList(listName, boardId)
        val id2 = dB.ListData().createList(listName2, boardId)
        val lists = dB.ListData().getLists(boardId)
        assertEquals(listName, lists[0].name)
        assertEquals(listName2, lists[1].name)
        dB.ListData().deleteList(id)
        dB.ListData().deleteList(id2)
    }

    @Test
    fun getListsWithWrongBoardIdMem() {
        val id = dB.ListData().createList(listName, boardId)
        val id2 = dB.ListData().createList(listName2, boardId)
        val bid = dB.BoardData().createBoard("test", "description")
        val list = dB.ListData().getLists(bid)
        assertTrue { list.isEmpty() }
        dB.ListData().deleteList(id)
        dB.ListData().deleteList(id2)
        dB.BoardData().deleteBoard(bid)
    }

    @Test
    fun deleteListTestMem() {
        val id = dB.ListData().createList(listName, boardId)
        val newList = dB.ListData().getList(id)
        val deletedList = dB.ListData().deleteList(id)
        assertEquals(newList.id, deletedList.id)
        assertEquals(newList.name, deletedList.name)
        assertEquals(boardId, deletedList.boardId)
        assertEquals(newList.cards, deletedList.cards)
    }
}
