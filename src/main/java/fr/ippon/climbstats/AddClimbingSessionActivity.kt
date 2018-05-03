package fr.ippon.climbstats

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import fr.ippon.climbstats.retrofit.ApiRepositoryProvider
import fr.ippon.climbstats.retrofit.model.ClimbingSession
import fr.ippon.climbstats.retrofit.model.Location
import fr.ippon.climbstats.retrofit.model.Route
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import junit.framework.TestSuite.warning
import kotlinx.android.synthetic.main.activity_add.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.joda.time.DateTime
import java.util.*

class AddClimbingSessionActivity : AppCompatActivity(), AnkoLogger {
    private var routesMap = HashMap<String, EditText>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val datePicker = createDatePicker()
        fetchLocations { locations ->
            populateLocationDropdown(locations)
            initializeSpinnerListener(locations)
            refreshRoutesTable(locations, locations_spinner.selectedItem.toString())
        }

        add_button.onClick {
            val routes = routesMap.map { (k,v) ->
                Route(color = k, number = v.text.toString().toInt())
            }

            val climbingSession = ClimbingSession(
                    username = username_input.text.toString(),
                    comments = comments_input.text.toString(),
                    location = locations_spinner.selectedItem.toString(),
                    date = DateTime(datePicker.year, datePicker.month, datePicker.dayOfMonth, 2, 0),
                    routes = routes
                    )
            recordClimbingSession(climbingSession, {
                startActivity<MainActivity>()
                toast("Added")
            })
        }
    }

    private fun initializeSpinnerListener(locations: List<Location>) {
        locations_spinner.onItemSelectedListener {
            onItemSelected { parent, view, pos, id ->
                val location = parent?.getItemAtPosition(pos).toString()
                refreshRoutesTable(locations, location)
            }
        }
    }

    private fun refreshRoutesTable(locations: List<Location>, location: String) {
        val values = locations.filter { it.name == location }.map { it.colors }.first()
        routesMap = updateTable(values)
    }

    private fun updateTable(colors: List<String>): HashMap<String, EditText> {
        add_routes_table.removeAllViews()

        val routesMap = HashMap<String, EditText>()
        add_routes_table.tableRow { // Header
            addTableCell(resources.getString(R.string.add_label_color_name))
            addTableCell(resources.getString(R.string.add_label_color_number))
        }
        colors.forEach({ c ->
            add_routes_table.tableRow {
                addTableCell(c)
                routesMap[c] = editText{
                    setText("0")
                }
            }
        })
        return routesMap
    }

    private fun createDatePicker(): DatePicker {
        val datePicker = DatePicker(this)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
            { _, pyear, month, day->
                // Do nothing
            })
        datepicker_layout.addView(datePicker)
        return datePicker
    }

    private fun populateLocationDropdown(locations: List<Location>) {
        locations_spinner.adapter = ArrayAdapter(ctx, R.layout.support_simple_spinner_dropdown_item, locations.map { it.name }.sortedBy { it })
    }

    private fun fetchLocations(callback: (List<Location>) -> Unit) {
        info("Fetch Locations")
        if (!isNetwork()) {
            toast("Pas de connexion")
            return
        }
        val locationsObservable = ApiRepositoryProvider.provideRepository().findAllLocations()
        locationsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(callback, { error ->
            warning(error.message)
            toast("Serveur indisponible")
        })
    }

    private fun recordClimbingSession(climbingSession: ClimbingSession, callback: (ClimbingSession) -> Unit) {
        info("Record Climbing session")
        if (!isNetwork()) {
            toast("Pas de connexion")
            return
        }
        val csWithFilteredRoutes = climbingSession.copy(routes = climbingSession.routes.filter { route -> route.number > 0 })
        val addObservable = ApiRepositoryProvider.provideRepository().addClimbingSession(csWithFilteredRoutes)
        addObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(callback, { error ->
            warning(error.message)
            toast("Serveur indisponible ou informations erronées")
        })
    }

    private fun isNetwork() : Boolean {
        val cs = getSystemService(Context.CONNECTIVITY_SERVICE)
        if (cs is ConnectivityManager) {
            return cs.activeNetworkInfo.isConnected
        }
        return false
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
