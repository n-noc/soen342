package search;

import domain.Route;
import infra.TrainNetwork;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Runs a direct search: pick candidates -> filter -> sort. */
public final class SearchService {
    private SearchService() {}

    public static List<Route> direct(TrainNetwork net, SearchQuery q) {
        // start from an indexed subset if possible
        Stream<Route> base = (q.getFromCity() != null)
                ? net.getRoutesFrom(q.getFromCity()).stream()
                : net.getAllRoutes().stream();

        // choose comparator based on sortBy and sortDir
        Comparator<Route> cmp = Comparators.maybeReverse(
                Comparators.choose(q), q.getSortDir());

        // filter then sort
        return base
                .filter(r -> RouteFilters.routeMatches(q, r))
                .sorted(cmp)
                .toList();
    }
}
