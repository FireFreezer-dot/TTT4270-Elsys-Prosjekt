package com.example.ticketscanner

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import com.example.ticketscanner.databinding.ActivityMainBinding
import android.R.bool

class MainActivity : AppCompatActivity() {

    // ADD THIS LINE HERE (The "Introduction")
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 2. Initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 3. Use binding.root to show the screen
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var ticketShown : Boolean = false

        binding.btnGetTicket.setOnClickListener {
            Log.d("MainActivity", binding.btnGetTicket.text.toString())
            if(!ticketShown) {

                val payload = OobPayloadBuilder().createPayload() //henter OOB string
                val bitmap = QrGenerator().getBitmapFromString(payload) //generer bitmap av OOB string
                binding.ivQRCode.setImageBitmap(bitmap) //fremviser bitmap i app

                //binding.ivQRCode.setImageResource(R.drawable.rick_rolling_code)
                binding.btnGetTicket.text = "HIDE TICKET"
            }
            else {
                binding.ivQRCode.setImageResource(0)
                binding.btnGetTicket.setText(R.string.get_ticket)
            }
            ticketShown = !ticketShown
        }
//        val btnGetTicket = findViewById<Button>(R.id.btnGetTicket)
//        btnGetTicket.setOnClickListener {
            //val firstName = etFirstName.text.toString()
            //val lastName = etLastName.text.toString()
            //Log.d("MainActivity", "$firstName $lastName")
    }
}

