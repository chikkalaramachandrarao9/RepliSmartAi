package com.zeroonesekai.replismart.network.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


/**
 * Author: Ramachandrarao Chikkala
 * Created on: 11/04/25
 */
data class Review(
    @SerializedName("review_id")
    val reviewId: String?,
    @SerializedName("author_name")
    val authorName: String?,
    @SerializedName("comment")
    val comment: String?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("star_rating")
    val starRating: Int?,
    @SerializedName("device_metadata")
    val deviceMetadata: String?
) : Serializable


