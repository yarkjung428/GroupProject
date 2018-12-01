package com.egci428.groupproject

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private var myService: MyService? = null;
    private val TIME_OUT: Int = 6000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myService = MyService()
        startService(Intent(this, myService!!::class.java))

        val serviceIntent = Intent(this, MyService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)



        Handler().postDelayed({
            val i = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(i)
            finish()
        }, TIME_OUT.toLong())
    }

}
