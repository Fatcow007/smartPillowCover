package com.example.smartpillowcover

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(val y: Int, val m: Int, val d: Int) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var listener: DatePickedListener
    interface DatePickedListener{
        fun onDatePicked(year: Int, month: Int, day: Int)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        listener = targetFragment as DatePickedListener

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity as Context, this, y, m, d)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        listener.onDatePicked(p1, p2, p3)
    }


}