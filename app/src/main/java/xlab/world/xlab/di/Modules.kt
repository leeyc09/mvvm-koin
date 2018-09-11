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
import xlab.world.xlab.server.`interface`.IPetRequest
import xlab.world.xlab.server.`interface`.IPostRequest
import xlab.world.xlab.server.`interface`.IUserRequest
import xlab.world.xlab.server.provider.*
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.main.MainViewModel
import xlab.world.xlab.view.onBoarding.OnBoardingViewModel
import xlab.world.xlab.view.register.RegisterViewModel
import xlab.world.xlab.view.resetPassword.ResetPasswordViewModel
import xlab.world.xlab.view.topicSetting.TopicSettingViewModel
import java.util.concurrent.TimeUnit

/**
 *  Koin main module
 */
val baseModule: Module = applicationContext {
    // provided rx scheduler
    bean { ApplicationSchedulerProvider() as SchedulerProvider }
    // ViewModel for OnBoarding View
    viewModel { OnBoardingViewModel(scheduler = get()) }
    // ViewModel for Login View
    viewModel { LoginViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Register View
    viewModel { RegisterViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for reset password
    viewModel { ResetPasswordViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for main
    viewModel { MainViewModel(apiPost = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Topic Setting
    viewModel { TopicSettingViewModel(apiPet = get(), networkCheck = get(), scheduler = get()) }
}

val utilModule: Module = applicationContext {
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
}

val remoteModule: Module = applicationContext {
    // provided web components
    bean { createOkHttpClient() }

    // user api interface
    bean { createRetrofit<IUserRequest>(client = get(), baseUrl = ApiURL.XLAB_API_URL) }
    // user api implement
    bean { ApiUser(iUserRequest = get()) as ApiUserProvider }

    // pet api interface
    bean { createRetrofit<IPetRequest>(client = get(), baseUrl = ApiURL.XLAB_API_URL) }
    // pet api implement
    bean { ApiPet(iPetRequest = get()) as ApiPetProvider }

    // post api interface
    bean { createRetrofit<IPostRequest>(client = get(), baseUrl = ApiURL.XLAB_API_URL) }
    // post api implement
    bean { ApiPost(iPostRequest = get()) as ApiPostProvider }
}

/**
 * Module list
 */
val appModule = listOf(baseModule, utilModule, remoteModule)