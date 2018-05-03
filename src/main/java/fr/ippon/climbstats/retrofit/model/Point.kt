package fr.ippon.climbstats.retrofit.model

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class Point (
    @SerializedName("date") val date: DateTime,
    @SerializedName("score") val score: Int
)