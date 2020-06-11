package com.example.smartpillowcover



import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.util.*

class TempHumidDetailFragment : MyFragment() , DatePickerFragment.DatePickedListener {

    lateinit var humidityGraph : GraphView
    lateinit var tempGraph : GraphView
    lateinit var changeDateButton : Button
    lateinit var currentDateTextView : TextView
    lateinit var humidGraphSeries : LineGraphSeries<DataPoint>
    lateinit var tempGraphSeries : LineGraphSeries<DataPoint>



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_temphumid_detail, container, false)
    }

    override fun update(d : SleepData) {
        val time = d.getTimeAsInt().toDouble()
        val h = d.humidity
        val t = d.temperature
        //humidGraphSeries.appendData(DataPoint(time, h), false, 3600)
        //tempGraphSeries.appendData(DataPoint(time, t), false, 3600)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        humidityGraph = view.findViewById(R.id.humidityGraph) as GraphView
        tempGraph = view.findViewById(R.id.tempGraph) as GraphView
        changeDateButton = view.findViewById(R.id.thChangeDateBtn)
        changeDateButton.setOnClickListener {showDatePickerDialog(view)}
        currentDateTextView = view.findViewById(R.id.tempHumidCurrentDateTextView)



        val (humidityDataPoints, tempDataPoints) = getDataPoints()
        humidGraphSeries = LineGraphSeries(humidityDataPoints)
        tempGraphSeries = LineGraphSeries(tempDataPoints)


        val string = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) + " " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR)
        currentDateTextView.text = string


        with(humidityGraph){
            this.title = getString(R.string.text_humidity)
            this.addSeries(humidGraphSeries)
            this.gridLabelRenderer.numHorizontalLabels = 4
            this.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(
                    value: Double,
                    isValueX: Boolean
                ): String {
                    return if (isValueX) {
                        // show normal x values
                        val ihour = (value / 3600).toInt()
                        var hour = if(ihour < 10) "0$ihour" else "$ihour"
                        val imin = ((value / 60) % 60).toInt()
                        var min = if(imin < 10) "0$imin" else "$imin"
                        val isec = ((value % 60) % 60).toInt()
                        var sec = if(isec < 10) "0$isec" else "$isec"
                        "$hour:$min"
                        //super.formatLabel(value, isValueX)
                    } else {
                        // show currency for y values
                        super.formatLabel(value, isValueX) + "%"
                    }
                }
            }
            this.gridLabelRenderer.textSize = 28.toFloat()
            with(this.viewport){
                this.isScalable = true
                this.isXAxisBoundsManual = true
                this.isYAxisBoundsManual = true
                this.setMinX(humidGraphSeries.lowestValueX)
                this.setMaxX(humidGraphSeries.highestValueX)

                val yOffsetRatio = 0.1

                val graphMaxY = humidGraphSeries.highestValueY
                val graphLowY = humidGraphSeries.lowestValueY
                val yDif = (graphMaxY - graphLowY) * yOffsetRatio

                val newLowY = graphLowY - yDif
                val newMaxY = graphMaxY + yDif
                this.setMinY(newLowY)
                this.setMaxY(newMaxY)
            }
        }


        with(tempGraph){
            this.title = getString(R.string.text_temperature)
            this.addSeries(tempGraphSeries)
            this.gridLabelRenderer.numHorizontalLabels = 4
            this.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(
                    value: Double,
                    isValueX: Boolean
                ): String {
                    return if (isValueX) {
                        // show normal x values
                        val ihour = (value / 3600).toInt()
                        var hour = if(ihour < 10) "0$ihour" else "$ihour"
                        val imin = ((value / 60) % 60).toInt()
                        var min = if(imin < 10) "0$imin" else "$imin"
                        val isec = ((value % 60) % 60).toInt()
                        var sec = if(isec < 10) "0$isec" else "$isec"
                        "$hour:$min"
                        //super.formatLabel(value, isValueX)
                    } else {
                        // show currency for y values
                        super.formatLabel(value, isValueX) + "°C"
                    }
                }
            }
            this.gridLabelRenderer.textSize = 28.toFloat()
            with(this.viewport){
                this.isScalable = true
                this.isXAxisBoundsManual = true
                this.isYAxisBoundsManual = true
                this.setMinX(tempGraphSeries.lowestValueX)
                this.setMaxX(tempGraphSeries.highestValueX)

                val yOffsetRatio = 0.1

                val graphMaxY = tempGraphSeries.highestValueY
                val graphLowY = tempGraphSeries.lowestValueY
                val yDif = (graphMaxY - graphLowY) * yOffsetRatio

                val newLowY = graphLowY - yDif
                val newMaxY = graphMaxY + yDif
                this.setMinY(newLowY)
                this.setMaxY(newMaxY)
            }

        }



        //debugDataTest(activity as MainActivity)


    }

    override fun onStart() {
        super.onStart()
    }




    fun showDatePickerDialog(v: View) {
        val date = getYearMonthDay()
        val frag = DatePickerFragment(date[0], date[1], date[2])
        frag.setTargetFragment(this, 256)
        frag.show(fragmentManager, "timePicker")
    }

    fun loadUi() {

        humidityGraph.isEnabled = true
        tempGraph.isEnabled = true
        humidityGraph.isVisible = true
        tempGraph.isVisible = true
        currentDateTextView.text = getTextForTextViewDisplay()
        humidityGraph.removeAllSeries()
        tempGraph.removeAllSeries()
        val (humidityDataPoints, tempDataPoints) = getDataPoints()
        humidGraphSeries = LineGraphSeries(humidityDataPoints)
        tempGraphSeries = LineGraphSeries(tempDataPoints)
        humidityGraph.addSeries(humidGraphSeries)
        tempGraph.addSeries(tempGraphSeries)
        with(humidityGraph.viewport){
            this.setMinX(humidGraphSeries.lowestValueX)
            this.setMaxX(humidGraphSeries.highestValueX)

            val yOffsetRatio = 0.1

            val graphMaxY = humidGraphSeries.highestValueY
            val graphLowY = humidGraphSeries.lowestValueY
            val yDif = (graphMaxY - graphLowY) * yOffsetRatio

            val newLowY = graphLowY - yDif
            val newMaxY = graphMaxY + yDif
            this.setMinY(newLowY)
            this.setMaxY(newMaxY)
        }
        with(tempGraph.viewport){
            this.setMinX(tempGraphSeries.lowestValueX)
            this.setMaxX(tempGraphSeries.highestValueX)

            val yOffsetRatio = 0.1

            val graphMaxY = tempGraphSeries.highestValueY
            val graphLowY = tempGraphSeries.lowestValueY
            val yDif = (graphMaxY - graphLowY) * yOffsetRatio

            val newLowY = graphLowY - yDif
            val newMaxY = graphMaxY + yDif
            this.setMinY(newLowY)
            this.setMaxY(newMaxY)

        }


    }


    fun getDataPoints() : Pair<Array<DataPoint>, Array<DataPoint>>{
        val dataList = getData()
        var humidityDataPoints = mutableListOf<DataPoint>()
        var tempDataPoints = mutableListOf<DataPoint>()
        if(dataList != null){
            val timeSet = dataList.keys
            var tempTimeList = mutableListOf<Double>()
            var tempHumidList = mutableListOf<Double>()
            var tempTempList = mutableListOf<Double>()
            for (time in timeSet){
                humidityDataPoints.add(DataPoint(time.toDouble(), dataList.get(time)!!.humidity))
                tempDataPoints.add(DataPoint(time.toDouble(), dataList.get(time)!!.temperature))
            }
            if(tempHumidList.size  > 0){
                val aveTime = tempTimeList.average()
                val aveHumid = tempHumidList.average()
                val aveTemp = tempTempList.average()
                humidityDataPoints.add(DataPoint(aveTime, aveHumid))
                tempDataPoints.add(DataPoint(aveTime, aveTemp))
            }
        }
        return Pair(humidityDataPoints.toTypedArray(), tempDataPoints.toTypedArray())
    }

    override fun onDatePicked(year: Int, month: Int, day: Int) {
        val s = mutableListOf(year.toString(), month.toString(), day.toString())
        Log.d("DATE_PICK_LISTENER", s.joinToString("/"))
        calendar.set(year, month, day)
        loadUi()
    }


}
