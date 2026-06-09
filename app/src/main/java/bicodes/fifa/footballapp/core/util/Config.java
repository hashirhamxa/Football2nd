package bicodes.fifa.footballapp.core.util;

public class Config {
    // Base URL for API Football
    public static final String BASE_URL = "https://apiv2.apifootball.com/";
    
    // Replace with your actual API key from apifootball.com
    public static final String API_KEY = "2e804ef7d4fe7366f3cb9252664b6b977e776b293c2100dfb6f178ca38793ba4";

    // API Actions
    public static final String ACTION_GET_EVENTS = "get_events";
    public static final String ACTION_GET_STANDINGS = "get_standings";
    public static final String ACTION_GET_LEAGUES = "get_leagues";
    
    // Cache expiry settings (example: 5 minutes for live matches, 24 hours for standings)
    public static final long LIVE_MATCH_CACHE_EXPIRY = 5 * 60 * 1000;
    public static final long STANDINGS_CACHE_EXPIRY = 24 * 60 * 60 * 1000;
}

