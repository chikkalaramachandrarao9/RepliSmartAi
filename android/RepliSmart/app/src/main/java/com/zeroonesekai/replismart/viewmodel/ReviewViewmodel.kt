package com.zeroonesekai.replismart.viewmodel

import androidx.lifecycle.ViewModel
import com.zeroonesekai.replismart.network.NetworkClient
import com.zeroonesekai.replismart.network.request.ReviewRequest
import com.zeroonesekai.replismart.network.response.FaqUpdateResponse
import com.zeroonesekai.replismart.network.response.Review
import com.zeroonesekai.replismart.network.response.ReviewResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Author: Ramachandrarao Chikkala
 * Created on: 06/04/25
 */
class ReviewViewmodel : ViewModel() {

    private val _reviewResponse = MutableStateFlow<ApiResource<ReviewResponse?>>(ApiResource.idle())
    val reviewResponse: StateFlow<ApiResource<ReviewResponse?>>
        get() = _reviewResponse

    private val _faqUpdateResponse =
        MutableStateFlow<ApiResource<FaqUpdateResponse?>>(ApiResource.idle())
    val faqUpdateResponse: StateFlow<ApiResource<FaqUpdateResponse?>>
        get() = _faqUpdateResponse

    private val _reviewsListResponse =
        MutableStateFlow<ApiResource<List<Review?>?>>(ApiResource.idle())
    val reviewsListResponse: StateFlow<ApiResource<List<Review?>?>>
        get() = _reviewsListResponse

    fun getReviews(page: Int,category: String) {
        _reviewsListResponse.value = ApiResource.loading()
        var selectedCategory:String? = category
        if(category.equals("All",true)) {
           selectedCategory = null
        }

        NetworkClient.apiService.getReviews(page,selectedCategory)
            .enqueue(object : Callback<List<Review>> {
                override fun onResponse(
                    call: Call<List<Review>>,
                    response: Response<List<Review>>
                ) {
                    if (response.isSuccessful) {
                        _reviewsListResponse.value = ApiResource.success(response.body())
                    } else {
                        _reviewsListResponse.value =
                            ApiResource.error("No reviews found ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                    _reviewsListResponse.value = ApiResource.error("Failure: ${t.message}")
                }
            })
    }

    fun postReview(reviewRequest: ReviewRequest) {
        _reviewResponse.value = ApiResource.loading()

        NetworkClient.apiService.getReviewResponse(reviewRequest)
            .enqueue(object : Callback<ReviewResponse> {
                override fun onResponse(
                    call: Call<ReviewResponse>,
                    response: Response<ReviewResponse>
                ) {
                    if (response.isSuccessful) {
                        _reviewResponse.value = ApiResource.success(response.body())
                    } else {
                        _reviewResponse.value = ApiResource.error("Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<ReviewResponse>, t: Throwable) {
                    _reviewResponse.value = ApiResource.error("Failure: ${t.message}")
                }
            })
    }

    fun updateFaq(faqUpdateRequest: MultipartBody.Part) {
        _faqUpdateResponse.value = ApiResource.loading()

        NetworkClient.apiService.uploadFile(faqUpdateRequest)
            .enqueue(object : Callback<FaqUpdateResponse> {
                override fun onResponse(
                    call: Call<FaqUpdateResponse>,
                    response: Response<FaqUpdateResponse>
                ) {
                    if (response.isSuccessful) {
                        _faqUpdateResponse.value = ApiResource.success(response.body())
                    } else {
                        _faqUpdateResponse.value = ApiResource.error("Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<FaqUpdateResponse>, t: Throwable) {
                    _faqUpdateResponse.value = ApiResource.error("Failure: ${t.message}")
                }
            })
    }

    fun resetResponses() {
        _reviewResponse.value = ApiResource.idle()
        _faqUpdateResponse.value = ApiResource.idle()
        _reviewsListResponse.value = ApiResource.idle()
    }

}