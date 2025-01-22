package com.example.concertio.data

class ValidationResult(e: IllegalArgumentException? = null) {
    val success = e == null
    val message = e?.message
}
