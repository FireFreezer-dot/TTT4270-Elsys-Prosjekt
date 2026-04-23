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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ticketscanner.databinding.ActivityMainBinding
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.UUID


//initialiserer globale verdier
object Globalvariable {
    var amount: Int = 0 //Int må bli initialisert med en gang, skriver lik 0 men blir oppdater i if setning
    var canShowTicket: Boolean = false //standard verdi er false, oppdateres til true når alt stemmer
    private val json = JSONObject().apply {
        put("ticketID", "")
        put("amountCustomers", 0)
        put("sessionKey", "")
    }
    var bitmap = QrGenerator().getBitmapFromString(json.toString())
    var sessionKey: String? = null

}
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentCallback: AdvertiseCallback? = null

    private fun getAdvertiser(): BluetoothLeAdvertiser? {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager //får tilgang til bluetooth (som manager)
        val bluetoothAdapter = bluetoothManager.adapter //henter adapteren

        //Checking CONNECT permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE", "No CONNECT permission")
            return null
        }

        bluetoothAdapter.name = "PlisFunk"

        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser //lagrer BLE advertising i varibelen advertiser
        return advertiser
    }

    //Advertise from android to esp32
    private fun advertise(sessionKey: String?) {

        //Checking that the function runs
        Log.d("BLE", "advertise() called with key: $sessionKey")

        val advertiser = getAdvertiser() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
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
                Log.d("BLE", "Advertising started successfully!")  // ← legg til
            }

            override fun onStartFailure(errorCode: Int) {
                val reason = when(errorCode) {
                    ADVERTISE_FAILED_DATA_TOO_LARGE -> "DATA TOO LARGE"
                    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "TOO MANY ADVERTISERS"
                    ADVERTISE_FAILED_ALREADY_STARTED -> "ALREADY STARTED"
                    ADVERTISE_FAILED_INTERNAL_ERROR -> "INTERNAL ERROR"
                    else -> "UNKNOWN: $errorCode"
                }
                Log.e("BLE", "Advertising failed: $reason")  // ← erstatt gammel linje
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

    fun idleState()
    {
        binding.btnDeleteTicket.visibility = View.VISIBLE
        binding.etTicketAmount.visibility = View.GONE
        binding.textView.visibility = View.GONE
        binding.btnGetTicket.text = "SHOW TICKET"
        binding.ivQRCode.setImageResource(0)
    }

    fun inputState()
    {
        binding.btnDeleteTicket.visibility = View.GONE
        binding.etTicketAmount.visibility = View.VISIBLE
        binding.textView.visibility = View.VISIBLE
        binding.btnGetTicket.text = "GET TICKET"
        binding.ivQRCode.setImageResource(0)
        binding.etTicketAmount.text.clear()
    }
    fun ticketState()
    {
        binding.ivQRCode.setImageBitmap(Globalvariable.bitmap) //fremviser bitmap i app.

        binding.btnDeleteTicket.visibility = View.GONE
        binding.etTicketAmount.visibility = View.GONE
        binding.textView.visibility = View.GONE
        binding.btnGetTicket.text = "HIDE TICKET"

        advertise(Globalvariable.sessionKey)
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

        //popup permission for bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        }

        var ticketShown : Boolean = false
        var isInput : Boolean = true

        binding.btnDeleteTicket.setOnClickListener {
            inputState()
            Globalvariable.canShowTicket = false
            ticketShown = false
            isInput = true
            stopAdvertising()
        }

        binding.btnGetTicket.setOnClickListener {

            // 1 Initialisering av verdier
            //Sjekker om begge inputfelt er fylt ut også gjør vi de om til riktig format
            Globalvariable.amount = binding.etTicketAmount.text.toString().toIntOrNull() ?: 0
            Globalvariable.canShowTicket = false
            if (Globalvariable.amount > 0 && Globalvariable.amount <= 12) {
                Globalvariable.canShowTicket = true
            }

            if (isInput) {
                if (Globalvariable.canShowTicket) {

                    val builder = OobPayloadBuilder()
                    val payload = builder.buildPayload(Globalvariable.amount) //henter OOB string
                    Globalvariable.bitmap = QrGenerator().getBitmapFromString(payload) //generer bitmap av OOB string
                    Globalvariable.sessionKey = builder.lastSessionKey //lagrer sessionkey i app

                    ticketState()
                    ticketShown = true
                    isInput = false
                    Globalvariable.canShowTicket = true
                }
                else {
                    inputState()
                    ticketShown = false
                    isInput = true
                    Globalvariable.canShowTicket = false
                }
            }
            else {
                if(ticketShown) {
                    idleState()
                    ticketShown = false
                    isInput = false
                    Globalvariable.canShowTicket = true
                }
                else {
                    ticketState()
                    ticketShown = true
                    isInput = false
                    Globalvariable.canShowTicket = true
                }
            }
        }
    }
}

