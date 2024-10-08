package pt.isel.ls.webapi

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import pt.isel.ls.management.CardMem
import pt.isel.ls.management.mem.DataMem
import pt.isel.ls.services.ServicesMem
import pt.isel.ls.webApi.WebApiMem
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CardsTests {
    private val userName = "Antonieta"
    private val userName2 = "Esdrubal"
    private val userEmail = "antonieta@gmail.com"
    private val userEmail2 = "esdrubal@gmail.com"
    private val password1 = "password1"
    private val password2 = "password2"
    private val userId = 1
    private val userId2 = 2
    private val boardName = "Board 1"
    private val description = "1st board"
    private val boardId = 1
    private val listName = "List 1"
    private val listId = 1
    private val listName2 = "List 2"
    private val cardId = 1
    private val cardName = "Card 1"
    private val cardDescription = "1st Card"
    private val cardName2 = "Card 2"
    private val cardDescription2 = "2nd Card"
    private val currDate = Calendar.getInstance().time.toString()
    private lateinit var userToken: String
    private lateinit var userToken2: String
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
        userToken2 = dB.UserData().createUser(userName2, userEmail2, password2).first
        dB.BoardData().createBoard(boardName, description)
        dB.BoardData().addUserToBoard(userId, boardId)
        dB.ListData().createList(listName, boardId)
    }

    @AfterTest
    fun finishTest() {
        dB.UserData().deleteUser(userId)
        dB.ListData().deleteList(listId)
        dB.BoardData().deleteUserBoard(userId, boardId)
        dB.BoardData().deleteBoard(boardId)
    }

    @Test
    fun createCardTest() {
        body = mapOf("cardName" to cardName, "cardDescription" to cardDescription, "conclusionDate" to null)
        request = Request(POST, "http://localhost:9000/cards")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
            .query("boardId", "$boardId")
            .query("userId", "$userId")
            .query("listId", "$listId")
        response = app(request)
        assertEquals(Status.CREATED, response.status)
        val card = Json.decodeFromString<Int>(response.bodyString())
        assertEquals(cardId, card)
        val card1 = dB.cards.first { it.id == card }
        assertEquals(cardName, card1.name)
        assertEquals(cardDescription, card1.description)
        assertEquals(null, card1.conclusionDate)
        assertEquals(listId, card1.listId)
        assertEquals(boardId, card1.boardId)
    }

    @Test
    fun createCardWithInvalidBodyParameters() {
        body = mapOf("cardDescription" to cardDescription, "conclusionDate" to null)
        request = Request(POST, "http://localhost:9000/cards")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
            .query("boardId", "$boardId")
            .query("userId", "$userId")
            .query("listId", "$listId")
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid Name", response.bodyString())
    }

    @Test
    fun createCardWithInvalidQueryParameters() {
        body = mapOf("cardName" to cardName, "cardDescription" to cardDescription, "conclusionDate" to null)
        request = Request(POST, "http://localhost:9000/cards")
            .header("Authorization", "Bearer $userToken")
            .body(Json.encodeToString(body))
            .query("userId", "$userId")
            .query("listId", "$listId")
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid BoardId", response.bodyString())
    }

    @Test
    fun getCardTest() {
        val card = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        request = Request(GET, "http://localhost:9000/cards/$card")
            .query("listId", "$listId")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val ret = Json.decodeFromString<CardMem>(response.bodyString())
        assertEquals(card, ret.id)
        assertEquals(cardName, ret.name)
        assertEquals(cardDescription, ret.description)
        assertEquals(null, ret.conclusionDate)
        assertEquals(boardId, ret.boardId)
        assertEquals(listId, ret.listId)
        dB.CardData().deleteCard(card)
    }

    @Test
    fun getCardWithInvalidPathParameters() {
        request = Request(GET, "http://localhost:9000/cards/-1")
            .query("listId", "$listId")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no card with the given cardId", response.bodyString())
    }

    @Test
    fun getCardWithInvalidQueryParams() {
        val card = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        request = Request(GET, "http://localhost:9000/cards/$card")
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid ListId", response.bodyString())
    }

    @Test
    fun getCardsTest() {
        val card1 = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        val card2 = dB.CardData().createCard(cardName2, cardDescription2, currDate, null, listId, boardId, userId)
        request = Request(GET, "http://localhost:9000/cards")
            .query("listId", "$listId")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val cards = Json.decodeFromString<List<CardMem>>(response.bodyString())
        assertEquals(cardName, cards[0].name)
        assertEquals(cardName2, cards[1].name)
        dB.CardData().deleteCard(card1)
        dB.CardData().deleteCard(card2)
    }

    @Test
    fun getCardsWithLimitAndSkipTest() {
        val card1 = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        val card2 = dB.CardData().createCard(cardName2, cardDescription2, currDate, null, listId, boardId, userId)
        request = Request(GET, "http://localhost:9000/cards")
            .query("boardId", "$boardId")
            .query("listId", "$listId")
            .query("skip", "1")
            .query("limit", "1")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val cards = Json.decodeFromString<List<CardMem>>(response.bodyString())
        assertEquals(cardName2, cards[0].name)
        dB.CardData().deleteCard(card1)
        dB.CardData().deleteCard(card2)
    }

    @Test
    fun getCardsWithInvalidQueryParams() {
        val card1 = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        val card2 = dB.CardData().createCard(cardName2, cardDescription2, currDate, null, listId, boardId, userId)
        request = Request(GET, "http://localhost:9000/cards")
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid ListId", response.bodyString())
        dB.CardData().deleteCard(card1)
        dB.CardData().deleteCard(card2)
    }

    @Test
    fun moveCardsTest() {
        val cix = 2
        val list2 = dB.ListData().createList(listName2, boardId)
        val card1 = dB.CardData().createCard("Test", cardDescription, currDate, null, list2, boardId, userId)
        val card2 = dB.CardData().createCard("Test2", cardDescription, currDate, null, list2, boardId, userId)
        val card3 = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        request = Request(PUT, "http://localhost:9000/cards/$card3")
            .header("Authorization", "Bearer $userToken")
            .query("newList", "$list2")
            .query("oldList", "$listId")
            .query("cix", "$cix")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.OK, response.status)
        assertEquals("application/json", response.header("content-type"))
        val card = Json.decodeFromString<CardMem>(response.bodyString())
        val cards = dB.CardData().getCards(list2)
        val oldCards = dB.CardData().getCards(listId)
        assertEquals(3, cards.size)
        assertEquals(0, oldCards.size)
        assertEquals(cardName, card.name)
        assertEquals(list2, card.listId)
        dB.ListData().deleteList(list2)
        dB.CardData().deleteCard(card1)
        dB.CardData().deleteCard(card2)
        dB.CardData().deleteCard(card3)
    }

    @Test
    fun moveCardWithInvalidQueryParam() {
        val list2 = dB.ListData().createList(listName2, boardId)
        dB.CardData().createCard("Test", cardDescription, currDate, null, list2, boardId, userId)
        dB.CardData().createCard("Test2", cardDescription, currDate, null, list2, boardId, userId)
        val card1 = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        request = Request(PUT, "http://localhost:9000/cards/$card1")
            .header("Authorization", "Bearer $userToken")
            .query("newList", "$list2")
            .query("oldList", "$listId")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid NewCardIdx", response.bodyString())
        dB.ListData().deleteList(list2)
    }

    @Test
    fun moveCardWithoutAccess() {
        val cix = 2
        val list2 = dB.ListData().createList(listName2, boardId)
        dB.CardData().createCard("Test", cardDescription, currDate, null, list2, boardId, userId)
        dB.CardData().createCard("Test2", cardDescription, currDate, null, list2, boardId, userId)
        val card1 = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        request = Request(PUT, "http://localhost:9000/cards/$card1")
            .header("Authorization", "Bearer $userToken2")
            .query("newList", "$list2")
            .query("oldList", "$listId")
            .query("cix", "$cix")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.FORBIDDEN, response.status)
        assertEquals("User $userId2 doesn't have access to that Board", response.bodyString())
        dB.ListData().deleteList(list2)
    }

    @Test
    fun deleteCardTest() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        request = Request(DELETE, "http://localhost:9000/cards/$card1")
            .header("Authorization", "Bearer $userToken")
            .query("listId", "$listId")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.OK, response.status)
        assertEquals("application/json", response.header("content-type"))
        val cards = dB.CardData().getCards(listId)
        assertFalse(cards.any { it.id == card1 })
    }

    @Test
    fun deleteCardInvalidParametersTest() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        request = Request(DELETE, "http://localhost:9000/cards/$card1")
            .header("Authorization", "Bearer $userToken")
            .query("listId", null)
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.BAD_REQUEST, response.status)
        assertEquals("Invalid ListId", response.bodyString())
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun deleteCardUnauthorizedUserTest() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        request = Request(DELETE, "http://localhost:9000/cards/$card1")
            .header("Authorization", "Bearer $userToken2")
            .query("listId", "$listId")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.FORBIDDEN, response.status)
        assertEquals("User 2 doesn't have access to that Board", response.bodyString())
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun deleteCardWrongListId() {
        val list2 = dB.ListData().createList(listName2, boardId)
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        request = Request(DELETE, "http://localhost:9000/cards/$card1")
            .header("Authorization", "Bearer $userToken")
            .query("listId", "$list2")
            .query("boardId", "$boardId")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("List $list2 doesn't have card $card1", response.bodyString())
        dB.CardData().deleteCard(card1)
        dB.ListData().deleteList(list2)
    }

    @Test
    fun getArchivedCards() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        val card2 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        dB.CardData().archiveCard(card1, listId)
        dB.CardData().archiveCard(card2, listId)
        request = Request(GET, "http://localhost:9000/cards/archived/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val cards = Json.decodeFromString<List<CardMem>>(response.bodyString())
        assertEquals(card1, cards[0].id)
        assertEquals(card2, cards[1].id)
        dB.CardData().deleteCard(card1)
        dB.CardData().deleteCard(card2)
    }

    @Test
    fun getArchivedCardsInvalidParams() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        val card2 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        dB.CardData().archiveCard(card1, listId)
        request = Request(GET, "http://localhost:9000/cards/archived/$card1/-1")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no board with the given boardId", response.bodyString())
        dB.CardData().deleteCard(card1)
        dB.CardData().deleteCard(card2)
    }

    @Test
    fun getArchivedCard() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        dB.CardData().archiveCard(card1, listId)
        request = Request(GET, "http://localhost:9000/cards/archived/$card1/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val card = Json.decodeFromString<CardMem>(response.bodyString())
        assertEquals(card1, card.id)
        assertEquals(cardName, card.name)
        assertEquals(description, card.description)
        assertEquals(null, card.conclusionDate)
        assertEquals(boardId, card.boardId)
        assertEquals(null, card.listId)
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun getUnarchivedCard() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        request = Request(GET, "http://localhost:9000/cards/archived/$card1/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("No archived card with given cardId", response.bodyString())
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun getArchivedCardInvalidParams() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        dB.CardData().archiveCard(card1, listId)
        request = Request(GET, "http://localhost:9000/cards/archived/$card1/-1")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no board with the given boardId", response.bodyString())
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun archiveCard() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        request = Request(PUT, "http://localhost:9000/cards/archived/$card1/$listId/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val card = Json.decodeFromString<Int>(response.bodyString())
        assertEquals(card1, card)
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun archiveArchivedCard() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        dB.CardData().archiveCard(card1, listId)
        request = Request(PUT, "http://localhost:9000/cards/archived/$card1/$listId/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("List $listId doesn't have card $card1", response.bodyString())
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun unarchiveCard() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        dB.CardData().archiveCard(card1, listId)
        request = Request(PUT, "http://localhost:9000/cards/unarchive/$card1/$listId/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.OK, response.status)
        val card = Json.decodeFromString<CardMem>(response.bodyString())
        assertEquals(card1, card.id)
        assertEquals(cardName, card.name)
        assertEquals(description, card.description)
        assertEquals(null, card.conclusionDate)
        assertEquals(boardId, card.boardId)
        assertEquals(listId, card.listId)
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun archiveCardInvalidParams() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)

        request = Request(PUT, "http://localhost:9000/cards/unarchive/$card1/-1/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no list with the given listId", response.bodyString())
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun unarchiveUnarchivedCard() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        request = Request(PUT, "http://localhost:9000/cards/unarchive/$card1/$listId/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("No archived card with given cardId", response.bodyString())
        dB.CardData().deleteCard(card1)
    }

    @Test
    fun unarchiveCardInvalidParams() {
        val card1 = dB.CardData().createCard(cardName, description, currDate, null, listId, boardId, userId)
        dB.CardData().archiveCard(card1, listId)
        request = Request(PUT, "http://localhost:9000/cards/unarchive/$card1/-1/$boardId")
            .header("Authorization", "Bearer $userToken")
        response = app(request)
        assertEquals(Status.NOT_FOUND, response.status)
        assertEquals("There's no list with the given listId", response.bodyString())
        dB.CardData().deleteCard(card1)
    }
}
