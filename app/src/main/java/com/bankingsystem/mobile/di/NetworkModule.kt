package com.bankingsystem.mobile.di

import com.bankingsystem.mobile.BuildConfig
import com.bankingsystem.mobile.data.local.AuthStore
import com.bankingsystem.mobile.data.storage.TokenManager
import com.bankingsystem.mobile.data.service.ApiService
import com.bankingsystem.mobile.data.service.KycApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @IODispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    class UnauthorizedHandler(
        private val tokenManager: TokenManager,
        private val io: CoroutineDispatcher
    ) {
        fun onUnauthorized() {
            CoroutineScope(io).launch {
                runCatching { tokenManager.clearToken() }
            }
        }
    }

    class AuthInterceptor(
        private val authStore: AuthStore,
        private val unauthorizedHandler: UnauthorizedHandler
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token: String? = runBlocking { authStore.getToken() }

            val req = chain.request().newBuilder().apply {
                if (!token.isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                }
            }.build()

            val resp = chain.proceed(req)
            if (resp.code == 401) {
                unauthorizedHandler.onUnauthorized()
            }
            return resp
        }
    }

    @Provides
    @Singleton
    fun provideUnauthorizedHandler(
        tokenManager: TokenManager,
        @IODispatcher io: CoroutineDispatcher
    ) = UnauthorizedHandler(tokenManager, io)

    @Provides
    @Singleton
    fun provideOkHttp(
        authStore: AuthStore,
        unauthorizedHandler: UnauthorizedHandler
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(AuthInterceptor(authStore, unauthorizedHandler))
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG)
                        HttpLoggingInterceptor.Level.BASIC
                    else
                        HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()


    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideKycApi(retrofit: Retrofit): KycApi =
        retrofit.create(KycApi::class.java)
}
