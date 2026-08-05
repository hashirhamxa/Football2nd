package livecricket.livecrickettv.cricketstreaming.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.database.AppEntity
import livefootball.footballstreamning.fifaworldcup.models.SocialMediaLink
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 * Observes the application configuration to reactively update settings-related data.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _appConfig = MutableStateFlow<AppEntity?>(null)
    val appConfig: StateFlow<AppEntity?> = _appConfig

    val socialLinks: StateFlow<List<SocialMediaLink>> = _appConfig.map { app ->
        if (app?.socialMediaLinks != null) {
            try {
                val type = object : TypeToken<List<SocialMediaLink>>() {}.type
                Gson().fromJson<List<SocialMediaLink>>(app.socialMediaLinks, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        observeSettings()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAndSaveConfig { _, _ ->
                _isRefreshing.value = false
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            repository.getAppFlow().collectLatest { app ->
                _appConfig.value = app
            }
        }
    }
}
