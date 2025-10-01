package com.shop.pos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var textViewPinDisplay: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button
    private lateinit var button6: Button
    private lateinit var button7: Button
    private lateinit var button8: Button
    private lateinit var button9: Button
    private lateinit var button0: Button
    private lateinit var buttonBackspace: ImageButton

    private var enteredPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // **အရေးကြီးသော အပြောင်းအလဲ (၁)**
        // App မစခင် PIN Lock ဖွင့်ထား/မထားကို PinManager ထဲမှာ အရင်စစ်ဆေးပါ
        if (!PinManager.isPinEnabled(this)) {
            // PIN Lock ပိတ်ထားပါက Dashboard ကို တိုက်ရိုက်သွားပါ
            goToDashboard()
            // ဒီ Screen ကိုဆက်မဖွင့်တော့ပါ
            return
        }

        // PIN Lock ဖွင့်ထားမှသာ ဒီ Screen ကို ပြပါ
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        textViewPinDisplay = findViewById(R.id.textViewPinDisplay)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)
        button5 = findViewById(R.id.button5)
        button6 = findViewById(R.id.button6)
        button7 = findViewById(R.id.button7)
        button8 = findViewById(R.id.button8)
        button9 = findViewById(R.id.button9)
        button0 = findViewById(R.id.button0)
        buttonBackspace = findViewById(R.id.buttonBackspace)
    }

    private fun setupClickListeners() {
        val numberButtons = listOf(button1, button2, button3, button4, button5, button6, button7, button8, button9, button0)
        numberButtons.forEach { button ->
            button.setOnClickListener { onNumberClick(button) }
        }

        buttonBackspace.setOnClickListener { onBackspaceClick() }
    }

    private fun onNumberClick(button: Button) {
        if (enteredPin.length < 4) {
            enteredPin += button.text.toString()
            updatePinDisplay()
        }
    }

    private fun onBackspaceClick() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.substring(0, enteredPin.length - 1)
            updatePinDisplay()
        }
    }

    private fun updatePinDisplay() {
        val maskedPin = "*".repeat(enteredPin.length)
        textViewPinDisplay.text = maskedPin

        if (enteredPin.length == 4) {
            checkPin()
        }
    }

    private fun checkPin() {
        // **အရေးကြီးသော အပြောင်းအလဲ (၂)**
        // PIN အသေ ("1234") နဲ့ မစစ်တော့ဘဲ PinManager ထဲမှာ သိမ်းထားတဲ့ PIN နဲ့ စစ်ဆေးပါ
        val savedPin = PinManager.getPin(this)
        if (enteredPin == savedPin) {
            goToDashboard()
        } else {
            Toast.makeText(this, "PIN မှားယွင်းပါသည်", Toast.LENGTH_SHORT).show()
            enteredPin = ""
            updatePinDisplay()
        }
    }

    // Dashboard ကိုသွားရန် Function အသစ်
    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}