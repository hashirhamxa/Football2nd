package livefootball.footballstreamning.fifaworldcup.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import livefootball.footballstreamning.fifaworldcup.models.FootballMatchScoreModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FootballScoreRepository @Inject constructor(
    private val footballScoreApiService: FootballScoreApiService
) {
    suspend fun getLiveMatches(apiKey: String): List<FootballMatchScoreModel> {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())
            
            val response = footballScoreApiService.getMatches(
                from = today,
                to = today,
                apiKey = apiKey
            )
            
            if (response.isJsonArray) {
                val type = object : TypeToken<List<FootballMatchScoreModel>>() {}.type
                Gson().fromJson(response, type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
