package com.egci428.groupproject

// Will listen to step alerts
interface StepListener {
    fun step(timeNs: Long)
}