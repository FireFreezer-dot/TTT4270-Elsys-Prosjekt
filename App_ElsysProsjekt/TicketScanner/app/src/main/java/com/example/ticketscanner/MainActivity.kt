package com.example.ticketscanner

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import com.example.ticketscanner.databinding.ActivityMainBinding
import android.R.bool
import android.view.View

//initialiserer globale verdier
object Globalvariable {
    lateinit var name: String
    var amount: Int = 0
    var canShowTicket: Boolean = false
}
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

            // 1 Initialisering av verdier

            //Sjekker om begge inputfelt er fylt ut også gjør vi de om til riktig format
            if (!binding.etTicketName.text.isEmpty() && !binding.etTicketAmount.text.isEmpty())
            {
                Globalvariable.name = binding.etTicketName.text.toString()
                //Henter innhold fra inputfelt og konverterer til en Int. Gir null hvis ikke en Int.
                Globalvariable.amount = binding.etTicketAmount.text.toString().toIntOrNull() ?: 0
                Globalvariable.canShowTicket = false
                if (Globalvariable.amount > 0 && Globalvariable.amount <= 12) {
                    Globalvariable.canShowTicket = true
                }
            }

            // 2 Sjekker tilstanden til programmet å går ut ifra det.
            // Viser ikke billet + gyldig tekst => hvis billet.
            if(!ticketShown && Globalvariable.canShowTicket) {

                val payload = OobPayloadBuilder().buildPayload(Globalvariable.name, Globalvariable.amount) //henter OOB string
                val bitmap = QrGenerator().getBitmapFromString(payload) //generer bitmap av OOB string
                val sessionKey = OobPayloadBuilder().lastSessionKey //lagrer sessionkey i app
                binding.ivQRCode.setImageBitmap(bitmap) //fremviser bitmap i app.

                //Endrer eller skjuler knapp tekst / tekstfelt.
                binding.btnGetTicket.text = "HIDE TICKET"
                binding.etTicketAmount.visibility = View.GONE
                binding.etTicketName.visibility = View.GONE
                ticketShown = true
            }
            else {
                binding.ivQRCode.setImageResource(0)
                binding.btnGetTicket.setText(R.string.get_ticket)
                binding.etTicketAmount.visibility = View.VISIBLE
                binding.etTicketName.visibility = View.VISIBLE
                //Fjerner teksten fra disse inputfeltene.
                binding.etTicketName.text.clear()
                binding.etTicketAmount.text.clear()
                ticketShown = false
            }
            //ticketShown = !ticketShown
        }
//        val btnGetTicket = findViewById<Button>(R.id.btnGetTicket)
//        btnGetTicket.setOnClickListener {
            //val firstName = etFirstName.text.toString()
            //val lastName = etLastName.text.toString()
            //Log.d("MainActivity", "$firstName $lastName")
    }
}

