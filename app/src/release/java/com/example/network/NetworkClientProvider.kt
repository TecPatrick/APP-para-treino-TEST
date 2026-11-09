package com.example.network

import okhttp3.OkHttpClient

object NetworkClientProvider {
    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }
}
