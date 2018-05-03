package fr.ippon.climbstats

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import fr.ippon.climbstats.Utils.generateFakeClimbingSessions
import fr.ippon.climbstats.Utils.isNetwork
import fr.ippon.climbstats.retrofit.ApiRepositoryProvider
import fr.ippon.climbstats.retrofit.model.ClimbingSession
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ranking.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.joda.time.format.DateTimeFormat


class RankingActivity : AppCompatActivity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        fetchRanking()
//        fetchRankingMock()

        update_ranking_button.onClick {
            fetchRanking()
//            fetchRankingMock()
        }
    }

    private fun fetchRanking() {
        info("Fetch Ranking")
        if (!isNetwork(this)) {
            toast("Pas de connexion")
            return
        }
        val climbingSessionsObservable = ApiRepositoryProvider.provideRepository().findRanking()
        climbingSessionsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({result ->
            updateTable(result)
        }, { error ->
            info(error.message)
            toast("Serveur indisponible")
        })
    }

    private fun fetchRankingMock() {
        val result = generateFakeClimbingSessions()
                .groupBy ({ it.username })
                .map { it.value.maxBy { it.routes.size } }
                .filterNot { it==null }
                .map { it!! }
                .sortedByDescending { it.routes.map { it.number }.reduce { acc, i -> acc + i } }
        updateTable(result)
    }


    private fun updateTable(cs: List<ClimbingSession>) {
        ranking_table.removeAllViews()
        ranking_table.tableRow { // Header
            addTableCell(resources.getString(R.string.ranking_label_rank))
            addTableCell(resources.getString(R.string.ranking_label_username))
            addTableCell(resources.getString(R.string.ranking_label_best_score))
            addTableCell(resources.getString(R.string.ranking_label_date))
            addTableCell(resources.getString(R.string.ranking_label_location))
        }

        val dtfOut = DateTimeFormat.forPattern("dd/MM/yy")

        cs.forEachIndexed({ i, c ->
            ranking_table.tableRow {
                val j = i+1
                addTableCell("#$j")
                addTableCell(c.username)
                addTableCell(c.routes.map { it.number }.reduce { acc, i -> acc + i }.toString()) // TODO Retrieve the real score
                addTableCell(dtfOut.print(c.date))
                addTableCell(c.location)
            }
        })
    }

    private fun @AnkoViewDslMarker _TableRow.addTableCell(content: String) {
        textView {
            text = content
        }.lparams(height = wrapContent) {
            width = dip(0)
            weight = 1f
        }
    }
}
