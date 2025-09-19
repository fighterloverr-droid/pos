package com.shop.pos

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothPrinterService(private val device: BluetoothDevice) {

    private var outputStream: OutputStream? = null
    private var socket: BluetoothSocket? = null

    // Standard UUID for SPP (Serial Port Profile)
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // ESC/POS Commands
    private val CMD_INIT_PRINTER = byteArrayOf(0x1B, 0x40)
    private val CMD_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 1)
    private val CMD_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0)
    private val CMD_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 2)
    private val CMD_FONT_BOLD = byteArrayOf(0x1B, 0x45, 1)
    private val CMD_FONT_NORMAL = byteArrayOf(0x1B, 0x45, 0)
    private val CMD_FONT_SIZE_NORMAL = byteArrayOf(0x1D, 0x21, 0)
    private val CMD_FONT_SIZE_WIDE = byteArrayOf(0x1D, 0x21, 0x10) // Wide
    private val CMD_FONT_SIZE_TALL = byteArrayOf(0x1D, 0x21, 0x01) // Tall
    private val CMD_FONT_SIZE_TALL_WIDE = byteArrayOf(0x1D, 0x21, 0x11) // Tall & Wide
    private val CMD_FULL_CUT = byteArrayOf(0x1D, 0x56, 0)

    @Throws(IOException::class)
    fun connect() {
        socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
        socket?.connect()
        outputStream = socket?.outputStream
        outputStream?.write(CMD_INIT_PRINTER)
    }

    fun disconnect() {
        outputStream?.close()
        socket?.close()
    }

    private fun write(data: ByteArray) {
        outputStream?.write(data)
    }

    fun printText(text: String) {
        write(text.toByteArray(charset("UTF-8")))
    }

    fun printLine(text: String) {
        printText(text + "\n")
    }

    fun printTwoColumn(left: String, right: String, lineWidth: Int = 32) {
        val spaces = lineWidth - left.length - right.length
        val line = left + " ".repeat(if (spaces > 0) spaces else 0) + right
        printLine(line)
    }

    fun setAlignCenter() {
        write(CMD_ALIGN_CENTER)
    }

    fun setAlignLeft() {
        write(CMD_ALIGN_LEFT)
    }

    fun setAlignRight() { // <-- Function အမှန်
        write(CMD_ALIGN_RIGHT)
    }

    fun setFontSize(size: String, isBold: Boolean = false) {
        if(isBold) write(CMD_FONT_BOLD) else write(CMD_FONT_NORMAL)

        when(size) {
            "wide" -> write(CMD_FONT_SIZE_WIDE)
            "tall" -> write(CMD_FONT_SIZE_TALL)
            "tall_wide" -> write(CMD_FONT_SIZE_TALL_WIDE)
            else -> write(CMD_FONT_SIZE_NORMAL)
        }
    }

    fun feedLine(lines: Int = 1) {
        for (i in 1..lines) {
            printText("\n")
        }
    }

    fun cut() {
        write(CMD_FULL_CUT)
    }
}