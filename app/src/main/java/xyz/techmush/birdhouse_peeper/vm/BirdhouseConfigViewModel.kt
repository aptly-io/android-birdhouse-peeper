package xyz.techmush.birdhouse_peeper.vm

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import xyz.techmush.birdhouse_peeper.model.BirdhouseRepository
import xyz.techmush.birdhouse_peeper.ui.BirdhouseConfigFragmentArgs
import javax.inject.Inject

@HiltViewModel
class BirdhouseConfigViewModel @Inject constructor(private val repository: BirdhouseRepository): ViewModel() {

    private var address: String = "11:22:33:44:55:66"
    private var uuid: String = ""

    sealed class Event {
        object AddPhoto : Event()
        object Location : Event()
        class Configure(address: String, uuid: String): Event()
    }

    val event = LiveEvent<Event>() // cause a single trigger

    val name = MutableLiveData<String>("My birdhouse")

    val hasLocation = MutableLiveData<Boolean>(true)

    val location = MutableLiveData<Location>(null)

    val hasPhoto = MutableLiveData<Boolean>(true)

    val photoPath: String? = null

    val ssid = MutableLiveData<String>("BHP")

    val password = MutableLiveData<String>("")

    val pop = MutableLiveData<String>("")

    val configuring = MutableLiveData<Boolean>(false)

    fun setArgs(args: BirdhouseConfigFragmentArgs) {
        address = args.address
        uuid = args.uuid
    }

    fun onPhotoSwitchChanged(on: Boolean) {
        Timber.d("onPhotoSwitchChanged: $on")
        if (on) {
            event.postValue(Event.AddPhoto)
        } else {

        }
    }

    fun onLocationSwitchChanged(on: Boolean) {
        Timber.d("onLocationSwitchChanged: $on")
        if (on) {
            event.postValue(Event.Location)
        } else {

        }
    }

    fun onCancel() {
        Timber.d("onCancel")

    }

    fun onConfigure() {
        Timber.d("onConfigure")
        val latLng = if (hasLocation.value!!) {
            location.value?.let { "${it.latitude},${it.longitude}" } ?: ""
        } else {
            ""
        }
        val imagePath = if (hasPhoto.value!!) {
            photoPath ?: ""
        } else {
            ""
        }
        repository.save(BirdhouseRepository.Birdhouse(
            address, uuid,  "192.168.4.1", 80, name.value!!, latLng, imagePath, ssid.value!!, password.value!!, pop.value!!))
        event.postValue(Event.Configure(address, uuid))
    }
 }