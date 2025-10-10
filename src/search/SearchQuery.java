package search;

import java.util.*;


public class SearchQuery {
    // fields
    private String fromCity;
    private String toCity;
    private String depStart;   // "HH:mm" or null
    private String depEnd;
    private String arrStart;
    private String arrEnd;
    private String trainType;
    private Set<String> days;  // e.g., {"MON","TUE"}
    private String priceClass; // FIRST | SECOND | ANY
    private Integer maxPrice;  // cents or euros (document which)
    private String sortBy;     // DURATION | PRICE_FIRST | PRICE_SECOND
    private String sortDir;    // ASC | DESC

    // constructor
    public SearchQuery(String fromCity, String toCity, String depStart, String depEnd,
                       String arrStart, String arrEnd, String trainType, Set<String> days,
                       String priceClass, Integer maxPrice, String sortBy, String sortDir) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.depStart = depStart;
        this.depEnd = depEnd;
        this.arrStart = arrStart;
        this.arrEnd = arrEnd;
        this.trainType = trainType;
        this.days = (days != null) ? new HashSet<>(days) : new HashSet<>();
        this.priceClass = priceClass;
        this.maxPrice = maxPrice;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
    }

    // normalize
    public void normalize() {
        if (fromCity != null)  fromCity  = fromCity.trim().toLowerCase();
        if (toCity != null)    toCity    = toCity.trim().toLowerCase();
        if (trainType != null) trainType = trainType.trim().toLowerCase();

        // Normalize days: "Monday"/"mon" -> "MON"
        if (days != null && !days.isEmpty()) {
            Set<String> normalizedDays = new HashSet<>();
            for (String d : days) {
                if (d == null) continue;
                String s = d.trim().toUpperCase(Locale.ROOT);
                // keep first 3 chars when possible, otherwise keep whatever we have
                normalizedDays.add(s.length() >= 3 ? s.substring(0, 3) : s);
            }
            days = normalizedDays;
        }
    }

    // validate
    public void validate() {
        if (sortBy == null)  sortBy = "DURATION";   
        if (sortDir == null) sortDir = "ASC";
        if (priceClass == null) priceClass = "ANY";
    
        List<String> validSortBy   = Arrays.asList("DURATION","PRICE_FIRST","PRICE_SECOND");
        List<String> validSortDir  = Arrays.asList("ASC","DESC");
        List<String> validPriceCls = Arrays.asList("FIRST","SECOND","ANY");
    
        if (!validSortBy.contains(sortBy.toUpperCase()))
            throw new IllegalArgumentException("Invalid sortBy: " + sortBy);
    
        if (!validSortDir.contains(sortDir.toUpperCase()))
            throw new IllegalArgumentException("Invalid sortDir: " + sortDir);
    
        if (!validPriceCls.contains(priceClass.toUpperCase()))             
            throw new IllegalArgumentException("Invalid priceClass: " + priceClass);
    
        String hhmm = "^([01]?\\d|2[0-3]):[0-5]\\d$";
        if (depStart != null && !depStart.matches(hhmm)) throw new IllegalArgumentException("Invalid depStart: " + depStart);
        if (depEnd   != null && !depEnd.matches(hhmm))   throw new IllegalArgumentException("Invalid depEnd: "   + depEnd);
        if (arrStart != null && !arrStart.matches(hhmm)) throw new IllegalArgumentException("Invalid arrStart: " + arrStart);
        if (arrEnd   != null && !arrEnd.matches(hhmm))   throw new IllegalArgumentException("Invalid arrEnd: "   + arrEnd);
        if (maxPrice != null && maxPrice < 0)            throw new IllegalArgumentException("Invalid maxPrice: " + maxPrice);
    }
    
    // getters
    public String getFromCity()   { return fromCity; }
    public String getToCity()     { return toCity; }
    public String getDepStart()   { return depStart; }
    public String getDepEnd()     { return depEnd; }
    public String getArrStart()   { return arrStart; }
    public String getArrEnd()     { return arrEnd; }
    public String getTrainType()  { return trainType; }
    public Set<String> getDays()  { return days; }
    public String getPriceClass() { return priceClass; }
    public Integer getMaxPrice()  { return maxPrice; }
    public String getSortBy()     { return sortBy; }
    public String getSortDir()    { return sortDir; }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "fromCity='" + fromCity + '\'' +
                ", toCity='" + toCity + '\'' +
                ", depStart='" + depStart + '\'' +
                ", depEnd='" + depEnd + '\'' +
                ", arrStart='" + arrStart + '\'' +
                ", arrEnd='" + arrEnd + '\'' +
                ", trainType='" + trainType + '\'' +
                ", days=" + days +
                ", priceClass='" + priceClass + '\'' +
                ", maxPrice=" + maxPrice +
                ", sortBy='" + sortBy + '\'' +
                ", sortDir='" + sortDir + '\'' +
                '}';
    }
}