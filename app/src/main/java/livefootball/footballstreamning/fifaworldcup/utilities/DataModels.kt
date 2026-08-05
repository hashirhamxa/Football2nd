package livefootball.footballstreamning.fifaworldcup.utilities

data class HomeMatch(
    val title: String,
    val tournament: String,
    val status: String,
    val score: String? = null,
    val isLive: Boolean = true,
    val imageUrl: String = "",
    val eventId: Int? = null,
    val eventThumbUrl: String? = null
)

data class HomeTrending(
    val title: String,
    val category: String,
    val description: String,
    val isLive: Boolean = true,
    val imageUrl: String = ""
)

data class TournamentHighlight(
    val name: String,
    val subtitle: String,
    val imageUrl: String = "",
    val eventCount: String = "0"
)
