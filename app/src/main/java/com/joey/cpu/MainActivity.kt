package com.joey.cpu


import CustomAdapter
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.*
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {


    private val receiver:BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val health = when(intent?.getIntExtra(
                    BatteryManager.EXTRA_HEALTH, -1
            )){
                // determine the battery health from intent
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Over heat"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
                BatteryManager
                        .BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified failure"
                else -> "Error"
            }


            battery_health.setText(health)
        }
    }



    lateinit var available_Memory: TextView
    lateinit var uasge_Memory: TextView
    lateinit var cpu_temperature: TextView

    lateinit var cpuInfo: TextView

    lateinit var totalCpuUsage: TextView
    lateinit var  battery_voltage: TextView
    lateinit var  battery_capacity: TextView
    lateinit var  battery_health: TextView
    lateinit var recyclerView:RecyclerView

     var mContext: Context? = null

    private var sLastCpuCoreCount = -1

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)


        available_Memory = findViewById(R.id.available_Memory)
        uasge_Memory = findViewById(R.id.uasge_Memory)
        cpu_temperature = findViewById(R.id.cpu_temperature)
        cpuInfo = findViewById(R.id.cpuInfo)
        totalCpuUsage = findViewById(R.id.totalCpuUsage)

        battery_voltage = findViewById(R.id.battery_voltage)
        battery_capacity = findViewById(R.id.battery_capacity)
        battery_health = findViewById(R.id.battery_health)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)








     //cpu_temperature------------------------------------------------------

        cpu_temperature.setText(DecimalFormat("##.#").format(cpuTemperature()) + "");


        val handler3 = Handler()
        val delay3 = 500 //milliseconds

        handler3.postDelayed(object : Runnable {
            override fun run() {
                cpu_temperature.setText(DecimalFormat("##.#").format(cpuTemperature()) + "");

                handler3.postDelayed(this, delay3.toLong())
            }
        }, delay3.toLong())


        //getMemInfo -------------------------------------------------------------------------------

        getMemInfo()
        val handler2 = Handler()
        val delay2 = 500 //milliseconds

        handler2.postDelayed(object : Runnable {
            override fun run() {
                getMemInfo()
                handler2.postDelayed(this, delay2.toLong())
            }
        }, delay2.toLong())

        //---------------------------------------


     //battery health-------------------------------------------------------------------------------

        Log.i("ReadCpu3--------------", ReadCpu3())




        //------------------------total cpu percent usage----------------------------------------

        totalCpuUsage()


        val handler8 = Handler()
        val delay8 = 500 //milliseconds

        handler8.postDelayed(object : Runnable {
            override fun run() {

                totalCpuUsage()

                handler8.postDelayed(this, delay8.toLong())
            }
        }, delay8.toLong())



        //------------------------every core usage----------------------------------------
// it will give use the usage of evey core in HZ core, you have to device the frequency by 1e+6 or by 1 million






        //------------------------every core percentage----------------------------------------



        coresUsagePercentage()
        val handler7 = Handler()
        val delay7 = 500 //milliseconds

        handler7.postDelayed(object : Runnable {
            override fun run() {

                coresUsagePercentage()

                handler7.postDelayed(this, delay7.toLong())
            }
        }, delay7.toLong())


        //----------------------------------------------------------------
        Log.d("coersInfo", ReadCoresinfo() + "")

    //----------------------------------------------------------------------------------------------







        // Battery Capacity

        battery_capacity.setText(getBatteryCapacity(this).toString() + "");



        // Battery Voltage
        val intentfilter: IntentFilter
        intentfilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        this@MainActivity.registerReceiver(broadcastreceiver, intentfilter)


        //battery heakth


        //battery heakth

        // initialize a new intent filter instance
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        // register the broadcast receiver
        registerReceiver(receiver, filter)


//-------------------------------------------------------


        // cpu info 1
        //-------------------------------------------------------

/*
        try {

            // cpuInfo.setText("cpu info:     "+getCPUInfo());
            for ((key, value) in getCPUInfo()?.entries!!) {
                cpuInfo.append("$key:  $value\n")
            }
        } catch (e: IOException) {
        }

 */

        Log.i("TAG", "SERIAL: " + Build.SERIAL);
        Log.i("TAG","MODEL: " + Build.MODEL);
        Log.i("TAG","ID: " + Build.ID);
        Log.i("TAG","Manufacture: " + Build.MANUFACTURER);
        Log.i("TAG","brand: " + Build.BRAND);
        Log.i("TAG","type: " + Build.TYPE);
        Log.i("TAG","user: " + Build.USER);
        Log.i("TAG","BASE: " + Build.VERSION_CODES.BASE);
        Log.i("TAG","INCREMENTAL " + Build.VERSION.INCREMENTAL);
        Log.i("TAG","SDK  " + Build.VERSION.SDK);
        Log.i("TAG","BOARD: " + Build.BOARD);
        Log.i("TAG","BRAND " + Build.BRAND);
        Log.i("TAG","HOST " + Build.HOST);
        Log.i("TAG","FINGERPRINT: "+Build.FINGERPRINT);
        Log.i("TAG","Version Code: " + Build.VERSION.RELEASE);



    }//------------------------------------------------------------------------------------------------------------- oncreate






    fun totalCpuUsage(){

        var totalCpuPercentageUsage=0.0
        var x=0.0
        for (i in 0 until calcCpuCoreCount()) {

            x+=  ( takeCurrentCpuFreq(i).toFloat()/takeMaxCpuFreq(i).toFloat() )*100

        }

        totalCpuPercentageUsage=x/calcCpuCoreCount().toFloat()
        totalCpuUsage.setText("" + totalCpuPercentageUsage.roundToInt() + "%")
    }



    private fun ReadCoresinfo(): String? {
        val cmd: ProcessBuilder
        var result = ""
        try {
            val args = arrayOf("/system/bin/cat", "/proc/cpuinfo")
            cmd = ProcessBuilder(*args)
            val process = cmd.start()
            val `in` = process.inputStream
            val re = ByteArray(1024)
            while (`in`.read(re) !== -1) {
                println(String(re))
                result = result + String(re)
            }
            `in`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return result
    }

    //-----------------------------------------------------------------------------

    fun     coresUsagePercentage () {


        val data = arrayListOf<String>();


        for (i in 0 until calcCpuCoreCount()) {

            val x=  ( takeCurrentCpuFreq(i).toFloat()/takeMaxCpuFreq(i).toFloat() )*100
            val currentCorePercentageUsage=x.roundToInt()


            // we devide takeCurrentCpuFreq(i) by 1 million to convert hertz to mega hertz
            data.add(
                    "Core " + (i + 1) + "  " + currentCorePercentageUsage.toString() + "%   " + (takeCurrentCpuFreq(
                            i
                    ).toFloat() / 1000000).toString() + "Mhz"
            )


        }



        val adapter = CustomAdapter(this, data)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 2)
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.adapter = adapter


    }

    //------------------------------------------------------------------------------



    private fun readIntegerFile(filePath: String): Int {
        return try {
            val reader = BufferedReader(
                    InputStreamReader(FileInputStream(filePath)), 1000
            )
            val line = reader.readLine()
            reader.close()
            line.toInt()
        } catch (e: java.lang.Exception) {
            0
        }
    }

    private fun takeCurrentCpuFreq(coreIndex: Int): Int {
        return readIntegerFile("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_cur_freq")
    }

    private fun takeMaxCpuFreq(coreIndex: Int): Int {
        return readIntegerFile("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_max_freq")
    }




    fun calcCpuCoreCount(): Int {
        if (sLastCpuCoreCount >= 1) {
            // キャッシュさせる
            return sLastCpuCoreCount
        }
        sLastCpuCoreCount = try {
            // Get directory containing CPU info
            val dir = File("/sys/devices/system/cpu/")
            // Filter to only list the devices we care about
            val files = dir.listFiles { pathname -> //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.name)) {
                    true
                } else false
            }

            // Return the number of cores (virtual CPU devices)
            files.size
        } catch (e: java.lang.Exception) {
            Runtime.getRuntime().availableProcessors()
        }
        return sLastCpuCoreCount
    }







//--------------------------------------------------------------------------------------------------
    fun cpuTemperature(): Float {
        val process: Process
        return try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp")
            process.waitFor()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line: String = reader.readLine()
            if (line != null) {
                val temp = line.toFloat()
                temp / 1000.0f
            } else {
                51.0f
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            0.0f
        }
    }


    //---------------------------------


    private fun ReadCpu3():String{

        val sb = StringBuffer();
        sb.append("abi: ").append(Build.CPU_ABI).append("\n");

        if (File("/proc/cpuinfo").exists()) {
            try {
                //val br =  BufferedReader(FileReader(File("/proc/cpuinfo")));
                val file = File("/proc/cpuinfo")

                file.bufferedReader().forEachLine {
                    sb.append(it + "\n");
                }

            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    fun getBatteryCapacity(context: Context?): Double {
        val mPowerProfile: Any
        var batteryCapacity = 0.0
        val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context::class.java)
                    .newInstance(context)
            batteryCapacity = Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile) as Double
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return batteryCapacity
    }

    //---------------------------------------------------------------------------


    private val broadcastreceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //float fullVoltage = (float) (batteryVol * 0.001);

            battery_voltage.setText(
                    intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toString() + ""
            )


        }
    }

    //----------------------------------------------------------------------------







    private fun  getMemInfo() {


        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        val runtime = Runtime.getRuntime()
        var x = DecimalFormat("##.#")
            .format(memoryInfo.availMem / 1024.0 / 1024.0 / 1024.0)
        var y = DecimalFormat("##.#")
            .format(memoryInfo.totalMem / 1024.0 / 1024.0 / 1024.0)

        available_Memory.setText("" + x + "")


        x = x.toString().replace(',', '.')
        y = y.toString().replace(',', '.')



        val used: Float =  convertAllIndianToArabic(y).toFloat() - convertAllIndianToArabic(x).toFloat()
        uasge_Memory.setText("" + DecimalFormat("##.#").format(used.toDouble()) + "")
        //Toast.makeText(activity,""+x,Toast.LENGTH_SHORT).show();

    }

    fun convertAllIndianToArabic(str: String): String {
        var str = str
        for (i in 0 until str.length) {
            if (str[i] == '٠') str =
                str.substring(0, i) + "0" + str.substring(i + 1) else if (str[i] == '١') str =
                str.substring(0, i) + "1" + str.substring(i + 1) else if (str[i] == '٢') str =
                str.substring(0, i) + "2" + str.substring(i + 1) else if (str[i] == '٣') str =
                str.substring(0, i) + "3" + str.substring(i + 1) else if (str[i] == '٤') str =
                str.substring(0, i) + "4" + str.substring(i + 1) else if (str[i] == '٥') str =
                str.substring(0, i) + "5" + str.substring(i + 1) else if (str[i] == '٦') str =
                str.substring(0, i) + "6" + str.substring(i + 1) else if (str[i] == '٧') str =
                str.substring(0, i) + "7" + str.substring(i + 1) else if (str[i] == '٨') str =
                str.substring(0, i) + "8" + str.substring(i + 1) else if (str[i] == '٩') str =
                str.substring(0, i) + "9" + str.substring(i + 1) else if (str[i] == '٫') str =
                str.substring(0, i) + "." + str.substring(i + 1)
        }
        return str
    }






}