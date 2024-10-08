package pt.isel.ls.http

import org.http4k.core.Request
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import pt.isel.ls.management.api.DataPostgres
import pt.isel.ls.services.Services
import pt.isel.ls.webApi.WebApi

private val logger = LoggerFactory.getLogger("pt.isel.ls.http.HTTPServer")
private val renderUrl = "jdbc:postgresql://dpg-chln0k67avj217fb1cm0-a\n" +
    "\n.frankfurt-postgres.render.com:5432\n" +
    "\n/db_ls_2223_2_42d_g11\n" +
    "\n?user=db_ls_2223_2_42d_g11_user\n" +
    "\n&password=nJAxgcX4dgOuIaY85awPquEOVW3m8cXL"

fun logRequest(request: Request) {
    request.query("name")
    logger.info(
        "incoming request: method={}, uri={}, content-type={} accept={}",
        request.method,
        request.uri,
        request.header("content-type"),
        request.header("accept")
    )
}

class HTTPServer(webApi: WebApi) {
    private val w = webApi

    fun start() {
        val app = w.getRoutes()
        val jettyServer = app.asServer(Jetty(9000)).start()
        logger.info("server started listening")
        readln()
        jettyServer.stop()
    }
}

fun main() {
    val dataSource = PGSimpleDataSource()
    val dbUrl = System.getenv("JDBC_DATABASE_URL")
    dataSource.setURL(
        dbUrl
        // "jdbc:postgresql://localhost/postgres?user=postgres&password=lsg11"
    )
    val dataBase = DataPostgres(dataSource)
    val services = Services(dataBase)
    val webApi = WebApi(services)
    val server = HTTPServer(webApi)

    server.start()

    logger.info("leaving Main")
}
