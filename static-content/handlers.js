
import submits from "./submit.js"
import utils from "./utils.js"
import dataFetch from "./data.js"

function getHome(mainContent) {
    const div = utils.createDiv("Home")
    div.style.display = "flex"
    div.style.flexDirection = "column"
    div.style.alignItems = "center"
    div.style.justifyContent = "center"
    const button = utils.createButton("Continue without Login")
    const form = utils.createForm([button])
    form.action = "#users"
    div.appendChild(form)
    makeLoginAndSignIn(div)
    mainContent.replaceChildren(div)
}

function error(mainContent, path) {
    const parts = path.split("/")
    const div = utils.createDiv("Something went wrong!")
    div.style.textAlign = "center"
    const statusText = utils.createParagraph([document.createTextNode(`Status: ${parts[1]}`)])
    statusText.style.fontSize = "18px"
    const reasonText = utils.createParagraph([document.createTextNode(decodeURIComponent(`Reason: ${parts[2]}`))])
    reasonText.style.fontSize = "18px"
    const button = utils.createButton("GO BACK")
    const form = utils.createForm([statusText, reasonText, button])
    form.action = window.location.last
    div.appendChild(form)
    mainContent.replaceChildren(div)
}

function makeLoginAndSignIn(mainContent) {
    const div = document.createElement("div")
    div.style.display = "flex"
    div.style.justifyContent = "center"
    div.style.gap = `${window.innerWidth/6}px`
    login(div)
    signIn(div)
    mainContent.appendChild(div)
}

function login(mainContent) {
    const div = utils.createDiv("Login")
    const email = utils.createInput("Email: ", "text", "idEmailLogin", undefined, true)
    const password = utils.createInput("Password: ", "password", "idPasswordLogin", undefined, true)
    const form = utils.createForm([
        utils.createParagraph([email.label, email.input]),
        utils.createParagraph([password.label, password.input]),
        utils.createButton("Login")
    ])
    div.appendChild(form)
    form.addEventListener('submit', submits.loginSubmit)
    div.style.display = "inline-block"
    div.style.width = "200px"
    mainContent.appendChild(div)
}

function signIn(mainContent) {
    const div = utils.createDiv("Create an account")
    const username = utils.createInput("Username: ", "text", "idName", undefined, true)
    const email = utils.createInput("Email: ", "text", "idEmailSignIn", undefined, true)
    const password = utils.createInput("Password: ", "password", "idPasswordSignIn", undefined, true)
    const form = utils.createForm([
        utils.createParagraph([username.label, username.input]),
        utils.createParagraph([email.label, email.input]),
        utils.createParagraph([password.label, password.input]),
        utils.createButton("Sign In")
    ])
    div.appendChild(form)
    form.addEventListener('submit', submits.signInSubmit)
    div.style.display = "inline-block"
    div.style.width = "200px"
    mainContent.appendChild(div)
}

function logout() {
    submits.token = undefined
    window.location.hash = `home`
}

function getUsers(mainContent, path){
    dataFetch(path)
        .then(users=> {
            utils.checkError(users)
            const div = utils.createDiv("Users")
            const u = users.map(u => utils.liWithRef(`User ${u.id}: ${u.name}`, "#users/" + u.id))
            const boards = utils.liWithRef("Boards", "#boards/search")
            const createBoards = utils.liWithRef("Create Board", "#createboards")
            if(submits.token === undefined)
                div.appendChild(utils.ul(u.concat([createBoards, boards, utils.liWithRef("Home", `#home`)])))
            else
                div.appendChild(utils.ul(u.concat([createBoards, boards, utils.liWithRef("Logout", `#logout`)])))
            mainContent.replaceChildren(div)
        })
}

function getUser(mainContent, path){
    dataFetch(path)
        .then(user => {
            utils.checkError(user)
            const div = utils.createDiv("User Details")
            div.appendChild(utils.ul([
                utils.li("Name: " + user.name),
                utils.li("Number: " + user.id),
                utils.liWithRef("Boards", "#boards/search"),
                utils.liWithRef("Home", "#users")
            ]))
            mainContent.replaceChildren(div)
    }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function createBoard(mainContent) {
    const div = utils.createDiv("Create Board")
    const name = utils.createInput("Name:", "text", "idName", undefined, true)
    const desc = utils.createInputTextArea("Description:", "idDescription", true)
    const form = utils.createForm([
        utils.createParagraph([name.label, name.input]),
        utils.createParagraph([desc.label, desc.input]),
        utils.createButton("Create"),
        utils.liWithRef("Back", `#users`)
    ])
    form.addEventListener('submit', submits.createBoardSubmit)
    div.appendChild(form)
    mainContent.replaceChildren(div)
}

function searchBoardsByName(mainContent) {
    const div = utils.createDiv("Search Boards")
    const searchBar = utils.createInput("Name:", "text", "idName", undefined)
    const form = utils.createForm([searchBar.label, searchBar.input, utils.createButton("Search")])
    form.addEventListener('submit', submits.searchSubmit)
    div.append(form, utils.liWithRef("Home", "#users"))
    mainContent.replaceChildren(div)
}

function getBoards(mainContent, path) {
    dataFetch(path)
        .then(boards => {
            utils.checkError(boards)
            const div = utils.createDiv("Boards")
            const [previous, next] = utils.createPreviousAndNextButtons(path, boards.length)
            const b = boards.map(b => utils.liWithRef(b.name, "#boards/" + b.id))
            div.append(previous, next, utils.ul(b.concat(utils.liWithRef("Back", "#boards/search"))))
            mainContent.replaceChildren(div)
        })
}

function getBoard(mainContent, path) {
    dataFetch(path)
        .then(board => {
            utils.checkError(board)
            const div = utils.createDiv("Board Details")
            const lists = board.lists.map(l => utils.liWithRef(`${l.name}`, `#lists/${l.id}?boardId=${board.id}`))
            const ulBoard = utils.ul([
                utils.li("Name: " + board.name),
                utils.li("Description: " + board.description),
                utils.liWithRef("Users", `#boards/${board.id}/users`),
                utils.createElementList("Lists:", lists),
                utils.liWithRef("Create List", "#createlists/" + board.id),
                utils.liWithRef("Archived Cards", `#cards/archived/${board.id}?skip=0&limit=3`),
                utils.liWithRef("Boards", "#boards/search")
            ])
            div.appendChild(ulBoard)
            mainContent.replaceChildren(div)
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function getUsersOfBoard(mainContent, path) {
    dataFetch(path)
        .then(uBoard => {
            utils.checkError(uBoard)
            const div = utils.createDiv("Users of Board " + uBoard[0].second)
            const users = uBoard.map(uB => utils.li("User " + uB.first))
            const board = utils.liWithRef("Board", "#boards/" + uBoard[0].second)
            div.appendChild(utils.ul(users.concat([board])))
            mainContent.replaceChildren(div)
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function createList(mainContent, path) {
    const boardId = path.match(/\d+/)[0]
    const div = utils.createDiv("Create List")
    const name = utils.createInput("Name:", "text", "idName", undefined, true)
    const form = utils.createForm([
        utils.createParagraph([name.label, name.input]),
        utils.createInput(undefined, "hidden", "idBoardId", boardId),
        utils.createButton("Create"),
        utils.liWithRef("Back", `#boards/${boardId}`)
    ])
    form.addEventListener('submit', submits.createListSubmit)
    div.appendChild(form)
    mainContent.replaceChildren(div)
}

function getList(mainContent, path) {
    dataFetch(path)
        .then(list => {
            utils.checkError(list)
            const div = utils.createDiv("List Details")
            let cards = list.cards.map(c => utils.liWithRef(`${c.name}`, `#cards/${c.id}?listId=${list.id}`))
            div.appendChild(utils.ul([
                utils.li("Name: " + list.name),
                utils.createElementList("Cards:", cards),
                utils.liWithRef("Create Card", `#createcards/${list.boardId}/${list.id}`),
                utils.liWithRef("Delete List", `#lists/${list.id}/${list.boardId}`),
                utils.liWithRef("Board", "#boards/" + list.boardId)
            ]))
            mainContent.replaceChildren(div)
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function deleteList(mainContent, path) {
    dataFetch(path, "DELETE")
        .then(res =>{
            utils.checkError(res)
            window.location.hash = `boards/${path.match(/\d+/g)[1]}`
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function createCard(mainContent, path) {
    const boardId = path.match(/\d+/g)[0]
    const listId = path.match(/\d+/g)[1]
    const div = utils.createDiv("Create Card")
    const name = utils.createInput("Name:", "text", "idName", undefined, true)
    const desc = utils.createInputTextArea("Description:", "idDescription")
    const date = utils.createInput("Conclusion Date(YYYY-MM-DD):", "date", "idDate", undefined)
    const form = utils.createForm([
        utils.createParagraph([name.label, name.input]),
        utils.createParagraph([desc.label, desc.input]),
        utils.createParagraph([date.label, date.input]),
        utils.createInput(undefined, "hidden", "idBoardId", boardId),
        utils.createInput(undefined, "hidden", "idListId", listId),
        utils.createButton("Create"),
        utils.liWithRef("Back", `#lists/${listId}?boardId=${boardId}`)
    ])
    form.addEventListener('submit', submits.createCardSubmit)
    div.appendChild(form)
    mainContent.replaceChildren(div)
}

function getCard(mainContent, path) {
    dataFetch(path)
        .then(card => {
            utils.checkError(card)
            const div = utils.createDiv("Card Details")
            const ulCard = utils.ul([
                utils.li("Name: " + card.name),
                utils.li("Description: " + card.description),
                utils.li("Creation Date: " + card.creationDate),
                utils.li("Conclusion Date: " + (card.conclusionDate || "No conclusion date")),
                utils.liWithRef("Move Card", `#cards/move/${card.id}/${card.listId}/${card.boardId}`),
                utils.liWithRef("Archive this card", `#cards/archived/${card.id}/${card.listId}/${card.boardId}`),
                utils.liWithRef("List", `#lists/${card.listId}?boardId=${card.boardId}`)
            ])
            div.appendChild(ulCard)
            mainContent.replaceChildren(div)
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function moveCard(mainContent, path) {
    const args = path.split("/")
    const cardId = parseInt(args[2])
    const oldListId = parseInt(args[3])
    const boardId = parseInt(args[4])
    dataFetch(`lists?boardId=${boardId}`)
        .then(lists => {
            utils.checkError(lists)
            lists = lists.filter(l => l.id != oldListId)
            const div = utils.createDiv("Move Card")
            const list = utils.createSelect("New List: ", "idFirstSelect", lists)
            list.select.onclick = () => { utils.updateSelect(lists) }
            const cix = utils.createSelect("Cix: ", "idSecondSelect")
            const button = utils.createButton()
            const lIdOld = utils.createInput(undefined, "hidden", "idOldListId", `${oldListId}`)
            const bId = utils.createInput(undefined, "hidden", "idBoardId", `${boardId}`)
            const cId = utils.createInput(undefined, "hidden", "idCardId", `${cardId}`)
            const form = utils.createForm([
                list.label,
                list.select,
                cix.label,
                cix.select,
                lIdOld,
                bId,
                cId,
                utils.createParagraph([button]),
                utils.liWithRef("Go Back", `#cards/${cardId}?listId=${oldListId}`)
            ])
            form.addEventListener('submit', submits.moveCardSubmit)
            div.append(form)
            mainContent.replaceChildren(div)
        })
}

function getArchivedCards(mainContent, path) {
    const args = path.split("/")
    const boardId = args[2]
    dataFetch(path)
        .then(cards => {
            utils.checkError(cards)
            const div = utils.createDiv("Archived Cards")
            const [previous, next] = utils.createPreviousAndNextButtons(path, cards.length)
            const c = cards.map(c => utils.liWithRef(c.name, `#cards/archived/${c.id}/${c.boardId}`))
            div.append(previous, next, utils.ul(c),utils.ul(c.concat(utils.liWithRef("Back", `#boards/${boardId}`))))
            mainContent.replaceChildren(div)
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function getArchivedCard(mainContent, path) {
    dataFetch(path)
        .then(card => {
            utils.checkError(card)
            const div = utils.createDiv("Archived Card Details")
            const ulCard = utils.ul([
                utils.li("Name: " + card.name),
                utils.li("Description: " + card.description),
                utils.li("Creation Date: " + card.creationDate),
                utils.li("Conclusion Date: " + (card.conclusionDate || "No conclusion date")),
                utils.liWithRef("Unarchive this card", `#cards/unarchive/${card.id}/${card.boardId}`),
                utils.liWithRef("Board", `#boards/${card.boardId}`)
            ])
            div.appendChild(ulCard)
            mainContent.replaceChildren(div)
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

function archiveCard(mainContent, path) {
    const boardId = path.split("/")[4]
    dataFetch(path, "PUT")
        .then(cardId => {
            utils.checkError(cardId)
            window.location.hash = `cards/archived/${cardId}/${boardId}`
        })
        .catch(error => {
            window.location.hash = `error/${error.status}/${error.message}`
        })
}

function unarchiveCard(mainContent, path) {
    const args = path.split("/")
    const cardId = parseInt(args[2])
    const boardId = parseInt(args[3])
    dataFetch(`lists?boardId=${boardId}`)
        .then(lists => {
            utils.checkError(lists)
            const div = utils.createDiv("Unarchive Card")
            const list = utils.createSelect("New List: ", "idSelect", lists)
            const button = utils.createButton()
            const bId = utils.createInput(undefined, "hidden", "idBoardId", `${boardId}`)
            const cId = utils.createInput(undefined, "hidden", "idCardId", `${cardId}`)
            const form = utils.createForm([
                list.label,
                list.select,
                bId,
                cId,
                utils.createParagraph([button]),
                utils.liWithRef("Go Back", `#cards/archived/${cId}/${bId}`)
            ])
            form.addEventListener('submit', submits.unarchiveSubmit)
            div.append(form)
            mainContent.replaceChildren(div)
        }).catch(error => {
        window.location.hash = `error/${error.status}/${error.message}`
    })
}

export const handlers = {
    getHome: getHome,
    error: error,
    logout: logout,
    getUser: getUser,
    getUsers: getUsers,
    createBoard: createBoard,
    searchBoardsByName: searchBoardsByName,
    getBoards: getBoards,
    getBoard: getBoard,
    getUsersOfBoard: getUsersOfBoard,
    createList: createList,
    getList: getList,
    deleteList: deleteList,
    createCard: createCard,
    getCard: getCard,
    moveCard: moveCard,
    getArchivedCards: getArchivedCards,
    getArchivedCard: getArchivedCard,
    archiveCard: archiveCard,
    unarchiveCard: unarchiveCard
}

export default handlers