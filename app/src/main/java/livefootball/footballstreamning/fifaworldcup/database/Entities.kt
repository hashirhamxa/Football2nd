package livefootball.footballstreamning.fifaworldcup.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val id: Int,
    val packageName: String?,
    val appName: String?,
    val appType: String?,
    val category: String?,
    val currentVersion: String?,
    val updateRequired: Boolean?,
    val isActive: Boolean?,
    val newPackageName: String?,
    val licenseKey: String?,
    val productId: String?,
    val socialMediaLinks: String?, // Store as JSON string
    val adsTimeCountDown: Int?
)

@Entity(tableName = "ads")
data class AdEntity(
    @PrimaryKey val id: Int,
    val platform: String?,
    val adUnitId: String?,
    val adPlacement: String?,
    val priority: Int?,
    val isActive: Boolean?,
    val frequencyCap: Int?,
    val appId: Int
)

@Entity(tableName = "streaming")
data class StreamingEntity(
    @PrimaryKey val id: Int,
    val status: String?,
    val showCricketHighlights: Boolean?,
    val showFootballHighlights: Boolean?,
    val showOtherSportsHighlights: Boolean?,
    val newAppOutsideUrl: String?,
    val forceNewAppOutsideUrl: Boolean?,
    val appSportType: String?,
    val bannerAds: Boolean?,
    val onPauseAd: Boolean?,
    val showOtherSports: Boolean?,
    val liveCricket: Boolean?,
    val liveFootball: Boolean?,
    val liveOtherSport: Boolean?,
    val splashImageLink: String?,
    val otherSports: String?,
    val showScore: Boolean?,
    val outsideUrlTitle: String?,
    val outsideUrlDescription: String?,
    val outsideUrlImageUrl: String?,
    val appId: Int
)

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String?,
    val api: String?,
    val status: String?,
    val streamingId: Int
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: Int,
    val status: String?,
    val name: String?,
    val thumbUrl: String?,
    val isVisible: Boolean?,
    val startTime: String?,
    val endTime: String?,
    val description: String?,
    val sportType: String?,
    val excludedAppPackageNames: String?,
    val streamingId: Int,
    val sort: Int?)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Int,
    val eventName: String?,
    val eventSlug: String?,
    val eventThumbUrl: String?,
    val eventUrl: String?,
    val isHighlight: Boolean?,
    val isVisible: Boolean?,
    val startTime: String?,
    val endTime: String?,
    val description: String?,
    val teamAName: String?,
    val teamAImage: String?,
    val teamBName: String?,
    val teamBUrl: String?,
    val metadata: String?,
    val isLive: Boolean?,
    val excludedAppPackageNames: String?,
    val tournamentId: Int
)

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey val id: Int,
    val linkName: String?,
    val linkUrl: String?,
    val linkImage: String?,
    val durationSeconds: Int?,
    val viewCount: Int?,
    val isVisible: Boolean?,
    val publishedAt: String?,
    val eventId: Int
)

@Entity(tableName = "links")
data class LinkEntity(
    @PrimaryKey val id: Int,
    val linkName: String?,
    val linkUrl: String?,
    val linkType: String?,
    val mpdLink: String?,
    val mpdKey: String?,
    val linkImage: String?,
    val isVisible: Boolean?,
    val priority: Int?,
    val excludedAppPackageNames: String?,
    val refererHeader: String?,
    val originHeader: String?,
    val userAgentHeader: String?,
    val eventId: Int
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val matchType: String?,
    val status: String?,
    val venue: String?,
    val date: String?,
    val team1: String?,
    val team2: String?,
    val team1Img: String?,
    val team2Img: String?,
    val scoreJson: String?, // JSON string of List<Inning>
    val lastUpdated: Long = System.currentTimeMillis()
)

data class TournamentWithEvents(
    @Embedded val tournament: TournamentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tournamentId"
    )
    val events: List<EventEntity>
)

data class StreamingWithTournaments(
    @Embedded val streaming: StreamingEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "streamingId",
        entity = TournamentEntity::class
    )
    val tournaments: List<TournamentWithEvents>,
    @Relation(
        parentColumn = "id",
        entityColumn = "streamingId"
    )
    val scores: List<ScoreEntity>
)
