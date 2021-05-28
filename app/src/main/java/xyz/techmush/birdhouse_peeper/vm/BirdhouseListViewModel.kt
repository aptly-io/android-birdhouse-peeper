package xyz.techmush.birdhouse_peeper.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import xyz.techmush.birdhouse_peeper.model.BirdhouseRepository
import xyz.techmush.birdhouse_peeper.model.BirdhouseRepository.Birdhouse
import javax.inject.Inject


@HiltViewModel
class BirdhouseListViewModel @Inject constructor(private val repository: BirdhouseRepository): ViewModel() {

    sealed class Event {
        object AddBirdhouse: Event()
        class PeepBirdhouse(val birdhouse: Birdhouse): Event()
    }

    val event = LiveEvent<Event>() // cause a single trigger

    private val _birdhouses = MutableLiveData(repository.read())
    val birdhouses: LiveData<List<Birdhouse>> get() = _birdhouses

    val birdhouse = MutableLiveData<Birdhouse>(null)


    fun onBirdhouseClick(birdhouse: Birdhouse) {
        Timber.d("onBirdhouseClick: $birdhouse")
        event.postValue(Event.PeepBirdhouse(birdhouse))
    }


    fun onAddBirdhouse() {
        Timber.d("onAddBirdhouse")
        event.postValue(Event.AddBirdhouse)
    }


    fun onRemoveBirdhouse(birdhouse: Birdhouse) {
        Timber.d("onRemoveBirdhouse: $birdhouse")
        repository.delete(birdhouse)
        _birdhouses.postValue(repository.read())
    }
    

    fun reload() {
        _birdhouses.value= repository.read()
    }
}