package fr.ippon.climbstats

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import fr.ippon.climbstats.Utils.isNetwork
import fr.ippon.climbstats.retrofit.ApiRepositoryProvider
import fr.ippon.climbstats.retrofit.model.ClimbingSession
import fr.ippon.climbstats.retrofit.model.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import junit.framework.TestSuite.warning
import kotlinx.android.synthetic.main.activity_personal.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PersonalChartActivity : AppCompatActivity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)

        fetchUsernames {
            populateUsernamesSpinner(it.map { it.name })
        }

        usernames_spinner.onItemSelectedListener {
            this.onItemSelected { parent, view, pos, id ->
                val username = parent?.getItemAtPosition(pos).toString()
                fetchClimbingSessions(username) { climbingSessions ->
                    drawChart(climbingSessions)
                }
            }
        }
    }

    private fun drawChart(climbingSessions: List<ClimbingSession>) {
        val maxScore = climbingSessions.map { it.routes }.flatMap { it }.map { it.number }.max()
        val yAxisHeight =
                if (maxScore != null) {
                    maxScore + 2f
                } else {
                    5f
                }

        personal_chart.setDrawGridBackground(false)
        personal_chart.description.isEnabled = false
        personal_chart.setTouchEnabled(true)
        personal_chart.isDragEnabled = true
        personal_chart.setScaleEnabled(true)
        personal_chart.setPinchZoom(true)

        val xAxis = personal_chart.xAxis
        xAxis.enableGridDashedLine(10f, 10f, 0f)
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : IAxisValueFormatter {
            private val mFormat = SimpleDateFormat("dd/MM/yy")
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                val millis = TimeUnit.DAYS.toMillis(value.toLong()) + TimeUnit.DAYS.toMillis(1) // FIXME The points are 1 day late
                return mFormat.format(Date(millis))
            }
        }

        val leftAxis = personal_chart.axisLeft
        leftAxis.removeAllLimitLines() // reset all limit lines to avoid overlapping lines
        leftAxis.axisMaximum = yAxisHeight
        leftAxis.axisMinimum = 0f
        leftAxis.enableGridDashedLine(10f, 10f, 0f)
        leftAxis.setDrawZeroLine(false)

        // Limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true)

        personal_chart.axisRight.isEnabled = false

        // Add data
        setData(climbingSessions)

        personal_chart.animateX(1500)

        // Draw the legend
        val l = personal_chart.legend
        l.form = LegendForm.LINE
    }

    private fun fetchUsernames(callback: (List<User>) -> Unit) {
        info("Fetch Usernames")
        if (!isNetwork(this)) {
            toast("Pas de connexion")
            return
        }
        val usernamesObservable = ApiRepositoryProvider.provideRepository().findAllUsernames()
        usernamesObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback, { error ->
                    warning(error.message)
                    toast("Serveur indisponible")
                })
    }

    private fun fetchClimbingSessions(username: String, callback: (List<ClimbingSession>) -> Unit) {
        info("Fetch Climbing Sessions")
        if (!isNetwork(this)) {
            toast("Pas de connexion")
            return
        }
        val mapPointsObservable = ApiRepositoryProvider.provideRepository().findClimbingSessionsByUsername(username)
        mapPointsObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback, { error ->
                    info(error.message)
                    toast("Serveur indisponible")
                })
    }

    private fun populateUsernamesSpinner(usernames: List<String>) {
        usernames_spinner.adapter = ArrayAdapter(ctx, R.layout.support_simple_spinner_dropdown_item, usernames.sortedBy { it })
    }

    private fun setData(climbingSessions: List<ClimbingSession>) {
        val dataSets =
                climbingSessions
                        .sortedBy { it.date }
                        .map { climbingSession ->
                            climbingSession.routes.map {
                                Pair(it.color, Pair(climbingSession.date, it.number))
                            }
                        }
                        .flatMap { it }
                        .groupBy({ it.first }, { it.second })
                        .map {
                            val values = ArrayList<Entry>()
                            values += it.value.map {
                                val date = TimeUnit.MILLISECONDS.toDays(it.first.toLocalDate().toDate().time).toFloat() // Number of days since 1970
                                val score = it.second
                                Entry(date, score.toFloat())
                            }
                            val set = configureDataSet(values, it.key)
                            set
                        }

        personal_chart.data = LineData(dataSets)
    }

    private fun configureDataSet(values: ArrayList<Entry>, username: String): LineDataSet {
        val lineDataSet = LineDataSet(values, username)
        lineDataSet.setDrawIcons(false)

        val rnd = Random()
        val randomColor: Int = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        lineDataSet.color = randomColor
        lineDataSet.setCircleColor(randomColor)
        lineDataSet.lineWidth = 2f
        lineDataSet.circleRadius = 3f
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.valueTextSize = 9f
        lineDataSet.formLineWidth = 1f
        lineDataSet.formSize = 15f
        return lineDataSet
    }
}
