package bicodes.fifa.footballapp.feature.matches.model;

public class MatchEvent {
    public enum Type { GOAL, YELLOW_CARD, RED_CARD }

    public final String time;
    public final String playerName;
    public final String score;
    public final Type type;

    public MatchEvent(String time, String playerName, String score, Type type) {
        this.time = time;
        this.playerName = playerName;
        this.score = score;
        this.type = type;
    }
}

