package search;

import java.util.Set;


// class that holds all the filters that the user can set except route-id
public class SearchQuery {
    private String fromCity;
    private String toCity;
    private String depStart;
    private String depEnd;
    private String arrStart;
    private String arrEnd;
    private String trainType;
    private Set<String> days;  //mon,tue...
    private String priceClass;   // first, second, class (price chnages)
    private Integer maxPrice;
    private String sortBy;      //can sort by duration, first price, second pruce (class)
    private String sortDir;    //sort in ascending/descending order 


    // constructor
    public SearchQuery(String fromCity, String toCity, String depStart, String depEnd, String arrStart, String arrEnd,
                       String trainType, Set<String> days, String priceClass, Integer maxPrice, String sortBy, String sortDir) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.depStart = depStart;
        this.depEnd = depEnd;
        this.arrStart = arrStart;
        this.arrEnd = arrEnd;
        this.trainType = trainType;
        this.days = days;
        this.priceClass = priceClass;
        this.maxPrice = maxPrice;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
    }
    // normalzie the input
    public void normalize() {
        if(fromCity!=null){
            fromCity=fromCity.trim().toLowerCase()
        }
        if(toCity!=null){
            toCity=toCity.trim().toLowerCase()
        }
    }

    // validate the input
    public void validate() {
      // defualt sorting is duration
      if(fromCity==null){
        sortBy="DURATION";
    }
    if(sortDir==null){
        sortDir="ASC";
    }
    if(!(sortBy.equals("DURATION") || sortBy.equals("PRICE_FIRST") || sortBy.equals("PRICE_SECOND"))){
        throw new IllegalArgumentException("Invalid sortBy: "+sortBy);
    }   
    }
    
    // API
    
}
