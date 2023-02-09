package ru.astashev

import io.ktor.server.application.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import ru.astashev.data.MongoUserDataSource
import ru.astashev.plugins.*
import ru.astashev.security.hashing.SHA256HashingService
import ru.astashev.security.token.JwtTokenService
import ru.astashev.security.token.TokenConfig

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val mongoPassword = System.getenv("MONGO_PASSWORD")
    val dbname = "ktor-notes"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://sysdba:$mongoPassword@cluster0.bdoucou.mongodb.net/$dbname?retryWrites=true&w=majority"
    )
        .coroutine
        .getDatabase(dbname)
    val userDataSource = MongoUserDataSource(db)
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        exiresIn = 365L * 1000L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()
    configureMonitoring()
    configureSerialization()
    configureSecurity(tokenConfig)
    configureRouting(
        userDataSource = userDataSource,
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = tokenConfig
    )
}
