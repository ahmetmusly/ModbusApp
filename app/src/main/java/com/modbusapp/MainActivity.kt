package com.modbusapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.textfield.TextInputLayout
import de.re.easymodbus.modbusclient.ModbusClient
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Created by Ahmet_MUŞLUOĞLU on 17.05.2022
 */


class MainActivity : AppCompatActivity() {


    companion object {
        const val CHANNEL_COUNT: Int = 12
    }

    private var ipAddress: String = "192.168.1.13"

    private var port: Int = 502

    private var modbusClient: ModbusClient? = null
    private var modbusAdapter: ModbusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initViews()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        ip_address_layout.editText?.setText(ipAddress)
        port_layout.editText?.setText("$port")

        ip_address_layout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
        port_layout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT

        btnConnect.setOnClickListener {
            connect()
        }

        root.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (et_ip_address.isFocused) {
                    val outRect = Rect()
                    et_ip_address.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        et_ip_address.clearFocus()
                        val imm: InputMethodManager = v.context
                            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
                if (et_port.isFocused) {
                    val outRect = Rect()
                    et_port.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        et_port.clearFocus()
                        val imm: InputMethodManager =
                            v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
            false
        }

        tv_open_all.setOnClickListener {
            modbusAdapter?.apply {
                for (i in 0..itemCount)
                    updateChannel(i, 1)
            }

            if (modbusClient == null || !modbusClient!!.isConnected) {
                toast("Bağlantı yok")
            }
        }

        tv_close_all.setOnClickListener {
            modbusAdapter?.apply {
                for (i in 0..itemCount)
                    updateChannel(i, 0)
            }
            if (modbusClient == null || !modbusClient!!.isConnected) {
                toast("Bağlantı yok")
            }
        }

        loadChannels()
    }

    @SuppressLint("SetTextI18n")
    private fun connect() {

        val thread = Thread {
            try {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(et_ip_address.windowToken, 0)

                ipAddress = ip_address_layout.editText?.text.toString()
                port = port_layout.editText?.text.toString().toInt()
                modbusClient = ModbusClient(ipAddress, port)

                try {
                    if (!modbusClient!!.isConnected) {
                        modbusClient?.Connect()
                        toast("Bağlantı kuruldu")
                        log("Bağlantı kuruldu")

                        btnConnect.text = "Bağlantıyı\nKes"
                        tv_connection_status.text = "Bağlı"
                        tv_connection_status.setBackgroundColor(Color.parseColor("#70CA70"))
                    } else {
                        modbusClient?.Disconnect()
                        toast("Bağlantı var")
                        log("bağlantı var")

                        btnConnect.text = "Bağlantı\nKur"
                        tv_connection_status.text = "Bağlı Değil"
                        tv_connection_status.setBackgroundColor(Color.parseColor("#BD2222"))
                    }
                } catch (e: Exception) {
                    log(e.message)
                    toast(e.message)
                    if (modbusClient!!.isConnected)
                        modbusClient!!.Disconnect()
                }
            } catch (e: java.lang.Exception) {
                toast(e.message)
                log(e.message)
                e.printStackTrace()
            }

        }
        thread.start()
    }

    private fun loadChannels() {
        val channelList = mutableListOf<ModbusModel>()

        for (i in 0..CHANNEL_COUNT) {
            channelList.add(ModbusModel(index = i))
        }
        modbusAdapter = ModbusAdapter(modbusClient, channelList)

        val spanCount = if (isTablet()) 4 else 2
        recyclerView.apply {
            layoutManager =
                StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            adapter = modbusAdapter!!
            hasFixedSize()
        }
    }

    private fun isTablet(): Boolean {
        return ((resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE)
    }

    @SuppressLint("LogNotTimber")
    fun log(log: Any? = "EMPTY") {
        Log.i("ModbusActivity", "connect: $log")
    }

    private fun toast(log: Any? = "EMPTY") {
        runOnUiThread {
            Toast.makeText(this, "$log", Toast.LENGTH_LONG).show()
        }
    }
}