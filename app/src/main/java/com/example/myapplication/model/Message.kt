package com.example.myapplication.model

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val imageUrl: String? = null
)
