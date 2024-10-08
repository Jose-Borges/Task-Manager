package pt.isel.ls.mem

import pt.isel.ls.management.mem.DataMem
import java.util.Calendar
import kotlin.NoSuchElementException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardsMemTests {
    private val userName = "Antonieta"
    private val userEmail = "antonieta@gmail.com"
    private val userPassword = "password"
    private val userId = 1
    private val boardName = "Board 1"
    private val boardDescription = "1st board"
    private val boardId = 1
    private val listName = "List 1"
    private val listId = 1
    private val listName2 = "List 2"
    private val listId2 = 2
    private val cardName = "Card 1"
    private val cardDescription = "1st Card"
    private val cardName2 = "Card 2"
    private val cardDescription2 = "2nd Card"
    private val currDate = Calendar.getInstance().time.toString()
    private val dB = DataMem()

    @BeforeTest
    fun setUpNecessities() {
        dB.UserData().createUser(userName, userEmail, userPassword).second
        dB.BoardData().createBoard(boardName, boardDescription)
        dB.ListData().createList(listName, boardId)
        dB.ListData().createList(listName2, boardId)
    }

    @AfterTest
    fun deleteNecessities() {
        dB.UserData().deleteUser(userId)
        dB.BoardData().deleteBoard(boardId)
        dB.ListData().deleteList(listId)
        dB.ListData().deleteList(listId2)
    }

    @Test
    fun createCardTestMem() {
        val id = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        val id2 = dB.CardData().createCard(cardName2, cardDescription2, currDate, null, listId, boardId, userId)
        assertTrue(dB.lists.size == 2)
        assertEquals(1, id)
        assertEquals(2, id2)
        dB.CardData().deleteCard(id)
        dB.CardData().deleteCard(id2)
    }

    @Test
    fun getCardTestMem() {
        val id = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        val card = dB.CardData().getCard(id)
        assertEquals(cardName, card.name)
        assertEquals(cardDescription, card.description)
        assertEquals(null, card.conclusionDate)
        assertEquals(listId, card.listId)
        dB.CardData().deleteCard(id)
    }

    @Test
    fun getCardWithInvalidIdMem() {
        val id = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        assertFailsWith<NoSuchElementException> {
            dB.CardData().getCard(id + 1)
        }
        dB.CardData().deleteCard(id)
    }

    @Test
    fun getCardsTestMem() {
        val id = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        val id2 = dB.CardData().createCard(cardName2, cardDescription2, currDate, null, listId, boardId, userId)
        val cards = dB.CardData().getCards(listId)
        assertEquals(cardName, cards[0].name)
        assertEquals(cardName2, cards[1].name)
        dB.CardData().deleteCard(id)
        dB.CardData().deleteCard(id2)
    }

    @Test
    fun getCardsWithWrongListIdMem() {
        val id = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        val id2 = dB.CardData().createCard(cardName2, cardDescription2, currDate, null, listId, boardId, userId)
        val cards = dB.CardData().getCards(listId2)
        assertTrue { cards.isEmpty() }
        dB.CardData().deleteCard(id)
        dB.CardData().deleteCard(id2)
    }

    @Test
    fun moveCardsTestMem() {
        val cix = 2
        val card1 = dB.CardData().createCard("Test", cardDescription, currDate, null, listId2, boardId, userId)
        val card2 = dB.CardData().createCard("Test2", cardDescription, currDate, null, listId2, boardId, userId)
        val card3 = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        dB.CardData().moveCard(listId, listId2, card3, cix)
        val cards = dB.CardData().getCards(listId2)
        val oldCards = dB.CardData().getCards(listId)
        val card = cards.first { it.idx == cix }
        assertEquals(3, cards.size)
        assertEquals(0, oldCards.size)
        assertEquals(cardName, card.name)
        dB.CardData().deleteCard(card1)
        dB.CardData().deleteCard(card2)
        dB.CardData().deleteCard(card3)
    }

    @Test
    fun deleteCardTestMem() {
        val card1 = dB.CardData().createCard(cardName, cardDescription, currDate, null, listId, boardId, userId)
        assertFalse { dB.CardData().deleteCard(card1) }
        val cards = dB.CardData().getCards(listId)
        assertFalse { cards.any { it.id == card1 } }
    }
}
