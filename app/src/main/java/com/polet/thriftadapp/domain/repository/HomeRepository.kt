package com.polet.thriftadapp.domain.repository

import com.polet.thriftadapp.domain.model.HomeResponse
import retrofit2.Response

interface HomeRepository {
    suspend fun getHomeData(userId: Int): Response<HomeResponse>
}