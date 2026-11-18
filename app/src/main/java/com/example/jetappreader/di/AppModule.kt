package com.example.jetappreader.di

import android.util.Log
import com.example.jetappreader.network.BookApi
import com.example.jetappreader.repository.BookRepository
import com.example.jetappreader.repository.FireRepository
import com.example.jetappreader.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val apiKeyInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            val urlBuilder = originalUrl.newBuilder()
            if (originalUrl.queryParameter("key") == null && Constants.API_KEY.isNotBlank()) {
                urlBuilder.addQueryParameter("key", Constants.API_KEY)
            }
            val newRequest = originalRequest.newBuilder()
                .url(urlBuilder.build())
                .build()
            chain.proceed(newRequest)
        }

        val rateLimitRetryInterceptor = Interceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)

            // If an API key is attached and results in HTTP 403 (Quota Exceeded / Forbidden) or 429,
            // fallback to an unauthenticated request without the 'key' query parameter.
            if (!response.isSuccessful && (response.code == 403 || response.code == 429)) {
                val url = request.url
                if (url.queryParameter("key") != null) {
                    response.close()
                    val newUrl = url.newBuilder().removeAllQueryParameters("key").build()
                    val newRequest = request.newBuilder().url(newUrl).build()
                    response = chain.proceed(newRequest)
                }
            }

            var retryCount = 0
            val maxRetries = 2
            while (!response.isSuccessful && response.code == 429 && retryCount < maxRetries) {
                retryCount++
                response.close()
                try {
                    Thread.sleep(1000L * retryCount)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
                response = chain.proceed(request)
            }
            response
        }

        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(rateLimitRetryInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideBookApi(okHttpClient: OkHttpClient): BookApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBookRepository(api: BookApi): BookRepository = BookRepository(api)

    @Provides
    @Singleton
    fun provideFireRepository(queryBook: Query): FireRepository = FireRepository(queryBook)

    @Provides
    @Singleton
    fun provideBookQuery(): Query {
        return try {
            FirebaseFirestore.getInstance().collection("books")
        } catch (e: Exception) {
            Log.e("AppModule", "Error providing BookQuery: ${e.message}")
            FirebaseFirestore.getInstance().collection("books")
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("AppModule", "Error providing FirebaseAuth: ${e.message}")
            FirebaseAuth.getInstance()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("AppModule", "Error providing FirebaseFirestore: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }
}
