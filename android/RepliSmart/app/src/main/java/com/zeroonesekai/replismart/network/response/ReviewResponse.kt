package com.zeroonesekai.replismart.network.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReviewResponse(
    @SerializedName("response")
    val response: ReplyData,
    @SerializedName("username")
    val username: String
):Serializable