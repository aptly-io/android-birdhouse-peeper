package xyz.techmush.birdhouse_peeper

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPDevice
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.BleScanListener
import com.espressif.provisioning.listeners.ProvisionListener
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object{
        private const val SSID = ""
        private const val PASSPHRASE = ""
        private const val PROOF_OF_POSSESSION = "abcd1234"
    }

    lateinit var searchResult : MutableMap<String, ScanResult>
    lateinit var espDevice: ESPDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("need permission for Manifest.permission.ACCESS_FINE_LOCATION. For now force it through settings!")
            return
        }

        // todo on a background thread
        // todo refactor the call-back/listeners
        // todo handle credentials properly
        // todo change the device's prefix (e.g. "BHP")
        // todo provision for running an AP or STA
        // todo proper UI/fragment

        searchResult = mutableMapOf()
        val espManager = ESPProvisionManager.getInstance(this)
        espDevice = espManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1)
        espManager.searchBleEspDevices("PROV_", object: BleScanListener {
            override fun onPeripheralFound(device: BluetoothDevice?, scanResult: ScanResult?) {
                Timber.d("searchBleEspDevices found: $device, $scanResult")

                if (null == device) {
                    Timber.w("no BluetoothDevice")
                    return
                }
                if (null == scanResult) {
                    Timber.w("no ScanResult")
                    return
                }
                if (null == scanResult.scanRecord) {
                    Timber.w("no ScanRecord")
                    return
                }
                if (scanResult.scanRecord!!.serviceUuids.isEmpty()) {
                    Timber.w("no service UUIDs")
                }

                searchResult[device.address] = scanResult
            }

            override fun scanCompleted() {
                Timber.d("searchBleEspDevices completed")

                if (searchResult.isNotEmpty()) {
                    val scanEntry = searchResult.entries.first()
                    val uuids = scanEntry.value.scanRecord!!.serviceUuids
                    espDevice.connectBLEDevice(scanEntry.value.device, uuids.first().toString())
                    espDevice.proofOfPossession = PROOF_OF_POSSESSION

                    // give time to connectBLEDevice to succeed
                    Thread.sleep(3000)

                    espDevice.provision(SSID, PASSPHRASE, object: ProvisionListener {
                        override fun createSessionFailed(e: java.lang.Exception?) {
                            Timber.w("provision failed: $e")
                        }

                        override fun wifiConfigSent() {
                            Timber.d("wifiConfigSent")
                        }

                        override fun wifiConfigFailed(e: java.lang.Exception?) {
                            Timber.w("provision failed in wifiConfig: $e")
                            espDevice.disconnectDevice()
                        }

                        override fun wifiConfigApplied() {
                            Timber.d("wifiConfigApplied")
                        }

                        override fun wifiConfigApplyFailed(e: java.lang.Exception?) {
                            Timber.w("provision failed in wifiConfigApply: $e")
                            espDevice.disconnectDevice()
                        }

                        override fun provisioningFailedFromDevice(failureReason: ESPConstants.ProvisionFailureReason?) {
                            Timber.w("provision failed from device: $failureReason")
                            espDevice.disconnectDevice()
                        }

                        override fun deviceProvisioningSuccess() {
                            Timber.d("deviceProvisioningSuccess")
                            espDevice.disconnectDevice()
                        }

                        override fun onProvisioningFailed(e: java.lang.Exception?) {
                            Timber.w("provision failed onProvisionFailed: $e")
                            espDevice.disconnectDevice()
                        }
                    })
                }
            }

            override fun scanStartFailed() {
                Timber.w("searchBleEspDevices: scanStartFailed")
            }

            override fun onFailure(e: Exception?) {
                Timber.w("searchBleEspDevices failed: $e")
            }
        })

    }
}