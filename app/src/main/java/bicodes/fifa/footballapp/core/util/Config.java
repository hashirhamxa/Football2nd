package bicodes.fifa.footballapp.core.util;

public class Config {
    // Base URL for API Football
    public static final String BASE_URL = "https://apiv2.apifootball.com/";
    
    // Replace with your actual API key from apifootball.com
    public static final String API_KEY = "519a4722282608af8cbc6c81f02f4e0f48bde851be65aa4a4987a706fe464225";

    // API Actions
    public static final String ACTION_GET_EVENTS = "get_events";
    public static final String ACTION_GET_STANDINGS = "get_standings";
    public static final String ACTION_GET_LEAGUES = "get_leagues";
    
    // Cache expiry settings (example: 5 minutes for live matches, 24 hours for standings)
    public static final long LIVE_MATCH_CACHE_EXPIRY = 5 * 60 * 1000;
    public static final long STANDINGS_CACHE_EXPIRY = 24 * 60 * 60 * 1000;
}

