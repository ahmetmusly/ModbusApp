package com.modbusapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputLayout
import de.re.easymodbus.modbusclient.ModbusClient
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Created by Ahmet_MUŞLUOĞLU on 17.05.2022
 */


class MainActivity : AppCompatActivity() {


    private var ipAddress: String = "127.0.0.1"
    private var port: Int = 502
    private var modbusClient: ModbusClient? = null

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setTheme(R.style.Theme_MyTheme)
        setContentView(R.layout.activity_main)

        tilIpAddress.editText?.setText(ipAddress)
        tilPort.editText?.setText("$port")

        tilIpAddress.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
        tilPort.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT


        switch1.setOnCheckedChangeListener { _, isChecked ->
            if (modbusClient != null && modbusClient!!.isConnected) {
                val light1Value: Int = modbusClient!!.ReadHoldingRegisters(0, 1)[0]
                log("light1Value : $light1Value")

                try {
                    if (isChecked) {
                        modbusClient!!.WriteSingleRegister(0, 1)
                        toast("1.Işık açıldı")
                    } else {
                        modbusClient!!.WriteSingleRegister(0, 0)
                        toast("1.Işık kapatıldı")
                    }
                } catch (e: Exception) {
                    toast(e.message)
                }
            } else {
                toast("Bağlantı yok")
            }
        }

        switch2.setOnCheckedChangeListener { _, isChecked ->
            if (modbusClient != null && modbusClient!!.isConnected) {
                try {
                    val light2Value: Int = modbusClient!!.ReadHoldingRegisters(1, 1)[0]
                    log("light2Value : $light2Value")

                    if (isChecked) {

                        modbusClient!!.WriteSingleRegister(1, 1)
                        toast("2.Işık açıldı")
                    } else {
                        modbusClient!!.WriteSingleRegister(1, 0)
                        toast("2.Işık kapatıldı")
                    }
                } catch (e: Exception) {
                    toast(e.message)
                }
            } else {
                toast("Bağlantı yok")
            }
        }

        btnConnect.setOnClickListener {
            connect()
        }

        val touchInterceptor = findViewById<View>(R.id.root) as ConstraintLayout
        touchInterceptor.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (tieIpAddress.isFocused) {
                    val outRect = Rect()
                    tieIpAddress.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        tieIpAddress.clearFocus()
                        val imm: InputMethodManager = v.context
                            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
                if (tiePort.isFocused) {
                    val outRect = Rect()
                    tiePort.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        tiePort.clearFocus()
                        val imm: InputMethodManager =
                            v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
            false
        }
    }

    private fun connect() {
        val thread = Thread {
            try {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(tieIpAddress.windowToken, 0)

                ipAddress = tilIpAddress.editText?.text.toString()
                port = tilPort.editText?.text.toString().toInt()
                modbusClient = ModbusClient(ipAddress, port)
                try {
                    if (!modbusClient!!.isConnected) {

                        modbusClient!!.Connect()

                        val light1Value: Int = modbusClient!!.ReadHoldingRegisters(0, 1)[0]
                        val light2Value: Int = modbusClient!!.ReadHoldingRegisters(1, 1)[0]
                        log("light1Value : $light1Value")
                        log("light2Value : $light2Value")

                        toast("Bağlantı kuruldu")
                    } else {
                        toast("Bağlantı var")
                        log("bağlantı var")
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