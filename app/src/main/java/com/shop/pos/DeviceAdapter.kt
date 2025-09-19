package com.shop.pos

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Very important to suppress this warning as we will check permission in the Activity
@SuppressLint("MissingPermission")
class DeviceAdapter(
    private val devices: List<BluetoothDevice>,
    private val onDeviceClicked: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.textViewDeviceName)
        val deviceAddress: TextView = itemView.findViewById(R.id.textViewDeviceAddress)

        init {
            itemView.setOnClickListener {
                onDeviceClicked(devices[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_device, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.name ?: "Unknown Device"
        holder.deviceAddress.text = device.address
    }
}