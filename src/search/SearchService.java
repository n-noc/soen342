package search;

import domain.Route;
import infra.Network;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Runs a direct search: pick candidates -> filter -> sort. */
public final class SearchService {
    private SearchService() {}

    public static List<Route> direct(Network net, SearchQuery q) {
        // start from an indexed subset if possible
        Stream<Route> base = (q.getFromCity() != null)
                ? net.routesByDeparture(q.getFromCity()).stream()
                : net.allRoutes().stream();

        Comparator<Route> cmp = Comparators.maybeReverse(
                Comparators.choose(q), q.getSortDir());

        return base
                .filter(r -> RouteFilters.routeMatches(q, r))
                .sorted(cmp)
                .toList();
    }
}
