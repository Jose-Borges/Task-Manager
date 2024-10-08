import router from "./router.js";
import handlers from "./handlers.js";

window.addEventListener('load', loadHandler)
window.addEventListener('hashchange', hashChangeHandler)

function loadHandler(){
    router.addRouteHandler("home", handlers.getHome)
    router.addRouteHandler("error/:status/:message", handlers.error)
    router.addRouteHandler("logout", handlers.logout)
    router.addRouteHandler("users", handlers.getUsers)
    router.addRouteHandler("users/:id", handlers.getUser)
    router.addRouteHandler("boards/search", handlers.searchBoardsByName)
    router.addRouteHandler("boards", handlers.getBoards)
    router.addRouteHandler("boards/:id", handlers.getBoard)
    router.addRouteHandler("boards/:boardId/users", handlers.getUsersOfBoard)
    router.addRouteHandler("createboards", handlers.createBoard)
    router.addRouteHandler("lists/:id", handlers.getList)
    router.addRouteHandler("createlists/:boardId", handlers.createList)
    router.addRouteHandler("lists/:listId/:boardId", handlers.deleteList)
    router.addRouteHandler("cards/:id", handlers.getCard)
    router.addRouteHandler("createcards/:boardId/:listId", handlers.createCard)
    router.addRouteHandler("cards/move/:id/:listId/:boardId", handlers.moveCard)
    router.addRouteHandler("cards/archived/:boardId", handlers.getArchivedCards)
    router.addRouteHandler("cards/archived/:id/:boardId", handlers.getArchivedCard)
    router.addRouteHandler("cards/archived/:id/:listId/:boardId", handlers.archiveCard)
    router.addRouteHandler("cards/unarchive/:id/:boardId", handlers.unarchiveCard)
    router.addDefaultNotFoundRouteHandler(() => window.location.hash = "home")
    hashChangeHandler()
}

function hashChangeHandler(){

    const mainContent = document.getElementById("mainContent")
    const path =  window.location.hash.replace("#", "")

    const handler = router.getRouteHandler(path)
    handler(mainContent, path)
}