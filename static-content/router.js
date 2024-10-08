const routes = []
let notFoundRouteHandler = () => { throw "Route handler for unknown routes not defined" }

function addRouteHandler(path, handler){
    routes.push({path, handler})
}
function addDefaultNotFoundRouteHandler(notFoundRH) {
    notFoundRouteHandler = notFoundRH
}

function getRouteHandler(path){
    const route = routes.find(r => comparePaths(r.path, path) || compareQuery(r.path, path))
    return route ? route.handler : notFoundRouteHandler
}

function comparePaths(pattern, path) {
    const patternParts = pattern.split("/");
    const pathParts = path.split("/");
    if (patternParts.length !== pathParts.length) {
        return false;
    }
    for (let i = 0; i < patternParts.length; i++) {
        if (patternParts[i].startsWith(":")) {
            continue;
        }
        if (patternParts[i] !== pathParts[i]) {
            return false;
        }
    }
    return true;
}

function compareQuery(pattern, path) {
    const pathParts = path.split("?");
    return pattern === pathParts[0];

}

const router = {
    addRouteHandler,
    getRouteHandler,
    addDefaultNotFoundRouteHandler
}

export default router