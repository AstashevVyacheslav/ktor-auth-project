package ru.astashev.security.hashing

interface HashingService {
    fun generateSaltedHash(value: String, saltLeight: Int = 32): SaltedHash

    fun verify(value: String, saltedHash: SaltedHash): Boolean
}