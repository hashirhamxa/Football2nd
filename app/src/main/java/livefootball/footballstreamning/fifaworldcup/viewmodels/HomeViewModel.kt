package livefootball.footballstreamning.fifaworldcup.viewmodels

import android.util.Log
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
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 * Handles fetching streaming data from the database and processing it into displayable sections.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // Holds the list of sections (Cricket, Football, Trending) to be displayed
    private val _sections = MutableStateFlow<List<HomeSection>>(emptyList())
    val sections: StateFlow<List<HomeSection>> = _sections

    // Flag to indicate if only one section is visible (affects layout in Fragment)
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
     * Processes raw database entities into a list of HomeSection objects based on business rules.
     */
    private fun processData(data: StreamingWithTournaments) {
        val streaming = data.streaming
        val tournaments = data.tournaments

        // Check which sports are currently enabled for live streaming
        val liveCricket = streaming.liveCricket == true
        val liveFootball = streaming.liveFootball == true
        val liveOtherSport = streaming.liveOtherSport == true

        // Determine if we are in "Single Section Mode"
        val trueCount = listOf(liveCricket, liveFootball, liveOtherSport).count { it }
        _isSingleSection.value = trueCount == 1

        val sectionList = mutableListOf<HomeSection>()

        // 1. Process Cricket Section
        if (liveCricket) {
            val cricketItems = processTournaments(tournaments.filter {
                it.tournament.sportType?.lowercase() == "cricket"
            }, false)
            if (cricketItems.isNotEmpty()) {
                sectionList.add(HomeSection("CRICKET", cricketItems, "cricket"))
            }
        }

        // 2. Process Football Section
        if (liveFootball) {
            val footballItems = processTournaments(tournaments.filter {
                it.tournament.sportType?.lowercase() == "football"
            }, false)
            if (footballItems.isNotEmpty()) {
                sectionList.add(HomeSection("FOOTBALL", footballItems, "football"))
            }
        }

        // 3. Process Other Sports (Trending Now)
        if (liveOtherSport) {
            val otherTournaments = tournaments.filter {
                val type = it.tournament.sportType?.lowercase()
                type != "cricket" && type != "football"
            }
            val trendingItems = processTournaments(otherTournaments, true)
            if (trendingItems.isNotEmpty()) {
                sectionList.add(HomeSection("TRENDING NOW", trendingItems, "other"))
            }
        }

        // 4. Handle Prioritization based on app_sport_type
        // Move the preferred sport to the top of the list
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
     * Converts a list of TournamentWithEvents into HomeDisplayItems.
     * Logic: If a tournament has exactly one event, promote the event to the list.
     * Otherwise, show the tournament itself.
     */
    private fun processTournaments(
        tournaments: List<TournamentWithEvents>,
        isTrending: Boolean
    ): List<HomeDisplayItem> {
        return tournaments.mapNotNull { tWithE ->
            // Include anything that is marked visible and NOT exclusively a highlight in Live fragment
            // We want to show Live events AND upcoming events with countdowns.
            val liveEvents = tWithE.events.filter { it.isVisible != false }

            if (liveEvents.size == 1) {
                // Promotion logic: Show event directly
                val event = liveEvents[0]
                    HomeDisplayItem(
                        id = event.id,
                        title = event.eventName ?: "",
                        subtitle = tWithE.tournament.name,
                        status = event.description ?: if (event.isLive == true) "LIVE NOW" else "UPCOMING",
                        imageUrl = event.eventThumbUrl ?: tWithE.tournament.thumbUrl,
                        isLive = event.isLive == true,
                        isTrending = isTrending,
                        startTime = event.startTime,
                        team1Name = event.teamAName,
                        team1Image = event.teamAImage,
                        team2Name = event.teamBName,
                        team2Image = event.teamBUrl, // Mapping teamBUrl as image
                        originalObject = event
                    )
            } else if (liveEvents.size > 1) {
                // Show Tournament group
                // Check if any event is currently live
                val hasLiveEvent = liveEvents.any { it.isLive == true }
                HomeDisplayItem(
                    id = tWithE.tournament.id,
                    title = tWithE.tournament.name ?: "",
                    subtitle = tWithE.tournament.sportType,
                    status = tWithE.tournament.description ?: if (hasLiveEvent) "LIVE" else "UPCOMING",
                    imageUrl = tWithE.tournament.thumbUrl,
                    isLive = hasLiveEvent,
                    isTrending = isTrending,
                    originalObject = tWithE.tournament
                )
            } else null
        }
    }
}
