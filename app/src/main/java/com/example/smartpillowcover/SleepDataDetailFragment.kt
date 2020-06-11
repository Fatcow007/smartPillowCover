package com.example.smartpillowcover




import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.isVisible
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class SleepDataDetailFragment : MyFragment(), DatePickerFragment.DatePickedListener {

    lateinit var pillowImageView : ImageView
    lateinit var currentDateTextView : TextView
    lateinit var changeDateButton : Button
    lateinit var snoreGraph : GraphView
    lateinit var snoreGraphSeries : LineGraphSeries<DataPoint>
    lateinit var timeSelectSeekBar : SeekBar
    var seekBarProgress : Int = 0
    val CIRCLE_RADIUS = 8.toFloat()

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GRAY
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sleepdata_detail, container, false)
    }

    override fun update(d : SleepData) {
        val time = d.getTimeAsInt().toDouble()
        //loadUi()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pillowImageView = view.findViewById(R.id.pillowImageView)
        changeDateButton = view.findViewById(R.id.sdChangeDateBtn)
        currentDateTextView = view.findViewById(R.id.sleepDataCurrentDateTextView)
        timeSelectSeekBar = view.findViewById(R.id.timeSelectSeekBar)
        changeDateButton.setOnClickListener {showDatePickerDialog(view)}

        snoreGraph = view.findViewById(R.id.snoreGraph)
        val snoreDataPoints = getDataPoints()
        snoreGraphSeries = LineGraphSeries(snoreDataPoints)
        snoreGraph.isVisible = true
        snoreGraph.isVisible = true

        timeSelectSeekBar.max = 300
        timeSelectSeekBar.progress = 0
        timeSelectSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                seekBarProgress = progress
                drawDots()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })





        with(snoreGraph){
            this.title = context.getString(R.string.text_snore)
            this.addSeries(snoreGraphSeries)
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
                        super.formatLabel(value, isValueX) + "dB"
                    }
                }
            }
            this.gridLabelRenderer.textSize = 28.toFloat()
            with(this.viewport) {
                this.isScalable = true
                this.isXAxisBoundsManual = true
                this.isYAxisBoundsManual = true
                this.setMinX(snoreGraphSeries.lowestValueX)
                this.setMaxX(snoreGraphSeries.highestValueX)
                val yOffsetRatio = 0.1

                val graphMaxY = snoreGraphSeries.highestValueY
                val graphLowY = snoreGraphSeries.lowestValueY
                val yDif = (graphMaxY - graphLowY) * yOffsetRatio

                val newLowY = graphLowY - yDif
                val newMaxY = graphMaxY + yDif
                this.setMinY(newLowY)
                this.setMaxY(newMaxY)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        loadUi()
    }


    fun loadUi() {
        drawDots()
        currentDateTextView.text = getTextForTextViewDisplay()
        snoreGraph.isEnabled = true
        snoreGraph.isVisible = true
        snoreGraph.removeAllSeries()
        val snoreDataPoints = getDataPoints()
        snoreGraphSeries = LineGraphSeries(snoreDataPoints)
        snoreGraph.addSeries(snoreGraphSeries)
        with(snoreGraph.viewport){
            this.setMinX(snoreGraphSeries.lowestValueX)
            this.setMaxX(snoreGraphSeries.highestValueX)

            val yOffsetRatio = 0.1

            val graphMaxY = snoreGraphSeries.highestValueY
            val graphLowY = snoreGraphSeries.lowestValueY
            val yDif = (graphMaxY - graphLowY) * yOffsetRatio

            val newLowY = graphLowY - yDif
            val newMaxY = graphMaxY + yDif
            this.setMinY(newLowY)
            this.setMaxY(newMaxY)
        }
    }

    fun drawDots(){
        val myOptions = BitmapFactory.Options()
        val pointsToDraw = 30
        myOptions.inScaled = false
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 // important

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.pillow, myOptions)

        val workingBitmap = Bitmap.createBitmap(bitmap)
        val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val sleepDataMap = getData()
        if(sleepDataMap != null && !sleepDataMap.isEmpty()){
            val drawCoordArray = getDrawCoord(pointsToDraw)
            if(drawCoordArray != null){
                for((x, y, a) in drawCoordArray){
                    val xOffset = 50
                    val yOffset = 50
                    val newX = ((x+1.0)/2.0) * (mutableBitmap.width - 2 * xOffset) + xOffset
                    val newY = ((2.0-y)/2.0) * (mutableBitmap.height - 2 * yOffset) + yOffset
                    circlePaint.alpha = (a * 0.8).toInt()
                    canvas.drawCircle(newX.toFloat(), newY.toFloat(), CIRCLE_RADIUS, circlePaint)
                }

                pillowImageView.adjustViewBounds = true
                pillowImageView.setImageBitmap(mutableBitmap)
            }else{
                //NO DATA
            }
        }
    }

    fun getDataPoints() : Array<DataPoint>{
        val minFreq = 110
        val maxFreq = 150
        val averageValueCount = 10
        val dataList = getData()
        var dataPointArray = mutableListOf<DataPoint>()
        if(dataList != null){
            val timeSet = dataList.keys
            var tempTimeList = mutableListOf<Double>()
            var tempDBList = mutableListOf<Double>()
            var tempFreqList = mutableListOf<Double>()
            for (time in timeSet){

                dataPointArray.add(DataPoint(time.toDouble(), dataList[time]!!.dB))
                /*
            if(minFreq < freq && freq < maxFreq){
                dataPointArray.add(DataPoint(time.toDouble(), dB))
            }*/
            }
        }
        return dataPointArray.toTypedArray()
    }



    fun getDebugCoord() : Array<Triple<Float, Float, Int>>{
        val testMArray = mutableListOf<Triple<Float, Float, Int>>()
        val offSetX = 30
        val offSetY = 30
        val testSize = 50
        for(i in 0..testSize){
            val x = i.toFloat() + offSetX
            val y = (i*2).toFloat() + offSetY
            val a = ((1 - (abs(i - testSize/2).toDouble()/(testSize.toDouble()/2)))*255).toInt()
            testMArray.add(Triple(x, y, a))
        }
        return testMArray.toTypedArray()
    }

    fun getDrawCoord(pointCount : Int) : Array<Triple<Float, Float, Int>>?{
        //Get the timestamp cloest to reference time
        val sleepData = getData()
        if(sleepData != null){
            val indexRatio = seekBarProgress.toDouble()/timeSelectSeekBar.max.toDouble()
            val sleepIndex = (indexRatio * sleepData!!.size).toInt()

            val minIndex = max(sleepIndex - pointCount, 0)
            val maxIndex = min(sleepIndex + pointCount, sleepData.size - 1)

            var sleepDataKeyList = sleepData.keys.toList()
            var coordList = mutableListOf<Triple<Float,Float,Int>>()
            for (i in minIndex..maxIndex){
                val alpha = ((1.0 - abs(i - sleepIndex).toDouble()/pointCount.toDouble())*255.0).toInt()
                with(sleepData[sleepDataKeyList[i]]){
                    coordList.add(Triple(this!!.px.toFloat(), this!!.py.toFloat(), alpha))
                }
            }
            return coordList.toTypedArray()
        }
        return null
    }




    fun showDatePickerDialog(v: View) {
        val date = getYearMonthDay()
        val frag = DatePickerFragment(date[0], date[1], date[2])
        frag.setTargetFragment(this, 256)
        frag.show(fragmentManager, "timePicker")
    }

    override fun onDatePicked(year: Int, month: Int, day: Int) {
        val s = mutableListOf(year.toString(), month.toString(), day.toString())
        Log.d("DATE_PICK_LISTENER", s.joinToString("/"))
        calendar.set(year, month, day)
        loadUi()
    }
}
