package pt.isel.ls.mem

import pt.isel.ls.management.BoardMem
import pt.isel.ls.management.mem.DataMem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardMemTests {
    private val userId = 1
    private val boardId = 1
    private val userName = "Antonieta"
    private val userEmail = "antonieta@gmail.com"
    private val password = "password"
    private val boardName = "Board 1"
    private val boardName2 = "Board 2"
    private val description = "1st board"
    private val dB = DataMem()

    @BeforeTest
    fun createUser() {
        dB.UserData().createUser(userName, userEmail, password)
    }

    @AfterTest
    fun deleteUser() {
        dB.UserData().deleteUser(userId)
    }

    @Test
    fun createBoardTestMem() {
        val boardId = dB.BoardData().createBoard(boardName, description)
        assertEquals(userId, boardId)
        assertEquals("Board 1", dB.boards.first().name)
        assertEquals("1st board", dB.boards.first().description)
        dB.BoardData().deleteBoard(boardId)
    }

    @Test
    fun addUserToBoardTestMem() {
        val x = dB.BoardData().createBoard(boardName, description)
        dB.BoardData().addUserToBoard(userId, boardId)
        assertTrue(dB.userBoards.contains(Pair(userId, boardId)))
        dB.BoardData().deleteUserBoard(userId, boardId)
        assertFalse(dB.userBoards.contains(Pair(userId, boardId)))
        dB.BoardData().deleteBoard(x)
    }

    @Test
    fun getBoardsOfUserTestMem() {
        val b1 = dB.BoardData().createBoard(boardName, description)
        val b2 = dB.BoardData().createBoard(boardName2, "2nd Board")
        dB.BoardData().addUserToBoard(1, b1)
        val boards = dB.BoardData().getBoardsOfUser(userId)
        assertTrue(boards.size == 1)
        assertEquals(boardName, boards[0].name)
        dB.BoardData().deleteBoard(b1)
        dB.BoardData().deleteBoard(b2)
    }

    @Test
    fun getBoardsTest() {
        val b1 = dB.BoardData().createBoard(boardName, description)
        val b2 = dB.BoardData().createBoard(boardName2, "2nd Board")
        dB.BoardData().addUserToBoard(1, b1)
        val boards = dB.BoardData().getBoards()
        assertEquals(boardName, boards[0].name)
        assertEquals(boardName2, boards[1].name)
        dB.BoardData().deleteBoard(b1)
        dB.BoardData().deleteBoard(b2)
    }

    @Test
    fun getBoardTestMem() {
        val x = dB.BoardData().createBoard(boardName, description)
        val board = dB.BoardData().getBoard(x)
        assertEquals(boardName, board.name)
        assertEquals(description, board.description)
        dB.BoardData().deleteBoard(x)
    }

    @Test
    fun searchBoardByNameTest() {
        val x = dB.BoardData().createBoard(boardName, description)
        val y = dB.BoardData().createBoard(boardName2, description)
        val boards = dB.BoardData().searchBoardByName("1")
        assertTrue(boards.contains(BoardMem(x, boardName, description, mutableListOf())))
        assertFalse(boards.contains(BoardMem(y, boardName2, description, mutableListOf())))
        dB.BoardData().deleteBoard(x)
        dB.BoardData().deleteBoard(y)
    }
}
