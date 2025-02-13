package com.example.concertio.storage

data class CachedFile(
    val key: String,
    val localPath: String,
    val lastAccessed: Long = System.currentTimeMillis()
)
