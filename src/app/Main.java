package app;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.List;

import infra.TrainNetwork;

public class Main {

        public static void main(String[] args) {
            String csvPath = "./resources/eu_rail_network.csv";
            TrainNetwork net = new TrainNetwork();
            try {
                net.load(csvPath);
            } catch (IOException e) {
                System.err.println("Failed to load CSV: " + e.getMessage());
                return;
            }

            System.out.println("Total connections loaded: " + net.getAllConnections().size());
            System.out.println("Total direct routes (city pairs): " + net.getAllRoutes().size());

            System.out.println("\nSome routes:");
            net.getAllRoutes().stream().limit(5).forEach(r -> {
                System.out.println("  " + r);
            });

            String city = "Alicante";
            System.out.println("\nDepartures from " + city + ":");
            net.getDeparturesFrom(city).stream().limit(5).forEach(tc -> {
                System.out.println("  " + tc);
            });


            String from = "A Coruña";
            String to   = "Santander";
            Route route = net.getRoute(from, to);
            if (route != null) {
                System.out.println("\nDirect route " + from + " → " + to + " has " + route.getConnections().size() + " connections.");
                route.getConnections().stream().limit(5).forEach(tc -> System.out.println("  " + tc));

                var fastest = route.getFastest();
                if (fastest != null) System.out.println("  Fastest: " + fastest);
            } else {
                System.out.println("\nNo direct route " + from + " → " + to);
            }

            System.out.println("\nDone.");
        }
    }
