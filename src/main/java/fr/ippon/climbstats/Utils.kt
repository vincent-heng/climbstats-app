package fr.ippon.climbstats

import android.content.Context
import android.net.ConnectivityManager
import fr.ippon.climbstats.retrofit.model.ClimbingSession
import fr.ippon.climbstats.retrofit.model.Point
import fr.ippon.climbstats.retrofit.model.Route
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.HashMap

object Utils {
    fun isNetwork(ctx : Context) : Boolean {
        val cs = ctx.getSystemService(Context.CONNECTIVITY_SERVICE)
        if (cs is ConnectivityManager) {
            return cs.activeNetworkInfo.isConnected
        }
        return false
    }

    fun generateFakeUsernames(): List<String> {
        return listOf("Jean", "Roger", "Gérard")
    }

    fun generateFakeClimbingSessions(): List<ClimbingSession> {
        val climbingSessions = LinkedList<ClimbingSession>()
        // Jean Climbing Sessions
        for (i in 0..9) {
            val paths = listOf(
            Route("Jaune", ((Math.random() * 5) + 3).toInt()),
            Route("Vert", ((Math.random() * 6) + 1).toInt()),
            Route("Bleu", ((Math.random() * 2)).toInt()))
            val cs = ClimbingSession(username = "Jean", comments = "Formidable", location = "Arkose", date = DateTime.now().minusDays(10-i), routes = paths)
            climbingSessions += cs
        }

        // Roger Climbing Sessions
        for (i in 0..9) {
            val paths = listOf(
            Route("Jaune", ((Math.random() * 3)).toInt()),
            Route("Vert", ((Math.random() * 8) + 1).toInt()),
            Route("Bleu", ((Math.random() * 5)).toInt()))

            val cs = ClimbingSession(username = "Roger", comments = "Cool", location = "Arkose", date = DateTime.now().minusDays(10-i), routes = paths)
            climbingSessions += cs
        }

        return climbingSessions
    }

    fun generateFakePointsMap(): Map<String, List<Point>> {
        val pointsMap = HashMap<String, List<Point>>()
        val jeanPoints = java.util.ArrayList<Point>()
        for (i in 0..9) {
            val randomScore = ((Math.random() * 70) + 3).toInt()
            jeanPoints += Point(date = DateTime.now().minusDays(i), score = randomScore)
        }
        pointsMap["Jean"] = jeanPoints

        val rogerPoints = java.util.ArrayList<Point>()
        for (i in 0..9) {
            val randomScore = ((Math.random() * 70) + 3).toInt()
            rogerPoints += Point(date = DateTime.now().minusDays(i), score = randomScore)
        }
        pointsMap["Roger"] = rogerPoints
        return pointsMap
    }

    fun generateFakeLocations(): Map<String, List<String>> {
        val locations = HashMap<String, List<String>>()
        locations["Arkose"] = listOf("Jaune", "Vert", "Bleu")
        locations["Blockout"] = listOf("Jaune", "Orange", "Vert", "Bleu")
        locations["Blockbuster"] = listOf("Jaune", "Vert", "Bleu", "Rouge")
        return locations
    }
}