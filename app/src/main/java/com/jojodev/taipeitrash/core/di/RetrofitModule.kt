package com.jojodev.taipeitrash.core.di

import com.jojodev.taipeitrash.core.HsinchuApiService
import com.jojodev.taipeitrash.core.TaipeiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    @TaipeiRetrofit
    fun provideTaipeiBaseUrl(): String = "https://data.taipei/api/v1/dataset/"

    @Provides
    @Singleton
    @HsinchuRetrofit
    fun provideHsinchuBaseUrl(): String = "https://7966.hccg.gov.tw/WEB/_IMP/API/CleanWeb/"

    private fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @TaipeiRetrofit
    fun provideTaipeiRetrofit(@TaipeiRetrofit baseUrl: String): Retrofit {
        val networkJson = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(provideOkHttpClient())
            .addConverterFactory(
                networkJson.asConverterFactory(
                "application/json; charset=UTF8".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @HsinchuRetrofit
    fun provideHsinchuRetrofit(@HsinchuRetrofit baseUrl: String): Retrofit {
        val networkJson = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(provideOkHttpClient())
            .addConverterFactory(
                networkJson.asConverterFactory(
                "application/json; charset=UTF8".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideTrashApiService(@TaipeiRetrofit retrofit: Retrofit): TaipeiApiService {
        return retrofit.create(TaipeiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideHsinchuApiService(@HsinchuRetrofit retrofit: Retrofit): HsinchuApiService {
        return retrofit.create(HsinchuApiService::class.java)
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TaipeiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HsinchuRetrofit
