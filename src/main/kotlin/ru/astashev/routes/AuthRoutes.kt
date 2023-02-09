package ru.astashev.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.astashev.data.UserDataSource
import ru.astashev.data.models.User
import ru.astashev.data.models.requests.AuthRequest
import ru.astashev.security.hashing.HashingService

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