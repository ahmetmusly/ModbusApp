package com.modbusapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.*
import de.re.easymodbus.modbusclient.ModbusClient


/**
 * Created by Ahmet_MUŞLUOĞLU on 21.05.2022
 */


@SuppressLint("SetTextI18n")
class ModbusAdapter(
    modbusClient: ModbusClient?,
    private val channelList: MutableList<ModbusModel>
) :
    Adapter<ModbusAdapter.ModelViewHolder>() {


    private var mClient: ModbusClient? = modbusClient

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
        return ModelViewHolder(view)
    }

    override fun getItemCount(): Int {
        return channelList.toList().size
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bindItems(channelList[position])
    }

    inner class ModelViewHolder(view: View) : ViewHolder(view) {

        private val channelName: TextView = view.findViewById(R.id.tv_all_channell)
        private val channelStatus: TextView = view.findViewById(R.id.tv_channel_status)
        private val openChannel: TextView = view.findViewById(R.id.tv_open)
        private val closeChannel: TextView = view.findViewById(R.id.tv_close)


        fun bindItems(item: ModbusModel) {
            channelName.text = "${item.index.plus(1)}. ${item.name}"

            Log.i("TAG", "bindItems: $itemCount")
            if (mClient != null && mClient!!.ReadCoils(0, itemCount)[item.index]) {//item.status
                channelStatus.text = "Açık"
                channelStatus.setBackgroundColor(Color.parseColor("#70CA70"))
            } else {
                channelStatus.text = "Kapalı"
                channelStatus.setBackgroundColor(Color.parseColor("#BD2222"))
            }

            openChannel.setOnClickListener {
                updateChannel(item.index, 1)
                if (mClient == null || !mClient!!.isConnected) {
                    Toast.makeText(it.context, "Bağlantı yok", Toast.LENGTH_LONG).show()
                }
            }

            closeChannel.setOnClickListener {
                updateChannel(item.index, 0)
                if (mClient == null || !mClient!!.isConnected) {
                    Toast.makeText(it.context, "Bağlantı yok", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun updateChannel(index: Int, value: Int) {
        try {
            Log.i("TAG", "update: $index $value")
            if (mClient != null && mClient!!.isConnected) {
                mClient?.WriteSingleRegister(index, value)
                channelList[index].status = value == 1
                notifyItemChanged(index)
            } else {
                Log.i("TAG", "updateChannel: bağlantı yok")
            }
        } catch (e: Exception) {
            Log.i("TAG", "update: ${e.message}")
        }
    }
}