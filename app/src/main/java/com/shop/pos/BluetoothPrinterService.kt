package com.shop.pos

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothPrinterService(
    private val device: BluetoothDevice,
    private val charactersPerLine: Int
) {

    private var outputStream: OutputStream? = null
    private var socket: BluetoothSocket? = null
    private val lineWidth: Int = charactersPerLine

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // ESC/POS Commands
    private val CMD_INIT_PRINTER = byteArrayOf(0x1B, 0x40)
    private val CMD_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 1)
    private val CMD_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0)
    private val CMD_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 2)
    private val CMD_FONT_BOLD = byteArrayOf(0x1B, 0x45, 1)
    private val CMD_FONT_NORMAL = byteArrayOf(0x1B, 0x45, 0)
    private val CMD_FONT_SIZE_NORMAL = byteArrayOf(0x1D, 0x21, 0)
    private val CMD_FONT_SIZE_WIDE = byteArrayOf(0x1D, 0x21, 0x10)
    private val CMD_FONT_SIZE_TALL = byteArrayOf(0x1D, 0x21, 0x01)
    private val CMD_FONT_SIZE_TALL_WIDE = byteArrayOf(0x1D, 0x21, 0x11)
    private val CMD_FULL_CUT = byteArrayOf(0x1D, 0x56, 0)

    @Throws(IOException::class)
    fun connect() {
        socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
        socket?.connect()
        outputStream = socket?.outputStream
        outputStream?.write(CMD_INIT_PRINTER)
    }

    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun write(data: ByteArray) {
        try {
            outputStream?.write(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printText(text: String) {
        try {
            write(text.toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            write(text.toByteArray())
        }
    }

    fun printLine(text: String) {
        printText(text + "\n")
    }

    fun printTwoColumn(left: String, right: String) {
        val spaces = lineWidth - left.length - right.length
        val line = left + " ".repeat(if (spaces > 0) spaces else 0) + right
        printLine(line)
    }

    fun setAlignCenter() { write(CMD_ALIGN_CENTER) }
    fun setAlignLeft() { write(CMD_ALIGN_LEFT) }
    fun setAlignRight() { write(CMD_ALIGN_RIGHT) }

    fun setFontSize(size: String, isBold: Boolean = false) {
        if (isBold) write(CMD_FONT_BOLD) else write(CMD_FONT_NORMAL)
        when (size) {
            "wide" -> write(CMD_FONT_SIZE_WIDE)
            "tall" -> write(CMD_FONT_SIZE_TALL)
            "tall_wide" -> write(CMD_FONT_SIZE_TALL_WIDE)
            else -> write(CMD_FONT_SIZE_NORMAL)
        }
    }

    fun feedLine(lines: Int = 1) {
        repeat(lines) { printText("\n") }
    }

    fun cut() { write(CMD_FULL_CUT) }

    // --- Image Printing (for Myanmar text fallback) ---
    fun printTextAsImage(text: String, textSize: Float = 24f, isBold: Boolean = false, alignment: Paint.Align = Paint.Align.LEFT) {
        val paint = Paint()
        paint.typeface = if(isBold) Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) else Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = textSize
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.textAlign = alignment

        val width = if (lineWidth == 48) 576 else 384 // 80mm -> 576px, 58mm -> 384px
        val baseline = -paint.ascent() // ascent() is negative
        val height = (baseline + paint.descent()).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE) // Set background to white
        val canvas = Canvas(bitmap)

        val x = when(alignment) {
            Paint.Align.CENTER -> (width / 2).toFloat()
            Paint.Align.RIGHT -> width.toFloat()
            else -> 0f
        }

        canvas.drawText(text, x, baseline, paint)

        printBitmap(bitmap)
    }

    fun printBitmap(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val command = byteArrayOf(0x1D, 0x76, 0x30, 0x00)
        val widthBytes = (width + 7) / 8
        val xL = (widthBytes % 256).toByte()
        val xH = (widthBytes / 256).toByte()
        val yL = (height % 256).toByte()
        val yH = (height / 256).toByte()

        write(command)
        write(byteArrayOf(xL, xH, yL, yH))

        val rasterData = ByteArray(widthBytes * height)
        var i = 0
        for(y in 0 until height) {
            for(x in 0 until widthBytes) {
                for(bit in 0..7) {
                    val pixelX = x * 8 + bit
                    if(pixelX < width) {
                        val pixelIndex = y * width + pixelX
                        val color = pixels[pixelIndex]
                        val r = (color shr 16) and 0xFF
                        val g = (color shr 8) and 0xFF
                        val b = color and 0xFF
                        val gray = (r * 0.3 + g * 0.59 + b * 0.11).toInt()

                        if(gray < 128) {
                            rasterData[i] = (rasterData[i].toInt() or (1 shl (7 - bit))).toByte()
                        }
                    }
                }
                i++
            }
        }
        write(rasterData)
    }
}
