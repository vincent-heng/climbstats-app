package fr.ippon.climbstats.retrofit

import fr.ippon.climbstats.retrofit.model.ClimbingSession
import fr.ippon.climbstats.retrofit.model.Location
import fr.ippon.climbstats.retrofit.model.Point
import fr.ippon.climbstats.retrofit.model.User
import io.reactivex.Observable

/**
 * Repository method to access search functionality of the api service
 */
class Api(val apiService: ApiService) {
    fun addClimbingSession(climbingSession: ClimbingSession) : Observable<ClimbingSession> {
        return apiService.addClimbingSession(climbingSession)
    }

    fun findClimbingSessionsByUsername(username: String): Observable<List<ClimbingSession>> {
        return apiService.findClimbingSessionsByUsername(username)
    }

    fun findRanking(): Observable<List<ClimbingSession>> {
        return apiService.findRanking()
    }

    fun findMapPoints(): Observable<Map<String, List<Point>>> {
        return apiService.findMapPoints()
    }

    fun findAllLocations(): Observable<List<Location>> {
        return apiService.findAllLocations()
    }

    fun findAllUsernames(): Observable<List<User>> {
        return apiService.findAllUsernames()
    }
}