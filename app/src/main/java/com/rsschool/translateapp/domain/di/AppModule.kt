package com.rsschool.translateapp.domain.di

import android.app.Application
import androidx.room.Room
import com.rsschool.translateapp.data.local.TranslationBookmarkDatabase
import com.rsschool.translateapp.data.remote.TranslatorApi
import com.rsschool.translateapp.data.repository.TranslationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTranslatorApi(): TranslatorApi {
        return Retrofit.Builder()
            .baseUrl(TranslatorApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(TranslatorApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTranslationRepository(
        api: TranslatorApi,
        db: TranslationBookmarkDatabase
    ): TranslationRepository {
        return TranslationRepository(api,db.bookmarksDao)
    }

    @Provides
    @Singleton
    fun provideBookmarksDatabase(app: Application): TranslationBookmarkDatabase {
        return Room.databaseBuilder(
                app.applicationContext,
                TranslationBookmarkDatabase::class.java,
            "translationBookmarksDB"
                )
            .fallbackToDestructiveMigration()
            .build()
    }
}