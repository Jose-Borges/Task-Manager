package pt.isel.ls.management.mem

import pt.isel.ls.management.BoardMem
import pt.isel.ls.management.CardMem
import pt.isel.ls.management.ListsMem
import pt.isel.ls.management.User
import pt.isel.ls.management.UserMem

interface UserMemInterface {
    fun createUser(name: String, email: String, password: String): Pair<String, Int>
    fun login(email: String, password: String): String
    fun getUsers(): List<User>
    fun getUser(userId: Int): User
    fun getUserWithToken(token: String): Int?
    fun getUsersIds(): List<Int>
    fun getUsersEmails(): List<String>
    fun getUserBoards(userId: Int): List<Int>
    fun deleteUser(userId: Int): UserMem
}

interface BoardMemInterface {
    fun createBoard(name: String, description: String): Int
    fun addUserToBoard(userId: Int, boardId: Int): Pair<Int, Int>
    fun getBoards(): List<BoardMem>
    fun getBoardsOfUser(userId: Int): List<BoardMem>
    fun getBoard(boardId: Int): BoardMem
    fun getAllUserBoard(): List<Pair<Int, Int>>
    fun getUsersOfBoard(bId: Int): List<Pair<Int, Int>>
    fun getBoardsIds(): List<Int>
    fun getBoardLists(boardId: Int): List<Int>
    fun getBoardNames(): List<String>
    fun searchBoardByName(boardName: String): List<BoardMem>
    fun getBoardCards(boardId: Int): List<Int>

    fun getBoardArchivedCards(boardId: Int): List<Int>
    fun deleteBoard(boardId: Int): BoardMem
    fun deleteUserBoard(userId: Int, boardId: Int): Pair<Int, Int>
}

interface ListMemInterface {
    fun createList(name: String, boardId: Int): Int
    fun getLists(boardId: Int): List<ListsMem>
    fun getList(listId: Int): ListsMem
    fun getListsIds(): List<Int>
    fun getListCards(listId: Int): List<Int>
    fun deleteList(listId: Int): ListsMem
    fun getListsNamesOfBoards(boardId: Int): List<String>
}

interface CardMemInterface {
    fun createCard(
        name: String,
        description: String,
        currDate: String,
        dueDate: String?,
        listId: Int,
        boardId: Int,
        userId: Int
    ): Int
    fun getCards(listId: Int): List<CardMem>
    fun getCard(cardId: Int): CardMem
    fun moveCard(oldListId: Int, newListId: Int, cardId: Int, cix: Int): CardMem
    fun getCardIds(): List<Int>
    fun getCardNames(listId: Int): List<String>
    fun getCurrDate(): String
    fun deleteCard(cardId: Int): Boolean
    fun getCardsArchived(boardId: Int): List<CardMem>
    fun getCardArchived(cardId: Int): CardMem
    fun archiveCard(cardId: Int, listId: Int): Int
    fun unarchiveCard(cardId: Int, listId: Int): CardMem
}
