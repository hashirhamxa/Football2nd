package livefootball.footballstreamning.fifaworldcup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.database.MatchEntity
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.network.MatchRepository
import javax.inject.Inject

@HiltViewModel
class CricketScoresViewModel @Inject constructor(
    private val repository: AppRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<MatchEntity>>(emptyList())
    val matches: StateFlow<List<MatchEntity>> = _matches

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        observeMatches()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            // 1. Refresh global server config first (App config, Ads, API Keys)
            repository.fetchAndSaveConfig { success, _ ->
                if (success) {
                    // 2. After config is updated, re-fetch live scores using the fresh API key
                    viewModelScope.launch {
                        repository.getApp()?.let { app ->
                            val streamingData = repository.getStreamingData(app.id).firstOrNull()
                            val apiKey = streamingData?.scores?.find { it.type == "cricket" }?.api
                            if (!apiKey.isNullOrEmpty()) {
                                matchRepository.fetchCurrentMatches(apiKey)
                            }
                        }
                        _isRefreshing.value = false
                    }
                } else {
                    _isRefreshing.value = false
                }
            }
        }
    }

    private fun observeMatches() {
        viewModelScope.launch {
            matchRepository.getAllMatchesFlow().collectLatest {
                _matches.value = it
            }
        }
    }
}
