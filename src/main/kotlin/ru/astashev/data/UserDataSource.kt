package ru.astashev.data

import ru.astashev.data.models.User

interface UserDataSource {
    suspend fun getUserByUsername(username: String): User?

    suspend fun insertUser(user: User): Boolean
}