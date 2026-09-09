package livefootball.footballstreamning.fifaworldcup.models

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("data")
    val data: List<AppData>
)

data class AppData(
    @SerializedName("id") val id: Int,
    @SerializedName("package_name") val packageName: String?,
    @SerializedName("app_name") val appName: String?,
    @SerializedName("app_type") val appType: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("current_version") val currentVersion: String?,
    @SerializedName("update_required") val updateRequired: Boolean?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("new_package_name") val newPackageName: String?,
    @SerializedName("license_key") val licenseKey: String?,
    @SerializedName("product_id") val productId: String?,
    @SerializedName("socail_media_links") val socialMediaLinks: List<SocialMediaLink>?,
    @SerializedName("ads_time_count_down") val adsTimeCountDown: Int?,
    @SerializedName("ads") val ads: List<Ad>?,
    @SerializedName("streaming") val streaming: List<StreamingWrapper>?
)

data class SocialMediaLink(
    @SerializedName("name") val name: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("image_url") val imageUrl: String?
)

data class Ad(
    @SerializedName("id") val id: Int,
    @SerializedName("platform") val platform: String?,
    @SerializedName("ad_unit_id") val adUnitId: String?,
    @SerializedName("ad_placement") val adPlacement: String?,
    @SerializedName("priority") val priority: Int?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("frequency_cap") val frequencyCap: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("ads") val appId: Int
)

data class StreamingWrapper(
    @SerializedName("id") val id: Int,
    @SerializedName("apps_id") val appsId: Int,
    @SerializedName("streaming_id") val streamingId: Streaming?
)

data class Streaming(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String?,
    @SerializedName("show_cricket_highlights") val showCricketHighlights: Boolean?,
    @SerializedName("show_football_highlights") val showFootballHighlights: Boolean?,
    @SerializedName("show_other_sports_highlights") val showOtherSportsHighlights: Boolean?,
    @SerializedName("new_app_outside_url") val newAppOutsideUrl: String?,
    @SerializedName("force_new_app_outside_url") val forceNewAppOutsideUrl: Boolean?,
    @SerializedName("app_sport_type") val appSportType: String?,
    @SerializedName("banner_ads") val bannerAds: Boolean?,
    @SerializedName("on_pause_ad") val onPauseAd: Boolean?,
    @SerializedName("show_other_sports") val showOtherSports: Boolean?,
    @SerializedName("live_cricket") val liveCricket: Boolean?,
    @SerializedName("live_football") val liveFootball: Boolean?,
    @SerializedName("live_other_sport") val liveOtherSport: Boolean?,
    @SerializedName("splash_image_link") val splashImageLink: String?,
    @SerializedName("other_sports") val otherSports: String?,
    @SerializedName("show_score") val showScore: Boolean?,
    @SerializedName("outside_url_title") val outsideUrlTitle: String?,
    @SerializedName("outside_url_description") val outsideUrlDescription: String?,
    @SerializedName("outside_url_image_url") val outsideUrlImageUrl: String?,
    @SerializedName("Scores") val scores: List<Score>?,
    @SerializedName("tournaments") val tournaments: List<TournamentWrapper>?
)

data class Score(
    @SerializedName("type") val type: String?,
    @SerializedName("api") val api: String?,
    @SerializedName("status") val status: String?
)

data class TournamentWrapper(
    @SerializedName("id") val id: Int,
    @SerializedName("streaming_id") val streamingId: Int,
    @SerializedName("tournaments_id") val tournamentsId: Tournament?
)

data class Tournament(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("thumb_url") val thumbUrl: String?,
    @SerializedName("is_visible") val isVisible: Boolean?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("sport_type") val sportType: String?,
    @SerializedName("excluded_app_package_names") val excludedAppPackageNames: String?,
    @SerializedName("sort") val sort: Int?,
    @SerializedName("events") val events: List<EventWrapper>?
)

data class EventWrapper(
    @SerializedName("id") val id: Int,
    @SerializedName("tournaments_id") val tournamentsId: Int,
    @SerializedName("events_id") val eventsId: Event?
)

data class Event(
    @SerializedName("id") val id: Int,
    @SerializedName("event_name") val eventName: String?,
    @SerializedName("event_slug") val eventSlug: String?,
    @SerializedName("event_thumb_url") val eventThumbUrl: String?,
    @SerializedName("event_url") val eventUrl: String?,
    @SerializedName("is_highlight") val isHighlight: Boolean?,
    @SerializedName("is_visible") val isVisible: Boolean?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("team_a_name") val teamAName: String?,
    @SerializedName("team_a_image") val teamAImage: String?,
    @SerializedName("team_b_name") val teamBName: String?,
    @SerializedName("team_b_url") val teamBUrl: String?,
    @SerializedName("metadata") val metadata: String?,
    @SerializedName("is_live") val isLive: Boolean?,
    @SerializedName("excluded_app_package_names") val excludedAppPackageNames: String?,
    @SerializedName("highligths") val highlights: List<HighlightWrapper>?,
    @SerializedName("links") val links: List<LinkWrapper>?
)

data class HighlightWrapper(
    @SerializedName("id") val id: Int,
    @SerializedName("events_id") val eventsId: Int,
    @SerializedName("highlights_id") val highlightsId: Highlight?
)

data class Highlight(
    @SerializedName("id") val id: Int,
    @SerializedName("link_name") val linkName: String?,
    @SerializedName("link_url") val linkUrl: String?,
    @SerializedName("link_image") val linkImage: String?,
    @SerializedName("duration_seconds") val durationSeconds: Int?,
    @SerializedName("view_count") val viewCount: Int?,
    @SerializedName("is_visible") val isVisible: Boolean?,
    @SerializedName("published_at") val publishedAt: String?
)

data class LinkWrapper(
    @SerializedName("id") val id: Int,
    @SerializedName("events_id") val eventsId: Int,
    @SerializedName("links_id") val linksId: Link?
)

data class Link(
    @SerializedName("id") val id: Int,
    @SerializedName("link_name") val linkName: String?,
    @SerializedName("link_url") val linkUrl: String?,
    @SerializedName("link_type") val linkType: String?,
    @SerializedName("mpd_link") val mpdLink: String?,
    @SerializedName("mpd_key") val mpdKey: String?,
    @SerializedName("link_image") val linkImage: String?,
    @SerializedName("is_visible") val isVisible: Boolean?,
    @SerializedName("priority") val priority: Int?,
    @SerializedName("excluded_app_package_names") val excludedAppPackageNames: String?,
    @SerializedName("referer_header") val refererHeader: String?,
    @SerializedName("origin_header") val originHeader: String?,
    @SerializedName("user_agent_header") val userAgentHeader: String?
)
