package livefootball.footballstreamning.fifaworldcup.viewmodels

data class HomeDisplayItem(
    val id: Int,
    val title: String,
    val subtitle: String?,
    val status: String?,
    val imageUrl: String?,
    val isLive: Boolean,
    val isTrending: Boolean = false,
    val startTime: String? = null,
    val originalObject: Any // Keep the original entity for navigation
)

data class HomeSection(
    val title: String,
    val items: List<HomeDisplayItem>,
    val sportType: String
)
