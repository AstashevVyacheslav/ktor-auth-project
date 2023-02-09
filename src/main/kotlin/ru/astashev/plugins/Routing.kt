package ru.astashev.plugins


import io.ktor.server.application.*
import io.ktor.server.routing.*
import ru.astashev.data.UserDataSource
import ru.astashev.routes.signIn
import ru.astashev.routes.signUp
import ru.astashev.security.hashing.HashingService
import ru.astashev.security.token.JwtTokenService
import ru.astashev.security.token.TokenConfig

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: JwtTokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signUp(hashingService =hashingService, userDataSource = userDataSource)
        signIn(userDataSource,hashingService,tokenService,tokenConfig)
    }
}
