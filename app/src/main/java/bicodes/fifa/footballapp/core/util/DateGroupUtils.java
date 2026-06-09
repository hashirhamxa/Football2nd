package bicodes.fifa.footballapp.core.util;

import bicodes.fifa.footballapp.feature.matches.data.MatchEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DateGroupUtils {

    public static class DisplayItem {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_MATCH = 1;

        public final int type;
        public final String headerText;
        public final MatchEntity match;

        public DisplayItem(int type, String headerText, MatchEntity match) {
            this.type = type;
            this.headerText = headerText;
            this.match = match;
        }
    }

    public static List<DisplayItem> groupMatchesByDate(List<MatchEntity> matches) {
        if (matches == null || matches.isEmpty()) return new ArrayList<>();

        // Sort matches by date (newest first for past matches)
        Collections.sort(matches, (m1, m2) -> m2.matchDate.compareTo(m1.matchDate));

        return buildDisplayItems(matches);
    }

    public static List<DisplayItem> groupUpcomingByDate(List<MatchEntity> matches) {
        if (matches == null || matches.isEmpty()) return new ArrayList<>();

        // Sort matches by date (oldest first for upcoming matches)
        Collections.sort(matches, (m1, m2) -> m1.matchDate.compareTo(m2.matchDate));

        return buildDisplayItems(matches);
    }

    private static List<DisplayItem> buildDisplayItems(List<MatchEntity> matches) {
        Map<String, List<MatchEntity>> groupedMap = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (MatchEntity match : matches) {
            String label = getDateLabel(match.matchDate, sdf);
            if (!groupedMap.containsKey(label)) {
                groupedMap.put(label, new ArrayList<>());
            }
            groupedMap.get(label).add(match);
        }

        List<DisplayItem> displayItems = new ArrayList<>();
        for (Map.Entry<String, List<MatchEntity>> entry : groupedMap.entrySet()) {
            displayItems.add(new DisplayItem(DisplayItem.TYPE_HEADER, entry.getKey(), null));
            for (MatchEntity m : entry.getValue()) {
                displayItems.add(new DisplayItem(DisplayItem.TYPE_MATCH, null, m));
            }
        }
        return displayItems;
    }

    private static String getDateLabel(String matchDate, SimpleDateFormat sdf) {
        try {
            Date date = sdf.parse(matchDate);
            if (date == null) return matchDate;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar matchCal = Calendar.getInstance();
            matchCal.setTime(date);

            long diff = matchCal.getTimeInMillis() - today.getTimeInMillis();
            long days = diff / (24 * 60 * 60 * 1000);

            if (days == 0) return "TODAY";
            if (days == 1) return "TOMORROW";
            if (days == -1) return "YESTERDAY";
            
            if (days > 1 && days < 7) return days + " DAYS";
            if (days == 7) return "1 WEEK";
            if (days < -1 && days > -7) return Math.abs(days) + " DAYS AGO";

            return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return matchDate;
        }
    }
}

