package com.polet.thriftadapp.domain.use_case

import com.polet.thriftadapp.domain.model.HomeResponse
import com.polet.thriftadapp.domain.repository.HomeRepository
import retrofit2.Response

class GetHomeDataUseCase(private val repository: HomeRepository) {
    suspend operator fun invoke(userId: Int): Response<HomeResponse> {
        return repository.getHomeData(userId)
    }
}