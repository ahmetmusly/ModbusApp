package com.modbusapp


/**
 * Created by Ahmet_MUŞLUOĞLU on 21.05.2022
 */


data class ModbusModel(
    val index: Int = 0,
    val name: String = "Kanal",
    var status: Boolean = false
)
