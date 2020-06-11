package com.example.smartpillowcover

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var listener: TimePickedListener
    interface TimePickedListener{
        fun onTimePicked(hour : Int, min : Int)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        listener = targetFragment as TimePickedListener

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, android.R.style.Theme_Holo_Light_Dialog,this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        listener.onTimePicked(hourOfDay, minute)
    }


}
