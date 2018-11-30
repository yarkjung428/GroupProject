package com.egci428.groupproject

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_walk.*
import kotlinx.android.synthetic.main.fragment_walk_detail.*
import com.egci428.groupproject.MyService.Companion.numSteps
import android.os.AsyncTask
import android.os.Handler
import java.util.*




class WalkActivity : AppCompatActivity() , WalkTrendFragment.OnFragmentInteractionListener, WalkDetailFragment.OnFragmentInteractionListener{

    var value:String? = ""
    val list: MutableList<String> = ArrayList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk)




        val fragment = WalkDetailFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.walk_fl_container, fragment!!)
        transaction.commit()

        walk_detail.setOnClickListener(){
            val fragment = WalkDetailFragment()
            val transaction = supportFragmentManager.beginTransaction()
            walk_detail.setImageResource(R.drawable.detail_wtrack_selected)
            walk_trend.setImageResource(R.drawable.detail_trend)
            transaction.replace(R.id.walk_fl_container, fragment!!)
            transaction.commit()
        }

        walk_trend.setOnClickListener(){
            val fragment = WalkTrendFragment()
            val transaction = supportFragmentManager.beginTransaction()
            walk_detail.setImageResource(R.drawable.detail_track)
            walk_trend.setImageResource(R.drawable.detail_wtrend_selected)
            transaction.replace(R.id.walk_fl_container, fragment!!)
            transaction.commit()

        }

        actionbar_back.setOnClickListener(){ this.finish() }

        /*
        myRef = database.getReference("message")
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                value = dataSnapshot.getValue(String::class.java)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        */
    }
    override fun onFragmentInteraction(uri: Uri) {}



}