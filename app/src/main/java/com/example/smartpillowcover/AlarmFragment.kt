package com.example.smartpillowcover



import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_alarm.*
import java.nio.charset.Charset
import java.util.*

class AlarmFragment: MyFragment(), TimePickerFragment.TimePickedListener, AdapterView.OnItemSelectedListener {
    lateinit var currentAlarmTextView : TextView
    lateinit var alarmDateTextView : TextView
    lateinit var alarmOnOffSwitch : Switch
    lateinit var vibrateOnOffSwitch : Switch
    lateinit var alarmMusicSelectSpinner : Spinner


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm, container, false)
    }

    override fun update(d : SleepData) {
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentAlarmTextView = view.findViewById(R.id.currentAlarmTextView)
        currentAlarmTextView.setOnClickListener { showTimePickerDialog(view) }
        alarmDateTextView = view.findViewById(R.id.alarmDateTextView)
        val dateText = getTextForTextViewDisplay()
        alarmDateTextView.text = dateText
        alarmOnOffSwitch = view.findViewById(R.id.alarmOnOffSwitch)
        alarmOnOffSwitch.isChecked = (activity as MainActivity).currentAlarmOnOff
        alarmOnOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            val act = activity as MainActivity
            if (isChecked) {
                // The toggle is enabled
                val data = "AO1"
                if(act.sendBluetoothData(data)){
                    Toast.makeText(act.applicationContext, getString(R.string.alarm_on_text), Toast.LENGTH_SHORT).show()
                    act.currentAlarmOnOff = true
                }else{
                    Toast.makeText(act.applicationContext, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show()
                    alarmOnOffSwitch.isChecked = false
                }
            } else {
                // The toggle is disabled
                val data = "AO0"
                if(act.sendBluetoothData(data)){
                    Toast.makeText(act.applicationContext, getString(R.string.alarm_off_text), Toast.LENGTH_SHORT).show()
                    act.currentAlarmOnOff = false
                }else{
                    Toast.makeText(act.applicationContext, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show()
                    alarmOnOffSwitch.isChecked = false
                }
            }
        }
        vibrateOnOffSwitch = view.findViewById(R.id.vibrateOnOffSwitch)
        vibrateOnOffSwitch.isChecked = (activity as MainActivity).currentVibrationOnOff
        vibrateOnOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            val act = activity as MainActivity
            if (isChecked) {
                // The toggle is enabled
                val data = "VO1"
                if(act.sendBluetoothData(data)){
                    Toast.makeText(act.applicationContext, getString(R.string.vibration_on_text), Toast.LENGTH_SHORT).show()
                    act.currentVibrationOnOff = true
                }else{
                    Toast.makeText(act.applicationContext, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show()
                    vibrateOnOffSwitch.isChecked = false
                    act.currentVibrationOnOff = false
                }
            } else {
                // The toggle is disabled
                val data = "VO0"
                if(act.sendBluetoothData(data)){
                    Toast.makeText(act.applicationContext, getString(R.string.vibration_off_text), Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(act.applicationContext, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show()
                    vibrateOnOffSwitch.isChecked = false
                }
            }
        }
        alarmMusicSelectSpinner = view.findViewById(R.id.alarmMusicSelectSpinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this.activity!!.applicationContext,
            R.array.alarm_music_array,
            R.layout.spinneritem_alarm
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            alarmMusicSelectSpinner.adapter = adapter
        }
        alarmMusicSelectSpinner.setSelection((activity as MainActivity).selectedAlarm)
        alarmMusicSelectSpinner.onItemSelectedListener = this



    }



    override fun onStart() {
        super.onStart()
        loadUi()
        updateCurrentTimeTextView()
    }


    fun loadUi() {
    }

    fun showTimePickerDialog(v: View) {
        val frag = TimePickerFragment()
        frag.setTargetFragment(this, 256)
        frag.show(fragmentManager, "timePicker")
    }

    override fun onTimePicked(hour: Int, min: Int) {
        val timeLabel = "AS"
        val selectedTime = hour.toString() + ":" + min.toString()
        val data = timeLabel+selectedTime

        Log.d("TimePickedData", data)
        val act = activity as MainActivity
        if(act.sendBluetoothData(data)){
            act.currentAlarmHour = hour
            act.currentAlarmMin = min
            updateCurrentTimeTextView()
        }
    }

    fun updateCurrentTimeTextView(){
        val act = activity as MainActivity
        var h : String = act.currentAlarmHour.toString()
        var m : String = act.currentAlarmMin.toString()
        if(h.length == 1){
            h = "0" + h
        }
        if(m.length == 1){
            m = "0" + m
        }
        var displayText = h + ":" + m
        if( h == "0" && m == "0"){
            displayText = "알람 설정 없음"
        }
        currentAlarmTextView.text = displayText
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val act = activity as MainActivity
        val data = "AT" + p2.toString()
        act.sendBluetoothData(data)
    }
}
