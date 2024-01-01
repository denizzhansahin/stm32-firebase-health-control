package com.denizhan.stm32_control



import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import java.io.IOException
import java.io.InputStream
import java.util.UUID



import java.util.*


class MainActivity2 : AppCompatActivity() {

    lateinit var lineChart : LineChart
    lateinit var lineChart_ADC : LineChart

    lateinit var mBtAdapter: BluetoothAdapter
    var mAdressDevices : ArrayAdapter<String> ? = null
    var mNameDevices : ArrayAdapter<String>? = null




    companion object{
        var m_myUUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var m_bluetoothSocket : BluetoothSocket? = null

        var m_isConnected : Boolean = false
        lateinit var m_adress : String
        lateinit var okunan_deger: TextView

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        mAdressDevices = ArrayAdapter(this,android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this,android.R.layout.simple_list_item_1)


        lineChart=findViewById(R.id.line_chart_1)


        lineChart_ADC=findViewById(R.id.line_chart)



        var list = ArrayList<Entry>()
        var list_ADC = ArrayList<Entry>()

        /*
        list.add(Entry(10f, 4f))
        list.add(Entry(20f, 8f))
        list.add(Entry(30f, 12f))
        list.add(Entry(40f, 12f))
        list.add(Entry(50f, 7f))
        list.add(Entry(60f, 19f))
        list.add(Entry(70f, 19f))
        list.add(Entry(80f, 12f))
        list.add(Entry(90f, 35f))
        list.add(Entry(100f, 28f))

         */


        var lineDataSet = LineDataSet(list, "Sıcaklık")
        var lineDataSet_ADC = LineDataSet(list_ADC, "ADC")
        //lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)

        /*
        lineChart.data = LineData(lineDataSet)

        lineChart.invalidate()
        */



        val idBtnConect = findViewById<Button>(R.id.idBtnConect)

        val idBtnDispBT = findViewById<Button>(R.id.idBtnDispBT)
        val idSpinDisp = findViewById<Spinner>(R.id.idSpinDisp)

        val veri_getir = findViewById<Button>(R.id.idBtnConect2)


        val someActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == REQUEST_ENABLE){
                Log.i("MainActivity2","KAYITLI")
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


        var son_kontrol = 0
        idBtnConect.setOnClickListener {
            try {
                if(m_bluetoothSocket == null || !m_isConnected){
                    val IntValSpin = idSpinDisp.selectedItemPosition
                    m_adress = mAdressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(this, m_adress,Toast.LENGTH_LONG).show()
                    mBtAdapter?.cancelDiscovery()
                    val device : BluetoothDevice = mBtAdapter.getRemoteDevice(m_adress)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(
                        m_myUUID
                    )
                    m_bluetoothSocket!!.connect()


                    //m_isConnected = true
                    //readThread.start()
                    //readThread()
                }

                Toast.makeText(this,"Bağlantı",Toast.LENGTH_LONG).show()
                Log.i("MainActivity","Bağlantı")
                son_kontrol = 1



            } catch (e : IOException){
                e.printStackTrace()
                Toast.makeText(this,"Bağlantı hata",Toast.LENGTH_LONG).show()
                Log.i("MainActivity","Bağlantı hata")
            }
        }


        var zaman = 0f




        veri_getir.setOnClickListener {
            try {

                val inputStream: InputStream = m_bluetoothSocket!!.inputStream//inputtan gelen veri
                Toast.makeText(this, "no", Toast.LENGTH_SHORT).show()


                val buffer = ByteArray(1024)
                val bytesRead: Int = inputStream.read(buffer)

                var deger = ""

                var kontrol = 0


                while (kontrol<1) {

                    if (bytesRead > 0) {
                        val data = String(buffer, 0, bytesRead)
                        // Process the received data here
                        Log.i("MainActivity", "Alınan veri: $data")
                        //MainActivity.okunan_deger.text = "yes"

                        //Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT).show()
                        deger = data.toString()
                        kontrol = kontrol + 1


                        val text = data.toString()
                        val words = text.split(" ")
                        //Toast.makeText(this, words[0]+words[1], Toast.LENGTH_SHORT).show()

                        val sicaklik = words[0].split(":")
                        val adc = words[1].split(":")

                        //Toast.makeText(this, sicaklik[1]+" "+adc[1].replace("Temp",""), Toast.LENGTH_SHORT).show()
                        println(sicaklik[1])
                        println(adc[1].replace("Temp",""))

/*
                        runBlocking {
                            delay(1000)
                            // 1 saniye sonra buraya geçilir
                            println("1 saniye geçti")
                        }

 */

                        var sicaklik_float = sicaklik[1].toFloat()
                        list.add(Entry(zaman, sicaklik_float))


                        lineDataSet = LineDataSet(list, "Sıcaklık")
                        lineChart.data = LineData(lineDataSet)
                        lineChart.invalidate()


                        var adc_float = adc[1].replace("Temp","").toFloat()
                        list_ADC.add(Entry(zaman, adc_float))

                        lineDataSet_ADC = LineDataSet(list_ADC, "ADC")
                        lineChart_ADC.data = LineData(lineDataSet_ADC)
                        lineChart_ADC.invalidate()

                        zaman += 1f







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
            }
            catch (e:IOException){
                println(e.message)
            }
        }

    }


    fun readThread () {
        val inputStream: InputStream = m_bluetoothSocket!!.inputStream//inputtan gelen veri
        Toast.makeText(this, "no", Toast.LENGTH_SHORT).show()


        val buffer = ByteArray(1024)
        val bytesRead: Int = inputStream.read(buffer)

        var deger = ""

        var kontrol = 0
        while (kontrol < 1) {
            if (bytesRead > 0) {
                val data = String(buffer, 0, bytesRead)
                // Process the received data here
                Log.i("MainActivity", "Alınan veri: $data")
                //MainActivity.okunan_deger.text = "yes"

                //Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT).show()
                deger = data.toString()
                kontrol = kontrol + 1


                val text = data.toString()
                val words = text.split(" ")
                //Toast.makeText(this, words[0]+words[1], Toast.LENGTH_SHORT).show()

                val sicaklik = words[0].split(":")
                val adc = words[1].split(":")

                Toast.makeText(this, sicaklik[1]+" "+adc[1], Toast.LENGTH_SHORT).show()





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
    }
}