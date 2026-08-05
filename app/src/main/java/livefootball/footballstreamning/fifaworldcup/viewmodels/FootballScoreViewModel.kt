package livefootball.footballstreamning.fifaworldcup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.models.FootballMatchScoreModel
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.network.FootballScoreRepository
import javax.inject.Inject

@HiltViewModel
class FootballScoreViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val footballRepository: FootballScoreRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<FootballMatchScoreModel>>(emptyList())
    val matches: StateFlow<List<FootballMatchScoreModel>> = _matches

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            
            appRepository.fetchAndSaveConfig { success, _ ->
                if (success) {
                    viewModelScope.launch {
                        try {
                            appRepository.getApp()?.let { app ->
                                val streamingData = appRepository.getStreamingData(app.id).firstOrNull()
                                val scoreConfig = streamingData?.scores?.find { it.type?.lowercase() == "football" }
                                val apiKey = scoreConfig?.api
                                
                                if (!apiKey.isNullOrEmpty()) {
                                    val result = footballRepository.getLiveMatches(apiKey)
                                    _matches.value = result
                                } else {
                                    _error.value = "API Key not found"
                                }
                            }
                        } catch (e: Exception) {
                            _error.value = e.message ?: "Unknown error"
                        } finally {
                            _isRefreshing.value = false
                        }
                    }
                } else {
                    _error.value = "Failed to update config"
                    _isRefreshing.value = false
                }
            }
        }
    }
}
