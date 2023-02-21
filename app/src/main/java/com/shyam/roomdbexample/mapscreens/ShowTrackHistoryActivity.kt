package com.shyam.roomdbexample.mapscreens

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.shyam.roomdbexample.R
import com.shyam.roomdbexample.RoomDB.AppDatabase
import com.shyam.roomdbexample.RoomDB.book.BookDao
import com.shyam.roomdbexample.RoomDB.book.LocationModel
import java.text.SimpleDateFormat
import java.util.*

class ShowTrackHistoryActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private val TAG: String = "TAG11";
    private var locationArrayLists: ArrayList<LocationModel> = ArrayList()
    var listLatLng: ArrayList<LatLng> = ArrayList()

    private lateinit var bookDao: BookDao;
    private lateinit var selectedDate: String;

    private val markerOptions = MarkerOptions();
    private var googleMap: GoogleMap? = null
    var txtForDate: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_track_history)
        txtForDate = findViewById(R.id.txtForDate)

        selectedDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        Log.e(TAG, "onCreate: $selectedDate")
        val db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "book_database"
        ).allowMainThreadQueries().build()
        bookDao = db.bookDao()
        getAllTravelledRecords()
        val supportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        val googleApiClient =
            GoogleApiClient.Builder(applicationContext).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
        supportMapFragment?.getMapAsync(this)


    }

    private fun getAllTravelledRecords() {
        locationArrayLists.clear()
//        lifecycleScope.launch(Dispatchers.IO) {
        //Query
        val books = bookDao.getLocationForSelectedDate(selectedDate)
        Log.e(TAG, "books size: " + books.size)
        for (book in books) {
            Log.i(
                TAG,
                "id: ${book.id} latitude: ${book.lat} Longitude: ${book.lng} time: ${book.created_at}"
            )
            locationArrayLists.add(book)
        }
        if (locationArrayLists.size > 0) onMapReady(googleMap)
        else Toast.makeText(this, "No Records Found", Toast.LENGTH_LONG).show()
        //        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        try {
            Log.e(TAG, "onMapReady: " )
            this.googleMap = p0;
            this.googleMap?.clear();
            if (locationArrayLists.size > 0) {
                Log.e(TAG, "onMapReady: ${locationArrayLists.size} " )
                if (!locationArrayLists[0].lat.equals("")) {
                    val markerOptionForOffice = MarkerOptions()
                    val cordForMyOffice = LatLng(
                        locationArrayLists[0].lat.toDouble(), locationArrayLists[0].lng.toDouble()
                    )
                    markerOptionForOffice.position(cordForMyOffice);
                    markerOptionForOffice.title("My Office");
                    markerOptionForOffice.snippet("Start Point");
                    markerOptionForOffice.icon(BitmapDescriptorFactory.fromResource(R.drawable.office_marker_));
                    googleMap?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                locationArrayLists[0].lat.toDouble(),
                                locationArrayLists[0].lng.toDouble()
                            ), 40F
                        )
                    )
                    googleMap?.addMarker(markerOptionForOffice)?.showInfoWindow();
                }
                for (model in locationArrayLists) {
                    Log.e(TAG, "onMapReady: ${model.lng} --- ${model.lat}" )
                    listLatLng.add(
                        LatLng(model.lat.toDouble(), model.lng.toDouble())
                    )
                }
                try {
                    googleMap?.addPolyline(
                        PolylineOptions().geodesic(true).addAll(listLatLng).width(10F)
                            .color(resources.getColor(R.color.purple_200))
                    )
                } catch (e: NumberFormatException) {
                    e.printStackTrace();
                }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace();
        }
    }

    private var calendarFrom = Calendar.getInstance()

    fun selectDate(view: View) {
        val datePickerDialog = DatePickerDialog.OnDateSetListener { datePicker, i, i2, i3 ->
            calendarFrom.set(Calendar.YEAR, i)
            calendarFrom.set(Calendar.MONTH, i2)
            calendarFrom.set(Calendar.DAY_OF_MONTH, i3)
            calendarFrom.set(Calendar.HOUR_OF_DAY, 0)
            calendarFrom.set(Calendar.MINUTE, 0)

            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
            txtForDate?.text = "Selected Date" + simpleDateFormat.format(calendarFrom.time)
            val simpleDateFormatNew = SimpleDateFormat("yyyy-MM-dd")
            selectedDate = simpleDateFormatNew.format(calendarFrom.time)
            Log.e(TAG, "selectDate: $selectedDate ")
            getAllTravelledRecords()
        }

        DatePickerDialog(
            this,
            R.style.MyDatePickerDialogTheme,
            datePickerDialog,
            calendarFrom[Calendar.YEAR],
            calendarFrom[Calendar.MONTH],
            calendarFrom[Calendar.DAY_OF_MONTH]
        ).show()
    }

    override fun onConnected(p0: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }
}