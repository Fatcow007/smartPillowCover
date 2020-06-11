package com.example.smartpillowcover


import androidx.fragment.app.Fragment
import java.util.*

abstract class MyFragment : Fragment(){

    var calendar : Calendar = Calendar.getInstance()
    fun getTextForTextViewDisplay() : String{

        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) + " " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR)
    }
    fun getYearMonthDay() : List<Int>{
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        return listOf(y, m, d)
    }
    fun getYearMonthDayAsString() : String{
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        return listOf(y, m, d).map{ it.toString() }.joinToString("_")
    }
    fun getData() : SortedMap<Int, SleepData>?{
        return getAverageData(activity as MainActivity, getYearMonthDayAsString())
    }

    abstract fun update(d : SleepData)

}
