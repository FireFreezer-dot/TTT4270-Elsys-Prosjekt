package com.example.ticketscanner

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ticketscanner.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.util.UUID


//initialiserer globale verdier
object Globalvariable {
    lateinit var name: String //lateinit slik at den blir oppdatert i senere if setning
    var amount: Int = 0 //Int må bli initialisert med en gang, skriver lik 0 men blir oppdater i if setning
    var canShowTicket: Boolean = false //standard verdi er false, oppdateres til true når alt stemmer
}
class MainActivity : AppCompatActivity() {

    // ADD THIS LINE HERE (The "Introduction")
    private lateinit var binding: ActivityMainBinding
    private var currentCallback: AdvertiseCallback? = null

    private fun getAdvertiser(): BluetoothLeAdvertiser? {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager //får tilgang til bluetooth (som manager)
        val bluetoothAdapter = bluetoothManager.adapter //henter adapteren
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser //lagrer BLE advertising i varibelen advertiser
        return advertiser
    }

    //Advertise from android to esp32
    private fun advertise(sessionKey: String?) {

        val advertiser = getAdvertiser() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY ) //for å spare strøm
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )      //for å få signalet sendt
            .setConnectable( false )
            .build()


        //transforming sessionKey form string to ByteArray for advertisement
        val sessionKeyBytes = sessionKey?.toByteArray(Charsets.UTF_8) ?: ByteArray(0)

        //convert to UUID object
        val buffer = ByteBuffer.wrap(sessionKeyBytes)
        val sessionKeyUuid = UUID(buffer.long, buffer.long)

        //wrap it in a parcel
        val parcelSessionKey = ParcelUuid(sessionKeyUuid)

        //Data sent in the advertisement
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName( true )
            .addServiceUuid( parcelSessionKey )
            .build()

        val advertisingCallback: AdvertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("BLE", "Advertising onStartFailure: " + errorCode)
                super.onStartFailure(errorCode)
            }
        }
        currentCallback = advertisingCallback
        advertiser.startAdvertising(settings, data, advertisingCallback)
    }

    private fun stopAdvertising() {
        // check if the bluetooth is permitted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        //stop the advertisement
        currentCallback?.let { callback ->
            getAdvertiser()?.stopAdvertising(callback)
            currentCallback = null
            Log.d("BLE", "Stopped.")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 2. Initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 3. Use binding.root to show the screen
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
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

                //advertising (fjerner // når funksjonen er definert
                //if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() ) {
                    //Toast.makeText( this, "Multiple advertisement not supported", Toast.LENGTH_SHORT ).show()
                    //mAdvertiseButton.setEnabled( false );
                    //mDiscoverButton.setEnabled( false );
                //}
                //else {
                    //advertise(sessionKey)
                //}
                advertise(sessionKey)
            }
            else {
                binding.ivQRCode.setImageResource(0)
                binding.btnGetTicket.setText("GET TICKET")
                binding.etTicketAmount.visibility = View.VISIBLE
                binding.etTicketName.visibility = View.VISIBLE
                //Fjerner teksten fra disse inputfeltene.
                binding.etTicketName.text.clear()
                binding.etTicketAmount.text.clear()
                ticketShown = false
                stopAdvertising()
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

