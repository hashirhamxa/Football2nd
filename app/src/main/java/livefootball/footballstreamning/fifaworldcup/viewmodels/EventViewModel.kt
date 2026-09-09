package livefootball.footballstreamning.fifaworldcup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.database.EventEntity
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<EventEntity>>(emptyList())
    val events: StateFlow<List<EventEntity>> = _events

    private val _highlightEvents = MutableStateFlow<List<EventEntity>>(emptyList())
    val highlightEvents: StateFlow<List<EventEntity>> = _highlightEvents

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun refresh(tournamentId: Int, isHighlights: Boolean) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAndSaveConfig { _, _ ->
                _isRefreshing.value = false
            }
        }
    }

    fun loadEvents(tournamentId: Int) {
        viewModelScope.launch {
            repository.getEventsForTournamentFlow(tournamentId).collectLatest { eventList ->
                // Show events if isLive is true and isVisible is true
                _events.value = eventList.filter { it.isLive == true && it.isVisible == true }
            }
        }
    }

    fun loadHighlightEvents(tournamentId: Int) {
        viewModelScope.launch {
            repository.getHighlightEventsForTournamentFlow(tournamentId)
                .collectLatest { eventList ->
                    // Show highlights if isVisible is true
                    _highlightEvents.value = eventList.filter { it.isVisible == true }
                }
        }
    }
}
