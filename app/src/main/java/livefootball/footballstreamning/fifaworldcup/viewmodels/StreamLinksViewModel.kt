package livefootball.footballstreamning.fifaworldcup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.database.HighlightEntity
import livefootball.footballstreamning.fifaworldcup.database.LinkEntity
import livefootball.footballstreamning.fifaworldcup.database.StreamingEntity
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import javax.inject.Inject

@HiltViewModel
class StreamLinksViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _links = MutableStateFlow<List<LinkEntity>>(emptyList())
    val links: StateFlow<List<LinkEntity>> = _links

    private val _highlights = MutableStateFlow<List<HighlightEntity>>(emptyList())
    val highlights: StateFlow<List<HighlightEntity>> = _highlights

    private val _streaming = MutableStateFlow<StreamingEntity?>(null)
    val streaming: StateFlow<StreamingEntity?> = _streaming
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadStreamingData()
    }

    private fun loadStreamingData() {
        viewModelScope.launch {
            repository.getAppFlow().collectLatest { app ->
                if (app != null) {
                    repository.getStreamingDataFlow(app.id).collectLatest { list ->
                        if (list.isNotEmpty()) {
                            _streaming.value = list[0].streaming
                        }
                    }
                }
            }
        }
    }

    fun refresh(eventId: Int, isHighlights: Boolean) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAndSaveConfig { _, _ ->
                _isRefreshing.value = false
            }
        }
    }

    fun loadLinks(eventId: Int) {
        viewModelScope.launch {
            repository.getLinksForEventFlow(eventId).collectLatest { linkList ->
                _links.value = linkList
            }
        }
    }

    fun loadHighlights(eventId: Int) {
        viewModelScope.launch {
            repository.getHighlightsForEventFlow(eventId).collectLatest { highlightList ->
                _highlights.value = highlightList
            }
        }
    }
}
