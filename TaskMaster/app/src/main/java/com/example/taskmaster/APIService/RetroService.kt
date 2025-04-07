package com.example.taskmaster.APIService

import com.example.taskmaster.data.model.LoginReq
import com.example.taskmaster.data.model.LoginRes
import com.example.taskmaster.data.model.RegisterReq
import com.example.taskmaster.data.model.RegisterRes
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

data class RegisterFcmTokenRequest(val user_id: Int ?, val fcmToken: String)

interface ApiService {

    @POST("clients/login")
    fun login(@Body req: LoginReq): retrofit2.Call<LoginRes>

    @Multipart
    @POST("products")
    suspend fun addProduct(
        @Part("nombre") name: RequestBody,
        @Part("precio") price: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("cantidad") cantidad: RequestBody,
        @Part("user_id") user_id: RequestBody
    ): Response<Unit>

    //@GET("products/all/{id}")
    //suspend fun getProductsByUserId(@Path("id") user_id: Int): Response<List<ProductResponse>>

    //@GET("products")
    //suspend fun getProducts(): Response<List<ProductResponse>>

    @DELETE("products/{nombre}")
    suspend fun deleteProduct(@Path("nombre") nombre: String): Response<Unit>

    @Multipart
    @PUT("products")
    suspend fun updateProduct(
        @Part("nombre") nombre: RequestBody,
        @Part("nuevoNombre") nuevoNombre: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("cantidad") cantidad: RequestBody // Para la cantidad modificada
    ): Response<Unit>

    //@GET("products/{id}")
    //suspend fun getProductById(@Path("id") id: Int): Response<ProductResponse>

    // Registro de usuario
    @POST("clients/")
    suspend fun register(@Body req: RegisterReq): Response<RegisterRes>

    @POST("notifications/register-fcm-token")  // Define la ruta en tu servidor
    fun registerFcmToken(@Body request: RegisterFcmTokenRequest): Call<Unit> // Cambia a Call<Unit>
}