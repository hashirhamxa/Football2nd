package livefootball.footballstreamning.fifaworldcup.network

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import livefootball.footballstreamning.fifaworldcup.database.AdEntity
import livefootball.footballstreamning.fifaworldcup.database.AppDao
import livefootball.footballstreamning.fifaworldcup.database.AppEntity
import livefootball.footballstreamning.fifaworldcup.database.EventEntity
import livefootball.footballstreamning.fifaworldcup.database.HighlightEntity
import livefootball.footballstreamning.fifaworldcup.database.LinkEntity
import livefootball.footballstreamning.fifaworldcup.database.ScoreEntity
import livefootball.footballstreamning.fifaworldcup.database.StreamingEntity
import livefootball.footballstreamning.fifaworldcup.database.StreamingWithTournaments
import livefootball.footballstreamning.fifaworldcup.database.TournamentEntity
import livefootball.footballstreamning.fifaworldcup.database.TournamentWithEvents
import livefootball.footballstreamning.fifaworldcup.BuildConfig
import livefootball.footballstreamning.fifaworldcup.database.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDao: AppDao
) {
    suspend fun fetchAndSaveConfig(onResult: (Boolean, String?) -> Unit): Boolean {
        Log.d("AppRepository", "fetchAndSaveConfig: Starting API call")
        try {
            val response = apiService.getAppConfig("Bearer ${BuildConfig.API_TOKEN}")
            Log.d("AppRepository", "fetchAndSaveConfig: Response received")
            
            if (response.data.isNotEmpty()) {
                Log.d("AppRepository", "fetchAndSaveConfig: Data received, processing...")
                val appData = response.data[0]

                val appEntity = AppEntity(
                    id = appData.id,
                    packageName = appData.packageName,
                    appName = appData.appName,
                    appType = appData.appType,
                    category = appData.category,
                    currentVersion = appData.currentVersion,
                    updateRequired = appData.updateRequired,
                    isActive = appData.isActive,
                    newPackageName = appData.newPackageName,
                    licenseKey = appData.licenseKey,
                    productId = appData.productId,
                    socialMediaLinks = appData.socialMediaLinks?.let { Gson().toJson(it) },
                    adsTimeCountDown = appData.adsTimeCountDown
                )

                val adEntities = appData.ads?.map { ad ->
                    AdEntity(
                        id = ad.id,
                        platform = ad.platform,
                        adUnitId = ad.adUnitId,
                        adPlacement = ad.adPlacement,
                        priority = ad.priority,
                        isActive = ad.isActive,
                        frequencyCap = ad.frequencyCap,
                        appId = appData.id
                    )
                } ?: emptyList()

                val streamingEntities = mutableListOf<StreamingEntity>()
                val scoreEntities = mutableListOf<ScoreEntity>()
                val tournamentEntities = mutableListOf<TournamentEntity>()
                val eventEntities = mutableListOf<EventEntity>()
                val highlightEntities = mutableListOf<HighlightEntity>()
                val linkEntities = mutableListOf<LinkEntity>()

                appData.streaming?.forEach { streamingWrapper ->
                    streamingWrapper.streamingId?.let { streaming ->
                        streamingEntities.add(
                            StreamingEntity(
                                id = streaming.id,
                                status = streaming.status,
                                showCricketHighlights = streaming.showCricketHighlights,
                                showFootballHighlights = streaming.showFootballHighlights,
                                showOtherSportsHighlights = streaming.showOtherSportsHighlights,
                                newAppOutsideUrl = streaming.newAppOutsideUrl,
                                forceNewAppOutsideUrl = streaming.forceNewAppOutsideUrl,
                                appSportType = streaming.appSportType,
                                bannerAds = streaming.bannerAds,
                                onPauseAd = streaming.onPauseAd,
                                showOtherSports = streaming.showOtherSports,
                                liveCricket = streaming.liveCricket,
                                liveFootball = streaming.liveFootball,
                                liveOtherSport = streaming.liveOtherSport,
                                splashImageLink = streaming.splashImageLink,
                                otherSports = streaming.otherSports,
                                showScore = streaming.showScore,
                                outsideUrlTitle = streaming.outsideUrlTitle,
                                outsideUrlDescription = streaming.outsideUrlDescription,
                                outsideUrlImageUrl = streaming.outsideUrlImageUrl,
                                appId = appData.id
                            )
                        )

                        streaming.scores?.forEach { score ->
                            scoreEntities.add(
                                ScoreEntity(
                                    type = score.type,
                                    api = score.api,
                                    status = score.status,
                                    streamingId = streaming.id
                                )
                            )
                        }

                        streaming.tournaments?.forEach { tournamentWrapper ->
                            tournamentWrapper.tournamentsId?.let { tournament ->
                                tournamentEntities.add(
                                    TournamentEntity(
                                        id = tournamentWrapper.id,
                                        status = tournament.status,
                                        name = tournament.name,
                                        thumbUrl = tournament.thumbUrl,
                                        isVisible = tournament.isVisible,
                                        startTime = tournament.startTime,
                                        endTime = tournament.endTime,
                                        description = tournament.description,
                                        sportType = tournament.sportType,
                                        excludedAppPackageNames = tournament.excludedAppPackageNames,
                                        streamingId = streaming.id,
                                        sort = tournament.sort
                                    )
                                )

                                tournament.events?.forEach { eventWrapper ->
                                    eventWrapper.eventsId?.let { event ->
                                        eventEntities.add(
                                            EventEntity(
                                                id = eventWrapper.id,
                                                eventName = event.eventName,
                                                eventSlug = event.eventSlug,
                                                eventThumbUrl = event.eventThumbUrl,
                                                eventUrl = event.eventUrl,
                                                isHighlight = event.isHighlight,
                                                isVisible = event.isVisible,
                                                startTime = event.startTime,
                                                endTime = event.endTime,
                                                description = event.description,
                                                teamAName = event.teamAName,
                                                teamAImage = event.teamAImage,
                                                teamBName = event.teamBName,
                                                teamBUrl = event.teamBUrl,
                                                metadata = event.metadata,
                                                isLive = event.isLive,
                                                excludedAppPackageNames = event.excludedAppPackageNames,
                                                tournamentId = tournamentWrapper.id
                                            )
                                        )

                                        event.highlights?.forEach { highlightWrapper ->
                                            highlightWrapper.highlightsId?.let { highlight ->
                                                highlightEntities.add(
                                                    HighlightEntity(
                                                        id = highlightWrapper.id,
                                                        linkName = highlight.linkName,
                                                        linkUrl = highlight.linkUrl,
                                                        linkImage = highlight.linkImage,
                                                        durationSeconds = highlight.durationSeconds,
                                                        viewCount = highlight.viewCount,
                                                        isVisible = highlight.isVisible,
                                                        publishedAt = highlight.publishedAt,
                                                        eventId = eventWrapper.id
                                                    )
                                                )
                                            }
                                        }

                                        event.links?.forEach { linkWrapper ->
                                            linkWrapper.linksId?.let { link ->
                                                linkEntities.add(
                                                    LinkEntity(
                                                        id = linkWrapper.id,
                                                        linkName = link.linkName,
                                                        linkUrl = link.linkUrl,
                                                        linkType = link.linkType,
                                                        mpdLink = link.mpdLink,
                                                        mpdKey = link.mpdKey,
                                                        linkImage = link.linkImage,
                                                        isVisible = link.isVisible,
                                                        priority = link.priority,
                                                        excludedAppPackageNames = link.excludedAppPackageNames,
                                                        refererHeader = link.refererHeader,
                                                        originHeader = link.originHeader,
                                                        userAgentHeader = link.userAgentHeader,
                                                        eventId = eventWrapper.id
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                appDao.clearAndInsert(
                    appEntity,
                    adEntities,
                    streamingEntities,
                    tournamentEntities,
                    eventEntities,
                    highlightEntities,
                    linkEntities,
                    scoreEntities
                )
                Log.d("AppRepository", "fetchAndSaveConfig: Success - Data saved to DB")
                onResult(true, null)
                return true
            } else {
                Log.e("AppRepository", "fetchAndSaveConfig: Error - Data list is empty")
                onResult(false, "No data available from server")
                return false
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "fetchAndSaveConfig: Exception occurred "+e.message, e)
            e.printStackTrace()
            onResult(false, e.message ?: "Server connection error")
            return false
        }
    }

    suspend fun getAllAds(): List<AdEntity> {
        return appDao.getAllAds()
    }

    suspend fun getApp(): AppEntity? {
        return appDao.getApp()
    }

    fun getAppFlow(): Flow<AppEntity?> {
        return appDao.getAppFlow()
    }

    suspend fun getStreamingData(appId: Int): List<StreamingWithTournaments> {
        return appDao.getStreamingWithTournaments(appId)
    }

    fun getStreamingDataFlow(appId: Int): Flow<List<StreamingWithTournaments>> {
        return appDao.getStreamingWithTournamentsFlow(appId)
    }

    suspend fun getEventsForTournament(tournamentId: Int): List<EventEntity> {
        return appDao.getEventsForTournament(tournamentId)
    }

    fun getEventsForTournamentFlow(tournamentId: Int): Flow<List<EventEntity>> {
        return appDao.getEventsForTournamentFlow(tournamentId)
    }

    suspend fun getHighlightEventsForTournament(tournamentId: Int): List<EventEntity> {
        return appDao.getHighlightEventsForTournament(tournamentId)
    }

    fun getHighlightEventsForTournamentFlow(tournamentId: Int): Flow<List<EventEntity>> {
        return appDao.getHighlightEventsForTournamentFlow(tournamentId)
    }

    suspend fun getTournamentsWithEventsBySportType(sportType: String): List<TournamentWithEvents> {
        return appDao.getTournamentsWithEventsBySportType(sportType)
    }

    fun getTournamentsWithEventsBySportTypeFlow(sportType: String): Flow<List<TournamentWithEvents>> {
        return appDao.getTournamentsWithEventsBySportTypeFlow(sportType)
    }

    suspend fun getTrendingTournamentsWithEvents(): List<TournamentWithEvents> {
        return appDao.getTrendingTournamentsWithEvents()
    }

    fun getTrendingTournamentsWithEventsFlow(): Flow<List<TournamentWithEvents>> {
        return appDao.getTrendingTournamentsWithEventsFlow()
    }

    suspend fun getLinksForEvent(eventId: Int): List<LinkEntity> {
        return appDao.getLinksForEvent(eventId)
    }

    fun getLinksForEventFlow(eventId: Int): Flow<List<LinkEntity>> {
        return appDao.getLinksForEventFlow(eventId)
    }

    suspend fun getHighlightsForEvent(eventId: Int): List<HighlightEntity> {
        return appDao.getHighlightsForEvent(eventId)
    }

    fun getHighlightsForEventFlow(eventId: Int): Flow<List<HighlightEntity>> {
        return appDao.getHighlightsForEventFlow(eventId)
    }
}
