package com.zeroonesekai.replismart.network.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReplyData(
    @SerializedName("category")
    val category: String,
    @SerializedName("reply")
    val reply: String
) :Serializable