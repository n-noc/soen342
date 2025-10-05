import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Route {
    private final String departureCity;
    private final String arrivalCity;
    private final List<TrainConnection> connections = new ArrayList<>();

    public Route(String departureCity, String arrivalCity) {
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
    }

    public String getDepartureCity() { return departureCity; }
    public String getArrivalCity() { return arrivalCity; }

    public List<TrainConnection> getConnections() {
        //an unmodifiable view to keep Route’s internal list safe
        return Collections.unmodifiableList(connections);
    }

    public void addConnection(TrainConnection tc) {
        if (!tc.getDepartureCity().equalsIgnoreCase(departureCity)) return;
        if (!tc.getArrivalCity().equalsIgnoreCase(arrivalCity)) return;
        connections.add(tc);
    }

    public TrainConnection getFastest() {
        return connections.stream()
                .min(Comparator.comparing(TrainConnection::getTripDuration))
                .orElse(null);
    }

    public TrainConnection getCheapestFirstClass() {
        return connections.stream()
                .min(Comparator.comparingInt(TrainConnection::getFirstClassRate))
                .orElse(null);
    }

    public TrainConnection getCheapestSecondClass() {
        return connections.stream()
                .min(Comparator.comparingInt(TrainConnection::getSecondClassRate))
                .orElse(null);
    }

    @Override
    public String toString() {
        return departureCity + " → " + arrivalCity + " (" + connections.size() + " connections)";
    }
}
