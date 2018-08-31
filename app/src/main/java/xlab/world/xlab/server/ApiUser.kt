package xlab.world.xlab.server

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.server.`interface`.IUserRequest
import xlab.world.xlab.utils.rx.SchedulerProvider

interface ApiUser {
    fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData): Completable
}

class ApiUserImpl(private val iUserRequest: IUserRequest): ApiUser {
    override fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData): Completable =
            iUserRequest.login(reqLoginData = reqLoginData, type = reqLoginData.type).doOnSuccess {  }.toCompletable()
//                    .subscribeOn(scheduler.io())
//                    .observeOn(scheduler.ui())
//                    .subscribe({ response ->
//                    }, { error ->
//                    })
}