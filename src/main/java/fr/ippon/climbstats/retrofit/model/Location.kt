package fr.ippon.climbstats.retrofit.model

import com.google.gson.annotations.SerializedName

data class Location(
        @SerializedName("name") val name: String,
        @SerializedName("colors") val colors: List<String>
)