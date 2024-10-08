package pt.isel.ls.management.mem

import pt.isel.ls.management.BoardMem
import pt.isel.ls.management.CardMem
import pt.isel.ls.management.ListsMem
import pt.isel.ls.management.User
import pt.isel.ls.management.UserMem
import java.util.Calendar
import java.util.UUID

class DataMem {
    var users = emptyArray<UserMem>()
    var usersShowed = emptyArray<User>()
    var userBoards = emptyArray<Pair<Int, Int>>()
    var boards = emptyArray<BoardMem>()
    var lists = emptyArray<ListsMem>()
    var cards = emptyArray<CardMem>()

    inner class UserData : UserMemInterface {
        override fun createUser(name: String, email: String, password: String): Pair<String, Int> {
            val token = UUID.randomUUID().toString()
            val id = users.size + 1
            val user = UserMem(id, name, email, password, token)
            usersShowed += User(id, name, email, token)
            users += user
            return Pair(token, id)
        }

        override fun login(email: String, password: String): String {
            return users.first { it.email == email && it.password == password }.token
        }

        override fun getUsers(): List<User> {
            return usersShowed.asList()
        }

        override fun getUser(userId: Int): User {
            return usersShowed.first { it.id == userId }
        }

        override fun getUserWithToken(token: String): Int? {
            return users.firstOrNull { it.token == token }?.id
        }

        override fun getUsersIds(): List<Int> {
            return users.map { it.id }
        }

        override fun getUsersEmails(): List<String> {
            return users.map { it.email }
        }

        override fun getUserBoards(userId: Int): List<Int> {
            return userBoards.filter { it.first == userId }.map { it.second }
        }

        override fun deleteUser(userId: Int): UserMem {
            val u = users.first { it.id == userId }
            users = users.filter { it != u }.toTypedArray()
            return u
        }
    }

    inner class BoardData : BoardMemInterface {
        override fun createBoard(name: String, description: String): Int {
            val id = boards.size + 1
            val new = BoardMem(id, name, description, mutableListOf())
            boards += new
            return id
        }

        override fun addUserToBoard(userId: Int, boardId: Int): Pair<Int, Int> {
            val pair = Pair(userId, boardId)
            userBoards += pair
            return pair
        }

        override fun getBoards(): List<BoardMem> {
            return boards.asList()
        }

        override fun getBoardsOfUser(userId: Int): List<BoardMem> {
            val boardIds = userBoards.filter { it.first == userId }.map { it.second }
            return boards.filter { boardIds.contains(it.id) }
        }

        override fun getBoard(boardId: Int): BoardMem = boards.first { it.id == boardId }

        override fun searchBoardByName(boardName: String): List<BoardMem> {
            return boards.filter { it.name.contains(boardName) }
        }

        override fun getBoardCards(boardId: Int): List<Int> {
            return boards.filter { it.id == boardId }.map { it.id }
        }

        override fun getAllUserBoard(): List<Pair<Int, Int>> {
            return userBoards.asList()
        }

        override fun getUsersOfBoard(bId: Int): List<Pair<Int, Int>> {
            return userBoards.filter { it.second == bId }
        }

        override fun getBoardsIds(): List<Int> {
            return boards.map { it.id }
        }

        override fun getBoardLists(boardId: Int): List<Int> {
            return lists.filter { it.boardId == boardId }.map { it.id }
        }

        override fun getBoardArchivedCards(boardId: Int): List<Int> {
            return cards.filter { it.boardId == boardId && it.listId == null }.map { it.id }
        }

        override fun getBoardNames(): List<String> {
            return boards.map { it.name }
        }

        override fun deleteBoard(boardId: Int): BoardMem {
            val b = boards.first { it.id == boardId }
            boards = boards.filter { it != b }.toTypedArray()
            return b
        }

        override fun deleteUserBoard(userId: Int, boardId: Int): Pair<Int, Int> {
            val uB = userBoards.first { it.first == userId && it.second == boardId }
            userBoards = userBoards.filter { it.first != userId || it.second != boardId }.toTypedArray()
            return uB
        }
    }

    inner class ListData : ListMemInterface {
        override fun createList(name: String, boardId: Int): Int {
            val id = lists.size + 1
            boards.map { if (it.id == boardId) it.lists.add(name) }
            lists += ListsMem(id, name, boardId, mutableListOf())
            return id
        }

        override fun getLists(boardId: Int): List<ListsMem> {
            return lists.filter { it.boardId == boardId }
        }

        override fun getList(listId: Int): ListsMem = lists.first { it.id == listId }

        override fun getListsIds(): List<Int> {
            return lists.map { it.id }
        }

        override fun getListCards(listId: Int): List<Int> {
            return cards.filter { it.listId == listId }.map { it.id }
        }

        override fun deleteList(listId: Int): ListsMem {
            val l = lists.first { it.id == listId }
            lists = lists.filter { it.id != listId }.toTypedArray()
            boards.map { if (it.id == l.boardId) it.lists.remove(l.name) }
            return l
        }

        override fun getListsNamesOfBoards(boardId: Int): List<String> {
            return lists.filter { it.boardId == boardId }.map { it.name }
        }
    }

    inner class CardData : CardMemInterface {
        override fun createCard(
            name: String,
            description: String,
            currDate: String,
            dueDate: String?,
            listId: Int,
            boardId: Int,
            userId: Int
        ): Int {
            val listCards = cards.filter { it.listId == listId }
            val id: Int = if (cards.isEmpty()) {
                1
            } else {
                cards.last().id + 1
            }
            val idx = listCards.size + 1
            val newCard = CardMem(id, idx, name, description, currDate, dueDate, listId, boardId)
            cards += newCard
            lists.map { if (it.id == listId) it.cards += newCard.name }
            return id
        }

        override fun getCards(listId: Int): List<CardMem> {
            return cards.filter { it.listId == listId }
        }

        override fun getCard(cardId: Int): CardMem = cards.first { it.id == cardId }

        override fun moveCard(oldListId: Int, newListId: Int, cardId: Int, cix: Int): CardMem {
            val oldCard = cards.first { it.id == cardId && it.listId == oldListId }
            val list = lists.first { it.id == newListId }
            val newCard = CardMem(
                cardId,
                cix,
                oldCard.name,
                oldCard.description,
                oldCard.creationDate,
                oldCard.conclusionDate,
                newListId,
                list.boardId
            )
            cards.map { if (it.listId == newListId && it.idx >= cix) it.idx = it.idx + 1 }
            cards = (cards.filter { it != oldCard } + newCard).toTypedArray()
            cards.map { if (it.listId == oldListId && it.idx > cardId) it.idx = it.idx - 1 }
            lists.map {
                if (it.id == oldListId) it.cards.remove(oldCard.name)
                if (it.id == newListId) it.cards.add(newCard.name)
            }
            return newCard
        }

        override fun getCardIds(): List<Int> {
            return cards.map { it.id }
        }

        override fun getCardNames(listId: Int): List<String> {
            return cards.filter { it.listId == listId }.map { it.name }
        }

        override fun getCurrDate(): String {
            return Calendar.getInstance().time.toString()
        }

        override fun deleteCard(cardId: Int): Boolean {
            val c = cards.first { it.id == cardId }
            cards = cards.filter { it != c }.toTypedArray()
            lists.map { if (it.id == c.listId) it.cards.remove(c.name) }
            return cards.contains(c)
        }

        override fun getCardsArchived(boardId: Int): List<CardMem> {
            return cards.filter { it.boardId == boardId && it.listId == null }
        }

        override fun getCardArchived(cardId: Int): CardMem {
            return cards.first { it.id == cardId }
        }

        override fun archiveCard(cardId: Int, listId: Int): Int {
            val card = cards.first { it.id == cardId }
            card.listId = null
            return cardId
        }

        override fun unarchiveCard(cardId: Int, listId: Int): CardMem {
            val card = cards.first { it.id == cardId }
            card.listId = listId
            return card
        }
    }
}
