package domain;
public enum TrainType {
    ICE("ICE"),
    RJX("RJX"),
    INTERCITY("InterCity"),
    FRECCIAROSSA("Frecciarossa"),
    TGV("TGV"),
    REGIONAL("Regional"),
    EUROCITY("EuroCity"),
    REGIOEXPRESS("RegioExpress"),
    ITALO("Italo"),
    NIGHTJET("Nightjet"),
    INTERCITÉS("Intercités"),
    THALYS("Thalys"),
    EUROSTAR("EuroStar"),
    RE("RE"),
    TER("TER"),
    IC("IC"),
    AVE("AVE"),
    RAILJET("Railjet"),

    UNKNOWN("Unknown");

    private final String displayName;

    TrainType(String displayName) {
        this.displayName = displayName;
    }

    public static TrainType fromString(String raw) {
        if (raw == null) return UNKNOWN;
        try {
            return TrainType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return displayName; // ensures pretty printing
    }


}
