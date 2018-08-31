package xlab.world.xlab.di

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import xlab.world.xlab.utils.rx.ApplicationSchedulerProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.view.login.LoginViewModel
import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import xlab.world.xlab.server.ApiURL
import xlab.world.xlab.server.ApiUser
import xlab.world.xlab.server.ApiUserImpl
import xlab.world.xlab.server.`interface`.IUserRequest
import xlab.world.xlab.utils.ViewFunction
import java.util.concurrent.TimeUnit

/**
 *  Koin main module
 */
val viewModelModule: Module = applicationContext {
    // provided common view function
    bean { ViewFunction() }

    // ViewModel for Login View
    viewModel { LoginViewModel(get()) }
}

val rxModule: Module = applicationContext {
    // provided components
    bean { ApplicationSchedulerProvider() as SchedulerProvider }
}

val remoteModule: Module = applicationContext {
    // provided web components
    bean { createOkHttpClient() }
    // user api interface
    bean { createRetrofit<IUserRequest>(client = get(), baseUrl = ApiURL.XLAB_API_URL_SSL) }
    // user api implement
    bean { ApiUserImpl(get()) as ApiUser }
}

fun createOkHttpClient(): OkHttpClient {
    val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()

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
    val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    return retrofit.create(T::class.java)
}

/**
 * Module list
 */
val appModule = listOf(viewModelModule, rxModule, remoteModule)