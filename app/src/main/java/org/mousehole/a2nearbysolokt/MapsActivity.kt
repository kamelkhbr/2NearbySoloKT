package org.mousehole.a2nearbysolokt

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.AbsSeekBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.mousehole.a2nearbysolokt.common.Common
import org.mousehole.a2nearbysolokt.model.MyPlaces
import org.mousehole.a2nearbysolokt.network.IGoogleAPIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private lateinit var mLastLocation: Location
    private var mMarker: Marker? = null

    private var myRadius: Int = 10000

    //Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object{
        private const val MY_PERMISSION_CODE: Int=1000
    }

    lateinit var mServices: IGoogleAPIService

    internal lateinit var currentPlace:MyPlaces

    private lateinit var bottom_navigation_view: BottomNavigationView

    private lateinit var seekBar: SeekBar
    private lateinit var seekRadius: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Let's get our radius setup
        seekRadius=findViewById(R.id.seek_radius)
        seekBar = findViewById(R.id.seek_bar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                seekRadius.text = "Radius: "+ (progress/1000).toString() + "km"
                myRadius=progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(this@MapsActivity, "Radius: " + seekBar.progress / 1000 + "km", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Toast.makeText(this@MapsActivity, "Radius: " + seekBar.progress / 1000 + "km", Toast.LENGTH_SHORT).show()
            }


        } )




        // Init Service
        mServices = Common.googleAPIService



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkLocationPermission()) {
                buildLocationRequest();
                buildLocationCallBack();

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
                );
            }
        }
        else {
            buildLocationRequest();
            buildLocationCallBack();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }


        bottom_navigation_view = findViewById(R.id.bottom_navigation_view)

        bottom_navigation_view.setOnNavigationItemSelectedListener {item ->
            when(item.itemId)
            {
                R.id.action_hospital -> nearByPlace("hospital", myRadius)
                R.id.action_market -> nearByPlace("market", myRadius)
                R.id.action_restaurant-> nearByPlace("restaurant", myRadius)
                R.id.action_school -> nearByPlace("school", myRadius)

            }
            true


        }

    }

    // This is where all the fun begins
    private fun nearByPlace(typePlace: String, radius: Int) {

        // Clear all marker on Map
        mMap.clear()
        // Build URL request base on location
        val url = getUrl(latitude,longitude, typePlace, radius)

        mServices.getNearbyPlaces(url)
            .enqueue(object: Callback<MyPlaces>{
                override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                    Toast.makeText(baseContext,  ""+t!!.message,Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<MyPlaces>, response: Response<MyPlaces>) {

                    currentPlace = response!!.body()!!

                    if (response!!.isSuccessful)
                    {

                        for (i in 0 until response!!.body()!!.results!!.size)
                        {
                            val markerOptions= MarkerOptions()
                            val googlePlace = response.body()!!.results!![i]
                            val lat= googlePlace.geometry!!.location!!.lat
                            val lng= googlePlace.geometry!!.location!!.lng
                            val placeName = googlePlace.name
                            val latLng = LatLng(lat,lng)

                            markerOptions.position(latLng)
                            markerOptions.title(placeName)

                            if (typePlace.equals("hospital"))
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital))
                            else if (typePlace.equals("market"))
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.shopping))
                            else if (typePlace.equals("school"))
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school))
                            else if (typePlace.equals("restaurant"))
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.resto))
                            else
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                            markerOptions.snippet(i.toString()) // assign index for marker

                            // Add marker to map
                            mMap!!.addMarker(markerOptions)

                            // Move Camera
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(10f))


                        }


                    }
                }

            })


    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String, radius:Int): String {
        // Come back here and add everything to constants util

        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=$radius") // 10km
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=AIzaSyDh8h-16Q-uDkppVJsfPFjqS-U2OxSyfi8")

        Log.d("URL_DEBUG", googlePlaceUrl.toString())

        return googlePlaceUrl.toString()
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation = p0!!.locations.get(p0!!.locations.size-1) // getting last location

                if(mMarker!=null)
                {
                    mMarker!!.remove()
                }
                latitude= mLastLocation.latitude
                longitude= mLastLocation.longitude

                val latLng= LatLng(latitude,longitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker= mMap!!.addMarker(markerOptions) // add a marker to you position
                // move the camera there
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))


            }
        }

    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f

    }

    private fun checkLocationPermission(): Boolean {

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            return false

        }
        else
            return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode)
        {
            MY_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                        if (checkLocationPermission()) {
                            buildLocationRequest();
                            buildLocationCallBack();

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            );
                            mMap!!.isMyLocationEnabled = true
                        }

                }
                else
                    Toast.makeText(this, "Permission Denied",Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }


// here where we make the map clickable
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        // init google play Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap!!.isMyLocationEnabled = true

            }

        }
        else
            mMap!!.isMyLocationEnabled = false

        // Enable zoom Control
        mMap.uiSettings.isZoomControlsEnabled=true
        // Create event click on markers
        mMap!!.setOnMarkerClickListener {marker ->

            // When the user selects a marker, assign the result of the marker to a static variable
            Common.currentResult = currentPlace!!.results!![Integer.parseInt(marker.snippet)]
            // start new intent to the ViewPlace activity
            startActivity(Intent(this, ViewPlace::class.java))
            true


        }





    }
}