package xlab.world.xlab.server

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import xlab.world.xlab.data.response.BaseErrorData
import java.util.concurrent.TimeUnit

fun createOkHttpClient(): OkHttpClient {
    val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)

    httpClient.addInterceptor { chain ->
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
                .header("Host", "app.host")
                .header("Content-Type", "application/json")
                .method(original.method(), original.body())
                .build()
        chain.proceed(request)
    }

    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    return httpClient
            .connectTimeout(60L, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()
}

inline fun <reified T> createRetrofit(client: OkHttpClient, baseUrl: String): T {
    return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(T::class.java)
}

inline fun <reified T>errorHandle(error: Throwable): T? {
    return if (error is HttpException) {
        try {
            val errorData = Gson().fromJson<T>(error.response().errorBody()?.charStream(), T::class.java)
            (errorData as BaseErrorData).errorCode = error.code()
            errorData
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}
