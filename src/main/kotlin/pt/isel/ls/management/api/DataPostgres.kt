package pt.isel.ls.management.api

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.management.Board
import pt.isel.ls.management.BoardShowed
import pt.isel.ls.management.Card
import pt.isel.ls.management.CardShowed
import pt.isel.ls.management.ListShowed
import pt.isel.ls.management.Lists
import pt.isel.ls.management.User
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

class DataPostgres(dataSource: PGSimpleDataSource) {
    val globalSource = dataSource
    lateinit var stm: PreparedStatement
    lateinit var rs: ResultSet

    inner class UserData : UserInterface {
        override fun createUser(name: String, email: String, password: String): Pair<String, Int> {
            val token = UUID.randomUUID().toString()
            val query = "insert into users(name, email, password, token) values (?, ?, ?, ?);"
            val query2 = "select number from users where token = ?;"
            val id: Int

            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setString(1, name)
                stm.setString(2, email)
                stm.setString(3, password)
                stm.setString(4, token)
                stm.executeUpdate()

                stm = it.prepareStatement(query2)
                stm.setString(1, token)
                rs = stm.executeQuery()
                rs.next()
                id = rs.getInt("number")
            }
            return Pair(token, id)
        }

        override fun login(email: String, password: String): String {
            val query = "select token from users where email = ? AND password = ?;"
            val token: String

            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setString(1, email)
                stm.setString(2, password)
                rs = stm.executeQuery()
                rs.next()
                token = rs.getString("token")
            }
            return token
        }

        override fun getUser(id: Int): User {
            val query = "select * from users where number = ?;"
            val user: User

            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, id)
                rs = stm.executeQuery()
                rs.next()
                user = User(
                    rs.getInt("number"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("token")
                )
            }
            return user
        }

        override fun getUserWithToken(token: String): Int? {
            val query = "select number from users where token = ?;"
            val id: Int
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setString(1, token)
                rs = stm.executeQuery()
                rs.next()
                id = rs.getInt("number")
            }
            return if (id != 0) id else null
        }

        override fun checkUserEmailAndPassword(): List<Pair<String, String>> {
            val query = "select email, password from users;"
            var combinations = emptyList<Pair<String, String>>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                while (rs.next()) {
                    combinations = combinations + Pair(rs.getString("email"), rs.getString("password"))
                }
            }
            return combinations
        }

        override fun getUsersIds(): List<Int> {
            val query = "select number from users"
            var list = emptyList<Int>()

            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getInt("number")
                }
            }
            return list
        }

        override fun getUsersEmails(): List<String> {
            val query = "select email from users"
            var list = emptyList<String>()

            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getString("email")
                }
            }
            return list
        }

        override fun getUserBoards(userId: Int): List<Int> {
            val query = "select boardId from user_board where userNumber = ?;"
            var boardIds = emptyList<Int>()

            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, userId)
                val rs = stm.executeQuery()
                while (rs.next()) {
                    boardIds = boardIds + rs.getInt("boardId")
                }
            }
            return boardIds
        }

        override fun getUsers(skip: Int, limit: Int): List<User> {
            val query = "select * from users OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;"
            var users = emptyList<User>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, skip)
                stm.setInt(2, limit)
                rs = stm.executeQuery()
                while (rs.next()) {
                    users = users + User(
                        rs.getInt("number"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("token")
                    )
                }
            }
            return users
        }
    }

    inner class BoardData : BoardInterface {
        override fun createBoard(name: String, description: String): Int {
            val query = "insert into boards(name, description) values (?, ?);"
            val query2 = "select * from boards where name = ?;"
            val id: Int

            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setString(1, name)
                stm.setString(2, description)
                stm.executeUpdate()

                stm = it.prepareStatement(query2)
                stm.setString(1, name)
                rs = stm.executeQuery()
                rs.next()
                id = rs.getInt("id")
            }
            return id
        }

        override fun addUserToBoard(userId: Int, boardId: Int): Pair<Int, Int> {
            val query = "insert into user_board(userNumber, boardId) values(?, ?);"
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, userId)
                stm.setInt(2, boardId)
                stm.executeUpdate()
            }
            return Pair(userId, boardId)
        }

        override fun getBoard(bId: Int): Board {
            val query = "select boards.id, boards.name, boards.description,\n" +
                "string_agg(lists.name, ',') as listNames, string_agg(CAST(lists.id AS text), ',') as listIds\n" +
                "from boards left join lists on boards.id = boardId where boards.id = ? GROUP BY boards.id;"
            val board: Board
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, bId)
                rs = stm.executeQuery()
                rs.next()

                val listNames = rs.getString("listNames")?.split(",") ?: emptyList()
                val listIds = rs.getString("listIds")?.split(",") ?: emptyList()

                val lists = listNames.zip(listIds).map { (name, id) ->
                    ListShowed(id.toInt(), name, bId)
                }

                board = Board(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    lists
                )
            }
            return board
        }

        override fun getBoards(skip: Int, limit: Int): List<BoardShowed> {
            val query = "SELECT boards.id, boards.name, boards.description\n" +
                "FROM boards GROUP BY boards.id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;"
            var list = emptyList<BoardShowed>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, skip)
                stm.setInt(2, limit)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + BoardShowed(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                    )
                }
            }
            return list
        }

        override fun getBoardsOfUser(userId: Int, skip: Int, limit: Int): List<BoardShowed> {
            val query = "SELECT boards.id, boards.name, boards.description\n" +
                "FROM boards JOIN user_board ON userNumber = ? GROUP BY boards.id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;"
            var list = emptyList<BoardShowed>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, userId)
                stm.setInt(2, skip)
                stm.setInt(3, limit)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + BoardShowed(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                    )
                }
            }
            return list
        }

        override fun getAllUserBoard(): List<Pair<Int, Int>> {
            val query = "select * from user_board"
            var list = emptyList<Pair<Int, Int>>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + Pair(rs.getInt("userNumber"), rs.getInt("boardId"))
                }
            }
            return list
        }

        override fun getUsersOfBoard(bId: Int, skip: Int, limit: Int): List<Pair<Int, Int>> {
            val query = "select * from user_board where ( boardId = ? ) OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"
            var list = emptyList<Pair<Int, Int>>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, bId)
                stm.setInt(2, skip)
                stm.setInt(3, limit)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + Pair(rs.getInt("userNumber"), rs.getInt("boardId"))
                }
            }
            return list
        }

        override fun getBoardsIds(): List<Int> {
            val query = "select id from boards"
            var list = emptyList<Int>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getInt("id")
                }
            }
            return list
        }

        override fun getBoardLists(bId: Int): List<Int> {
            val query = "select id from lists where boardId = ?;"
            var list = emptyList<Int>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, bId)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getInt("id")
                }
            }
            return list
        }

        override fun getBoardNames(): List<String> {
            val query = "select name from boards"
            var list = emptyList<String>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getString("name")
                }
            }
            return list
        }

        override fun getBoardCards(boardId: Int): List<Int> {
            val query = "select distinct id from cards where boardId = ?"
            var ids = emptyList<Int>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, boardId)
                rs = stm.executeQuery()
                while (rs.next()) {
                    ids = ids + rs.getInt("id")
                }
            }
            return ids
        }

        override fun getBoardArchivedCards(boardId: Int): List<Int> {
            val query = "select distinct id from cards where boardId = ? and listId is null"
            var ids = emptyList<Int>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, boardId)
                rs = stm.executeQuery()
                while (rs.next()) {
                    ids = ids + rs.getInt("id")
                }
            }
            return ids
        }

        override fun searchBoardByName(boardName: String, skip: Int, limit: Int): List<BoardShowed> {
            val query = "select * from boards where LOWER(boards.name) like LOWER('%$boardName%')" +
                " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;"
            var list = emptyList<BoardShowed>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, skip)
                stm.setInt(2, limit)
                rs = stm.executeQuery()

                while (rs.next()) {
                    list = list + BoardShowed(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                    )
                }
                return list
            }
        }
    }

    inner class ListData : ListInterface {
        override fun createList(name: String, bId: Int): Int {
            val query = "insert into lists(name, boardId) values (?, ?);"
            val query2 = "select id from lists where name = ? and boardId = ?;"
            val id: Int

            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setString(1, name)
                stm.setInt(2, bId)
                stm.executeUpdate()

                stm = it.prepareStatement(query2)
                stm.setString(1, name)
                stm.setInt(2, bId)
                rs = stm.executeQuery()
                rs.next()

                id = rs.getInt("id")
            }
            return id
        }

        override fun getList(lId: Int): Lists {
            val query2 = "select lists.id, lists.name, lists.boardId, " +
                "string_agg(cards.name, ', ' order by cards.index) as cardNames, " +
                "string_agg(CAST(cards.id as text), ',' order by cards.index) as cardIds " +
                "from lists left join cards on lists.id = cards.listId where lists.id = ? " +
                "group by lists.id, lists.name, lists.boardId;"
            val list: Lists
            globalSource.connection.use {
                stm = it.prepareStatement(query2)
                stm.setInt(1, lId)
                rs = stm.executeQuery()
                rs.next()

                val cardNames = rs.getString("cardNames")?.split(",") ?: emptyList()
                val cardIds = rs.getString("cardIds")?.split(",") ?: emptyList()

                val cards = cardNames.zip(cardIds).map { (name, id) ->
                    CardShowed(id.toInt(), name)
                }

                list = Lists(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("boardId"),
                    cards
                )
            }
            return list
        }

        override fun getLists(bId: Int, skip: Int, limit: Int): List<Lists> {
            val query = "SELECT lists.id, lists.name, lists.boardId, string_agg(cards.name, ', ') as cardNames, " +
                "string_agg(CAST(cards.id AS text), ',') as cardIds from lists " +
                "left join cards on cards.listId = lists.id WHERE lists.boardId = ? " +
                "GROUP BY lists.id, lists.name, lists.boardId " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;"
            var list = emptyList<Lists>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, bId)
                stm.setInt(2, skip)
                stm.setInt(3, limit)
                rs = stm.executeQuery()
                while (rs.next()) {
                    val cardNames = rs.getString("cardNames")?.split(",") ?: emptyList()
                    val cardIds = rs.getString("cardIds")?.split(",") ?: emptyList()

                    val cards = cardNames.zip(cardIds).map { (name, id) ->
                        CardShowed(id.toInt(), name)
                    }

                    list = list + Lists(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("boardId"),
                        cards
                    )
                }
            }
            return list
        }

        override fun getListsIds(): List<Int> {
            val query = "select id from lists"
            var list = emptyList<Int>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getInt("id")
                }
            }
            return list
        }

        override fun getListCards(listId: Int): List<Int> {
            val query = "select id from cards where listId = ?;"
            var list = emptyList<Int>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, listId)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getInt("id")
                }
            }
            return list
        }

        override fun deleteList(listId: Int): Boolean {
            val query = "DELETE FROM cards WHERE listId = ?;\n" +
                "DELETE FROM lists WHERE id = ?;"
            val ret: Int
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, listId)
                stm.setInt(2, listId)
                ret = stm.executeUpdate()
            }
            return ret == 1
        }

        override fun getListsNamesOfBoards(boardId: Int): List<String> {
            val query = "select name from lists where boardId = ?;"
            var list = emptyList<String>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, boardId)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getString("name")
                }
            }
            return list
        }
    }
    inner class CardData : CardInterface {
        override fun createCard(
            name: String,
            description: String,
            currDate: Date,
            dueDate: Date?,
            lId: Int,
            bId: Int,
            uId: Int
        ): Int {
            val query = "select count(id) + 1 as ids from cards where listId = ?"
            val query2 = "select boardId from lists where id = ?"
            val query3 = "insert into cards(index,name, description, creationDt, conclusionDt, listId, boardId) " +
                "values(?,?, ?, ?, ?, ?, ?);"
            val query4 = "select cards.id from cards where index = ? and listId = ? and boardId = ?"
            val id: Int
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, lId)
                rs = stm.executeQuery()
                rs.next()
                val cardId = rs.getInt("ids")

                stm = it.prepareStatement(query2)
                stm.setInt(1, lId)
                rs = stm.executeQuery()
                rs.next()
                val boardId = rs.getInt("boardId")

                stm = it.prepareStatement(query3)
                stm.setInt(1, cardId)
                stm.setString(2, name)
                stm.setString(3, description)
                stm.setDate(4, currDate)
                stm.setDate(5, dueDate)
                stm.setInt(6, lId)
                stm.setInt(7, boardId)
                stm.executeUpdate()

                stm = it.prepareStatement(query4)
                stm.setInt(1, cardId)
                stm.setInt(2, lId)
                stm.setInt(3, bId)
                rs = stm.executeQuery()
                rs.next()
                id = rs.getInt("id")
            }
            return id
        }

        override fun getCard(cardId: Int): Card {
            val query = "select * from cards where id = ?;"
            val card: Card
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, cardId)
                rs = stm.executeQuery()
                rs.next()
                card = Card(
                    rs.getInt("id"),
                    rs.getInt("index"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("creationDt").toString(),
                    rs.getDate("conclusionDt")?.toString(),
                    rs.getInt("listId"),
                    rs.getInt("boardId")
                )
            }
            return card
        }

        override fun getCardsArchived(boardId: Int, skip: Int, limit: Int): List<Card> {
            val query = "select * from cards where listId IS NULL AND boardId = ? OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;"
            var cards = emptyList<Card>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, boardId)
                stm.setInt(2, skip)
                stm.setInt(3, limit)
                rs = stm.executeQuery()
                while (rs.next()) {
                    cards = cards + Card(
                        rs.getInt("id"),
                        rs.getInt("index"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("creationDt").toString(),
                        rs.getDate("conclusionDt")?.toString(),
                        rs.getInt("listId"),
                        rs.getInt("boardId")
                    )
                }
            }
            return cards
        }

        override fun getCardArchived(cardId: Int): Card {
            val query = "select * from cards where id = ?"
            val card: Card
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, cardId)
                rs = stm.executeQuery()
                rs.next()
                card = Card(
                    rs.getInt("id"),
                    rs.getInt("index"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("creationDt").toString(),
                    rs.getDate("conclusionDt")?.toString(),
                    rs.getInt("listId"),
                    rs.getInt("boardId")
                )
            }
            return card
        }

        override fun archiveCard(cardId: Int, listId: Int): Int {
            val query = "select index from cards where id = ?"
            val query2 = "update cards set listId = null where id = ?"
            val query3 = "update cards set index = index - 1 where index > ? and listId = ?"
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, cardId)
                rs = stm.executeQuery()
                rs.next()
                val idx = rs.getInt("index")
                stm = it.prepareStatement(query2)
                stm.setInt(1, cardId)
                stm.executeUpdate()
                stm = it.prepareStatement(query3)
                stm.setInt(1, idx)
                stm.setInt(2, listId)
                stm.executeUpdate()
            }
            return cardId
        }

        override fun unarchiveCard(cardId: Int, listId: Int): Card {
            val query = "select count(id) as index from cards where listId = ?"
            val query2 = "update cards set listId = ?, index = ? where id = ?"
            val query3 = "select * from cards where id = ?"
            val card: Card
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, listId)
                rs = stm.executeQuery()
                rs.next()
                val idx = rs.getInt("index") + 1
                stm = it.prepareStatement(query2)
                stm.setInt(1, listId)
                stm.setInt(2, idx)
                stm.setInt(3, cardId)
                stm.executeUpdate()
                stm = it.prepareStatement(query3)
                stm.setInt(1, cardId)
                rs = stm.executeQuery()
                rs.next()
                card = Card(
                    rs.getInt("id"),
                    rs.getInt("index"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("creationDt").toString(),
                    rs.getDate("conclusionDt")?.toString(),
                    rs.getInt("listId"),
                    rs.getInt("boardId")
                )
            }
            return card
        }

        override fun getCards(lId: Int, skip: Int, limit: Int): List<Card> {
            val query = "select * from cards where listId = ?  ORDER BY cards.index " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;"
            var cards = emptyList<Card>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, lId)
                stm.setInt(2, skip)
                stm.setInt(3, limit)
                rs = stm.executeQuery()
                while (rs.next()) {
                    cards = cards + Card(
                        rs.getInt("id"),
                        rs.getInt("index"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("creationDt").toString(),
                        rs.getDate("conclusionDt")?.toString(),
                        rs.getInt("listId"),
                        rs.getInt("boardId")
                    )
                }
            }
            return cards
        }

        override fun moveCard(nlId: Int, olId: Int, cardId: Int, cix: Int): Card {
            val query = "UPDATE cards set index = index + 1 where index >= ? and listId = ?"
            val query2 = "UPDATE cards SET index = ?, listId = ? WHERE listId = ? and id = ?"
            val query3 = "UPDATE cards set index = index - 1 where index > ? and listId = ?"
            val query4 = "select * from cards where index = ? and listId = ?"
            val card: Card
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, cix)
                stm.setInt(2, nlId)
                stm.executeUpdate()

                stm = it.prepareStatement(query2)
                stm.setInt(1, cix)
                stm.setInt(2, nlId)
                stm.setInt(3, olId)
                stm.setInt(4, cardId)
                stm.executeUpdate()

                stm = it.prepareStatement(query3)
                stm.setInt(1, cix)
                stm.setInt(2, olId)
                stm.executeUpdate()

                stm = it.prepareStatement(query4)
                stm.setInt(1, cix)
                stm.setInt(2, nlId)
                rs = stm.executeQuery()
                rs.next()
                card = Card(
                    rs.getInt("id"),
                    rs.getInt("index"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("creationDt").toString(),
                    rs.getDate("conclusionDt")?.toString(),
                    rs.getInt("listId"),
                    rs.getInt("boardId")
                )
            }
            return card
        }

        override fun getCardIds(): List<Int> {
            val query = "select cards.id from cards"
            var list = emptyList<Int>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getInt("id")
                }
            }
            return list
        }

        override fun getCardNames(listId: Int): List<String> {
            val query = "select cards.name from cards where listId = ?"
            var list = emptyList<String>()
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, listId)
                rs = stm.executeQuery()
                while (rs.next()) {
                    list = list + rs.getString("name")
                }
            }
            return list
        }

        override fun getCurrDate(): Date {
            val query = "select CURRENT_DATE;"
            val date: Date
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                rs = stm.executeQuery()
                rs.next()
                date = rs.getDate("current_date")
            }
            return date
        }

        override fun deleteCard(cardId: Int): Boolean {
            val query = "DELETE FROM cards WHERE (id = ?);"
            val ret: Int
            globalSource.connection.use {
                stm = it.prepareStatement(query)
                stm.setInt(1, cardId)
                ret = stm.executeUpdate()
            }
            return ret == 1
        }
    }
}
