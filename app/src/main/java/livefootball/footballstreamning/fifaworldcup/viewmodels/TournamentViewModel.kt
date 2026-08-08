package livefootball.footballstreamning.fifaworldcup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.database.TournamentWithEvents
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDisplayItem
import javax.inject.Inject

/**
 * ViewModel for the Tournament screen.
 * Handles fetching tournaments by sport type and processing them into displayable items.
 */
@HiltViewModel
class TournamentViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<HomeDisplayItem>>(emptyList())
    val items: StateFlow<List<HomeDisplayItem>> = _items

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun refresh(category: String, isHighlights: Boolean) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAndSaveConfig { _, _ ->
                _isRefreshing.value = false
            }
        }
    }

    fun loadTournamentsBySportType(category: String, isHighlights: Boolean = false) {
        viewModelScope.launch {
            val tournamentsFlow = if (category == "TRENDING NOW") {
                repository.getTrendingTournamentsWithEventsFlow()
            } else {
                val sportType = when (category) {
                    "CRICKET" -> "cricket"
                    "FOOTBALL" -> "football"
                    else -> "other"
                }
                repository.getTournamentsWithEventsBySportTypeFlow(sportType)
            }

            tournamentsFlow.collectLatest { tournaments ->
                val displayItems = processTournaments(tournaments, isHighlights)
                _items.value = displayItems
            }
        }
    }

    private fun processTournaments(
        tournaments: List<TournamentWithEvents>,
        isHighlights: Boolean
    ): List<HomeDisplayItem> {
        return tournaments.mapNotNull { tWithE ->
            val eventsToUse = if (isHighlights) {
                // Show highlights
                tWithE.events.filter { it.isHighlight == true }
            } else {
                // Show in live mode if visible (includes live and upcoming)
                tWithE.events.filter { it.isVisible != false }
            }

            if (eventsToUse.size == 1) {
                // Show event directly if only one event
                val event = eventsToUse[0]
                HomeDisplayItem(
                    id = event.id,
                    title = event.eventName ?: "",
                    subtitle = tWithE.tournament.name,
                    status = event.description ?: if (isHighlights) "HIGHLIGHT" else if (event.isLive == true) "LIVE" else "UPCOMING",
                    imageUrl = event.eventThumbUrl ?: tWithE.tournament.thumbUrl,
                    isLive = event.isLive == true,
                    isTrending = false,
                    startTime = if (!isHighlights) event.startTime else null,
                    team1Name = event.teamAName,
                    team1Image = event.teamAImage,
                    team2Name = event.teamBName,
                    team2Image = event.teamBUrl,
                    originalObject = event
                )
            } else if (eventsToUse.size > 1) {
                // Show tournament group if multiple events
                val hasLive = eventsToUse.any { it.isLive == true }
                HomeDisplayItem(
                    id = tWithE.tournament.id,
                    title = tWithE.tournament.name ?: "",
                    subtitle = tWithE.tournament.sportType,
                    status = "${eventsToUse.size} ${if (isHighlights) "HIGHLIGHTS" else "MATCHES"}",
                    imageUrl = tWithE.tournament.thumbUrl,
                    isLive = hasLive,
                    isTrending = false,
                    originalObject = tWithE.tournament
                )
            } else null
        }
    }
}
