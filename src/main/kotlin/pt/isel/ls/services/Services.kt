package pt.isel.ls.services

import pt.isel.ls.management.Board
import pt.isel.ls.management.BoardShowed
import pt.isel.ls.management.Card
import pt.isel.ls.management.Lists
import pt.isel.ls.management.User
import pt.isel.ls.management.api.DataPostgres
import pt.isel.ls.utils.max
import pt.isel.ls.webApi.BadRequestException
import pt.isel.ls.webApi.ForbiddenRequestException
import pt.isel.ls.webApi.NotFoundException
import pt.isel.ls.webApi.UnauthorizedException
import java.sql.Date

class Services(dataBase: DataPostgres) {
    private val dB = dataBase
    fun createUser(name: String?, email: String?, password: String?): Pair<String, Int> {
        val params = listOf(Pair("username", name), Pair("email", email), Pair("password", password))
        val p = paramValidation(params)
        val nm = p.first { it.first == "username" }.second
        val eml = p.first { it.first == "email" }.second
        val pass = p.first { it.first == "password" }.second
        if (dB.UserData().getUsersEmails().contains(eml)) {
            throw BadRequestException("There's already an user with the given email")
        }
        return dB.UserData().createUser(nm, eml, pass)
    }

    fun login(email: String?, password: String?): String {
        val params = listOf(Pair("email", email), Pair("password", password))
        val p = paramValidation(params)
        val uEmail = p.first { it.first == "email" }.second
        val pass = p.first { it.first == "password" }.second
        checkLoginCombination(uEmail, pass)
        return dB.UserData().login(uEmail, pass)
    }

    fun getUsers(skip: String?, limit: String?): List<User> {
        val p = getSkipAndLimit(skip, limit)
        return dB.UserData().getUsers(p.first, p.second)
    }

    fun getUser(id: String?): User {
        if (id == null) throw BadRequestException("Invalid id")
        val uId = id.toInt()
        checkUserExists(uId)
        return dB.UserData().getUser(uId)
    }

    fun getUserWithToken(token: String): Int {
        if (token == "undefined") throw UnauthorizedException("User must be logged in")
        return dB.UserData().getUserWithToken(token)
            ?: throw NotFoundException("There's no user with the given token")
    }

    fun createBoard(user: Int, name: String?, description: String?): Int {
        val params = listOf(Pair("name", name), Pair("description", description))
        val p = paramValidation(params)
        val nm = p.first { it.first == "name" }.second
        val desc = p.first { it.first == "description" }.second
        val board = dB.BoardData().createBoard(nm, desc)
        dB.BoardData().addUserToBoard(user, board)
        return board
    }

    fun addUserToBoard(user: Int, userId: Int?, boardId: Int?): Pair<Int, Int> {
        val params = listOf(Pair("userId", userId), Pair("boardId", boardId))
        val p = paramValidation(params)
        val uId = p.first { it.first == "userId" }.second.toInt()
        val bId = p.first { it.first == "boardId" }.second.toInt()
        checkUserExists(uId)
        checkBoardExists(bId)
        checkUserHasBoard(user, bId)
        if (dB.BoardData().getAllUserBoard().contains(Pair(userId, boardId))) {
            throw BadRequestException("The user is already in this board")
        }
        return dB.BoardData().addUserToBoard(uId, bId)
    }

    fun getBoards(userId: String?, boardName: String?, skip: String?, limit: String?): List<BoardShowed> {
        val id = userId?.toInt()
        val p = getSkipAndLimit(skip, limit)
        if (boardName != null) return dB.BoardData().searchBoardByName(boardName, p.first, p.second)
        return if (id != null) {
            dB.BoardData().getBoardsOfUser(id, p.first, p.second)
        } else {
            dB.BoardData().getBoards(p.first, p.second)
        }
    }

    fun getBoard(boardId: Int?): Board {
        if (boardId == null) throw BadRequestException("Invalid boardId")
        checkBoardExists(boardId)
        return dB.BoardData().getBoard(boardId)
    }

    fun getUsersOfBoard(boardId: Int?, skip: String?, limit: String?): List<Pair<Int, Int>> {
        if (boardId == null) throw BadRequestException("Invalid boardId")
        checkBoardExists(boardId)
        val p = getSkipAndLimit(skip, limit)
        return dB.BoardData().getUsersOfBoard(boardId, p.first, p.second)
    }

    fun createList(name: String?, boardId: String?, userId: Int): Int {
        val params = listOf(Pair("name", name), Pair("boardId", boardId))
        val p = paramValidation(params)
        val board = p.first { it.first == "boardId" }.second.toInt()
        checkBoardExists(board)
        val nm = p.first { it.first == "name" }.second
        checkUserHasBoard(userId, board)
        checkListNameUnique(nm, board)
        return dB.ListData().createList(nm, board)
    }

    fun getLists(boardId: String?, skip: String?, limit: String?): List<Lists> {
        if (boardId == null) throw BadRequestException("Invalid boardId")
        val bId = boardId.toInt()
        checkBoardExists(bId)
        val p = getSkipAndLimit(skip, limit)
        return dB.ListData().getLists(bId, p.first, p.second)
    }

    fun getList(listId: String?, boardId: String?): Lists {
        val params = listOf(
            Pair("ListId", listId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val bId = p.first { it.first == "BoardId" }.second.toInt()
        val lId = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(lId)
        checkListInBoard(bId, lId)
        return dB.ListData().getList(lId)
    }

    fun deleteList(listId: String?, boardId: String?, userId: Int): Boolean {
        val params = listOf(
            Pair("ListId", listId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val bid = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(bid)
        checkUserHasBoard(userId, bid)
        val lid = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(lid)
        checkListInBoard(bid, lid)
        return dB.ListData().deleteList(lid)
    }

    fun createCard(
        name: String?,
        description: String?,
        dueDate: String?,
        lId: String?,
        bId: String?,
        userId: Int
    ): Int {
        val params = listOf(
            Pair("Name", name),
            Pair("Description", description),
            Pair("ListId", lId),
            Pair("BoardId", bId)
        )
        val p = paramValidation(params)
        val n = p.first { it.first == "Name" }.second
        val desc = p.first { it.first == "Description" }.second
        val boardId = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(boardId)
        checkUserHasBoard(userId, boardId)
        val listId = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(listId)
        checkListInBoard(boardId, listId)
        checkCardNameUnique(n, listId)
        val conDate = if (dueDate != null) Date.valueOf(dueDate) else null
        val currDate = dB.CardData().getCurrDate()
        if (conDate != null) {
            if (currDate >= conDate) {
                throw BadRequestException("It's impossible to conclude something before starting it")
            }
        }
        return dB.CardData().createCard(n, desc, currDate, conDate, listId, boardId, userId)
    }

    fun getCardsArchived(boardId: String?, skip: String?, limit: String?): List<Card> {
        if (boardId == null) throw BadRequestException("Invalid BoardId")
        val bId = boardId.toInt()
        val p = getSkipAndLimit(skip, limit)
        return dB.CardData().getCardsArchived(bId, p.first, p.second)
    }

    fun getCardArchived(cardId: String?, boardId: String?): Card {
        val params = listOf(
            Pair("CardId", cardId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val board = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(board)
        val card = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(card)
        checkCardInBoard(board, card)
        checkCardArchived(board, card)
        return dB.CardData().getCardArchived(card)
    }

    fun archiveCard(cardId: String?, listId: String?, boardId: String?, userId: Int): Int {
        val params = listOf(
            Pair("CardId", cardId),
            Pair("ListId", listId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val bId = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(bId)
        checkUserHasBoard(userId, bId)
        val lId = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(lId)
        checkListInBoard(bId, lId)
        val cId = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(cId)
        checkCardInList(lId, cId)
        return dB.CardData().archiveCard(cId, lId)
    }

    fun unarchiveCard(cardId: String?, listId: String?, boardId: String?, userId: Int): Card {
        val params = listOf(
            Pair("CardId", cardId),
            Pair("ListId", listId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val board = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(board)
        checkUserHasBoard(userId, board)
        val list = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(list)
        checkListInBoard(board, list)
        val card = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(card)
        checkCardInBoard(board, card)
        checkCardArchived(board, card)
        return dB.CardData().unarchiveCard(card, list)
    }

    fun getCards(listId: String?, skip: String?, limit: String?): List<Card> {
        if (listId == null) throw BadRequestException("Invalid ListId")
        val lId = listId.toInt()
        checkListExists(lId)
        val p = getSkipAndLimit(skip, limit)
        return dB.CardData().getCards(lId, p.first, p.second)
    }

    fun getCard(cardId: String?, listId: String?): Card {
        val params = listOf(
            Pair("CardId", cardId),
            Pair("ListId", listId)
        )
        val p = paramValidation(params)
        val lId = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(lId)
        val cid = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(cid)
        checkCardInList(lId, cid)
        return dB.CardData().getCard(cid)
    }

    fun moveCard(
        newListId: String?,
        cardId: String?,
        newCardIdx: String?,
        oldListId: String?,
        boardId: String?,
        userId: Int
    ): Card {
        val params = listOf(
            Pair("NewListId", newListId),
            Pair("CardId", cardId),
            Pair("NewCardIdx", newCardIdx),
            Pair("OldListId", oldListId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val bId = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(bId)
        checkUserHasBoard(userId, bId)
        val newList = p.first { it.first == "NewListId" }.second.toInt()
        val oldList = p.first { it.first == "OldListId" }.second.toInt()
        checkListExists(newList)
        checkListInBoard(bId, newList)
        if (newList == oldList) throw BadRequestException("Card is already in list $newList")
        checkListExists(oldList)
        checkListInBoard(bId, oldList)
        val cid = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(cid)
        checkCardInList(oldList, cid)
        checkCardNameUnique(dB.CardData().getCard(cid).name, newList)
        val cix = p.first { it.first == "NewCardIdx" }.second.toInt()
        return dB.CardData().moveCard(newList, oldList, cid, cix)
    }

    fun deleteCard(cardId: String?, listId: String?, boardId: String?, userId: Int): Boolean {
        val params = listOf(
            Pair("CardId", cardId),
            Pair("ListId", listId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val bid = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(bid)
        checkUserHasBoard(userId, bid)
        val lid = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(lid)
        checkListInBoard(bid, lid)
        val cid = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(cid)
        checkCardInList(lid, cid)
        return dB.CardData().deleteCard(cid)
    }

    private fun getSkipAndLimit(skip: String?, limit: String?): Pair<Int, Int> {
        val s = max(skip?.toInt() ?: 0, 0)
        val l = max(limit?.toInt() ?: 100, 0)
        return Pair(s, l)
    }

    private fun paramValidation(pairs: List<Pair<String, Any?>>): List<Pair<String, String>> {
        val l = mutableListOf<Pair<String, String>>()
        pairs.forEach {
            if (it.second == null) throw BadRequestException("Invalid ${it.first}")
            l.add(Pair(it.first, it.second.toString()))
        }
        return l
    }

    private fun checkListInBoard(boardId: Int, listId: Int) {
        if (!dB.BoardData().getBoardLists(boardId).contains(listId)) {
            throw NotFoundException("Board $boardId doesn't have a list $listId")
        }
    }

    private fun checkCardExists(cardId: Int) {
        if (!dB.CardData().getCardIds().contains(cardId)) {
            throw NotFoundException("There's no card with the given cardId")
        }
    }

    private fun checkCardInList(listId: Int, cardId: Int) {
        if (!dB.ListData().getListCards(listId).contains(cardId)) {
            throw NotFoundException("List $listId doesn't have card $cardId")
        }
    }

    private fun checkListExists(listId: Int) {
        if (!dB.ListData().getListsIds().contains(listId)) {
            throw NotFoundException("There's no list with the given listId")
        }
    }

    private fun checkBoardExists(boardId: Int) {
        if (!dB.BoardData().getBoardsIds().contains(boardId)) {
            throw NotFoundException("There's no board with the given boardId")
        }
    }

    private fun checkUserExists(userId: Int) {
        if (!dB.UserData().getUsersIds().contains(userId)) {
            throw NotFoundException("There's no user with the given id")
        }
    }

    private fun checkUserHasBoard(userId: Int, boardId: Int) {
        if (!dB.UserData().getUserBoards(userId).contains(boardId)) {
            throw ForbiddenRequestException("User doesn't have access to that Board")
        }
    }

    private fun checkListNameUnique(name: String, boardId: Int) {
        if (dB.ListData().getListsNamesOfBoards(boardId).contains(name)) {
            throw BadRequestException("There's already a list with that name")
        }
    }

    private fun checkCardNameUnique(name: String, listId: Int) {
        if (dB.CardData().getCardNames(listId).contains(name)) {
            throw BadRequestException("There's already a card in that list with that name")
        }
    }

    private fun checkCardInBoard(board: Int, card: Int) {
        if (!dB.BoardData().getBoardCards(board).contains(card)) {
            throw NotFoundException("Board $board doesn't have card $card")
        }
    }

    private fun checkCardArchived(board: Int, card: Int) {
        if (!dB.BoardData().getBoardArchivedCards(board).contains(card)) {
            throw BadRequestException("Card $card is not archived")
        }
    }

    private fun checkLoginCombination(email: String, password: String) {
        if (!dB.UserData().checkUserEmailAndPassword().contains(Pair(email, password))) {
            throw NotFoundException("Email or Password is wrong")
        }
    }
}
