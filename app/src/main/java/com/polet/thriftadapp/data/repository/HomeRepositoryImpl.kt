package com.polet.thriftadapp.data.repository

import com.polet.thriftadapp.data.remote.ApiService
import com.polet.thriftadapp.domain.model.HomeResponse
import com.polet.thriftadapp.domain.repository.HomeRepository
import retrofit2.Response
import javax.inject.Inject // <--- AGREGA ESTO
class HomeRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : HomeRepository {
    override suspend fun getHomeData(userId: Int): Response<HomeResponse> {
        return apiService.getHomeData(userId)
    }
}