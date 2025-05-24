package com.editecho.di

import com.editecho.BuildConfig
import com.editecho.network.ChatCompletionClient
import com.editecho.network.ClaudeApi
import com.editecho.network.ClaudeCompletionClient
import com.editecho.network.OpenAiChatApi
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
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenAiClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClaudeClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides 
    @Singleton
    @OpenAiClient
    fun provideOpenAiOkHttpClient(): OkHttpClient {
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
    @ClaudeClient
    fun provideClaudeOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader("x-api-key", BuildConfig.CLAUDE_API_KEY)
                        .addHeader("anthropic-version", "2023-06-01")
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
            encodeDefaults = true
        }
    }

    @Provides 
    @Singleton
    fun provideOpenAiChatApi(@OpenAiClient client: OkHttpClient, json: Json): OpenAiChatApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenAiChatApi::class.java)
    }

    @Provides 
    @Singleton
    fun provideClaudeApi(@ClaudeClient client: OkHttpClient, json: Json): ClaudeApi {
        return Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ClaudeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatCompletionClient(api: OpenAiChatApi, json: Json): ChatCompletionClient {
        return ChatCompletionClient(api, json)
    }

    @Provides
    @Singleton
    fun provideClaudeCompletionClient(api: ClaudeApi, json: Json): ClaudeCompletionClient {
        return ClaudeCompletionClient(api, json)
    }
} 