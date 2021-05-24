package xyz.techmush.birdhouse_peeper.vm

import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import timber.log.Timber

class BirdhouseFindViewModel : ViewModel() {

    sealed class Event {
        class NavigateCreateBirdhouse(val address: String, val uuid: String): Event()
    }

    val event = LiveEvent<Event>() // cause a single trigger

    private val _scanResults = mutableMapOf<String, ScanResult>()

    private val _peripherals = MutableLiveData<List<ScanResult>>()
    val peripherals: LiveData<List<ScanResult>> get() = _peripherals

    private val _scanResult = MutableLiveData<ScanResult>(null)
    val scanResult: LiveData<ScanResult> get() = _scanResult

    val scanning = MutableLiveData<Boolean>(false)

    fun onScanResult(scanResult: ScanResult) {
        _scanResults[scanResult.device.address] = scanResult
        _peripherals.postValue(_scanResults.values.toList())
    }

    fun onScanResultClick(scanResult: ScanResult) {
        Timber.d("onScanResultClick: $scanResult")

        val uuids = scanResult.scanRecord!!.serviceUuids
        val primaryUuid = uuids.first().toString()
        event.postValue(Event.NavigateCreateBirdhouse(
            scanResult.device.address, primaryUuid))
    }
}