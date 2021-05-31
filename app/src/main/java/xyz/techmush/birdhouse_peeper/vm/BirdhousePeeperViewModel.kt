package xyz.techmush.birdhouse_peeper.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import xyz.techmush.birdhouse_peeper.model.BirdhouseRepository
import xyz.techmush.birdhouse_peeper.ui.BirdhousePeeperFragmentArgs
import javax.inject.Inject

@HiltViewModel
class BirdhousePeeperViewModel @Inject constructor(private val repository: BirdhouseRepository): ViewModel() {

    sealed class Event {
        class LoadUrl(val url: String, val birdhouse: BirdhouseRepository.Birdhouse): Event()
        class NavigateEdit(val birdhouse: BirdhouseRepository.Birdhouse): Event()
    }

    private val _birdhouse = MutableLiveData<BirdhouseRepository.Birdhouse>(null)
    val birdhouse: LiveData<BirdhouseRepository.Birdhouse> get() = _birdhouse

    val event = LiveEvent<Event>() // cause a single trigger


    fun setArgs(args: BirdhousePeeperFragmentArgs) {
        _birdhouse.value = repository.read(args.address)
    }


    fun onPhoto() {
        Timber.d("onPhoto")
        _birdhouse.value?.let {
            event.postValue(Event.LoadUrl("http://${it.ip}:${it.port}/capture", it))
        }
    }


    fun onStart() {
        Timber.d("onStart")
        _birdhouse.value?.let {
            event.postValue(Event.LoadUrl("http://${it.ip}:${it.port+1}/stream", it))
        }
    }


    fun onStop() {
        Timber.d("onStop")
    }


    fun onEdit() {
        Timber.d("onEdit")
        _birdhouse.value?.let { event.postValue(Event.NavigateEdit(it)) }
    }
}