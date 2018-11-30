package com.egci428.groupproject

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.app.Fragment
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.fragment_sleep_detail.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SleepDetailFragment : android.support.v4.app.Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var pieChart: PieChart? = null
    private var detail_tv_hrs: TextView? = null
    private var detail_tv_mins: TextView? = null
    private var tv_avgbedtime: TextView? = null
    private var tv_avgwakeuptime: TextView? = null
    private var tv_avghrs: TextView? = null
    private var tv_avgmins: TextView? = null
    private var tv_ampm: TextView? = null
    private var tv_nodata: TextView? = null
    private var tv_date: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sleep_detail, container, false)
        detail_tv_hrs = view.findViewById(R.id.detail_tv_hrs) as TextView
        detail_tv_mins = view.findViewById(R.id.detail_tv_mins) as TextView
        tv_avgbedtime = view.findViewById(R.id.tv_avgbedtime) as TextView
        tv_avgwakeuptime = view.findViewById(R.id.tv_avgwakeuptime) as TextView
        tv_avghrs = view.findViewById(R.id.tv_avghrs) as TextView
        tv_avgmins = view.findViewById(R.id.tv_avgmins) as TextView
        tv_ampm = view.findViewById(R.id.tv_ampm) as TextView
        tv_nodata = view.findViewById(R.id.tv_nodata) as TextView
        tv_date = view.findViewById(R.id.tv_date) as TextView

        setPieChartData(view)
        setAverageData()

        return view
    }

    fun setPieChartData(view: View){
        //Set recent time slept data to text once page created
        var min = (MyService.dataList[(MyService.dataList.size)-1].sleepDuration)/60
        detail_tv_hrs!!.setText((min/60).toString())
        detail_tv_mins!!.setText((min%60).toString())

        //Set recent time slept data to pieChart
        //Set pieChart properties
        pieChart = view.findViewById(R.id.piechart)
        pieChart!!.setUsePercentValues(true)
        pieChart!!.getDescription().setEnabled(false)
        pieChart!!.setExtraOffsets(5F, 10F, 5F, 5F)

        pieChart!!.setDragDecelerationFrictionCoef(0.99f)

        pieChart!!.setDrawHoleEnabled(true)
        pieChart!!.setHoleColor(Color.WHITE)
        pieChart!!.setTransparentCircleRadius(31f)

        //Set pieChart data: the full circle equals to 8 hours
        //So we calculate percentage of recent sleep duration data and draw a circle according to the data
        var yValues = ArrayList<PieEntry>()
        if(min>0){
            if(min*100/480 >= 100){
                yValues.add(PieEntry(100f,""))
                yValues.add(PieEntry(0f,""))
            }
            else {
                yValues.add(PieEntry((min * 100 / 480).toFloat(), ""))
                yValues.add(PieEntry(100 - (min * 100 / 480).toFloat(), ""))
            }
            tv_nodata!!.visibility = View.INVISIBLE
        }
        else tv_nodata!!.visibility = View.VISIBLE

        pieChart!!.animateY(2000, Easing.EasingOption.EaseInOutCubic)

        ////Set pieChart properties
        var dataSet = PieDataSet(yValues, "");
        dataSet.setSliceSpace(3f)
        dataSet.setSelectionShift(5f)

        var colorList: MutableList<Int> = mutableListOf<Int>()
        colorList!!.add(getResources().getColor(R.color.colorSleep))
        colorList!!.add(getResources().getColor(R.color.colorGrey))
        dataSet.setColors(colorList)

        var data = PieData(dataSet)

        pieChart!!.setData(data)
        pieChart!!.invalidate()
    }

    fun setAverageData(){
        //Calculate average bedtime, wake-up time, and time slept
        var sleepHr: Int = 0
        var sleepMin: Int = 0
        var wakeHr: Int = 0
        var wakeMin: Int = 0
        var sleepDur = 0
        var countData: Int = 0
        for(i in 1..MyService.dataList.size) {
            //If there is data recorded, then calculate
            var sleepTimeData = MyService.dataList[i - 1].sleepTime.split("_")
            var wakeupTimeData = MyService.dataList[i - 1].wakeupTime.split("_")
            tv_date!!.text = wakeupTimeData.get(0)

            //Check if sleeptime data is available (recorded or not)
            if(!MyService.dataList[i-1].sleepTime.equals("??")) {
                //Data is stored in "17-Nov-2018_22:38" form, so split date ane time by "_"
                //And split between hr and min again by ":"
                sleepTimeData = sleepTimeData.get(1).split(":")
                wakeupTimeData = wakeupTimeData.get(1).split(":")

                //If sleeptime begins after 00:00 AM, we need to convert it
                var tempHr: Int
                if (sleepTimeData.get(0).toInt() == 0) tempHr = 24
                else if (sleepTimeData.get(0).toInt() == 1) tempHr = 25
                else if (sleepTimeData.get(0).toInt() == 2) tempHr = 26
                else tempHr = sleepTimeData.get(0).toInt()

                sleepDur = sleepDur + MyService.dataList[i - 1].sleepDuration.toInt()
                wakeHr = wakeHr + wakeupTimeData.get(0).toInt()
                wakeMin = wakeMin + wakeupTimeData.get(1).toInt()
                sleepHr = sleepHr + tempHr
                sleepMin = sleepMin + sleepTimeData.get(1).toInt()
                countData++
            }
        }

        //Finding avg sleep duration in minutes
        sleepDur = (sleepDur/countData)/60

        //Finding avg bedtime and wake-up time
        var tempwakeupHr = ((wakeHr.toDouble()%countData.toDouble())/countData.toDouble())*60.0
        wakeHr = wakeHr/countData
        wakeMin = wakeMin/countData + tempwakeupHr.toInt()

        var tempsleepHr = ((sleepHr.toDouble()%countData.toDouble())/countData.toDouble())*60.0
        sleepHr = sleepHr/countData
        sleepMin = sleepMin/countData + tempsleepHr.toInt()

        if(wakeMin/60>=1) {
            wakeHr = wakeHr + wakeMin / 60
            wakeMin = wakeMin-60
        }
        if(sleepMin/60>=1) {
            sleepHr = sleepHr + sleepMin / 60
            sleepMin = sleepMin-60
        }

        //If sleep time begins after 00:00, convert back
        if(sleepHr>=24) {
            if (sleepHr == 24) sleepHr = 0
            else if (sleepHr == 25) sleepHr = 1
            else if (sleepHr == 26) sleepHr = 2
            else if (sleepHr == 27) sleepHr = 3
            tv_ampm!!.text = "AM"
        }

        if(sleepMin<10) tv_avgbedtime!!.text = sleepHr.toString() + ":" + "0" + sleepMin.toString()
        else tv_avgbedtime!!.text = sleepHr.toString() + ":" + sleepMin.toString()

        if(wakeMin<10) tv_avgwakeuptime!!.text = wakeHr.toString() + ":" + "0" + wakeMin.toString()
        else tv_avgwakeuptime!!.text = wakeHr.toString() + ":" + wakeMin.toString()

        tv_avghrs!!.text = (sleepDur/60).toString()
        tv_avgmins!!.text = (sleepDur%60).toString()
    }
    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SleepDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
