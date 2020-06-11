package com.example.smartpillowcover

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KClass


class MainActivity : AppCompatActivity(), BluetoothRecyclerViewAdapter.OnBluetoothItemClickListener{
    private val EXTERNAL_PERMS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val EXTERNAL_REQUEST = 138
    private val REQUEST_ENABLE_BT = 111

    lateinit var alarmButton : ConstraintLayout
    lateinit var tempHumidButton : ConstraintLayout
    lateinit var sleepDataButton : ConstraintLayout
    lateinit var bluetoothUpdateButton : ConstraintLayout
    lateinit var bluetoothDataTextView: TextView
    lateinit var bluetoothRecyclerView : RecyclerView
    lateinit var bluetoothAdapter : BluetoothAdapter

    var mConnectedThread : ConnectedThread? = null
    //var currentAlarmTime : MutableList<String> = mutableListOf()
    var currentAlarmHour = 0
    var currentAlarmMin = 0
    var currentAlarmOnOff = false
    var currentVibrationOnOff = false
    var currentAsmrOnOff = false
    var selectedAlarm = 0
    var bluetoothDeviceList : MutableList<BluetoothDevice> = mutableListOf()

    var currentTemp = 0.0
    var currentHumid = 0.0

    lateinit var calendar : Calendar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPermission()
        checkBluetooth()
        initUi()

        calendar = Calendar.getInstance()

        bluetoothRecyclerView.adapter!!.notifyDataSetChanged();
    }

    fun initUi(){

        alarmButton = findViewById(R.id.alarmButton)
        alarmButton.setOnClickListener {
            createFragment(AlarmFragment::class, true)
        }
        tempHumidButton = findViewById(R.id.tempHumidButton)
        tempHumidButton.setOnClickListener {
            createFragment(TempHumidFragment::class, true)
        }
        sleepDataButton = findViewById(R.id.sleepDataButton)
        sleepDataButton.setOnClickListener {
            createFragment(SleepDataFragment::class, true)
        }
        bluetoothUpdateButton = findViewById(R.id.bluetoothUpdateButton)
        bluetoothUpdateButton.setOnClickListener {
            val existingFragment = supportFragmentManager.findFragmentByTag("OptionFragment")
            if(existingFragment != null){
                supportFragmentManager.popBackStackImmediate("OptionFragment", POP_BACK_STACK_INCLUSIVE)
            }
            updateBluetooth()
        }
        bluetoothDataTextView = findViewById(R.id.bluetoothDataTextView)
        bluetoothRecyclerView = findViewById(R.id.bluetoothRecyclerView)

        //Setup adapter for the recyclerView
        val viewManager = LinearLayoutManager(this)
        bluetoothRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
        }
        bluetoothRecyclerView.adapter = BluetoothRecyclerViewAdapter(bluetoothDeviceList, this)
    }
    private fun getPermission(){
        var permissionGranted = true
        for (s in EXTERNAL_PERMS){
            if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED){
                permissionGranted = false
            }
        }
        if (!permissionGranted){
            ActivityCompat.requestPermissions(this, EXTERNAL_PERMS, EXTERNAL_REQUEST)
        }
    }
    fun createFragment(fragmentType : KClass<out Any>, forceRun : Boolean  = false) : Boolean{
        if((mConnectedThread == null || !mConnectedThread!!.socket.isConnected ) && !forceRun){
            //Bluetooth not connected, Alert the user with toast
            Toast.makeText(applicationContext, getString(R.string.bluetooth_not_connected), Toast.LENGTH_SHORT).show()
            return false
        }
        var targetFragmentClass: Fragment
        val existingFragment = supportFragmentManager.findFragmentByTag("OptionFragment")
        if(existingFragment == null || existingFragment::class != fragmentType) {
            if(existingFragment != null){
                supportFragmentManager.popBackStack("OptionFragment", POP_BACK_STACK_INCLUSIVE)
            }

            //val existingFragment2 = supportFragmentManager.findFragmentByTag("OptionFragment")
            when (fragmentType) {
                AlarmFragment::class -> targetFragmentClass = AlarmFragment()
                TempHumidFragment::class -> targetFragmentClass = TempHumidFragment()
                SleepDataFragment::class -> targetFragmentClass = SleepDataFragment()
                else -> throw IllegalArgumentException("fragmentTypeRequired")
            }
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.mainActivityLayout,
                    targetFragmentClass,
                    "OptionFragment"
                )
                .addToBackStack("OptionFragment")
                .commit()
        }
        return true

    }
    fun checkBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            // Register for broadcasts when a device is discovered.
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
        }
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            bluetoothDeviceList.add(device)
        }

    }

    fun updateBluetooth(){
        if(!bluetoothAdapter.isDiscovering){
            if(!bluetoothAdapter.isEnabled){
                bluetoothAdapter.enable()
            }
            val discoveryStartSuccess = bluetoothAdapter.startDiscovery()
            if(discoveryStartSuccess){
                Toast.makeText(applicationContext, getString(R.string.bluetooth_searching), Toast.LENGTH_SHORT).show()
                // Register for broadcasts when search is finished.
                val filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                registerReceiver(bluetoothAdapterReceiver, filter)
            }else{
                Toast.makeText(applicationContext, getString(R.string.bluetooth_search_start_failed), Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(applicationContext, getString(R.string.bluetooth_already_searching), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
                System.exit(0)
            }else{
                checkBluetooth()
            }
        }
    }

    override fun onBluetoothItemClick(position: Int) {
        bluetoothDataTextView.text = getString(R.string.bluetooth_connecting)
        bluetoothRecyclerView.isEnabled = false
        ConnectThread(bluetoothDeviceList[position], this).start()
    }

    fun startBluetoothDataTransfer(socket : BluetoothSocket){
        Log.d("BLUETOOTH", "connected: Starting.")

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, this)
        mConnectedThread!!.start()


        //Check if device is valid


        //Get initial data
        sendBluetoothData("AR")

    }

    fun onBluetoothDataReceive(data: String){
        try{

            val ALARM_TIME_TAG = "[AlarmTime]"
            if(ALARM_TIME_TAG in data){
                val dataSplit = data.trim().removePrefix(ALARM_TIME_TAG).split("$")
                val times = dataSplit[0].split(":")
                currentAlarmHour = times[0].trim().toInt()
                currentAlarmMin = times[1].trim().toInt()
                currentAlarmOnOff = dataSplit[1].toBoolean()
                currentVibrationOnOff = dataSplit[2].toBoolean()
                currentAsmrOnOff = dataSplit[3].toBoolean()
                selectedAlarm = dataSplit[4].toInt()
            }

            val SLEEP_DATA_TAG = "[SleepData]"
            var newData : SleepData? = null
            if(SLEEP_DATA_TAG in data){
                val dataStringList = data.removePrefix(SLEEP_DATA_TAG).trim().split("$")
                if(dataStringList.size < 8){
                    Log.d("DATA_RECEIVE_ERROR", "Received data length is shorter than expected")
                }else{
                    val times = dataStringList[0].split(":")
                    val hour = times[0].toInt()
                    val min = times[1].toInt()
                    val sec = times[2].toInt()
                    val humidity = dataStringList[1].toDouble()
                    val temperature = dataStringList[2].toDouble()
                    val posx = dataStringList[3].toDouble()
                    val posy = dataStringList[4].toDouble()
                    val dB = dataStringList[5].toDouble()
                    val freq = dataStringList[6].toDouble()
                    val isSleeping = dataStringList[7].toBoolean()

                    //Create Data Class and save it
                    if(isSleeping){
                        newData = SleepData(hour, min, sec, humidity, temperature, posx, posy, dB, freq)
                        addData(this, getYearMonthDayAsString(), newData)
                    }
                    currentTemp = temperature
                    currentHumid = humidity
                }
            }

            //D

            val existingFragment = supportFragmentManager.findFragmentByTag("OptionFragment")
            if(existingFragment != null && newData != null){
                (existingFragment as MyFragment).update(newData)
            }
        }catch (e : Exception){
            Log.e("BluetoothDataError", e.message + "[receivedDataFormatError.. skipping data]")
        }
    }


    fun getYearMonthDayAsString() : String{
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        return listOf(y, m, d).map{ it.toString() }.joinToString("_")
    }


    fun sendBluetoothData(data: String) : Boolean{
        if(mConnectedThread != null){
            if(mConnectedThread!!.write(data.toByteArray(Charset.defaultCharset()))){
                return true
            }
        }
        return false
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    if(device !in bluetoothDeviceList && deviceName != null && deviceHardwareAddress != null){
                        bluetoothDeviceList.add(device)
                    }
                    // Update Adapter
                    bluetoothRecyclerView.adapter!!.notifyDataSetChanged()
                }
            }
        }
    }
    // Create a BroadcastReceiver for ACTION_DISCOVERY_FINISHED.
    private val bluetoothAdapterReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when(action) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Discovery is finished.
                    Toast.makeText(applicationContext, getString(R.string.bluetooth_search_finished), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
    private inner class ConnectThread(val device: BluetoothDevice, val activity:AppCompatActivity) : Thread() {

        val DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        override fun run() {    // Default UUID

            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            val SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // bluetooth serial port service
            //UUID SERIAL_UUID = device.getUuids()[0].getUuid(); //if you don't know the UUID of the bluetooth device service, you can get it like this from android cache

            var socket : BluetoothSocket? = null
            var successful = false
            try {
                socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID)
            } catch (e:Exception) {Log.e("","Error creating socket")}

            try {
                socket!!.connect()
                Log.e("","Connected")
                successful = true
            } catch (e:IOException) {
                Log.e("", e.message)
                try {
                    Log.e("", "trying fallback...")
                    socket = device.javaClass.getMethod(
                        "createRfcommSocket", *arrayOf<Class<*>?>(
                            Int::class.javaPrimitiveType
                        )
                    ).invoke(device, 1) as BluetoothSocket
                    socket.connect()

                    Log.e("BLUETOOTH", "Connected")
                    successful = true


                } catch (e2 : java.lang.Exception) {
                    Log.e("", "Couldn't establish Bluetooth connection!")
                }

            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(socket)
            if(successful){
                activity.runOnUiThread(Runnable {
                    val s = getString(R.string.bluetooth_connected) + device.name
                    bluetoothDataTextView.text = s
                    startBluetoothDataTransfer(socket!!)


                })
            }else{
                activity.runOnUiThread(Runnable {
                    bluetoothDataTextView.text = getString(R.string.bluetooth_not_connected)
                    bluetoothRecyclerView.isEnabled = true
                    Toast.makeText(applicationContext, getString(R.string.bluetooth_connect_failed), Toast.LENGTH_SHORT).show()
                })
            }

        }

    }
    class ConnectedThread(val socket: BluetoothSocket, val activity:AppCompatActivity) : Thread() {
        private val mmSocket: BluetoothSocket
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(1024*16) // buffer store for the stream
            var bytes: Int // bytes returned from read()
            var dataString = ""

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream!!.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes)
                    //Log.d("Bluetooth", "InputStream: $incomingMessage")
                    dataString += incomingMessage
                    if(dataString.endsWith(("\n"))){
                        val sendString = dataString.dropLast(1)
                        Log.d("BluetoothReceived", "DataString: $dataString")
                        activity.runOnUiThread({ (activity as MainActivity).onBluetoothDataReceive(sendString) })
                        dataString = ""
                    }

                } catch (e: IOException) {
                    Log.e("Bluetooth", "write: Error reading Input Stream. " + e.message
                    )
                    break
                }
            }
        }

        fun write(bytes: ByteArray?): Boolean {
            val text = String(bytes!!, Charset.defaultCharset())
            Log.d("Bluetooth", "write: Writing to outputstream: $text")
            try {
                mmOutStream!!.write(bytes)
                return true
            } catch (e: IOException) {
                Log.e("Bluetooth","write: Error writing to output stream. " + e.message)
                return false
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }
        }

        init {
            Log.d("Bluetooth", "ConnectedThread: Starting.")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        try {
            unregisterReceiver(receiver)
            //Register or UnRegister your broadcast receiver here
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
        }
        try {
            unregisterReceiver(bluetoothAdapterReceiver)
            //Register or UnRegister your broadcast receiver here
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
        }
    }

}
