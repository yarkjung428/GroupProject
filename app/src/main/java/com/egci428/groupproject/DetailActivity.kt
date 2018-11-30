package com.egci428.groupproject

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_detail.*
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.egci428.groupproject.MyService.Companion.sleepDataAvailable
import com.egci428.groupproject.MyService.Companion.sleepDuration
import com.egci428.groupproject.MyService.Companion.sleepTime
import com.egci428.groupproject.MyService.Companion.wakeupTime
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity(), SleepTrendFragment.OnFragmentInteractionListener, SleepDetailFragment.OnFragmentInteractionListener {

    var myDialog: Dialog? = null
    var timePickerDialog: TimePickerDialog? = null
    var datePickerDialog: DatePickerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        //Set dialog
        myDialog = Dialog(this);
        myDialog!!.setContentView(R.layout.fragment_sleepduration)
        val tv_save = myDialog!!.findViewById(R.id.tvSave) as TextView
        val tv_cancel = myDialog!!.findViewById(R.id.tvCancel) as TextView
        val tv_sleepTime = myDialog!!.findViewById(R.id.tv_sleeptime) as TextView
        val tv_wakeupTime = myDialog!!.findViewById(R.id.tv_wakeuptime) as TextView
        val tv_sleepDate = myDialog!!.findViewById(R.id.tv_sleepdate) as TextView
        val tv_wakeupDate = myDialog!!.findViewById(R.id.tv_wakeupdate) as TextView
        val tv_hrs = myDialog!!.findViewById(R.id.tv_hrs) as TextView
        val tv_mins = myDialog!!.findViewById(R.id.tv_mins) as TextView

        //If new sleeping data is available, show dialog -> allowing user to save the data (When detecting movement after the phone is sleep for more than 4 hours at night time)
        if(sleepDataAvailable){
            val splitedSleepTime = sleepTime!!.split("_")
            val splitedWakeupTime = wakeupTime!!.split("_")

            tv_sleepDate.setText(splitedSleepTime.get(0))
            tv_wakeupDate.setText(splitedWakeupTime.get(0))
            tv_sleepTime.setText(splitedSleepTime.get(1))
            tv_wakeupTime.setText(splitedWakeupTime.get(1))

            var hrs: Int = (sleepDuration!!/3600).toInt()
            var mins: Int = ((sleepDuration!! - hrs*3600)/60).toInt()
            tv_hrs.setText(hrs.toString())
            tv_mins.setText(mins.toString())
            myDialog!!.show()
        }

        //Set listener for dialog: Save and cancel
        tv_save.setOnClickListener(){
            //Save data
            sleepDataAvailable = false
            // Write a message to the database
            val database = FirebaseDatabase.getInstance()
            var myRef = database.getReference("Date/"+ MyService.currentDate +" " +  MyService.Month[MyService.currentMonth!!]+ " " + MyService.currentYear +"/sleepDuration")
            myRef.setValue(sleepDuration)

            myRef = database.getReference("Date/"+ MyService.currentDate +" " +  MyService.Month[MyService.currentMonth!!]+ " " + MyService.currentYear +"/sleepTime")
            myRef.setValue(sleepTime)

            myRef = database.getReference("Date/"+ MyService.currentDate +" " +  MyService.Month[MyService.currentMonth!!]+ " " + MyService.currentYear +"/wakeupTime")
            myRef.setValue(wakeupTime)
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()

            myDialog!!.dismiss()
        }

        tv_cancel.setOnClickListener(){
            sleepDataAvailable = false
            myDialog!!.dismiss()
        }

        //Set listener for dialog: Timepicker for sleeptime and wakeuptime
        tv_sleepTime.setOnClickListener(){
            timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                var sdf = SimpleDateFormat("HH:mm");
                var tempSleep = sdf.parse(selectedHour.toString() + ":" + selectedMinute.toString());

                tv_sleepTime.setText(sdf.format(tempSleep))
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true)
            timePickerDialog!!.show()
        }

        tv_wakeupTime.setOnClickListener(){
            timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                var sdf = SimpleDateFormat("HH:mm");
                var tempWakeup = sdf.parse(selectedHour.toString() + ":" + selectedMinute.toString());

                tv_wakeupTime.setText(sdf.format(tempWakeup))
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true)
            timePickerDialog!!.show()
        }

        //Set listener for dialog: Datepicker for sleepdate and wakeupdate
        tv_sleepDate.setOnClickListener(){
            datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, selectedYear, selectedMonth, selectedDay ->
                var sdf = SimpleDateFormat("d-MMM-yyyy");
                val cal = Calendar.getInstance()
                cal.set(Calendar.MONTH, selectedMonth)
                var tempSleep = sdf.parse(selectedDay.toString() + "-" + SimpleDateFormat("MMMM").format(cal.getTime()) + "-" + selectedYear.toString());

                tv_sleepDate.setText(sdf.format(tempSleep));
            }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog!!.show();
        }

        tv_wakeupDate.setOnClickListener(){
            datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, selectedYear, selectedMonth, selectedDay ->
                var sdf = SimpleDateFormat("d-MMM-yyyy");
                val cal = Calendar.getInstance()
                cal.set(Calendar.MONTH, selectedMonth)
                var tempWakeup = sdf.parse(selectedDay.toString() + "-" + SimpleDateFormat("MMMM").format(cal.getTime()) + "-" + selectedYear.toString());

                tv_wakeupDate.setText(sdf.format(tempWakeup));
            }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog!!.show();
        }

        //Add fragment to this Activity
        val fragment = SleepDetailFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_container, fragment!!)
        transaction.commit()

        imv_detail.setOnClickListener(){
            val fragment = SleepDetailFragment()
            val transaction = supportFragmentManager.beginTransaction()
            imv_detail.setImageResource(R.drawable.detail_strack_selected)
            imv_trend.setImageResource(R.drawable.detail_trend)
            transaction.replace(R.id.fl_container, fragment!!)
            transaction.commit()
        }

        imv_trend.setOnClickListener(){
            val fragment = SleepTrendFragment()
            val transaction = supportFragmentManager.beginTransaction()
            imv_detail.setImageResource(R.drawable.detail_track)
            imv_trend.setImageResource(R.drawable.detail_strend_selected)
            transaction.replace(R.id.fl_container, fragment!!)
            transaction.commit()
        }

        //Set listener for actionbar:
        actionbar_back.setOnClickListener(){ this.finish() }
    }

    override fun onFragmentInteraction(uri: Uri) {}
}
