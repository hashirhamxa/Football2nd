package livefootball.footballstreamning.fifaworldcup.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAds(ads: List<AdEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreaming(streaming: List<StreamingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournaments(tournaments: List<TournamentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlights(highlights: List<HighlightEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<LinkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScores(scores: List<ScoreEntity>)

    @Transaction
    suspend fun clearAndInsert(
        app: AppEntity,
        ads: List<AdEntity>,
        streaming: List<StreamingEntity>,
        tournaments: List<TournamentEntity>,
        events: List<EventEntity>,
        highlights: List<HighlightEntity>,
        links: List<LinkEntity>,
        scores: List<ScoreEntity>
    ) {
        deleteAll()
        insertApp(app)
        insertAds(ads)
        insertStreaming(streaming)
        insertTournaments(tournaments)
        insertEvents(events)
        insertHighlights(highlights)
        insertLinks(links)
        insertScores(scores)
    }

    @Query("DELETE FROM apps")
    suspend fun deleteAllApps()

    @Query("DELETE FROM ads")
    suspend fun deleteAllAds()

    @Query("DELETE FROM streaming")
    suspend fun deleteAllStreaming()

    @Query("DELETE FROM tournaments")
    suspend fun deleteAllTournaments()

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Query("DELETE FROM highlights")
    suspend fun deleteAllHighlights()

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()

    @Query("DELETE FROM scores")
    suspend fun deleteAllScores()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("SELECT * FROM matches ORDER BY lastUpdated DESC")
    fun getAllMatchesFlow(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()

    @Query("SELECT * FROM ads")
    suspend fun getAllAds(): List<AdEntity>

    @Transaction
    @Query("SELECT * FROM streaming WHERE appId = :appId")
    fun getStreamingWithTournamentsFlow(appId: Int): Flow<List<StreamingWithTournaments>>

    @Transaction
    @Query("SELECT * FROM streaming WHERE appId = :appId")
    suspend fun getStreamingWithTournaments(appId: Int): List<StreamingWithTournaments>

    @Query("SELECT * FROM events WHERE tournamentId = :tournamentId")
    fun getEventsForTournamentFlow(tournamentId: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE tournamentId = :tournamentId")
    suspend fun getEventsForTournament(tournamentId: Int): List<EventEntity>

    @Query("SELECT * FROM events WHERE tournamentId = :tournamentId AND isHighlight = 1")
    fun getHighlightEventsForTournamentFlow(tournamentId: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE tournamentId = :tournamentId AND isHighlight = 1")
    suspend fun getHighlightEventsForTournament(tournamentId: Int): List<EventEntity>

    @Transaction
    @Query("SELECT * FROM tournaments WHERE (sportType != 'cricket' AND sportType != 'football') ORDER BY sort ASC")
    fun getTrendingTournamentsWithEventsFlow(): Flow<List<TournamentWithEvents>>

    @Transaction
    @Query("SELECT * FROM tournaments WHERE (sportType != 'cricket' AND sportType != 'football') ORDER BY sort ASC")
    suspend fun getTrendingTournamentsWithEvents(): List<TournamentWithEvents>

    @Transaction
    @Query("SELECT * FROM tournaments WHERE sportType = :sportType ORDER BY sort ASC")
    fun getTournamentsWithEventsBySportTypeFlow(sportType: String): Flow<List<TournamentWithEvents>>

    @Transaction
    @Query("SELECT * FROM tournaments WHERE sportType = :sportType ORDER BY sort ASC")
    suspend fun getTournamentsWithEventsBySportType(sportType: String): List<TournamentWithEvents>

    @Query("SELECT * FROM links WHERE eventId = :eventId")
    fun getLinksForEventFlow(eventId: Int): Flow<List<LinkEntity>>

    @Query("SELECT * FROM links WHERE eventId = :eventId")
    suspend fun getLinksForEvent(eventId: Int): List<LinkEntity>

    @Query("SELECT * FROM highlights WHERE eventId = :eventId")
    fun getHighlightsForEventFlow(eventId: Int): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE eventId = :eventId")
    suspend fun getHighlightsForEvent(eventId: Int): List<HighlightEntity>

    @Query("SELECT * FROM apps LIMIT 1")
    fun getAppFlow(): Flow<AppEntity?>

    @Query("SELECT * FROM apps LIMIT 1")
    suspend fun getApp(): AppEntity?

    @Transaction
    suspend fun deleteAll() {
        deleteAllApps()
        deleteAllAds()
        deleteAllStreaming()
        deleteAllTournaments()
        deleteAllEvents()
        deleteAllHighlights()
        deleteAllLinks()
        deleteAllScores()
    }
}
