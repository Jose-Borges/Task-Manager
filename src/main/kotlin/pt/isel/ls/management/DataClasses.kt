package pt.isel.ls.management

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val token: String
)

@Serializable
data class Board(
    val id: Int,
    val name: String,
    val description: String,
    val lists: List<ListShowed>
)

@Serializable
data class BoardShowed(
    val id: Int,
    val name: String,
    val description: String
)

@Serializable
data class Lists(
    val id: Int,
    val name: String,
    val boardId: Int,
    val cards: List<CardShowed>
)

@Serializable
data class ListShowed(
    val id: Int,
    val name: String,
    val boardId: Int
)

@Serializable
data class CardShowed(
    val id: Int,
    val name: String
)

@Serializable
data class Card(
    val id: Int,
    val index: Int,
    val name: String,
    val description: String,
    val creationDate: String,
    val conclusionDate: String?,
    val listId: Int,
    val boardId: Int
)

@Serializable
data class UserMem(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val token: String
)

@Serializable
data class BoardMem(
    val id: Int,
    val name: String,
    val description: String,
    val lists: MutableList<String>
)

@Serializable
data class ListsMem(
    val id: Int,
    val name: String,
    val boardId: Int,
    val cards: MutableList<String>
)

@Serializable
data class CardMem(
    var id: Int,
    var idx: Int,
    val name: String,
    val description: String,
    val creationDate: String,
    val conclusionDate: String?,
    var listId: Int?,
    val boardId: Int
)
