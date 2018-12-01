package com.egci428.groupproject

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.support.annotation.Nullable
import java.text.SimpleDateFormat
import java.util.*
import android.os.PowerManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.concurrent.schedule
import kotlin.properties.Delegates

class MyService : Service(), SensorEventListener, StepListener {

    companion object {
        val CHANNEL_ID: String = "exampleServiceChannel"
        var sleepDataAvailable: Boolean = false
        var sleepDuration: Long = 0
        var sleepTime: String ="??"
        var wakeupTime: String = "??"
        var numSteps: Int =0

        var currentDate:Int? = null
        var currentMonth:Int? = null
        var currentYear:Int? = null

        val Month = arrayListOf<String>("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        var dataList:MutableList<dataSet>  = mutableListOf()
        var limitNum:Int = 7

        var lat = ArrayList<Double>()
        var long = ArrayList<Double>()
    }

    private var sensorManager: SensorManager? = null;
    private var mAccelerometer: Sensor? = null
    private var lastTime: String? = null
    private var lastUpdate: Long = 0
    private var simpleStepDetector: StepDetector? = null

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sdf = SimpleDateFormat("d-MMM-yyyy_HH:mm")
        lastTime = sdf.format(Calendar.getInstance().getTime())
        lastUpdate = System.currentTimeMillis()

        //Update date variable to current date
        updateCurrentDate()
        checkFirabaseData()
        //updateFireBaseData()
        //connectToFirebase()
        //mockData()
        val notificationIntent = Intent(this,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Example Service")
                .setContentText("Test!")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        //Get sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var sensors = sensorManager!!.getSensorList(Sensor.TYPE_ALL);
        if (mAccelerometer != null) {
            sensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            simpleStepDetector = StepDetector()
            simpleStepDetector!!.registerListener(this)
        }
    }

    //Sensor
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { 1 }
    override fun onSensorChanged(event: SensorEvent?) {
        if(event!!.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //if(sleepDataAvailable==false) {
            val sdf = SimpleDateFormat("HH")
            val isNight = sdf.format(Calendar.getInstance().getTime())
            if ((isNight.toInt() >= 20 && isNight.toInt() <= 24) || (isNight.toInt() >= 0 && isNight.toInt() <= 12)) {
                getAccelerometerSleep(event)
            }
            //}
            //Update walking step
            simpleStepDetector!!.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]
            )
        }
    }

    private fun getAccelerometerSleep(event: SensorEvent){
        val values = event.values;

        val x = values[0];
        val y = values[1];
        val z = values[2];
        val accel = (x*x + y*y + z*z)/(SensorManager.GRAVITY_EARTH*SensorManager.GRAVITY_EARTH);
        val actualTime = System.currentTimeMillis();

        //Check accelelator
        if(accel>=2){
            Log.d("Sensor status: ", "MOVED")
            //Check lastUpdate: if an hour
            if((actualTime - lastUpdate)>14400000){ //Change to 14400000 to change 4 hour
                //Check the locked state of the phone
                val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                var isLocked: Boolean

                if (km.inKeyguardRestrictedInputMode()) {
                    isLocked = true
                    Log.d("Locked status: ", "LOCKED")
                    return
                } else {
                    //inKeyguardRestrictedInputMode() returns false if
                    //(1) screen is not locked
                    //(2) password is not set in setting
                    //so need to check again in case the password is not set for the device (2)
                    //to ensure the screen is really unlocked
                    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                        isLocked = !powerManager.isInteractive

                        //Check locked state: isLocked = true, screen is locked ---- isLocked = false, screen is unlocked
                        if(isLocked) { return }
                        else {
                            val wakeupTimeMillis = System.currentTimeMillis();
                            val sdf = SimpleDateFormat("d-MMM-yyyy_HH:mm");
                            wakeupTime = sdf.format(Calendar.getInstance().getTime());
                            sleepTime = lastTime!!
                            sleepDuration = (wakeupTimeMillis-lastUpdate)/1000
                            sleepDataAvailable = true

                            Log.d("Locked status: ", "UNLOCKED")
                            val intent = Intent(applicationContext, DetailActivity::class.java);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent);
                        }
                    } else {
                        isLocked = !powerManager.isScreenOn
                        if(isLocked) return
                        else {
                        }
                    }
                }
            }
            lastUpdate = actualTime
            if((actualTime - lastUpdate)<14400000) {
                val sdf = SimpleDateFormat("d-MMM-yyyy_HH:mm");
                lastTime = sdf.format(Calendar.getInstance().getTime());
            }
        }
    }

    private fun connectToFirebase(){
        val database = FirebaseDatabase.getInstance()
        var myRef = database.getReference()
        //Get number of data in firebase
        val userReference = FirebaseDatabase.getInstance().getReference().child("Date");
        var childNum: Long = 0
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                childNum = dataSnapshot.childrenCount
                //If there is less than 7 data, only get those number of data, else get 7 lastest data
                if(childNum<7){
                    limitNum=childNum.toInt()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        //Sort data by timeStamp (Biggest==last) then get last 7  entry
        val postQuery = myRef.child("Date").orderByChild("timeStamp")
        // Attach a listener to read the data at our posts reference
        postQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot!!.exists()){
                    dataList.clear()
                    for(i in dataSnapshot.children){
                        val data = i.getValue(dataSet::class.java)
                        dataList.add(data!!)
                    }
                    Toast.makeText(applicationContext,"Update completed",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun updateCurrentDate(){
        var sdf = SimpleDateFormat("d")
        currentDate = sdf.format(Calendar.getInstance().getTime()).toInt()
        sdf = SimpleDateFormat("MM")
        currentMonth = sdf.format(Calendar.getInstance().getTime()).toInt()
        sdf = SimpleDateFormat("yyyy")
        currentYear = sdf.format(Calendar.getInstance().getTime()).toInt()
        //Toast.makeText(this, Month[1], Toast.LENGTH_SHORT).show()
    }

    private fun updateFireBaseData(){
        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        var myRef = database.getReference("Date/"+ currentDate+" " +  Month[currentMonth!!]+ " " + currentYear)

        val data = dataSet(sleepDataAvailable, sleepDuration, sleepTime,wakeupTime,numSteps, System.currentTimeMillis() / 1000L)
        myRef.setValue(data)
        Toast.makeText(this, "Data update", Toast.LENGTH_SHORT).show()
    }

    fun delayFunction(function: ()-> Unit, delay: Long) {
        Handler().postDelayed(function, delay)
    }

    private fun updatenumSteps(){
        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        var myRef = database.getReference("Date/"+ currentDate+" " +  Month[currentMonth!!]+ " " + currentYear+"/numSteps")

        myRef.setValue(numSteps)
        //Toast.makeText(this, "numSteps update", Toast.LENGTH_SHORT).show()
    }

    private fun checkFirabaseData(){
        val database = FirebaseDatabase.getInstance()
        var myRef = database.getReference("Date/"+ currentDate+" " +  Month[currentMonth!!]+ " " + currentYear)
        // Attach a listener to read the data at our posts reference
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(dataSet::class.java)
                if(value==null){
                    //Toast.makeText(applicationContext, "NULL!!!", Toast.LENGTH_SHORT).show()
                    val sdf = SimpleDateFormat("d-MMM-yyyy_HH:mm")

                    sleepDataAvailable = false
                    sleepDuration = 0
                    sleepTime ="??"
                    wakeupTime = sdf.format(Calendar.getInstance().getTime())
                    numSteps = 0
                    updateFireBaseData()
                }
                else{
                    //Toast.makeText(applicationContext, value.numSteps.toString(), Toast.LENGTH_SHORT).show()

                    /*sleepDataAvailable = value!!.sleepDataAvailable
                    sleepDuration = value!!.sleepDuration
                    sleepTime =value!!.sleepTime
                    wakeupTime = value!!.wakeupTime*/
                    numSteps = value!!.numSteps
                    connectToFirebase()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun step(timeNs: Long) {
        numSteps++
        updatenumSteps()
    }



    /*private fun mockData(){
        var date = arrayListOf("0")
        for(i in 1..29){
            date.add(i.toString())
        }

        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        var myRef = database.getReference("Date1/"+ currentDate+" " +  Month[currentMonth!!]+ " " + currentYear)

        for(i in 1..29) {
            var numS = Random().nextInt(5000 - 0 + 1)
            var time = Random().nextInt(23 - 20 + 1) + 20
            var time1 = Random().nextInt(12 - 5 + 1) + 5
            var sleepDur = Random().nextInt(30000 - 14400 + 1) + 14400
            if (i < 10) {
                myRef = database.getReference("Date/" + "0" + date.get(i) + " " + "Nov" + " " + "2018")
            } else myRef = database.getReference("Date/" + date.get(i) + " " + "Nov" + " " + "2018")
            val data = dataSet(sleepDataAvailable, sleepDur.toLong(), date.get(i - 1) + "-Nov-2018_" + time + ":25", date.get(i) + "-Nov-2018_" + time1 + ":25", numS, System.currentTimeMillis() / 1000L)
            myRef.setValue(data)
        }
        Toast.makeText(this, "Data update", Toast.LENGTH_SHORT).show()
    }*/
}