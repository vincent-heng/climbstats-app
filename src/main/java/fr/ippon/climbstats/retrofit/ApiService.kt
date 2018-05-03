package fr.ippon.climbstats.retrofit

import com.google.gson.GsonBuilder
import fr.ippon.climbstats.retrofit.model.ClimbingSession
import fr.ippon.climbstats.retrofit.model.Location
import fr.ippon.climbstats.retrofit.model.Point
import fr.ippon.climbstats.retrofit.model.User
import io.reactivex.Observable
import org.joda.time.DateTime
import retrofit2.http.*


interface ApiService {
    @POST("/climbing-sessions")
    fun addClimbingSession(@Body climbingSession: ClimbingSession) : Observable<ClimbingSession>

    @GET("/climbing-sessions/user/{user}")
    fun findClimbingSessionsByUsername(@Path("user") user: String): Observable<List<ClimbingSession>>

    @GET("/climbing-sessions/ranking")
    fun findRanking(): Observable<List<ClimbingSession>>

    @GET("/points-map")
    fun findMapPoints(): Observable<Map<String, List<Point>>>

    @GET("/locations/colors")
    fun findAllLocations(): Observable<List<Location>>

    @GET("/users")
    fun findAllUsernames(): Observable<List<User>>

    /**
     * Companion object for the factory
     */
    companion object Factory {
        fun create(): ApiService {
            val gson = GsonBuilder().registerTypeAdapter(DateTime::class.java, DateTimeTypeConverter()).create()

            val retrofit = retrofit2.Retrofit.Builder()
                    .addCallAdapterFactory(retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory.create())
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
                    .baseUrl("http://climbstats.herokuapp.com/")
                    .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}
