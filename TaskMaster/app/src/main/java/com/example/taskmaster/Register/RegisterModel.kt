package com.example.taskmaster.Register

import com.example.taskmaster.data.model.RegisterReq
import com.example.taskmaster.data.model.RegisterRes
import com.example.taskmaster.APIService.ApiService
import java.lang.Exception

class RegisterModel(private val apiService: ApiService) {

    suspend fun registerUser(name: String, email:String, password:String): Result<RegisterRes>{
        return try {
            val request = RegisterReq(
                name = name,
                email = email,
                password = password
            )
            val response = apiService.register(request)
            if (response.isSuccessful){
                Result.success(response.body()!!)
            }else{
                Result.failure(Exception("Error: ${response.errorBody()?.string()}"))
            }
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}