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
class FootballScoresViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val footballRepository: FootballScoreRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<FootballMatchScoreModel>>(emptyList())
    val matches: StateFlow<List<FootballMatchScoreModel>> = _matches

    private val _selectedMatch = MutableStateFlow<FootballMatchScoreModel?>(null)
    val selectedMatch: StateFlow<FootballMatchScoreModel?> = _selectedMatch

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadMatchDetail(matchId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                appRepository.getApp()?.let { app ->
                    val streamingDataList = appRepository.getStreamingData(app.id)
                    var apiKeyFound = false
                    streamingDataList.forEach { streaming ->
                        val scoreConfig = streaming.scores.find { it.type?.lowercase() == "football" }
                        if (scoreConfig != null) {
                            val apiKey = scoreConfig.api
                            if (!apiKey.isNullOrEmpty()) {
                                apiKeyFound = true
                                val detail = footballRepository.getMatchDetail(apiKey, matchId)
                                _selectedMatch.value = detail
                                return@forEach
                            }
                        }
                    }
                    if (!apiKeyFound) {
                        _error.value = "API Key not found"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            
            android.util.Log.d("FootballScoreVM", "refresh: Starting config fetch")
            appRepository.fetchAndSaveConfig { success, errorMsg ->
                android.util.Log.d("FootballScoreVM", "fetchAndSaveConfig result: $success, error: $errorMsg")
                if (success) {
                    viewModelScope.launch {
                        try {
                            appRepository.getApp()?.let { app ->
                                android.util.Log.d("FootballScoreVM", "App sport type: ${app.category}")
                                val streamingDataList = appRepository.getStreamingData(app.id)
                                android.util.Log.d("FootballScoreVM", "Found ${streamingDataList.size} streaming configs")
                                
                                var apiKeyFound = false
                                streamingDataList.forEach { streaming ->
                                    val scoreConfig = streaming.scores.find { it.type?.lowercase() == "football" }
                                    if (scoreConfig != null) {
                                        val apiKey = scoreConfig.api
                                        android.util.Log.d("FootballScoreVM", "Found Football API Key: $apiKey")
                                        if (!apiKey.isNullOrEmpty()) {
                                            apiKeyFound = true
                                            val result = footballRepository.getLiveMatches(apiKey)
                                            _matches.value = result
                                            return@forEach
                                        }
                                    }
                                }
                                
                                if (!apiKeyFound) {
                                    android.util.Log.e("FootballScoreVM", "Football API Key NOT FOUND in any config")
                                    _error.value = "Score configuration not found"
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FootballScoreVM", "Error in refresh logic", e)
                            _error.value = e.message ?: "Unknown error"
                        } finally {
                            _isRefreshing.value = false
                        }
                    }
                } else {
                    _error.value = "Failed to update config: $errorMsg"
                    _isRefreshing.value = false
                }
            }
        }
    }
}
