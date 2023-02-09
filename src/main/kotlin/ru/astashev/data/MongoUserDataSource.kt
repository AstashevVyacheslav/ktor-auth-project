package ru.astashev.data

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import ru.astashev.data.models.User

class MongoUserDataSource (
    db:CoroutineDatabase
):UserDataSource{

    private val users = db.getCollection<User>()
    override suspend fun getUserByUsername(username: String): User? =
        users.findOne(User::username eq username)

    override suspend fun insertUser(user: User): Boolean =
        users.insertOne(user).wasAcknowledged()

}