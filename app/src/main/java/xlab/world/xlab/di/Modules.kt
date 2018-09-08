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
import xlab.world.xlab.view.onBoarding.OnBoardingViewModel
import xlab.world.xlab.view.register.RegisterViewModel
import xlab.world.xlab.view.resetPassword.ResetPasswordViewModel
import java.util.concurrent.TimeUnit

/**
 *  Koin main module
 */
val baseModule: Module = applicationContext {
    // provided rx scheduler
    bean { ApplicationSchedulerProvider() as SchedulerProvider }
    // provided network check
    bean { NetworkCheck(context = get()) }
    // provided shared preference
    bean { SPHelper(context = get()) }
    // provided pet information
    bean { PetInfo(context = get()) }
    // provided font color span
    bean { FontColorSpan(context = get()) }
    // provided letter or digit input filter
    bean { LetterOrDigitInputFilter() }
    // provided data regex
    bean { DataRegex() }
    // provided run activity
    bean { RunActivity() }
    // ViewModel for OnBoarding View
    viewModel { OnBoardingViewModel(scheduler = get()) }
    // ViewModel for Login View
    viewModel { LoginViewModel(apiUser = get(), dataRegex = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Register View
    viewModel { RegisterViewModel(apiUser = get(), dataRegex = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for reset password
    viewModel { ResetPasswordViewModel(apiUser = get(), dataRegex = get(), networkCheck = get(), scheduler = get()) }
}

val utilModule: Module = applicationContext {
    // provided view function
    bean { ViewFunction() }
}

val remoteModule: Module = applicationContext {
    // provided web components
    bean { createOkHttpClient() }
    // user api interface
    bean { createRetrofit<IUserRequest>(client = get(), baseUrl = ApiURL.XLAB_API_URL_SSL) }
    // user api implement
    bean { ApiUser(iUserRequest = get()) as ApiUserProvider }
}

/**
 * Module list
 */
val appModule = listOf(baseModule, utilModule, remoteModule)