package com.denizhan.stm32_control


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle



//aaaaaaaaaaaaaaaaaaaa
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import  android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.util.*

import kotlinx.coroutines.*




//aaaaaaaaaaaaaaaaaaaa
//bbbbbbbbbbbbbbbbbbbb
const val REQUEST_ENABLE = 1
//bbbbbbbbbbbbbbbbbbbb
class MainActivity : AppCompatActivity() {
    //cccccccccccccccccccccccccccccccccc
    //BluetoothAdapter
    lateinit var mBtAdapter: BluetoothAdapter
    var mAdressDevices : ArrayAdapter<String> ? = null
    var mNameDevices : ArrayAdapter<String>? = null




    companion object{
        var m_myUUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket : BluetoothSocket? = null

        var m_isConnected : Boolean = false
        lateinit var m_adress : String
        lateinit var okunan_deger: TextView

    }




    //cccccccccccccccccccccccccccccccccc
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //dddddddddddddddddddddddddddddddddddddddd
        mAdressDevices = ArrayAdapter(this,android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this,android.R.layout.simple_list_item_1)

        val idBtnOnBT = findViewById<Button>(R.id.idBtnOnBT)
        val idBtnOffBT = findViewById<Button>(R.id.idBtnOffBT)
        val idBtnConect = findViewById<Button>(R.id.idBtnConect)
        val idBtnEnviar = findViewById<Button>(R.id.idBtnEnviar)

        val idBtnLuz_1on = findViewById<Button>(R.id.idBtnLuz_1on)

        val idBtnLuz_2on = findViewById<Button>(R.id.idBtnLuz_2on)


        val idBtnDispBT = findViewById<Button>(R.id.idBtnDispBT)
        val idSpinDisp = findViewById<Spinner>(R.id.idSpinDisp)
        val idTextOut = findViewById<EditText>(R.id.idTextOut)


        MainActivity.okunan_deger = findViewById(R.id.textView5)
        var okunan_deger1 = findViewById<TextView>(R.id.textView5)



        //----------------
        //----------------
        val someActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == REQUEST_ENABLE){
                Log.i("MainActivity","KAYITLI")
            }
        }

        mBtAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if(mBtAdapter == null){
            Toast.makeText(this,"Bluetooth bu cihazda kullanılamıyor",Toast.LENGTH_LONG).show()
        } else{
            Toast.makeText(this,"Bu cihazda Bluetooth mevcut",Toast.LENGTH_LONG).show()
        }

        //----------------
        //----------------

        idBtnOnBT.setOnClickListener {
            if (mBtAdapter.isEnabled){
                Toast.makeText(this,"Bluetoh zaten etkin",Toast.LENGTH_LONG).show()
            } else {
                val enabletIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED){
                    Log.i("MainActivity","ActivityCompat#requestPermission")
                }
                someActivityResultLauncher.launch(enabletIntent)
            }
        }


        idBtnOffBT.setOnClickListener {
            if(!mBtAdapter.isEnabled){
                Toast.makeText(this,"Bluetooth devre aktif",Toast.LENGTH_LONG).show()
            } else {
                mBtAdapter.disable()
                Toast.makeText(this,"bluetooth devre dışı bırakıldı",Toast.LENGTH_LONG).show()
            }
        }


        idBtnDispBT.setOnClickListener {
            if (mBtAdapter.isEnabled){
                val pairedDevices : Set<BluetoothDevice> ? = mBtAdapter?.bondedDevices
                mAdressDevices!!.clear()
                mNameDevices!!.clear()

                pairedDevices?.forEach{
                        device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address //mac
                    mAdressDevices!!.add(deviceHardwareAddress)
                    mNameDevices!!.add(deviceName)
                }

                idSpinDisp.setAdapter(mNameDevices)
            } else {
                val noDevices = "Hiçbir cihaz eşleştirilemedi"
                mAdressDevices!!.add(noDevices)
                Toast.makeText(this,"İlk önce Bluetooth'u etkinleştirin", Toast.LENGTH_LONG).show()
            }
        }



        idBtnConect.setOnClickListener {
            try {
                if(m_bluetoothSocket == null || !m_isConnected){
                    val IntValSpin = idSpinDisp.selectedItemPosition
                    m_adress = mAdressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(this, m_adress,Toast.LENGTH_LONG).show()
                    mBtAdapter?.cancelDiscovery()
                    val device : BluetoothDevice = mBtAdapter.getRemoteDevice(m_adress)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()


                    //m_isConnected = true
                    //readThread.start()
                    //readThread()
                }

                Toast.makeText(this,"Bağlantı",Toast.LENGTH_LONG).show()
                Log.i("MainActivity","Bağlantı")
            } catch (e : IOException){
                e.printStackTrace()
                Toast.makeText(this,"Bağlantı hata",Toast.LENGTH_LONG).show()
                Log.i("MainActivity","Bağlantı hata")
            }
        }

        idBtnLuz_1on.setOnClickListener {
            //sendCommand("A")
            m_isConnected = true
            readThread()
        }




        idBtnLuz_2on.setOnClickListener {
            //sendCommand("C")
            startActivity(Intent(this,MainActivity2::class.java))
        }



        idBtnEnviar.setOnClickListener {
            if(idTextOut.text.toString().isEmpty()){
                Toast.makeText(this,"Kelime gönderme boş olabilir ama dolu olsun lütfen",Toast.LENGTH_SHORT)
            }else{
                var mensaje_out : String = idTextOut.text.toString()
                sendCommand(mensaje_out)
            }
        }













        //ddddddddddddddddddddddddddddddddddddddddd
    }
    //eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee

    private fun sendCommand(input : String){
        if (m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e : IOException){
                e.printStackTrace()
            }
        }
    }
    //eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee


    private fun readThread ()
    {
        val inputStream: InputStream = m_bluetoothSocket!!.inputStream//inputtan gelen veri
        Toast.makeText(this,"no",Toast.LENGTH_SHORT).show()


        val buffer = ByteArray(1024)
        val bytesRead: Int = inputStream.read(buffer)

        var deger = ""

        var kontrol = 0
        while (kontrol<10) {
            if (bytesRead > 0) {
                val data = String(buffer, 0, bytesRead)
                // Process the received data here
                Log.i("MainActivity", "Alınan veri: $data")
                //MainActivity.okunan_deger.text = "yes"

                Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT).show()
                deger = data.toString()
                kontrol = kontrol + 1
            } else {
                val data = String(buffer, 0, bytesRead)
                // Process the received data here
                Log.i("MainActivity", "Alınan veri: $data")
                MainActivity.okunan_deger.text = "no"
                Toast.makeText(this, "no", Toast.LENGTH_SHORT).show()
                deger = data.toString()
                kontrol = kontrol + 1
            }
        }
        deger = ""





        /*
        while (true) {
            val buffer = ByteArray(1024)
            val bytesRead: Int = inputStream.read(buffer)

            if (bytesRead > 0) {
                val data = String(buffer, 0, bytesRead)
                // Process the received data here
                Log.i("MainActivity", "Alınan veri: $data")
                MainActivity.okunan_deger.text = "yes"

                Toast.makeText(this,data.toString(),Toast.LENGTH_SHORT).show()
            }
            else{
                val data = String(buffer, 0, bytesRead)
                // Process the received data here
                Log.i("MainActivity", "Alınan veri: $data")
                MainActivity.okunan_deger.text = "no"
                Toast.makeText(this,"no",Toast.LENGTH_SHORT).show()
            }
        }*/



    }
}