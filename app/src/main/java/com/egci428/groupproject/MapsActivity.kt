package com.egci428.groupproject

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.egci428.groupproject.MyService.Companion.lat
import com.egci428.groupproject.MyService.Companion.long

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var check = false
    private var locationManager: LocationManager? = null
    private var listener: LocationListener? = null
    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        listener = object: LocationListener{
            override fun onLocationChanged(location: Location){
                //latText.setText(location.latitude.toString())
                //longText.setText(location.longitude.toString())

                lat.add(location.latitude)
                long.add(location.longitude)
                //Toast.makeText(applicationContext,"New data",Toast.LENGTH_SHORT).show()
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {}
        }
        requestLocationService()

        actionbar_back.setOnClickListener(){ this.finish() }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permission: Array<String>,grantResults: IntArray){
        when(requestCode){
            10-> requestLocationService()
            else ->{}
        }
    }
    fun requestLocationService(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED  &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET),10)
            }
            return
        }
        locationManager!!.requestLocationUpdates("gps",2500,0f,listener)


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        updateBtn.setOnClickListener {
            if(lat!=null && long !=null){
                for(i in  lat.indices){

                    val curLat= lat[i]
                    val curLong= long[i]
                    val cur = LatLng(curLat, curLong)
                    //mMap.addMarker(MarkerOptions().position(cur))
                    //Toast.makeText(applicationContext,"data exist",Toast.LENGTH_SHORT).show()

                    if(i==0){
                       // mMap.addMarker(MarkerOptions().position(cur))
                        //Toast.makeText(applicationContext,"add1",Toast.LENGTH_SHORT).show()
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cur,20f))
                    }
                    else{
                        //Toast.makeText(applicationContext,"add2",Toast.LENGTH_SHORT).show()
                        //mMap.addMarker(MarkerOptions().position(cur))
                        mMap.addPolyline(PolylineOptions()
                                .add(
                                        LatLng(lat[i-1],long[i-1]),
                                        LatLng(lat[i],long[i])
                                )
                                .width(10f)
                                .color(Color.RED))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cur,20f))
                    }

                }
            }
            else{

            }
        }
        clearBtn.setOnClickListener {
            lat.clear()
            long.clear()
            Toast.makeText(applicationContext,"Location cleared",Toast.LENGTH_SHORT).show()
        }

    }
}
