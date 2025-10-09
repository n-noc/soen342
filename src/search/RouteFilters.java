package search;

import domain.Route;
import java.util.Set;

public final class RouteFilters {
    private RouteFilters() {}

    public static boolean matches(SearchQuery q, Route r) {
        return matchesCities(q, r)
            && matchesTimes(q, r)
            && matchesTrainType(q, r)
            && matchesDays(q, r)
            && matchesPrice(q, r);
    }

    static boolean matchesCities(SearchQuery q, Route r) {
        if (q.getFromCity() != null && !r.getDepartureCity().equalsIgnoreCase(q.getFromCity()))
            return false;
        if (q.getToCity()   != null && !r.getArrivalCity().equalsIgnoreCase(q.getToCity()))
            return false;
        return true;
    }

    // dep/arr as "HH:mm" strings → lexicographic compare works
    static boolean matchesTimes(SearchQuery q, Route r) {
        String dep = r.getDepartureTime();
        String arr = r.getArrivalTime();

        if (q.getDepStart() != null && dep.compareTo(q.getDepStart()) < 0) return false;
        if (q.getDepEnd()   != null && dep.compareTo(q.getDepEnd())   > 0) return false;

        if (q.getArrStart() != null && arr.compareTo(q.getArrStart()) < 0) return false;
        if (q.getArrEnd()   != null && arr.compareTo(q.getArrEnd())   > 0) return false;

        return true;
    }

    static boolean matchesTrainType(SearchQuery q, Route r) {
        if (q.getTrainType() == null || q.getTrainType().isBlank()) return true;
        return r.getTrainType() != null && r.getTrainType().equalsIgnoreCase(q.getTrainType());
    }

    // q.days are like {"MON","TUE",...}; r has getDaysSet()
    static boolean matchesDays(SearchQuery q, Route r) {
        Set<String> wanted = q.getDays();
        if (wanted == null || wanted.isEmpty()) return true;

        Set<String> operates = r.getDaysSet();
        if (operates == null || operates.isEmpty()) return false;

        for (String d : wanted) {
            if (operates.contains(d)) return true;   // any overlap
        }
        return false;
    }

    static boolean matchesPrice(SearchQuery q, Route r) {
        if (q.getMaxPrice() == null) return true;

        String cls = q.getPriceClass() == null ? "ANY" : q.getPriceClass().toUpperCase();
        int max = q.getMaxPrice();

        if ("FIRST".equals(cls))  return r.getFirstClassPrice()  <= max;
        if ("SECOND".equals(cls)) return r.getSecondClassPrice() <= max;
        return r.getFirstClassPrice() <= max || r.getSecondClassPrice() <= max;  // ANY
    }
}














