package search;

import domain.Itinerary;
import domain.Leg;
import domain.Route;
import domain.TransferRules;
import infra.TrainNetwork;

import java.util.*;


// Builds indirect itineraries (0..N transfers) between two cities.
// Expands city graph using TrainNetwork.getRoutesFrom(currentCity)
// Filters each candidate leg with RouteFilters.matches(SearchQuery, Route)
//  Validates connections with TransferRules (min transfer, city continuity, types)
// Avoids city cycles within a single itinerary

public final class IndirectSearchService {

    private IndirectSearchService() {}


    //Find itineraries from q.fromCity to q.toCity, honoring time/price/type filters in q,
    //and connection constraints in rules.
    public static List<Itinerary> find(TrainNetwork net,
                                       SearchQuery q,
                                       int maxTransfers,
                                       int maxResults) {
        String start = safeLower(q.getFromCity());
        String goal  = safeLower(q.getToCity());
        if (isBlank(start) || isBlank(goal)) return List.of();

        // seed: all FIRST LEGS from start city that pass Issue-2 filters
        Deque<PathState> queue = new ArrayDeque<>();
        for (Route r : net.getRoutesFrom(q.getFromCity())) {
            if (!RouteFilters.matches(q, r)) continue;

            Itinerary it = new Itinerary();
            it.addLeg(new Leg(r,0, r.getDurationMinutes()));
            it.recomputeTotals();

            Set<String> visited = new HashSet<>();
            visited.add(start);
            visited.add(safeLower(r.getArrivalCity())); // allow to block cycles

            queue.addLast(new PathState(it, visited));
        }

        // BFS: naturally prioritizes fewer transfers
        List<Itinerary> results = new ArrayList<>();
        // To dedupe identical sequences of routeIds
        Set<String> seenKeys = new HashSet<>();

        while (!queue.isEmpty() && results.size() < maxResults) {
            PathState cur = queue.removeFirst();
            Route last = lastRoute(cur.itinerary);
            String atCity = safeLower(last.getArrivalCity());

            // reached destination
            if (goal.equals(atCity)) {
                cur.itinerary.recomputeTotals();
                String key = itineraryKey(cur.itinerary);
                if (seenKeys.add(key)) {
                    results.add(cur.itinerary);
                }
                continue;
            }

            // transfers used so far = legs - 1
            int transfersUsed = cur.itinerary.getLegs().size() - 1;
            if (transfersUsed >= maxTransfers) continue;

            // expand from current city
            for (Route nxt : net.getRoutesFrom(last.getArrivalCity())) {
                // basic Issue-2 filters
                if (!RouteFilters.matches(q, nxt)) continue;

                // enforce connection feasibility (city continuity, min transfer, type penalty)
                if (!TransferRules.isValidConnection(last, nxt)) continue;

                String nextCity = safeLower(nxt.getArrivalCity());

                // avoid revisiting cities (simple cycle prevention)
                if (cur.visitedCities.contains(nextCity) && !goal.equals(nextCity)) continue;

                // compute layover between last.arrival and next.departure
                int gap = transferGapMinutes(last.getArrivalTime(), nxt.getDepartureTime());

                // build the next itinerary
                Itinerary nextIt = cloneItinerary(cur.itinerary);
                nextIt.addLeg(new Leg(nxt, gap, nxt.getDurationMinutes()));
                nextIt.recomputeTotals();

                Set<String> nextVisited = new HashSet<>(cur.visitedCities);
                nextVisited.add(nextCity);

                queue.addLast(new PathState(nextIt, nextVisited));
            }
        }

        return results;
    }

    // helpers

    private static class PathState {
        final Itinerary itinerary;
        final Set<String> visitedCities;
        PathState(Itinerary it, Set<String> visitedCities) {
            this.itinerary = it;
            this.visitedCities = visitedCities;
        }
    }

    private static Route lastRoute(Itinerary it) {
        return it.getLegs().get(it.getLegs().size() - 1).getRoute();
    }

    private static Itinerary cloneItinerary(Itinerary src) {
        Itinerary copy = new Itinerary();
        src.getLegs().forEach(L ->
            copy.addLeg(new Leg(L.getRoute(), L.getTransferFromPrevMinutes(), L.getLegDurationMinutes()))
        );
        copy.recomputeTotals();
        return copy;
    }

    // "A→B|B→C|..." using routeIds when available, else city/time tuple
    private static String itineraryKey(Itinerary it) {
        StringBuilder sb = new StringBuilder();
        it.getLegs().forEach(L -> {
            Route r = L.getRoute();
            String rid = r.getRouteId();
            if (rid != null && !rid.isBlank()) {
                sb.append(rid);
            } else {
                sb.append(safeLower(r.getDepartureCity()))
                  .append(">")
                  .append(safeLower(r.getArrivalCity()))
                  .append("@")
                  .append(r.getDepartureTime())
                  .append("-")
                  .append(r.getArrivalTime());
            }
            sb.append("|");
        });
        return sb.toString();
    }

    private static String safeLower(String s) {
        return s == null ? null : s.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** "HH:mm" difference considering next-day roll-over for departures before arrivals. */
    private static int transferGapMinutes(String arr, String dep) {
        int a = minutes(arr);
        int d = minutes(dep);
        int gap = d - a;
        return gap >= 0 ? gap : gap + 24 * 60;
    }

    private static int minutes(String hhmm) {
        String[] p = hhmm.split(":");
        int h = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        return h * 60 + m;
    }
}