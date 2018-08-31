package xlab.world.xlab.server

import io.reactivex.Observable
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.ResLoginData
import xlab.world.xlab.server.`interface`.IUserRequest

interface ApiUserProvider {
    fun requestLogin(reqLoginData: ReqLoginData): Observable<ResLoginData>
}

class ApiUser(private val iUserRequest: IUserRequest): ApiUserProvider {
    override fun requestLogin(reqLoginData: ReqLoginData): Observable<ResLoginData> =
            iUserRequest.login(reqLoginData = reqLoginData, type = reqLoginData.type)
}