package ru.astashev.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.astashev.data.UserDataSource
import ru.astashev.data.models.User
import ru.astashev.data.models.requests.AuthRequest
import ru.astashev.data.models.responses.AuthResponse
import ru.astashev.security.hashing.HashingService
import ru.astashev.security.hashing.SaltedHash
import ru.astashev.security.token.JwtTokenService
import ru.astashev.security.token.TokenClaim
import ru.astashev.security.token.TokenConfig

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("signup") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            return@post
        }
        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPasswordShort = request.password.length < 8

        if (userDataSource.getUserByUsername(request.username)!=null){
            call.respond(HttpStatusCode.Conflict,"Useraname is already exists")
            return@post
        }

        if (areFieldsBlank||isPasswordShort){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = userDataSource.insertUser(user)//если все проверки прошли генерируем пользователя
        if (!wasAcknowledged){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }else{
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: JwtTokenService,
    tokenConfig: TokenConfig
){
    post ("signin"){
        val request = call.receiveOrNull<AuthRequest>()?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val user = userDataSource.getUserByUsername(request.username)
        if (user == null){
            call.respond(HttpStatusCode.Conflict,"Incorrect username or passsword")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword){
            call.respond(HttpStatusCode.Conflict,"Incorrect username or passsword")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}