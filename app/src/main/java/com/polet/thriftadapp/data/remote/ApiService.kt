package com.polet.thriftadapp.data.remote

import com.polet.thriftadapp.domain.model.AuthResponse
import com.polet.thriftadapp.domain.model.CrearMovimientoResponse
import com.polet.thriftadapp.domain.model.HomeResponse
import com.polet.thriftadapp.domain.model.LoginRequest
import com.polet.thriftadapp.domain.model.MovimientoRequest
import com.polet.thriftadapp.domain.model.MovimientoResponse
import com.polet.thriftadapp.domain.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("home/{userId}")
    suspend fun getHomeData(@Path("userId") userId: Int): Response<HomeResponse>

    @POST("movimientos")
    suspend fun crearMovimiento(@Body request: MovimientoRequest): Response<CrearMovimientoResponse>

    @GET("movimientos/{userId}")
    suspend fun getMovimientos(@Path("userId") userId: Int): Response<List<MovimientoResponse>>

    @DELETE("movimientos/{id}")
    suspend fun eliminarMovimiento(@Path("id") movimientoId: String): Response<Unit>

    @PUT("users/{userId}/role")
    suspend fun updateRole(
        @Path("userId") userId: Int,
        @Query("role") role: String
    ): Response<Unit>
}