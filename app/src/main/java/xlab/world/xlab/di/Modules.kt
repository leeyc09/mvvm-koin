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
import xlab.world.xlab.server.*
import xlab.world.xlab.server.`interface`.IUserRequest
import xlab.world.xlab.server.provider.ApiUser
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.preload.PreloadViewModel
import xlab.world.xlab.view.register.RegisterViewModel
import java.util.concurrent.TimeUnit

/**
 *  Koin main module
 */
val baseModule: Module = applicationContext {
    // provided rx scheduler
    bean { ApplicationSchedulerProvider() as SchedulerProvider }
    // provided network check
    bean { NetworkCheck(context = get()) }
    // provided social auth
    bean { SocialAuth() }
    // provided shared preference
    bean { SPHelper(context = get()) }
    // provided pet information
    bean { PetInfo(context = get()) }
    // provided font color span
    bean { FontColorSpan(context = get()) }
    // ViewModel for PreLoad
    viewModel { PreloadViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Login View
    viewModel { LoginViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Register View
    viewModel { RegisterViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
}

val utilModule: Module = applicationContext {
    // provided view function
    bean { ViewFunction() }
}

val remoteModule: Module = applicationContext {
    // provided web components
    bean { createOkHttpClient() }
    // user api interface
    bean { createRetrofit<IUserRequest>(client = get(), baseUrl = ApiURL.XLAB_API_URL_SSL) } //XLAB_API_URL_SSL
    // user api implement
    bean { ApiUser(iUserRequest = get()) as ApiUserProvider }
}

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

/**
 * Module list
 */
val appModule = listOf(baseModule, utilModule, remoteModule)