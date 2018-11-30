package com.egci428.groupproject

class dataSet(val sleepDataAvailable:Boolean,val sleepDuration:Long,val sleepTime:String,var wakeupTime: String,val numSteps:Int,val timeStamp: Long) {
    constructor():this(false,0,"","",0, 0)
}