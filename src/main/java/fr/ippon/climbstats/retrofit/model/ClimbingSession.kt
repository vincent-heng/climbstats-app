package fr.ippon.climbstats.retrofit.model

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class ClimbingSession(
        @SerializedName("username") val username: String,
        @SerializedName("comments") val comments: String,
        @SerializedName("location") val location: String,
        @SerializedName("date") val date: DateTime,
        @SerializedName("routes") val routes:List<Route>
)