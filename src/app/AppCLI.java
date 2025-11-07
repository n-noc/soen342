package app;

import domain.*;
import infra.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import search.*;
import infra.RepositoryFactory.Mode;

public class AppCLI {

    private static final Scanner sc = new Scanner(System.in);

    // old repos

    // private static final ClientRepository clientRepo = new InMemoryClientRepository();
    // private static final TripRepository tripRepo = new InMemoryTripRepository();
    // private static final ReservationRepository reservationRepo = new InMemoryReservationRepository();
    // private static final TicketRepository ticketRepo = new InMemoryTicketRepository();
    // private static final BookingService booking = new BookingService(
    //         clientRepo, tripRepo, reservationRepo, ticketRepo
    // );

    private static final TrainNetwork net = new TrainNetwork();
    private static boolean dataLoaded = false;

    private static Client currentClient = null;
    private static Itinerary lastItinerary = null;
    private static Trip lastTrip = null;
    private static Reservation lastReservation = null;
    private static Ticket lastTicket = null;

    // connect to db
    private static final Mode MODE=Mode.SQLITE;
    private static final ClientRepository clientRepo       = RepositoryFactory.clients(MODE);
    private static final TripRepository tripRepo           = RepositoryFactory.trips(MODE);
    private static final ReservationRepository reservationRepo = RepositoryFactory.reservations(MODE);
    private static final TicketRepository ticketRepo       = RepositoryFactory.tickets(MODE);
    
    // application service
    private static final BookingService booking            =
        new BookingService(clientRepo, tripRepo, reservationRepo, ticketRepo);

    // ensure db schema exists
    static {
        RepositoryFactory.ensureSchema(MODE); // creates tables if needed
    }

    public static void main(String[] args) {
        System.out.println("""
                ==========================================
                 EU Rail Booking System
                ==========================================
                """);

        boolean running = true;
        while (running) {
            showMenu();
            System.out.print("Choose: ");
            String raw = sc.nextLine().trim();
            switch (raw) {
                case "1" ->
                    loadNetwork();
                case "2" ->
                    registerClient();
                case "3" ->
                    bookTripFlow();
                case "4" ->
                    confirmAllUnconfirmedReservations();
                case "5" ->
                    showSessionSummary();
                case "6" ->{
                    viewTripsForClient();
                }
                case "0" -> {
                    running = false;
                    System.out.println("Exit");
                }
                default ->
                    System.out.println("Invalid choice.");
            }
        }

        sc.close();
    }

    private static void showMenu() {
        System.out.println("""
                ------------------------------------------
                1. Load train network data from file
                2. Register / switch active client
                3. Search, choose itinerary, and create trip
                4. Confirm reservations & issue tickets
                5. Show current booking summary
                6.View all trips for a client
                ------------------------------------------
                0. Exit
                ------------------------------------------
                """);

        System.out.println("Current client:      " + (currentClient == null ? "(none)" : currentClient.getName() + " [" + currentClient.getClientId() + "]"));
        System.out.println("Last itinerary:      " + (lastItinerary == null ? "(none)" : routeSummary(lastItinerary)));
        System.out.println("Last trip:           " + (lastTrip == null ? "(none)" : lastTrip.getTripId() + " on " + lastTrip.getTripDate()));
        System.out.println("Last reservation:    " + (lastReservation == null ? "(none)" : lastReservation.getReservationId() + " (confirmed=" + lastReservation.isConfirmed() + ")"));
        System.out.println("Last ticket:         " + (lastTicket == null ? "(none)" : lastTicket.getTicketId()));
        System.out.println();
    }

    //Option 1: loadNetwork
    private static void loadNetwork() {
        if (dataLoaded) {
            System.out.println("Network already loaded.");
            return;
        }
        System.out.print("CSV path [default ./resources/eu_rail_network.csv]: ");
        String path = sc.nextLine().trim();
        if (path.isBlank()) {
            path = "./resources/eu_rail_network.csv";
        }

        try {
            net.load(path);
            dataLoaded = true;
            System.out.println("Loaded " + net.getAllConnections().size() + " connections.");
            System.out.println("Unique routes: " + net.getAllRoutes().size());
        } catch (IOException e) {
            System.out.println("Failed to load: " + e.getMessage());
        }
    }

    //Option 2: register client
    private static void registerClient() {
        System.out.println("=== Register / Switch Client ===");
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Phone: ");
        String phone = sc.nextLine();

        try {
            Client c = booking.registerClient(name, email, phone);
            currentClient = c;
            System.out.println("\nClient created: " + c);
            System.out.println("You are now acting as clientId=" + c.getClientId());
        } catch (IllegalArgumentException ex) {
            System.out.println("Error in client info: " + ex.getMessage());
        }
    }

    private static void bookTripFlow() {
        if (!dataLoaded) {
            System.out.println("You need to load network first (option 1).");
            return;
        }
        if (currentClient == null) {
            System.out.println("You need to be an active client, select option 2 first.");
            return;
        }

        //Search itineraries
        SearchQuery q;
        while (true) {
            System.out.println("=== Search Trip Options ===");
            System.out.print("Departure city: ");
            String from = sc.nextLine();
            System.out.print("Destination city: ");
            String to = sc.nextLine();

            q = new SearchQuery(
                    from,
                    to,
                    null, null,
                    null, null,
                    null,
                    null,
                    "ANY",
                    null,
                    "DURATION",
                    "ASC"
            );
            try {
                q.normalize();
                q.validate();
                break;
            } catch (IllegalArgumentException ex) {
                System.out.println("Bad search: " + ex.getMessage());
                System.out.println("Please try again.\n");
            }
        }

        List<Itinerary> list = IndirectSearchService.find(net, q, 2, 20);
        if (list.isEmpty()) {
            System.out.println("No options found.");
            return;
        }

        System.out.println("Found " + list.size() + " itineraries:");
        for (int i = 0; i < list.size(); i++) {
            System.out.print(i + 1 + ".");
            System.out.println(list.get(i));
            System.out.println();
        }

        int chosenIdx = -1;
        while (true) {
            System.out.print("Pick itinerary number: ");
            String pickRaw = sc.nextLine().trim();
            try {
                chosenIdx = Integer.parseInt(pickRaw) - 1;
                if (chosenIdx >= 0 && chosenIdx < list.size()) {
                    break;
                }
                System.out.println("Invalid index. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number");
            }
        }

        lastItinerary = list.get(chosenIdx);
        System.out.println("Selected itinerary " + (chosenIdx + 1));

        //trip info (date, fare class, passenger count)
        LocalDate tripDate = null;
        while (tripDate == null) {
            System.out.print("Enter Trip date (YYYY-MM-DD): ");
            String dateRaw = sc.nextLine().trim();
            try {
                tripDate = java.time.LocalDate.parse(dateRaw);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again");
            }
        }

        Trip.FareClass tripFareClass = null;
        while (tripFareClass == null) {
            System.out.print("Select fare class for the trip FIRST/SECOND: ");
            String fcRaw = sc.nextLine().trim().toUpperCase(java.util.Locale.ROOT);
            try {
                tripFareClass = Trip.FareClass.valueOf(fcRaw);
            } catch (Exception e) {
                System.out.println("Invalid fare class. Please type FIRST or SECOND.");
            }
        }

        Integer pax = null;
        while (pax == null) {
            System.out.print("Total passenger count for this trip: ");
            String paxRaw = sc.nextLine().trim();
            try {
                pax = Integer.parseInt(paxRaw);
                if (pax < 1) {
                    System.out.println("Passenger count must be at least 1.");
                    pax = null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid. Please enter a number >= 1");
            }
        }

        try {
            Trip t = booking.createTrip(
                    currentClient.getClientId(),
                    lastItinerary,
                    tripDate,
                    tripFareClass,
                    pax
            );
            lastTrip = t;
            System.out.println("Trip successfully created!");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error creating trip: " + ex.getMessage());
            return;
        }

        //Collect traveller info and create reservations for each traveller
        for (int i = 1; i <= pax; i++) {
            System.out.println("\nTraveller " + i);

            // name
            String passengerName = null;
            while (passengerName == null || passengerName.isBlank()) {
                System.out.print("Passenger full name: ");
                String tmp = sc.nextLine().trim();
                if (!tmp.isBlank()) {
                    passengerName = tmp;
                } else {
                    System.out.println("Name cannot be blank.");
                }
            }

            //age
            Integer passengerAge = null;
            while (passengerAge == null) {
                System.out.print("Passenger age: ");
                String ageRaw = sc.nextLine().trim();
                try {
                    passengerAge = Integer.parseInt(ageRaw);
                    if (passengerAge < 0) {
                        System.out.println("Age must be over 0!");
                        passengerAge = null;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid. Please enter a valid age");
                }
            }

            //id number
            String idNumber = null;
            while (idNumber == null || idNumber.isBlank()) {
                System.out.print("Passenger ID / passport number: ");
                String tmp = sc.nextLine().trim();
                if (!tmp.isBlank()) {
                    idNumber = tmp;
                } else {
                    System.out.println("Error. ID cannot be blank.");
                }
            }

            // fare class for this passenger
            Trip.FareClass passengerFare = null;
            while (passengerFare == null) {
                System.out.print("Fare class for this passenger FIRST/SECOND (Enter to use trip fare "
                        + lastTrip.getFareClass() + "): ");
                String fareRaw = sc.nextLine().trim();
                if (fareRaw.isEmpty()) {
                    passengerFare = lastTrip.getFareClass();
                    break;
                }
                try {
                    passengerFare = Trip.FareClass.valueOf(fareRaw.toUpperCase(java.util.Locale.ROOT));
                } catch (Exception e) {
                    System.out.println("Invalid fare class. Please try again.");
                }
            }

            boolean saved = false;
            while (!saved) {

                try {
                    Reservation r = booking.addReservation(
                            lastTrip.getTripId(),
                            currentClient.getClientId(),
                            passengerName,
                            passengerAge,
                            idNumber,
                            passengerFare
                    );
                    lastReservation = r;
                    System.out.println("Reservation for " + r.getPassengerName() + " was created!");
                    saved = true; // success -> exit retry loop
                } catch (IllegalArgumentException ex) {
                    System.out.println("Error creating reservation: " + ex.getMessage());
                    System.out.println("Please re-enter this traveller's details.\n");

                    // re-prompt name
                    passengerName = null;
                    while (passengerName == null || passengerName.isBlank()) {
                        System.out.print("Passenger full name: ");
                        String tmp = sc.nextLine().trim();
                        if (!tmp.isBlank()) {
                            passengerName = tmp;
                        } else {
                            System.out.println("Name cannot be blank.");
                        }
                    }

                    // re-prompt age
                    passengerAge = null;
                    while (passengerAge == null) {
                        System.out.print("Passenger age: ");
                        String ageRaw = sc.nextLine().trim();
                        try {
                            passengerAge = Integer.parseInt(ageRaw);
                            if (passengerAge < 0) {
                                System.out.println("Age must be over 0!");
                                passengerAge = null;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid. Please enter a valid age");
                        }
                    }

                    // re-prompt id number
                    idNumber = null;
                    while (idNumber == null || idNumber.isBlank()) {
                        System.out.print("Passenger ID / passport number: ");
                        String tmp = sc.nextLine().trim();
                        if (!tmp.isBlank()) {
                            idNumber = tmp;
                        } else {
                            System.out.println("Error. ID cannot be blank.");
                        }
                    }

                    // re-prompt fare
                    passengerFare = null;
                    while (passengerFare == null) {
                        System.out.print("Fare class for this passenger FIRST/SECOND (Enter to use trip fare "
                                + lastTrip.getFareClass() + "): ");
                        String fareRaw = sc.nextLine().trim();
                        if (fareRaw.isEmpty()) {
                            passengerFare = lastTrip.getFareClass();
                            break;
                        }
                        try {
                            passengerFare = Trip.FareClass.valueOf(fareRaw.toUpperCase(java.util.Locale.ROOT));
                        } catch (Exception e) {
                            System.out.println("Invalid fare class. Please try again.");
                        }
                    }
                }
            }
        }

        System.out.println("\nAll reservations done for this trip. Click option 4 to confirm!");
    }

    private static void confirmAllUnconfirmedReservations() {
        if (lastReservation == null) {
            System.out.println("You need at least one reservation first (option 3).");
            return;
        }
        System.out.println("=== Confirm Reservation & Issue Ticket ===");
        try {
            Ticket t = booking.confirmReservation(lastReservation.getReservationId());
            lastTicket = t;
            lastReservation = reservationRepo.findByTripId(lastTrip.getTripId())
                    .stream()
                    .filter(r -> r.getReservationId().equals(lastReservation.getReservationId()))
                    .findFirst()
                    .orElse(lastReservation);
            System.out.println("Ticket issued successfully: ");
            System.out.println(t);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error issuing a ticket: " + ex.getMessage());
        }
    }

    //Option 5: summary
    private static void showSessionSummary() {
        System.out.println("Current client:      "
                + (currentClient == null
                        ? "(none)"
                        : currentClient.getName() + " (" + currentClient.getEmail() + ")"));

        System.out.println("Last itinerary:      "
                + (lastItinerary == null
                        ? "(none)"
                        : routeSummary(lastItinerary)));

        System.out.println("Last trip:           "
                + (lastTrip == null
                        ? "(none)"
                        : lastTrip.getTripDate() + " "
                        + lastTrip.getItinerary().getOriginCity() + " → "
                        + lastTrip.getItinerary().getDestinationCity()
                        + " [" + lastTrip.getFareClass() + ", pax=" + lastTrip.getPassengerCount() + "]"));

        if (lastTrip == null) {
            System.out.println("Reservations:        (none)");
        } else {
            var tripReservations = reservationRepo.findByTripId(lastTrip.getTripId());
            if (tripReservations.isEmpty()) {
                System.out.println("Reservations:        (none)");
            } else {
                System.out.println("Reservations:");
                for (Reservation r : tripReservations) {
                    System.out.println("  - "
                            + r.getPassengerName()
                            + " (age " + r.getPassengerAge()
                            + ", class " + r.getFareClass()
                            + ", confirmed=" + r.isConfirmed()
                            + ")");
                }
            }
        }
        if (lastTrip == null) {
            System.out.println("Tickets:             (none)");
        } else {
            //get ALL tickets currently in the repo
            Collection<Ticket> allTickets = ticketRepo.findAll();

            //filter by only tickets that belong to the current trip
            List<Ticket> tripTickets = allTickets.stream()
                    .filter(t -> t.getTripId().equals(lastTrip.getTripId()))
                    .toList();

            if (tripTickets.isEmpty()) {
                System.out.println("Tickets:             (none issued yet)");
            } else {
                System.out.println("Tickets:");
                for (Ticket t : tripTickets) {
                    System.out.println("  - "
                            + t.getPassengerName()
                            + " (" + t.getFareClass()
                            + ", total €" + t.getTotalPriceCents()
                            + ") issued at " + t.getIssuedAt());
                }
            }
        }
    }

    private static String routeSummary(Itinerary it) {
        if (it.getLegs().isEmpty()) {
            return "(empty)";
        }
        Leg first = it.getLegs().get(0);
        Leg last = it.getLegs().get(it.getLegs().size() - 1);
        return first.getRoute().getDepartureCity() + " → " + last.getRoute().getArrivalCity()
                + " (" + it.getLegs().size() + " legs, "
                + it.getTotalDurationMinutes() + " min)";
    }


    // option 6: view all trips for a client
private static void viewTripsForClient() {
    System.out.println("Currently viewing trips for a client.");
    System.out.print("Enter client ID, or leave blank to use current client: ");
    String id = sc.nextLine().trim();

    //find client
    Client client = null;
    if (id.isBlank()) {
        if (currentClient == null) {
            System.out.println("No active client. Use option 2 first or enter a client ID.");
            return;
        }
        client = currentClient;
    } else {
        client = clientRepo.findById(id); // <-- use the entered id
        if (client == null) {
            System.out.println("Client not found: " + id);
            return;
        }
    }

    // Fetch trips for this client
    Collection<Trip> trips = tripRepo.findByClientId(client.getClientId());
    if (trips == null || trips.isEmpty()) {
        System.out.println("There were no trips found for " + client.getName());
        return;
    }

    System.out.println("Trips for client " + client.getName());
    int idx = 1;
    for (Trip t : trips) {
        Itinerary it = t.getItinerary();
        int legs = it.getLegs().size();
        int transfers = Math.max(0, legs - 1);
        int durationMin = it.getTotalDurationMinutes();

        // Reservations for this trip
        Collection<Reservation> resv = reservationRepo.findByTripId(t.getTripId());

        // Tickets for this trip
        Collection<Ticket> allTickets = ticketRepo.findAll();
        List<Ticket> tripTickets = allTickets.stream()
                .filter(k -> t.getTripId().equals(k.getTripId()))
                .toList();

        // Header
        System.out.println("\n--------------------------------------------------");
        System.out.println("#" + (idx++) + "  Trip " + t.getTripId());
        System.out.println("Date         : " + t.getTripDate());
        System.out.println("Status       : " + t.getStatus());
        System.out.println("Fare class   : " + t.getFareClass());
        System.out.println("Passengers   : " + t.getPassengerCount());

        // Route summary
        String origin = legs == 0 ? "?" : it.getLegs().get(0).getRoute().getDepartureCity();
        String dest   = legs == 0 ? "?" : it.getLegs().get(legs - 1).getRoute().getArrivalCity();
        System.out.println("Route        : " + origin + " → " + dest + "  (" + legs + " legs)");

        // Totals
        System.out.println("Totals       : duration " + durationMin + " min, transfers " + transfers
                + ", total price €" + t.totalPrice());

        // Reservations list
        System.out.println("Reservations : " + (resv == null ? 0 : resv.size()));
        if (resv != null && !resv.isEmpty()) {
            for (Reservation r : resv) {
                System.out.println("  - " + r.getPassengerName()
                        + " (age " + r.getPassengerAge()
                        + ", id " + r.getPassengerIdNumber()
                        + ", class " + r.getFareClass()
                        + ", confirmed=" + r.isConfirmed() + ")");
            }
        }

        // Tickets list
        System.out.println("Tickets      : " + (tripTickets == null ? 0 : tripTickets.size()));
        if (tripTickets != null && !tripTickets.isEmpty()) {
            for (Ticket tk : tripTickets) {
                System.out.println("  - " + tk.getPassengerName()
                        + " (" + tk.getFareClass()
                        + ", total €" + tk.getTotalPriceCents()
                        + ", issued " + tk.getIssuedAt() + ")");
            }
        }
    }
}
}