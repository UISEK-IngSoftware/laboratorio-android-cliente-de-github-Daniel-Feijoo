package ec.edu.uisek.githubclient.services

import android.util.Log
import ec.edu.uisek.githubclient.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private const val BASE_URL = "https://api.github.com/"

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = BuildConfig.GITHUB_API_TOKEN

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "token $token") // GitHub expects "token" prefix
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build()
        } else {
            Log.w(TAG, "⚠️ Token de GitHub NO configurado")
            originalRequest.newBuilder()
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build()
        }

        chain.proceed(newRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val gitHubApiService: GithubApiService by lazy {
        retrofit.create(GithubApiService::class.java)
    }
}