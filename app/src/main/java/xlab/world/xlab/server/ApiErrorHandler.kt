package xlab.world.xlab.server

import com.google.gson.Gson
import retrofit2.HttpException
import xlab.world.xlab.data.response.BaseErrorData
import xlab.world.xlab.utils.support.PrintLog

inline fun <reified T>errorHandle(error: Throwable): T? =
        if (error is HttpException) {
            try {
                val errorData =Gson().fromJson<T>(error.response().errorBody()?.charStream(),  T::class.java)
                (errorData as BaseErrorData).errorCode = error.code()
                errorData
            } catch (e: Exception) {
                PrintLog.d("errorHandle Exception", e.message!!)
                null
            }
        } else {
            PrintLog.d("errorHandle", "no HttpException")
            null
        }