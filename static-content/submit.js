import dataFetch from "./data.js"
import utils from "./utils.js";

let token = undefined

function signInSubmit(e) {
    e.preventDefault()
    const username = document.querySelector("#idName").value
    const email = document.querySelector("#idEmailSignIn").value
    const password = document.querySelector("#idPasswordSignIn").value
    dataFetch(
        `users`,
        "POST",
        JSON.stringify({ userName: username, email: email, password: password })
    ).then(user => {
        utils.checkError(user)
        submits.token = user.first
        window.location.hash = `users`
    }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function loginSubmit(e) {
    e.preventDefault()
    const email = document.querySelector("#idEmailLogin").value
    const password = document.querySelector("#idPasswordLogin").value
    dataFetch(`users/login?email=${email}&password=${password}`)
        .then(user => {
            utils.checkError(user)
            submits.token = user
            window.location.hash = `users`
        })
        .catch(error => {
            window.location.hash = `error/${error.status}/${error.message}`
        })
}

function boardSubmit(e) {
    e.preventDefault()
    const name = document.querySelector("#idName")
    const desc = document.querySelector("#idDescription")
    dataFetch(
        `boards`,
        "POST",
        JSON.stringify({ boardName : name.value, boardDescription : desc.value })
    ).then(board => {
        utils.checkError(board)
        window.location.hash = `boards/${board}`
    }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function searchBoardSubmit(e) {
    e.preventDefault()
    let name = document.querySelector("#idName")
    if(!name) {
        window.location.hash = `boards`
    }
    else window.location.hash = `boards?boardName=${name.value}&skip=0&limit=3`
}

function listSubmit(e) {
    e.preventDefault()
    const name = document.querySelector("#idName")
    const boardId = document.querySelector("#idBoardId")
    dataFetch(`lists?boardId=${boardId.value}`, "POST", JSON.stringify({ listName : name.value }))
        .then(list => {
            utils.checkError(list)
            window.location.hash = `lists/${list}?boardId=${boardId.value}`
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function cardSubmit(e) {
    e.preventDefault()
    const name = document.querySelector("#idName").value
    const boardId = document.querySelector("#idBoardId").value
    const listId = document.querySelector("#idListId").value
    const desc = document.querySelector("#idDescription").value
    let conDate = document.querySelector("#idDate")
    if(!conDate) {
        conDate = undefined
    }else conDate = conDate.value
    dataFetch(
        `cards?boardId=${boardId}&listId=${listId}`,
        "POST",
        JSON.stringify({ cardName : name, cardDescription : desc, conclusionDate : conDate })
    ).then(card => {
        utils.checkError(card)
        window.location.hash = `cards/${card}?listId=${listId}`
    }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function moveCardSubmit(e) {
    e.preventDefault()
    const oldListId = document.querySelector("#idOldListId").value
    const newListId = document.querySelector("#idFirstSelect").value
    const cardId = document.querySelector("#idCardId").value
    const boardId = document.querySelector("#idBoardId").value
    const cix = document.querySelector("#idSecondSelect").value
    dataFetch(
        `cards/${cardId}?newList=${newListId}&oldList=${oldListId}&boardId=${boardId}&cix=${cix}`,
        "PUT"
    ).then(card => {
        utils.checkError(card)
        window.location.hash = `cards/${card.id}?listId=${card.listId}`
    }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function unarchiveSubmit(e) {
    e.preventDefault()
    const listId = document.querySelector("#idSelect").value
    const cardId = document.querySelector("#idCardId").value
    const boardId = document.querySelector("#idBoardId").value
    dataFetch(
        `cards/unarchive/${cardId}/${listId}/${boardId}`,
        "PUT"
    ).then(card => {
        utils.checkError(card)
        window.location.hash = `cards/${card.id}?listId=${card.listId}`
    }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

export const submits = {
    token,
    loginSubmit: loginSubmit,
    signInSubmit: signInSubmit,
    createBoardSubmit: boardSubmit,
    searchSubmit: searchBoardSubmit,
    createListSubmit: listSubmit,
    createCardSubmit: cardSubmit,
    moveCardSubmit: moveCardSubmit,
    unarchiveSubmit: unarchiveSubmit
}

export default submits