package pt.isel.ls.management.api

import pt.isel.ls.management.Board
import pt.isel.ls.management.BoardShowed
import pt.isel.ls.management.Card
import pt.isel.ls.management.Lists
import pt.isel.ls.management.User
import java.sql.Date

interface UserInterface {
    fun createUser(name: String, email: String, password: String): Pair<String, Int>
    fun login(email: String, password: String): String
    fun getUsers(skip: Int, limit: Int): List<User>
    fun getUser(id: Int): User
    fun getUserWithToken(token: String): Int?
    fun getUsersIds(): List<Int>
    fun getUsersEmails(): List<String>
    fun getUserBoards(userId: Int): List<Int>
    fun checkUserEmailAndPassword(): List<Pair<String, String>>
}

interface BoardInterface {
    fun createBoard(name: String, description: String): Int
    fun addUserToBoard(userId: Int, boardId: Int): Pair<Int, Int>
    fun getBoards(skip: Int, limit: Int): List<BoardShowed>
    fun getBoardsOfUser(userId: Int, skip: Int, limit: Int): List<BoardShowed>
    fun getBoard(bId: Int): Board
    fun getAllUserBoard(): List<Pair<Int, Int>>
    fun getUsersOfBoard(bId: Int, skip: Int, limit: Int): List<Pair<Int, Int>>
    fun getBoardsIds(): List<Int>
    fun getBoardLists(bId: Int): List<Int>
    fun getBoardNames(): List<String>
    fun searchBoardByName(boardName: String, skip: Int, limit: Int): List<BoardShowed>
    fun getBoardCards(boardId: Int): List<Int>

    fun getBoardArchivedCards(boardId: Int): List<Int>
}

interface ListInterface {
    fun createList(name: String, bId: Int): Int
    fun getLists(bId: Int, skip: Int, limit: Int): List<Lists>
    fun getList(lId: Int): Lists
    fun getListsIds(): List<Int>
    fun getListCards(listId: Int): List<Int>
    fun deleteList(listId: Int): Boolean
    fun getListsNamesOfBoards(boardId: Int): List<String>
}

interface CardInterface {
    fun createCard(name: String, description: String, currDate: Date, dueDate: Date?, lId: Int, bId: Int, uId: Int): Int
    fun getCards(lId: Int, skip: Int, limit: Int): List<Card>
    fun getCard(cardId: Int): Card
    fun moveCard(nlId: Int, olId: Int, cardId: Int, cix: Int): Card
    fun getCardIds(): List<Int>
    fun getCardNames(listId: Int): List<String>
    fun getCurrDate(): Date
    fun deleteCard(cardId: Int): Boolean
    fun getCardsArchived(boardId: Int, skip: Int, limit: Int): List<Card>
    fun getCardArchived(cardId: Int): Card
    fun archiveCard(cardId: Int, listId: Int): Int
    fun unarchiveCard(cardId: Int, listId: Int): Card
}
