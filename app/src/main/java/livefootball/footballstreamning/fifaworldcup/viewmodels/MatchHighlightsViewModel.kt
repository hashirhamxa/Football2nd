package livefootball.footballstreamning.fifaworldcup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.database.StreamingWithTournaments
import livefootball.footballstreamning.fifaworldcup.database.TournamentWithEvents
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDisplayItem
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeSection
import javax.inject.Inject

/**
 * ViewModel for the Highlights (History) screen.
 * Filters data to show only items marked as highlights and handles category visibility.
 */
@HiltViewModel
class MatchHighlightsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // Holds filtered highlight sections
    private val _sections = MutableStateFlow<List<HomeSection>>(emptyList())
    val sections: StateFlow<List<HomeSection>> = _sections

    // Flag for single-category display mode
    private val _isSingleSection = MutableStateFlow(false)
    val isSingleSection: StateFlow<Boolean> = _isSingleSection

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        observeData()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAndSaveConfig { success, _ ->
                _isRefreshing.value = false
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            repository.getAppFlow().flatMapLatest { app ->
                if (app != null) {
                    repository.getStreamingDataFlow(app.id)
                } else {
                    flowOf(emptyList())
                }
            }.collectLatest { streamingDataList ->
                if (streamingDataList.isNotEmpty()) {
                    processData(streamingDataList[0])
                }
            }
        }
    }

    /**
     * Processes raw data into displayable highlight sections based on visibility flags and highlight markers.
     */
    private fun processData(data: StreamingWithTournaments) {
        val streaming = data.streaming
        val tournaments = data.tournaments

        // Highlights visibility flags from API
        val showCricket = streaming.showCricketHighlights == true
        val showFootball = streaming.showFootballHighlights == true
        val showOther = streaming.showOtherSportsHighlights == true

        val trueCount = listOf(showCricket, showFootball, showOther).count { it }
        _isSingleSection.value = trueCount == 1

        val sectionList = mutableListOf<HomeSection>()

        // 1. Cricket Highlights
        if (showCricket) {
            val cricketItems = processTournaments(tournaments.filter {
                it.tournament.sportType?.lowercase() == "cricket"
            })
            if (cricketItems.isNotEmpty()) {
                sectionList.add(HomeSection("CRICKET HIGHLIGHTS", cricketItems, "cricket"))
            }
        }

        // 2. Football Highlights
        if (showFootball) {
            val footballItems = processTournaments(tournaments.filter {
                it.tournament.sportType?.lowercase() == "football"
            })
            if (footballItems.isNotEmpty()) {
                sectionList.add(HomeSection("FOOTBALL HIGHLIGHTS", footballItems, "football"))
            }
        }

        // 3. Other Sports Highlights (Trending Now)
        if (showOther) {
            val otherSportsList = streaming.otherSports?.split(",")?.map { it.trim().lowercase() } ?: emptyList()
            val otherTournaments = tournaments.filter {
                val type = it.tournament.sportType?.lowercase()
                // NOT cricket/football, and within specified other sports
                type != "cricket" && type != "football" && (type in otherSportsList || type != null)
            }
            val otherItems = processTournaments(otherTournaments, true)
            if (otherItems.isNotEmpty()) {
                sectionList.add(HomeSection("TRENDING NOW", otherItems, "other"))
            }
        }

        // 4. Handle Prioritization based on app_sport_type
        val appSportType = streaming.appSportType?.lowercase()
        when (appSportType) {
            "football" -> {
                sectionList.find { it.sportType == "football" }?.let {
                    sectionList.remove(it)
                    sectionList.add(0, it)
                }
            }
            "cricket" -> {
                sectionList.find { it.sportType == "cricket" }?.let {
                    sectionList.remove(it)
                    sectionList.add(0, it)
                }
            }
        }

        _sections.value = sectionList
    }

    /**
     * Filters events within tournaments to only include visible highlights.
     * Implements tournament grouping rules for highlights:
     * - More than 2 highlight events → show tournament group
     * - Exactly 1 highlight event → show event directly (single event promotion)
     * - 0 highlight events → don't show tournament at all
     */
    private fun processTournaments(
        tournaments: List<TournamentWithEvents>,
        isTrending: Boolean = false
    ): List<HomeDisplayItem> {
        return tournaments.mapNotNull { tWithE ->
            // Only consider events where isHighlight is true
            val highlightEvents = tWithE.events.filter { it.isHighlight == true }

            if (highlightEvents.size == 1) {
                // Single highlight event: show event directly (skip tournament grouping)
                val event = highlightEvents[0]
                HomeDisplayItem(
                    id = event.id,
                    title = event.eventName ?: "",
                    subtitle = tWithE.tournament.name,
                    status = event.description ?: "HIGHLIGHT",
                    imageUrl = event.eventThumbUrl ?: tWithE.tournament.thumbUrl,
                    isLive = false,
                    isTrending = isTrending,
                    originalObject = event
                )
            } else if (highlightEvents.size >= 2) {
                // More than 1 highlight event: show tournament group
                HomeDisplayItem(
                    id = tWithE.tournament.id,
                    title = tWithE.tournament.name ?: "",
                    subtitle = tWithE.tournament.sportType,
                    status = "${highlightEvents.size} HIGHLIGHTS",
                    imageUrl = tWithE.tournament.thumbUrl,
                    isLive = false,
                    isTrending = isTrending,
                    originalObject = tWithE.tournament
                )
            } else null
            // 0 or exactly 2 highlight events: don't show tournament
        }
    }
}
