package search;
import java.util.Comparator;
import domain.Route;


    // supporting helpers for direct search results
public final class Comparators {
    private Comparators() {}

    // sort by total trip duration (min)
    public static Comparator<Route> byDuration(){
        return Comparator.comparingInt(Route::getDurationMinutes);
    }

    // sort by price in first class 
    public static Comparator<Route> byFirstPrice(){
        return Comparator.comparingInt(Route::getFirstClassPrice);
    }

    // sort by second class price
    public static Comparator<Route> bySecondPrice(){
        return Comparator.comparingInt(Route::getSecondClassPrice);
    }

    // sort direction
    public static Comparator<Route> choose (SearchQuery q){
        String sb=q.getSortBy()==null ? "DURATION" : q.getSortBy();
        return switch(sb) {
            case "PRICE_FIRST" -> byFirstPrice();
            case "PRICE_SECOND" -> bySecondPrice();
            default -> byDuration();
        };
    }

    // apply ASC/DESC to comparator from the query
    public static <T> Comparator<T> maybeReverse(Comparator<T> cmp, String dir){
        return "DESC".equalsIgnoreCase(dir) ? cmp.reversed() : cmp;
    }

    
}
