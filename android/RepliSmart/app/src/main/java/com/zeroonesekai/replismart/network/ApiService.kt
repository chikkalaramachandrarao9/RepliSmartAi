package com.zeroonesekai.replismart.network

import com.zeroonesekai.replismart.network.request.ReviewRequest
import com.zeroonesekai.replismart.network.response.FaqUpdateResponse
import com.zeroonesekai.replismart.network.response.Review
import com.zeroonesekai.replismart.network.response.ReviewResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


/**
 * Author: Ramachandrarao Chikkala
 * Created on: 06/04/25
 */
interface ApiService {
    @POST("review-response")
    fun getReviewResponse(
        @Body reviewRequest: ReviewRequest
    ): Call<ReviewResponse>

    @Multipart
    @POST("upload")
    fun uploadFile(@Part file: MultipartBody.Part): Call<FaqUpdateResponse>

    @GET("reviews")
    fun getReviews(@Query("page") page:Int,@Query("category") category: String?): Call<List<Review>>
}