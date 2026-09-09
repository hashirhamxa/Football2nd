package livefootball.footballstreamning.fifaworldcup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import livefootball.footballstreamning.fifaworldcup.database.AppEntity
import livefootball.footballstreamning.fifaworldcup.database.StreamingEntity
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.utilities.SplashPreloader
import javax.inject.Inject
import android.app.Application
import livefootball.footballstreamning.fifaworldcup.database.ScoreEntity
import livefootball.footballstreamning.fifaworldcup.models.SocialMediaLink

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    private val repository: AppRepository,
    private val application: Application
) : ViewModel() {

    private val _showHighlights = MutableStateFlow(true)
    val showHighlights: StateFlow<Boolean> = _showHighlights

    private val _showHome = MutableStateFlow(true)
    val showHome: StateFlow<Boolean> = _showHome

    private val _showScore = MutableStateFlow(false)
    val showScore: StateFlow<Boolean> = _showScore

    private val _scoreApiKey = MutableStateFlow<String?>(null)
    val scoreApiKey: StateFlow<String?> = _scoreApiKey

    private val _scoreConfig = MutableStateFlow<ScoreEntity?>(null)
    val scoreConfig: StateFlow<ScoreEntity?> = _scoreConfig

    private val _isConfigReady = MutableStateFlow(false)
    val isConfigReady: StateFlow<Boolean> = _isConfigReady

    private val _appConfig = MutableStateFlow<AppEntity?>(null)
    private val _whatsappLink = MutableStateFlow<String?>(null)
    val whatsappLink: StateFlow<String?> = _whatsappLink
    val appConfig: StateFlow<AppEntity?> = _appConfig

    private val _streamingConfig = MutableStateFlow<StreamingEntity?>(null)
    val streamingConfig: StateFlow<StreamingEntity?> = _streamingConfig

    init {
        observeConfig()
        observeSplashUpdate()
    }

    private fun observeSplashUpdate() {
        viewModelScope.launch {
            repository.getAppFlow().collectLatest { app ->
                app?.let {
                    // Extract WhatsApp link from social media links JSON
                    val linksJson = it.socialMediaLinks
                    if (!linksJson.isNullOrEmpty()) {
                        try {
                            val type = object : com.google.gson.reflect.TypeToken<List<SocialMediaLink>>() {}.type
                            val links: List<SocialMediaLink> = com.google.gson.Gson().fromJson(linksJson, type)
                            _whatsappLink.value = links.find { link -> link.name?.contains("whatsapp", ignoreCase = true) == true }?.link
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    repository.getStreamingData(it.id).firstOrNull()?.let { data ->
                        val splashUrl = data.streaming.splashImageLink
                        SplashPreloader(application).updateSplashImage(splashUrl)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeConfig() {
        viewModelScope.launch {
            repository.getAppFlow().collectLatest { app ->
                _appConfig.value = app
            }
        }

        viewModelScope.launch {
            repository.getAppFlow().flatMapLatest { app ->
                if (app != null) {
                    repository.getStreamingDataFlow(app.id)
                } else {
                    flowOf(emptyList())
                }
            }.collectLatest { streamingDataList ->
                if (streamingDataList.isNotEmpty()) {
                    val data = streamingDataList[0]
                    val streaming = data.streaming
                    _streamingConfig.value = streaming
                    _showHighlights.value = streaming.showCricketHighlights == true ||
                            streaming.showFootballHighlights == true ||
                            streaming.showOtherSportsHighlights == true
                    
                    _showHome.value = streaming.liveCricket == true ||
                            streaming.liveFootball == true ||
                            streaming.liveOtherSport == true
                    
                    _showScore.value = streaming.showScore == true
                    
                    val activeScore = data.scores.find { it.status?.lowercase() == "active" }
                    _scoreConfig.value = activeScore
                    _scoreApiKey.value = activeScore?.api

                    _isConfigReady.value = true
                }
            }
        }
    }
}
