package com.travelai.data.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object VisionApiClient {
    fun create(baseUrl: String, apiKey: String): VisionApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                if (apiKey.isNotBlank()) {
                    builder.addHeader("Authorization", "Bearer $apiKey")
                }
                val request = builder.build()
                val response = chain.proceed(request)
                // Log request structure (with base64 stripped) on error for diagnostics.
                if (!response.isSuccessful) {
                    runCatching {
                        val buffer = Buffer()
                        request.body?.writeTo(buffer)
                        val raw = buffer.readUtf8()
                        val sanitized = raw.replace(
                            Regex("\"url\"\\s*:\\s*\"data:[^\"]+\""),
                            "\"url\":\"data:image/jpeg;base64,<stripped>\""
                        )
                        Log.e("VisionAPI", "HTTP ${response.code} — request body: ${sanitized.take(600)}")
                    }
                }
                response
            }
            .build()

        // registerTypeHierarchyAdapter so the adapter is invoked for every
        // concrete subtype of VisionContentPart (Text / Image), even when the
        // declared field type is Any. With registerTypeAdapter Gson would fall
        // back to reflection for subclasses and drop the required "type" field.
        val gson = GsonBuilder()
            .registerTypeHierarchyAdapter(VisionContentPart::class.java, VisionContentPartAdapter())
            .create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(VisionApi::class.java)
    }
}
