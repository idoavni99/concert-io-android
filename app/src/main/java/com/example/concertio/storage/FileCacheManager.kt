package com.example.concertio.storage

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL


object FileCacheManager {
    private lateinit var cacheDir: File;

    fun init(context: Context) {
        this.cacheDir = context.cacheDir
        cacheDir.mkdirs()
    }

    suspend fun clearCacheAsync() = withContext(Dispatchers.IO) {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    suspend fun getFileLocalUri(remoteUrl: URL) =
        withContext(Dispatchers.IO) {
            val transformedUrl = getKeyByUrl(remoteUrl)
            getCachedFileUri(transformedUrl) ?: cacheFile(remoteUrl, transformedUrl)
        }

    private fun getKeyByUrl(url: URL) = url.toString().replace("/", "~")

    private fun getCachedFileUri(key: String): Uri? {
        val file = File(cacheDir, key)
        return if (file.exists()) {
            file.toUri()
        } else {
            null
        }
    }

    private fun cacheFile(remoteUrl: URL, key: String): Uri {
        val fileHandle = File(cacheDir, key)
        remoteUrl.openStream().use {
            FileOutputStream(fileHandle).use { outputStream ->
                it.copyTo(outputStream)
            }
        }
        return fileHandle.toUri()
    }
}