package com.travelai.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    const val BASE_URL: String = "https://api.deepseek.com/"

    /**
     * Build a shared OkHttpClient with the DeepSeek auth header injected.
     *
     * `readTimeout` is set to 0 (infinite) so SSE streaming calls can hold the
     * connection open without OkHttp killing the read. The blocking call
     * timeout (`callTimeout`) is also 0 for the same reason — non-streaming
     * code paths impose their own `withTimeout` at the ViewModel layer.
     */
    fun createOkHttpClient(apiKey: String): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val requestBuilder = chain.request()
                .newBuilder()
                .addHeader("Content-Type", "application/json")

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    fun create(apiKey: String): DeepSeekApi = create(createOkHttpClient(apiKey))

    fun create(client: OkHttpClient): DeepSeekApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DeepSeekApi::class.java)
}
