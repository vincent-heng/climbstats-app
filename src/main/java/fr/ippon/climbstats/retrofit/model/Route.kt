package fr.ippon.climbstats.retrofit.model

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class Route(
        @SerializedName("color") val color: String,
        @SerializedName("nb_climbed") val number:Int
)