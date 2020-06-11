package com.example.smartpillowcover

import android.content.Context
import android.util.Log
import java.io.*
import java.util.*

val FILE_PREFIX = "SD_"
val FILE_EXTENSION = "sleepdata"

class SleepData(
    val hour : Int,
    val minute : Int,
    val seconds : Int,
    val humidity : Double,
    val temperature : Double,
    val px : Double,
    val py : Double,
    val dB : Double,
    val freq : Double
) : Serializable
{
    fun getTimeAsInt() : Int{return hour*3600 + minute*60 + seconds}
}


var currentDay : String = ""
var currentData : SortedMap<Int, SleepData> ?= null
var averageData : SortedMap<Int, SleepData> ?= null

fun getFileName(date : String) : String{
    return FILE_PREFIX + date + "." + FILE_EXTENSION
}


fun loadData(activity : MainActivity, date : String) : SortedMap<Int, SleepData>?{
    try{
        if(currentData == null || date != currentDay){
            val fis: FileInputStream = activity.applicationContext.openFileInput(getFileName(date))
            val ist = ObjectInputStream(fis)
            val sleepDataMap: SortedMap<Int, SleepData> = ist.readObject() as SortedMap<Int, SleepData>
            ist.close()
            fis.close()
            currentData = sleepDataMap
            Log.d("DATA", "Data loaded successfully")
            return sleepDataMap
        }else{
            return currentData!!
        }

    }catch(e : FileNotFoundException){
        return null
    }
}

fun getAverageData(activity : MainActivity, date : String) : SortedMap<Int, SleepData>?{
    val averageCount = 10
    val resultData = sortedMapOf<Int, SleepData>()
    if(averageData == null || date != currentDay){
        currentDay = date
        currentData = loadData(activity, date)
        val chunkedData = currentData?.values?.chunked(averageCount) ?: null
        if(chunkedData != null){
            for(dataChunk in chunkedData){
                val size = dataChunk.size.toDouble()
                var aveHour : Int = 0
                var aveMinute : Int = 0
                var aveSeconds : Int = 0
                var aveHumidity : Double = 0.0
                var aveTemperature : Double = 0.0
                var avePx : Double = 0.0
                var avePy : Double = 0.0
                var aveDB : Double = 0.0
                var aveFreq : Double = 0.0
                for(data in dataChunk){
                    aveHour += data.hour
                    aveMinute += data.minute
                    aveSeconds += data.seconds
                    aveHumidity += data.humidity
                    aveTemperature += data.temperature
                    avePx += data.px
                    avePy += data.py
                    aveDB += data.dB
                    aveFreq += data.freq
                }
                aveHour = (aveHour.toDouble()/size).toInt()
                aveMinute = (aveMinute.toDouble()/size).toInt()
                aveSeconds = (aveSeconds.toDouble()/size).toInt()
                aveHumidity = aveHumidity/size
                aveTemperature = aveTemperature/size
                avePx = avePx/size
                avePy = avePy/size
                aveDB = aveDB/size
                aveFreq = aveFreq/size
                val averageSleepData = SleepData(aveHour, aveMinute, aveSeconds, aveHumidity,
                aveTemperature, avePx, avePy, aveDB, aveFreq)
                resultData[averageSleepData.getTimeAsInt()] = averageSleepData
            }
        }
        averageData = resultData
    }
    return averageData
}

fun saveData(activity : MainActivity, date : String, sleepDataMap : SortedMap<Int, SleepData>) {

    val fos: FileOutputStream = activity.applicationContext.openFileOutput(getFileName(date), Context.MODE_PRIVATE)
    val os = ObjectOutputStream(fos)
    os.writeObject(sleepDataMap)
    os.close()
    fos.close()

    Log.d("DATA", "Data saved successfully")
}

fun addData(activity : MainActivity, date : String, sleepData : SleepData) {
    val existingData = loadData(activity, date)
    val key = sleepData.getTimeAsInt()
    if(existingData == null){
        saveData(activity, date, sortedMapOf(key to sleepData))
    }else{
        existingData.put(key, sleepData)
        saveData(activity, date, existingData)
    }
}

fun getSleepPointData(activity : MainActivity, day : String) : Double{
    val sharedPref = activity.getSharedPreferences(
        "sleepPointPrefKey", Context.MODE_PRIVATE)
    val sleepPoint = sharedPref.getFloat(day, -1f).toDouble()
    return sleepPoint
}

fun saveSleepPointData(activity : MainActivity, day : String, sleepPoint : Double){
    val sharedPref = activity.getSharedPreferences(
        "sleepPointPrefKey", Context.MODE_PRIVATE)
    with (sharedPref.edit()) {
        putFloat(day, sleepPoint.toFloat())
        commit()
    }

}
/*

fun debugDataTest(activity: MainActivity) : List<SleepData>?{
    val TAG = "DataSaveLoadTest"
    val DEBUG_DATE = "2020_5_14"
    val sd = mutableListOf<SleepData>()
    //Create test data
    sd.add(SleepData(12, 0, 0, 10.toFloat(),  87.toFloat(),  97.toFloat(),  97.toFloat()))
    sd.add(SleepData(12, 20, 0, 20.toFloat(),  86.toFloat(),  97.toFloat(),  97.toFloat()))
    sd.add(SleepData(12, 40, 0, 30.toFloat(),  85.toFloat(),  97.toFloat(),  97.toFloat()))
    sd.add(SleepData(13, 0, 0, 40.toFloat(),  84.toFloat(),  97.toFloat(),  97.toFloat()))
    sd.add(SleepData(13, 20, 0, 50.toFloat(),  83.toFloat(),  97.toFloat(),  97.toFloat()))
    sd.add(SleepData(13, 40, 0, 60.toFloat(),  82.toFloat(),  97.toFloat(),  97.toFloat()))


    val DEBUG_DATE2 = "2020_5_13"
    val sd2 = mutableListOf<SleepData>()
    //Create test data
    sd2.add(SleepData(12, 0, 0, 60.toFloat(),  87.toFloat(),  97.toFloat(),  97.toFloat()))
    sd2.add(SleepData(12, 20, 0, 50.toFloat(),  86.toFloat(),  97.toFloat(),  97.toFloat()))
    sd2.add(SleepData(12, 40, 0, 40.toFloat(),  85.toFloat(),  97.toFloat(),  97.toFloat()))
    sd2.add(SleepData(13, 0, 0, 30.toFloat(),  84.toFloat(),  97.toFloat(),  97.toFloat()))
    sd2.add(SleepData(13, 20, 0, 20.toFloat(),  83.toFloat(),  97.toFloat(),  97.toFloat()))
    sd2.add(SleepData(13, 40, 0, 10.toFloat(),  82.toFloat(),  97.toFloat(),  97.toFloat()))

    Log.d(TAG, "DATA CREATED")
    //Save test data
    //saveData(activity, DEBUG_DATE2, sd2)


    Log.d(TAG, "DATA SAVED")
    //Load test data
    val result = loadData(activity, DEBUG_DATE)
    Log.d(TAG, "DATA LOADED")

    if(result != null){
        return result.toList()
    }else{
        return null
    }
}
*/