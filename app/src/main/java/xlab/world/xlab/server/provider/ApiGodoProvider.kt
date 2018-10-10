package xlab.world.xlab.server.provider

import xlab.world.xlab.server.`interface`.IGodoRequest

interface ApiGodoProvider {

}

class ApiGodo(private val iGodoRequest: IGodoRequest): ApiGodoProvider {
}