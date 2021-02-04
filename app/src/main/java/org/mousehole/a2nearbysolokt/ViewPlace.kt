package org.mousehole.a2nearbysolokt

import android.content.Intent
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import android.media.Rating
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.common.internal.service.Common
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.mousehole.a2nearbysolokt.model.PlacesDetail
import org.mousehole.a2nearbysolokt.network.IGoogleAPIService
import retrofit2.Call
import retrofit2.Response
import java.lang.StringBuilder

class ViewPlace : AppCompatActivity() {

    // Using "get place details API" here to show place details

    internal lateinit var mService : IGoogleAPIService
    var mPlace: PlacesDetail?=null

    private lateinit var place_name : TextView
    private lateinit var place_address : TextView
    private lateinit var place_open_hour : TextView
    private lateinit var rating_bar : RatingBar
    private lateinit var btn_show_map : Button
    private lateinit var btn_show_direction : Button
    private lateinit var photo : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_place)
        // Bind the views from the xml
        place_name = findViewById(R.id.place_name)
        place_address = findViewById(R.id.place_address)
        place_open_hour = findViewById(R.id.place_open_hour)
        rating_bar = findViewById(R.id.rating_bar)
        btn_show_map = findViewById(R.id.show_in_mapbtn)
        btn_show_direction= findViewById(R.id.btn_view_direction)

        photo = findViewById(R.id.photo)

        // Init service
        mService = org.mousehole.a2nearbysolokt.common.Common.googleAPIService

        // Set text views to empty
        place_name.text = ""
        place_address.text = ""
        place_open_hour.text = ""



        btn_show_map.setOnClickListener {

            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.result!!.url))
            startActivity(mapIntent)
        }

        // take me to show direction map activity

        btn_show_direction.setOnClickListener {

            val viewDirection = Intent(this@ViewPlace, ViewDirections::class.java)
            startActivity(viewDirection)
        }




        // Load photo of the place
        if (org.mousehole.a2nearbysolokt.common.Common.currentResult!!.photos != null
                && org.mousehole.a2nearbysolokt.common.Common.currentResult!!.photos!!.size > 0)
            Picasso.get().load(getPhotoOfPlace(org.mousehole.a2nearbysolokt.common.Common.currentResult!!.photos!![0].photo_reference!!, 1000))
                    .into(photo)
        // Load rating
        if (org.mousehole.a2nearbysolokt.common.Common.currentResult!!.rating != null)
            rating_bar.rating = org.mousehole.a2nearbysolokt.common.Common.currentResult!!.rating.toFloat()
        else
            rating_bar.visibility = View.GONE
        // Load open hours
        if (org.mousehole.a2nearbysolokt.common.Common.currentResult!!.opening_hours != null)
            place_open_hour.text = "Open now : " + org.mousehole.a2nearbysolokt.common.Common.currentResult!!.opening_hours!!.open_now
        else
            place_open_hour.visibility = View.GONE


        // Get the place name
        if (org.mousehole.a2nearbysolokt.common.Common.currentResult!!.name != null) {
            place_name.text = org.mousehole.a2nearbysolokt.common.Common.currentResult!!.name
        }
        //Get the place Address
        if (org.mousehole.a2nearbysolokt.common.Common.currentResult!!.formatted_phone_number != null) {
            place_address.text = org.mousehole.a2nearbysolokt.common.Common.currentResult!!.formatted_phone_number
        }


    }

    private fun getPhotoOfPlace(photoReference: String, maxWidth: Int): String {

        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        url.append("?maxwidth=$maxWidth")
        url.append("&photoreference=$photoReference")
        url.append("&key=AIzaSyDh8h-16Q-uDkppVJsfPFjqS-U2OxSyfi8")
        return url.toString()

    }
}