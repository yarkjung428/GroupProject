package com.egci428.groupproject

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private var myService: MyService? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myService = MyService()
        startService(Intent(this, myService!!::class.java))

        fl_sleep.setOnClickListener(){
            val intent = Intent(this, DetailActivity::class.java);
            startActivity(intent);
        }

        fl_walk.setOnClickListener(){
            val intent = Intent(this, WalkActivity::class.java);
            startActivity(intent);
        }
        fl_map.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java);
            startActivity(intent);
        }

        actionbar_profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java);
            startActivity(intent);
        }

        val serviceIntent = Intent(this, MyService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun setPieChartData(){
        //Set recent numSteps data to text once page created
        var steps = MyService.dataList[(MyService.dataList.size)-1].numSteps
        tv_detail_steps.setText(steps)

        //Set recent numSteps data to pieChart
        //Set pieChart properties
        piechart!!.setUsePercentValues(true)
        piechart!!.getDescription().setEnabled(false)
        piechart!!.setExtraOffsets(5F, 10F, 5F, 5F)

        piechart!!.setDragDecelerationFrictionCoef(0.99f)

        piechart!!.setDrawHoleEnabled(true)
        piechart!!.setHoleColor(Color.WHITE)
        piechart!!.setTransparentCircleRadius(31f)

        //Set pieChart data: the full circle equals to 8 hours
        //So we calculate percentage of recent sleep duration data and draw a circle according to the data
        var yValues = ArrayList<PieEntry>()
        if(steps>0){
            if(steps*100/500 >= 100){
                yValues.add(PieEntry(100f,""))
                yValues.add(PieEntry(0f,""))
            }
            else {
                yValues.add(PieEntry((steps * 100 / 500).toFloat(), ""))
                yValues.add(PieEntry(100 - (steps * 100 / 480).toFloat(), ""))
            }
            tv_nodata!!.visibility = View.INVISIBLE
        }
        else tv_nodata!!.visibility = View.VISIBLE

        piechart!!.animateY(2000, Easing.EasingOption.EaseInOutCubic)

        ////Set pieChart properties
        var dataSet = PieDataSet(yValues, "");
        dataSet.setSliceSpace(3f)
        dataSet.setSelectionShift(5f)

        var colorList: MutableList<Int> = mutableListOf<Int>()
        colorList!!.add(getResources().getColor(R.color.colorSleep))
        colorList!!.add(getResources().getColor(R.color.colorGrey))
        dataSet.setColors(colorList)

        var data = PieData(dataSet)

        piechart!!.setData(data)
        piechart!!.invalidate()
    }
}
