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
        object Configure: Event()
        object Navigate: Event()
    }

    val event = LiveEvent<Event>() // cause a single trigger

    val name = MutableLiveData<String>("My birdhouse")

    val hasLocation = MutableLiveData<Boolean>(true)

    val location = MutableLiveData<Location>(null)

    val hasPhoto = MutableLiveData<Boolean>(true)

    var photoPath: String? = null

    val ssid = MutableLiveData<String>("BHP")

    val password = MutableLiveData<String>("")

    val ip = MutableLiveData<String>("192.168.40.44")

    val pop = MutableLiveData<String>("")

    val progress = MutableLiveData<Int>(0)


    fun setArgs(args: BirdhouseConfigFragmentArgs) {
        // todo hack to edit
        if (args.uuid.isEmpty()) {
            repository.read(address)?.let { birdhouse ->
                name.value = birdhouse.name
                if (birdhouse.location.isNotEmpty()) {
                    hasLocation.value = true
                    Timber.d("How to set Location")
                } else {
                    hasLocation.value = false
                }
                if (birdhouse.photo.isNotEmpty()) {
                    hasPhoto.value = true
                    photoPath = birdhouse.photo
                }
                hasPhoto.value = false
                ssid.value = birdhouse.ssid
                password.value = birdhouse.ssidPassword
                ip.value = birdhouse.ip
                pop.value = birdhouse.pop
            }
        }
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


    fun onConfigure() {
        Timber.d("onConfigure")
        if (uuid.isEmpty()) {
            // todo hack to edit
            repository.read(address)?.let {
                repository.save(BirdhouseRepository.Birdhouse(
                    address, it.uuid,  ip.value!!, 80, name.value!!, it.location, it.photo, ssid.value!!, password.value!!, pop.value!!))
            }
            // get out this menu
        } else {
        progress.postValue(1)
        event.postValue(Event.Configure)
    }
    }


    fun onConfigured() {
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
            address, uuid,  ip.value!!, 80, name.value!!, latLng, imagePath, ssid.value!!, password.value!!, pop.value!!))
        event.postValue(Event.Navigate)
    }



 }