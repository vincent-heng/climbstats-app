package fr.ippon.climbstats

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import net.danlew.android.joda.JodaTimeAndroid
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity(), AnkoLogger {
    init {
        instance = this
    }

    companion object {
        private var instance: MainActivity? = null // FIXME This is a memory leak

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    var mContext: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        JodaTimeAndroid.init(this) // TODO Place it in "App" ?
        setContentView(R.layout.activity_main)
        info("MainActivity created")
        async(CommonPool) {
            bindButtons()
        }
    }

    override fun onResume() {
        super.onResume()
        bindButtons()
    }

    private fun bindButtons() {
        /* Public buttons */
        addClimbingSession.onClick {
            startActivity<AddClimbingSessionActivity>()
        }

        showPersonalChart.onClick {
            startActivity<PersonalChartActivity>()
        }

        showPointsChart.onClick {
            startActivity<PointsChartActivity>()
        }

        showRanking.onClick {
            startActivity<RankingActivity>()
        }

    }
}
