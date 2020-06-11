package com.example.smartpillowcover


import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class BluetoothRecyclerViewAdapter(
    var bluetoothDataSet : MutableList<BluetoothDevice>,
    var mOnBluetoothItemClickListener: OnBluetoothItemClickListener
) : RecyclerView.Adapter<BluetoothRecyclerViewAdapter.MyViewHolder>(){


    class MyViewHolder(view: View, val mOnBluetoothItemClickListener: OnBluetoothItemClickListener) : RecyclerView.ViewHolder(view), View.OnClickListener{
        var bluetoothItemTextView: TextView
        var bluetoothMACTextView: TextView
        init{
            bluetoothItemTextView = view.findViewById(R.id.bluetoothItemTextView)
            bluetoothMACTextView = view.findViewById(R.id.bluetoothMACTextView)
            view.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            mOnBluetoothItemClickListener.onBluetoothItemClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listitem_bluetooth, parent, false)
        return MyViewHolder(view, mOnBluetoothItemClickListener)
    }

    override fun getItemCount(): Int {
        return bluetoothDataSet.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val bluetoothName = bluetoothDataSet[position].name
        val bluetoothMAC = bluetoothDataSet[position].address
        holder.bluetoothItemTextView.setText(bluetoothName)
        holder.bluetoothMACTextView.setText(bluetoothMAC)
    }

    interface OnBluetoothItemClickListener{
        fun onBluetoothItemClick(position: Int)
    }
}