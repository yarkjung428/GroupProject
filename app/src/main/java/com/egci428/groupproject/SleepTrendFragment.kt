package com.egci428.groupproject

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_sleep_trend.*
import android.support.v4.os.HandlerCompat.postDelayed
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SleepTrendFragment : android.support.v4.app.Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var chartlabels: ArrayList<String>? = null
    private var hscrollview: HorizontalScrollView? = null
    private var tv: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sleep_trend, container, false)
        var barChart = view.findViewById(R.id.barchart) as BarChart
        var lineChart = view.findViewById(R.id.linechart) as LineChart
        var imv_back = view.findViewById(R.id.imv_back) as ImageView
        var imv_next = view.findViewById(R.id.imv_next) as ImageView
        var tv_date = view.findViewById(R.id.tv_date) as TextView
        var tv_hrs = view.findViewById(R.id.tv_hrs) as TextView
        var tv_mins = view.findViewById(R.id.tv_mins) as TextView
        var tv_dwm = view.findViewById(R.id.tv_DWM) as TextView
        tv = view.findViewById(R.id.tv) as TextView

        hscrollview = view.findViewById(R.id.hscrollview) as HorizontalScrollView

        //(default) Function call: when this page is first loaded, the barChart(showing daily sleeping data) will show up
        daysBarData(barChart,lineChart)

        //Set listener: when user clicks on 'back' or 'next' button
        imv_back.setOnClickListener(){
            if(tv_dwm.text == "Day"){
                tv_dwm.setText("Month")
                weekmonthLineData(barChart,lineChart,1)
            }
            else if(tv_dwm.text == "Week"){
                tv_dwm.setText("Day")
                daysBarData(barChart,lineChart)
            }
            else if(tv_dwm.text == "Month") {
                tv_dwm.setText("Week")
                weekmonthLineData(barChart,lineChart,0)
            }
        }

        imv_next.setOnClickListener(){
            if(tv_dwm.text == "Day"){
                tv_dwm.setText("Week")
                weekmonthLineData(barChart,lineChart,0)
            }
            else if(tv_dwm.text == "Week"){
                tv_dwm.setText("Month")
                weekmonthLineData(barChart,lineChart,1)
            }
            else if(tv_dwm.text == "Month") {
                tv_dwm.setText("Day")
                daysBarData(barChart,lineChart)
            }
        }

        //Set listener for barChart
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val x = e.getX()
                val y = e.getY()
                val time = y.toString().split(".")
                var timeMin: String = ""

                //When chart is clicked, if the data is 6.40, it will set text to 4 min instead of 40
                //So we add after it
                if(time.get(1).length == 1) timeMin = time.get(1) + "0"
                else timeMin = time.get(1)

                //set selected data to textview
                tv_date.text = chartlabels!!.get(x.toInt())
                tv_hrs.text = time.get(0)
                tv_mins.text = timeMin
            }
            override fun onNothingSelected() {}
        })

        //Set listener for lineChart
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val x = e.getX()
                val y = e.getY()

                val time = y.toString().split(".")
                var timeMin: String = ""

                //When chart is clicked, if the data is 6.40, it will set text to 4 min instead of 40
                //So we add after it
                if(time.get(1).length == 1) timeMin = time.get(1) + "0"
                else timeMin = time.get(1)

                //set selected data to textview
                tv_date.text = chartlabels!!.get(x.toInt())
                tv_hrs.text = time.get(0)
                tv_mins.text = timeMin
            }
            override fun onNothingSelected() {}
        })
        return view
    }

    //Prepare data for drawing barChart
    fun daysBarData(barChart: BarChart, lineChart: LineChart){
        //Set visibility: show barChart, hide lineChart
        barChart.visibility = View.VISIBLE
        lineChart.visibility = View.INVISIBLE

        //Set data
        val barEntries = ArrayList<BarEntry>()
        val currentDate = MyService.dataList[MyService.dataList.size-1].wakeupTime
        val spltCurrentDate = currentDate.split("-")
        var count = spltCurrentDate.get(0).toInt()
        val temp = MyService.dataList.size-count

        //get no of data
        for(i in 1..count) barEntries.add(BarEntry(i.toFloat(), 0f))
        for (i in count downTo 1) {
            var min: Long = 0
            if(temp>=0) {
                min = MyService.dataList[temp + i - 1].sleepDuration / 60
                Log.d("Sleep duration days: ", (MyService.dataList[temp+i-1].sleepDuration/60).toString())
            } else {
                min = MyService.dataList[i - 1].sleepDuration / 60
                Log.d("Sleep duration days: ", (MyService.dataList[i-1].sleepDuration/60).toString())
            }
            val tempmin = min%60
            var realmin: String = ""
            if(tempmin.toInt()<10) realmin = "0" + tempmin.toString()
            else realmin = tempmin.toString()
            var time = (min/60).toString() + "." + realmin
            barEntries.set(i-1, BarEntry(i.toFloat(), time.toFloat()))
        }

        //Set labels: "DD/MM"
        val date = SimpleDateFormat("MMMM").parse(spltCurrentDate.get(1))
        val cal = Calendar.getInstance()
        cal.time = date

        val labels = arrayListOf("")
        for(j in 1..31) labels.add(j.toString()+"/"+ ((cal.get(Calendar.MONTH)).toInt()+1).toString())

        barSetData(barChart,barEntries,labels, spltCurrentDate.get(0).toInt())

        //Adjust chart width according to the data size
        if(count<=10) {
            barChart.getLayoutParams().width = 1080
            lineChart.getLayoutParams().width = 1080
            tv!!.getLayoutParams().width = 1080
        }
        else {
            barChart.getLayoutParams().width = 100 * spltCurrentDate.get(0).toInt();
            lineChart.getLayoutParams().width = 100 * spltCurrentDate.get(0).toInt();
            tv!!.getLayoutParams().width = 100 * spltCurrentDate.get(0).toInt();
            hscrollview!!.post(Runnable {
                hscrollview!!.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
            })
        }
    }

    //Prepare data for drawing lineChart
    fun weekmonthLineData(barChart: BarChart, lineChart: LineChart, weekormonth: Int){
        //Set visibility: show lineChart, hide barChart
        lineChart.visibility = View.VISIBLE
        barChart.visibility = View.INVISIBLE

        //Set data: Week
        val lineEntries = ArrayList<Entry>()
        var weekLabels: ArrayList<String> = arrayListOf("", "", "", "", "", "", "", "", "", "", "", "", "")
        if(weekormonth==0) {
            var count: Int = MyService.dataList.size
            var dataCount = 0
            var sleepDur: Long = 0

            //get no of data
            for(i in 1..12) lineEntries.add(Entry(i.toFloat(), 0f))
            for (i in 12 downTo 1) {
                //Set labels: "DD/MM"
                if(count>0) {
                    val currentDate = MyService.dataList[count - 1].wakeupTime
                    val spltCurrentDate = currentDate.split("-")
                    val date = SimpleDateFormat("MMMM").parse(spltCurrentDate.get(1))
                    val cal = Calendar.getInstance()
                    cal.time = date
                    weekLabels.set(i, spltCurrentDate.get(0) + "/" + ((cal.get(Calendar.MONTH)).toInt() + 1).toString())
                }
                sleepDur = 0
                dataCount = 0
                //Calculate average sleep duration
                for (j in 1..7) {
                    if (count > 0) {
                        if(MyService.dataList[count - 1].sleepDuration.toInt() != 0) {
                            sleepDur = sleepDur + MyService.dataList[count - 1].sleepDuration
                            dataCount++
                        }
                        count--
                    }
                }
                var time = "0"
                if(dataCount > 0) time = (((sleepDur / dataCount) / 60) / 60).toString() + "." + (((sleepDur / dataCount) / 60) % 60).toString()
                lineEntries.set(i-1,Entry(i.toFloat(), time.toFloat()))
            }
        }

        //Set data: Month
        var monthLabel: ArrayList<String> = arrayListOf("")
        var monthLabelIndex: ArrayList<String> = arrayListOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        if(weekormonth==1){
            var count: Int = MyService.dataList.size
            var dataCount = 0
            var sleepDur: Long = 0

            //Set labels: "DD/MM"
            var currentDate = MyService.dataList[count - 1].wakeupTime
            var spltCurrentDate = currentDate.split("-")
            val date = SimpleDateFormat("MMMM").parse(spltCurrentDate.get(1))
            val cal = Calendar.getInstance()
            cal.time = date
            var currentMonth = ((cal.get(Calendar.MONTH)) + 1)

            for(i in 1..12) {
                currentMonth++
                if(currentMonth==13) currentMonth = 1
                monthLabel.add(monthLabelIndex.get(currentMonth))
            }

            //get no of data
            for(i in 1..12) lineEntries.add(Entry(i.toFloat(), 0f))
            for (i in 12 downTo 1) {
                sleepDur = 0
                dataCount = 0

                if(count>0) {
                    currentDate = MyService.dataList[count - 1].wakeupTime
                    spltCurrentDate = currentDate.split("-")
                }
                //Calculate average sleep duration
                for (j in 1..spltCurrentDate.get(0).toInt()) {
                    if (count > 0) {
                        if(MyService.dataList[count - 1].sleepDuration.toInt() != 0) {
                            sleepDur = sleepDur + MyService.dataList[count - 1].sleepDuration
                            dataCount++
                        }
                        count--
                    }
                }
                var time = "0"
                if(dataCount>0) time = (((sleepDur / dataCount) / 60) / 60).toString() + "." + (((sleepDur / dataCount) / 60) % 60).toString()
                lineEntries.set(i-1,Entry(i.toFloat(), time.toFloat()))
            }
        }
        //Set labels: If 'weekormonth' == 0, labels of week. If 'weekormonth' == 1, labels of month
        if(weekormonth == 0) lineSetData(lineChart,lineEntries,weekLabels)
        if(weekormonth == 1) lineSetData(lineChart,lineEntries,monthLabel)

        //Adjust chart width according to the data size
        barChart.getLayoutParams().width = 2000
        lineChart.getLayoutParams().width = 2000
        tv!!.getLayoutParams().width = 2000
    }

    //Set data to the barChart
    fun barSetData(barChart: BarChart, barEntries: ArrayList<BarEntry>, labels: ArrayList<String>, count: Int){
        //Set data & properties
        val barDataSet = BarDataSet(barEntries, "DATA SET 1")
        barDataSet.setColors(Color.parseColor("#dbbb3a"))
        var data = BarData(barDataSet)

        data.barWidth = 0.2f
        barChart.setData(data)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.axisLeft.axisMinimum = 0f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setLabelCount(count)

        barChart.animateY(1000)

        //Keep the current showing labels on the Chart in 'chartlabels'
        //So when the data is selected on the Chart, it will be easier referring to which data is selected
        chartlabels = labels
    }

    //Set data to the lineChart
    fun lineSetData(lineChart: LineChart, lineEntries: ArrayList<Entry>, labels: ArrayList<String>){
        //Set data & properties
        val lineDataSet = LineDataSet(lineEntries, "DATA SET 1")
        lineDataSet.setColors(Color.parseColor("#dbbb3a"))
        var data = LineData(lineDataSet)

        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(9f);
        lineDataSet.setDrawFilled(true);

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet)

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        lineChart.axisLeft.axisMinimum = 0f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setLabelCount(12)

        lineChart.setData(data)
        lineChart.animateY(2000)

        //Keep the current showing labels on the Chart in 'chartlabels'
        //So when the data is selected on the Chart, it will be easier referring to which data is selected
        chartlabels = labels
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
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener") as Throwable
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
                SleepTrendFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
