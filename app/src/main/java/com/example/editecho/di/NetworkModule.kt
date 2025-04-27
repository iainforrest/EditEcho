package com.example.editecho.di

import com.example.editecho.BuildConfig
import com.example.editecho.network.ChatCompletionClient
import com.example.editecho.network.OpenAiChatApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides 
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                        .build()
                )
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json { 
            ignoreUnknownKeys = true 
            isLenient = true
        }
    }

    @Provides 
    @Singleton
    fun provideOpenAiChatApi(client: OkHttpClient, json: Json): OpenAiChatApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenAiChatApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatCompletionClient(api: OpenAiChatApi, json: Json): ChatCompletionClient {
        return ChatCompletionClient(api, json)
    }
} 