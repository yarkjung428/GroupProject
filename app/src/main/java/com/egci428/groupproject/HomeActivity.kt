package com.egci428.groupproject

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.android.synthetic.main.activity_loading.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

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
        setPieChartData()
        setSleepData()
    }

    fun setPieChartData(){
        //Set recent numSteps data to text once page created
        var steps = MyService.dataList[(MyService.dataList.size)-1].numSteps
        tv_detail_steps.setText(steps.toString())

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
        colorList!!.add(getResources().getColor(R.color.colorWalkk))
        colorList!!.add(getResources().getColor(R.color.colorGrey))
        dataSet.setColors(colorList)

        var data = PieData(dataSet)

        piechart!!.setData(data)
        piechart!!.invalidate()
    }

    fun setSleepData(){
        if(MyService.dataList[(MyService.dataList.size)-1].sleepDuration>0) {
            var min = (MyService.dataList[(MyService.dataList.size) - 1].sleepDuration) / 60
            detail_tv_hrs.setText((min / 60).toString())
            detail_tv_mins.setText((min % 60).toString())
            var tempsleepTimeData = MyService.dataList[(MyService.dataList.size) - 1].sleepTime
            var sleepTimeData = tempsleepTimeData.split("_")
            var tempwakeupTimeData = MyService.dataList[(MyService.dataList.size) - 1].wakeupTime
            var wakeupTimeData = tempwakeupTimeData.split("_")

            var temp = sleepTimeData.get(1).split(":")
            if(temp.get(0).toInt() >= 20 && temp.get(0).toInt() <=23)
                sleepTime.setText(sleepTimeData.get(1) + " PM")
            else
                sleepTime.setText(sleepTimeData.get(1) + " AM")
            wakeupTime.setText(wakeupTimeData.get(1) + " AM")
        }
        else{
            detail_tv_hrs.setText("-")
            detail_tv_mins.setText("-")
            sleepTime.setText("-")
            wakeupTime.setText("-")
        }
        val sdf = SimpleDateFormat("HH:mm")
        var currentTime = sdf.format(Calendar.getInstance().getTime())
        tvtime.setText(currentTime)
    }
}
