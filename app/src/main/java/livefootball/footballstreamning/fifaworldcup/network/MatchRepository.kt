package livefootball.footballstreamning.fifaworldcup.network

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import livefootball.footballstreamning.fifaworldcup.database.AppDao
import livefootball.footballstreamning.fifaworldcup.database.MatchEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val scoreApiService: ScoreApiService,
    private val appDao: AppDao
) {
    fun getAllMatchesFlow(): Flow<List<MatchEntity>> = appDao.getAllMatchesFlow()

    suspend fun fetchCurrentMatches(apiKey: String) {
        try {
            val response = scoreApiService.getCurrentMatches(apiKey)
            if (response.status == "success" && response.data != null) {
                val entities = response.data.map { match ->
                    MatchEntity(
                        id = match.id,
                        name = match.name,
                        matchType = match.matchType,
                        status = match.status,
                        venue = match.venue,
                        date = match.date,
                        team1 = match.teams?.getOrNull(0),
                        team2 = match.teams?.getOrNull(1),
                        team1Img = match.teamInfo?.getOrNull(0)?.img,
                        team2Img = match.teamInfo?.getOrNull(1)?.img,
                        scoreJson = match.score?.let { Gson().toJson(it) }
                    )
                }
                appDao.insertMatches(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getMatchById(matchId: String): MatchEntity? = appDao.getMatchById(matchId)
}
