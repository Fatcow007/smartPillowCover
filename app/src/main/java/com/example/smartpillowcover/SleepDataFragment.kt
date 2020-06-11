package com.example.smartpillowcover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


class SleepDataFragment: MyFragment() {

    lateinit var sleepDataDetailButton : Button
    lateinit var sleepDataPointTextView : TextView
    lateinit var sleepDataPointComparisonTextView : TextView
    lateinit var sleepDataRecommendTextView : TextView
    lateinit var sleepDataSnorePercentTextView : TextView
    lateinit var asmrOnOffSwitch : Switch
    val SNORE_THRESHOLD_DB = 90.0
    val SNORE_AVERAGE_DB = 110.0
    val SNORE_MIN_FREQ = 100.0
    val SNORE_MAX_FREQ = 160.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sleepdata, container, false)
    }

    override fun update(d : SleepData) {
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sleepDataDetailButton = view.findViewById(R.id.sleepDataDetailButton)
        sleepDataDetailButton.setOnClickListener { openDetailFragment() }
        sleepDataPointTextView = view.findViewById(R.id.sleepDataPointTextView)
        sleepDataPointComparisonTextView = view.findViewById(R.id.sleepDataPointComparisonTextView)
        sleepDataRecommendTextView = view.findViewById(R.id.sleepDataRecommendTextView)
        sleepDataSnorePercentTextView = view.findViewById(R.id.sleepDataSnorePercentTextView)

        asmrOnOffSwitch = view.findViewById(R.id.asmrOnOffSwitch)
        asmrOnOffSwitch.isChecked = (activity as MainActivity).currentAsmrOnOff
        asmrOnOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            val act = activity as MainActivity
            if (isChecked) {
                // The toggle is enabled
                val data = "SO1"
                if(act.sendBluetoothData(data)){
                    Toast.makeText(act.applicationContext, getString(R.string.asmr_on_text), Toast.LENGTH_SHORT).show()
                    act.currentAsmrOnOff = true
                }else{
                    Toast.makeText(act.applicationContext, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show()
                    asmrOnOffSwitch.isChecked = false
                }
            } else {
                // The toggle is disabled
                val data = "SO0"
                if(act.sendBluetoothData(data)){
                    Toast.makeText(act.applicationContext, getString(R.string.asmr_off_text), Toast.LENGTH_SHORT).show()
                    act.currentAsmrOnOff = false
                }else{
                    Toast.makeText(act.applicationContext, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show()
                    asmrOnOffSwitch.isChecked = false
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        loadUi()
    }


    fun openDetailFragment(){
        activity!!.supportFragmentManager
            .beginTransaction()
            .add(
                R.id.mainActivityLayout,
                SleepDataDetailFragment(),
                "DetailFragment"
            )
            .addToBackStack("DetailFragment")
            .commit()
    }

    fun calculateSleepPoint(){


        val sleepDataList = getData()
        val movementList = mutableListOf<Double>()
        val dBList = mutableListOf<Double>()
        if(sleepDataList != null){
            val timeSet = sleepDataList.keys.toList()
            for (i in timeSet.indices){
                if(i + 1 != timeSet.size){
                    val t1 = timeSet[i]
                    val t2 = timeSet[i+1]
                    val d1 = sleepDataList[t1]
                    val d2 = sleepDataList[t2]
                    if(d1 != null && d2 != null){
                        movementList.add(getDistance(d1.px, d1.py, d2.px, d2.py))
                    }
                }
                val dB = sleepDataList[timeSet[i]]!!.dB
                val freq = sleepDataList[timeSet[i]]!!.freq

                if(
                    dB > SNORE_THRESHOLD_DB &&
                    freq < SNORE_MAX_FREQ &&
                    freq > SNORE_MIN_FREQ)
                dBList.add(dB)
            }
        }
        val dBCountRatio = dBList.size.toDouble() / movementList.size.toDouble()
        val dBSizeRatio = dBList.average() / SNORE_AVERAGE_DB
        val movementRatio = movementList.average() / 2.0

        var finalPoint = (dBCountRatio * 0.6 + dBSizeRatio * 0.1 + movementRatio * 0.3) * 100
        if(!finalPoint.isNaN()){
            sleepDataPointTextView.text =  "%.1f".format(finalPoint) + "점"
            saveSleepPointData(activity as MainActivity, getYearMonthDayAsString(), finalPoint)
        }else{
            sleepDataPointTextView.text =  "데이터가 없어요."
        }


        val sleepPointList = mutableListOf<Double>()
        sleepPointList.add(finalPoint)
        for (i in 0..6){
            calendar.add(Calendar.DAY_OF_YEAR, -1) // yesterday
            val sp = getSleepPointData(activity as MainActivity, getYearMonthDayAsString())
            if(sp > 0){
                sleepPointList.add(sp)
            }
        }
        var aveSleepPoint = sleepPointList.average()
        var text = ""
        if(!aveSleepPoint.isNaN()){
            sleepDataPointComparisonTextView.text =  "%.1f".format(finalPoint) + "점"
        }else{
            sleepDataPointComparisonTextView.text = "데이터가 없어요."
        }

        if(!aveSleepPoint.isNaN() || !finalPoint.isNaN()) {
            text = if (aveSleepPoint < finalPoint + 0.01) {
                "수면 점수가 올라가고 있어요!\n앞으로도 계속 이대로 주무시면 됩니다."
            } else if (aveSleepPoint < finalPoint + 5.0) {
                "어제 밤에는 자는게 조금 힘들었나요?\n수면 환경을 조금 바꿔보세요!"
            } else {
                "어제는 거의 못 주무신거 같아요.\n오늘은 한번 숙면을 취해보세요."
            }
        }

        sleepDataRecommendTextView.text = text
        val dBCountRatioPercent = dBCountRatio * 100.0

        var t2 = "%.1f".format(dBCountRatioPercent) + "%"
        if(dBCountRatioPercent.isNaN()){
            t2 = "데이터가 없어요."
        }
        sleepDataSnorePercentTextView.text = t2
        calendar = Calendar.getInstance()
    }

    fun getDistance(p1x : Double, p1y : Double, p2x : Double, p2y : Double) : Double{
        return sqrt((p1x - p2x).pow(2) + (p1y - p2y).pow(2))
    }

    fun loadUi() {
        calculateSleepPoint()
    }

}
