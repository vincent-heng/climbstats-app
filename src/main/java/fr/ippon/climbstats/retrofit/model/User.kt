package fr.ippon.climbstats.retrofit.model

import com.google.gson.annotations.SerializedName

data class User (
        @SerializedName("name") val name: String
)