package com.editecho.di

import android.app.NotificationManager
import android.content.Context
import com.editecho.data.SettingsRepository
import com.editecho.network.AssistantApiClient
import com.editecho.network.WhisperRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideWhisperRepository(): WhisperRepository {
        return WhisperRepository()
    }

    @Provides
    @Singleton
    fun provideAssistantApiClient(): AssistantApiClient {
        return AssistantApiClient()
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
} 