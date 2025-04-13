package com.zeroonesekai.replismart.network.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReviewRequest(
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("review")
    val review: String,
    @SerializedName("username")
    val username: String
) : Serializable