package fr.ippon.climbstats

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import fr.ippon.climbstats.Utils.generateFakePointsMap
import fr.ippon.climbstats.Utils.isNetwork
import fr.ippon.climbstats.retrofit.ApiRepositoryProvider
import fr.ippon.climbstats.retrofit.model.Point
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_points.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PointsChartActivity : AppCompatActivity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_points)

        fetchPointsMap { pointsMap ->
            drawChart(pointsMap)
        }
/*        async(CommonPool) {
            val pointsMap = generateFakePointsMap()
            drawChart(pointsMap)
        }*/
    }

    private fun drawChart(pointsMap: Map<String, List<Point>>) {
        val maxScore = pointsMap.flatMap { it.value }.maxBy { it.score }?.score
        val yAxisHeight =
                if (maxScore != null) {
                    maxScore + 5f
                } else {
                    5f
                }

        chart1.setDrawGridBackground(false)
        chart1.description.isEnabled = false
        chart1.setTouchEnabled(true)
        chart1.isDragEnabled = true
        chart1.setScaleEnabled(true)
        chart1.setPinchZoom(true)

        val xAxis = chart1.xAxis
        xAxis.enableGridDashedLine(10f, 10f, 0f)
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : IAxisValueFormatter {
            private val mFormat = SimpleDateFormat("dd/MM/yy")
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                val millis = TimeUnit.DAYS.toMillis(value.toLong())
                return mFormat.format(Date(millis))
            }
        }

        val leftAxis = chart1.axisLeft
        leftAxis.removeAllLimitLines() // reset all limit lines to avoid overlapping lines
        leftAxis.axisMaximum = yAxisHeight
        leftAxis.axisMinimum = 0f
        leftAxis.enableGridDashedLine(10f, 10f, 0f)
        leftAxis.setDrawZeroLine(false)

        // Limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true)

        chart1.axisRight.isEnabled = false

        // Add data
        setData(pointsMap)

        // Draw the legend
        val l = chart1.legend
        l.form = LegendForm.LINE
    }

    private fun fetchPointsMap(callback: (Map<String, List<Point>>) -> Unit) {
        info("Fetch Points Map")
        if (!isNetwork(this)) {
            toast("Pas de connexion")
            return
        }
        val mapPointsObservable = ApiRepositoryProvider.provideRepository().findMapPoints()
        mapPointsObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback, { error ->
                    info(error.message)
                    toast("Serveur indisponible")
                })
    }

    private fun setData(pointsMap: Map<String, List<Point>>) {
        val dataSets = ArrayList<ILineDataSet>()
        pointsMap
                .forEach({ (username, points) ->
                    val values = ArrayList<Entry>()
                    points.sortedBy { point -> point.date }
                            .forEach { point ->
                                val date = TimeUnit.MILLISECONDS.toDays(point.date.toLocalDate().toDate().time).toFloat() // Number of days since 1970
                                values.add(Entry(date, point.score.toFloat()))
                            }

                    val set = configureDataSet(values, username)
                    dataSets.add(set)
                })

        chart1.data = LineData(dataSets)
    }

    private fun configureDataSet(values: ArrayList<Entry>, username: String): LineDataSet {
        val lineDataSet = LineDataSet(values, username)

        lineDataSet.setDrawIcons(false)

        val rnd: Random = Random()
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
