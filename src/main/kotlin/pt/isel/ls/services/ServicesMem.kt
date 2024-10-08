package pt.isel.ls.services

import pt.isel.ls.management.BoardMem
import pt.isel.ls.management.CardMem
import pt.isel.ls.management.ListsMem
import pt.isel.ls.management.User
import pt.isel.ls.management.mem.DataMem
import pt.isel.ls.webApi.BadRequestException
import pt.isel.ls.webApi.ForbiddenRequestException
import pt.isel.ls.webApi.NotFoundException
import java.sql.Date

class ServicesMem(dataBase: DataMem) {
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
        return dB.UserData().login(uEmail, pass)
    }

    fun getUsers(skip: String?, limit: String?): List<User> {
        val p = getSkipAndLimit(skip, limit)
        return dB.UserData().getUsers().drop(p.first).take(p.second)
    }

    fun getUser(id: String?): User {
        if (id == null) throw BadRequestException("Invalid id")
        val uId = id.toInt()
        checkUserExists(uId)
        return dB.UserData().getUser(uId)
    }

    fun getUserWithToken(token: String): Int {
        return dB.UserData().getUserWithToken(token)
            ?: throw NotFoundException("There's no user with the given token")
    }

    fun createBoard(user: Int, name: String?, description: String?): Int {
        val params = listOf(
            Pair("Name", name),
            Pair("Description", description)
        )
        val p = paramValidation(params)
        val n = p.first { it.first == "Name" }.second
        val desc = p.first { it.first == "Description" }.second
        val board = dB.BoardData().createBoard(n, desc)
        dB.BoardData().addUserToBoard(user, board)
        return board
    }

    fun addUserToBoard(user: Int, userId: Int?, boardId: Int?): Pair<Int, Int> {
        val params = listOf(
            Pair("UserId", userId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val uId = p.first { it.first == "UserId" }.second.toInt()
        val bId = p.first { it.first == "BoardId" }.second.toInt()
        checkUserExists(uId)
        checkBoardExists(bId)
        checkUserHasBoard(user, bId)
        if (dB.BoardData().getAllUserBoard().contains(Pair(userId, boardId))) {
            throw BadRequestException("The user is already in this board")
        }
        return dB.BoardData().addUserToBoard(uId, bId)
    }

    fun getBoards(userId: String?, boardName: String?, skip: String?, limit: String?): List<BoardMem> {
        val id = userId?.toInt()
        val p = getSkipAndLimit(skip, limit)
        if (boardName != null) return dB.BoardData().searchBoardByName(boardName).drop(p.first).take(p.second)
        return if (id != null) {
            dB.BoardData().getBoardsOfUser(id).drop(p.first).take(p.second)
        } else {
            dB.BoardData().getBoards().drop(p.first).take(p.second)
        }
    }

    fun getBoard(boardId: Int?): BoardMem {
        if (boardId == null) throw BadRequestException("Invalid boardId")
        checkBoardExists(boardId)
        return dB.BoardData().getBoard(boardId)
    }

    fun getUsersOfBoard(boardId: Int?, skip: String?, limit: String?): List<Pair<Int, Int>> {
        if (boardId == null) throw BadRequestException("Invalid boardId")
        checkBoardExists(boardId)
        val p = getSkipAndLimit(skip, limit)
        return dB.BoardData().getUsersOfBoard(boardId).drop(p.first).take(p.second)
    }

    fun createList(name: String?, boardId: String?, userId: Int): Int {
        val params = listOf(
            Pair("Name", name),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val board = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(board)
        val nm = p.first { it.first == "Name" }.second
        checkUserHasBoard(userId, board)
        checkListNameUnique(nm, board)
        return dB.ListData().createList(nm, board)
    }

    fun getLists(boardId: String?, skip: String?, limit: String?): List<ListsMem> {
        if (boardId == null) throw BadRequestException("Invalid boardId")
        val bId = boardId.toInt()
        checkBoardExists(bId)
        val p = getSkipAndLimit(skip, limit)
        return dB.ListData().getLists(bId).drop(p.first).take(p.second)
    }

    fun getList(listId: String?, boardId: String?): ListsMem {
        val params = listOf(
            Pair("ListId", listId),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val bId = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(bId)
        val lId = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(lId)
        checkBoardHasList(bId, lId)
        return dB.ListData().getList(lId)
    }

    fun deleteList(listId: String?, boardId: String?, userId: Int): ListsMem {
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
        checkBoardHasList(bid, lid)
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
        checkBoardHasList(boardId, listId)
        checkCardNameUnique(n, listId)
        val conDate = if (dueDate != null) Date.valueOf(dueDate) else null
        val currDate = dB.CardData().getCurrDate()
        if (conDate != null) {
            if (Date.valueOf(currDate) >= conDate) {
                throw BadRequestException("It's impossible to conclude something before starting it")
            }
        }
        return dB.CardData().createCard(n, desc, currDate, dueDate, listId, boardId, userId)
    }

    fun getCardsArchived(boardId: String?, skip: String?, limit: String?): List<CardMem> {
        if (boardId == null) throw BadRequestException("Invalid BoardId")
        val bId = boardId.toInt()
        val p = getSkipAndLimit(skip, limit)
        return dB.CardData().getCardsArchived(bId).drop(p.first).take(p.second)
    }

    fun getCardArchived(cardId: String?, boardId: String?): CardMem {
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

    fun unarchiveCard(cardId: String?, listId: String?, boardId: String?, userId: Int): CardMem {
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

    fun getCards(
        listId: String?,
        skip: String?,
        limit: String?
    ): List<CardMem> {
        if (listId == null) throw BadRequestException("Invalid ListId")
        val lId = listId.toInt()
        checkListExists(lId)
        val p = getSkipAndLimit(skip, limit)
        return dB.CardData().getCards(lId).drop(p.first).take(p.second)
    }

    fun getCard(cardId: String?, listId: String?): CardMem {
        val params = listOf(
            Pair("CardId", cardId),
            Pair("ListId", listId)
        )
        val p = paramValidation(params)
        val lId = p.first { it.first == "ListId" }.second.toInt()
        checkListExists(lId)
        val cid = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(cid)
        checkListHasCard(lId, cid)
        return dB.CardData().getCard(cid)
    }

    fun moveCard(
        oldListId: String?,
        newListId: String?,
        cardId: String?,
        newCardIdx: String?,
        boardId: String?,
        userId: Int
    ): CardMem {
        val params = listOf(
            Pair("OldListId", oldListId),
            Pair("NewListId", newListId),
            Pair("CardId", cardId),
            Pair("NewCardIdx", newCardIdx),
            Pair("BoardId", boardId)
        )
        val p = paramValidation(params)
        val bId = p.first { it.first == "BoardId" }.second.toInt()
        checkBoardExists(bId)
        checkUserHasBoard(userId, bId)
        val newList = p.first { it.first == "NewListId" }.second.toInt()
        val oldList = p.first { it.first == "OldListId" }.second.toInt()
        checkListExists(newList)
        checkBoardHasList(bId, newList)
        if (newList == oldList) {
            throw BadRequestException("Card is already in list $newList")
        }
        checkListExists(oldList)
        checkBoardHasList(bId, oldList)
        val cid = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(cid)
        checkListHasCard(oldList, cid)
        checkCardNameUnique(dB.CardData().getCard(cid).name, newList)
        val cix = p.first { it.first == "NewCardIdx" }.second.toInt()
        return dB.CardData().moveCard(oldList, newList, cid, cix)
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
        checkBoardHasList(bid, lid)
        val cid = p.first { it.first == "CardId" }.second.toInt()
        checkCardExists(cid)
        checkListHasCard(lid, cid)
        return dB.CardData().deleteCard(cid)
    }

    private fun getSkipAndLimit(skip: String?, limit: String?): Pair<Int, Int> {
        val s = skip?.toInt() ?: 0
        val l = limit?.toInt() ?: 100
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
            throw ForbiddenRequestException("User $userId doesn't have access to that Board")
        }
    }

    private fun checkListNameUnique(name: String, boardId: Int) {
        if (dB.ListData().getListsNamesOfBoards(boardId).contains(name)) {
            throw BadRequestException("There's already a list with that name")
        }
    }

    private fun checkCardNameUnique(name: String, listId: Int) {
        val x = dB.CardData().getCardNames(listId)
        if (x.contains(name)) {
            throw BadRequestException("There's already a card in that list with that name")
        }
    }

    private fun checkListHasCard(listId: Int, cardId: Int) {
        if (!dB.ListData().getListCards(listId).contains(cardId)) {
            throw NotFoundException("List $listId doesn't have card $cardId")
        }
    }

    private fun checkBoardHasList(boardId: Int, listId: Int) {
        if (!dB.BoardData().getBoardLists(boardId).contains(listId)) {
            throw NotFoundException("Board $listId doesn't have a list $boardId")
        }
    }

    private fun checkCardInBoard(board: Int, card: Int) {
        if (!dB.BoardData().getBoardCards(board).contains(card)) {
            throw NotFoundException("Board $board doesn't have card $card")
        }
    }
    private fun checkCardArchived(board: Int, card: Int) {
        if (!dB.BoardData().getBoardArchivedCards(board).contains(card)) {
            throw NotFoundException("No archived card with given cardId")
        }
    }
}
