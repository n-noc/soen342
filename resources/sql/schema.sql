
-- table of clients
CREATE TABLE IF NOT EXISTS clients(
    client_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone TEXT
);

-- table of trips 
CREATE TABLE IF NOT EXISTS trips(
    trip_id TEXT PRIMARY KEY,
    client_id TEXT NOT NULL,    --foreign key
    trip_date TEXT NOT NULL,    -- ISO yyyy-mm-dd
    fare_class TEXT NOT NULL.   --FIRST or SECOND
    passenger_count INT NOT NULL,
    status TEXT NOT NULL,  --BOOKED, CANCELLED, COMPLETED
    orign_city TEXT NOT NULL,
    destination_city TEXT NOT NULL,
    total_minutes INT,
    first_total_euro INT,
    second_total_euro INT,
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE

);

-- tale of reservations
CREATE TABLE IF NOT EXISTS reservations(
    reservation_id TEXT PRIMARY KEY,
    trip_id TEXT NOT NULL,   --foreign key
    client_id TEXT NOT NULL,  --foreign key
    passenger_name TEXT NOT NULL,
    passenger_age INT NOT NULL,
    passenger_id_number TEXT NOT NULL,
    fare_class TEXT NOT NULL,  --FIRST or SECOND
    confirmed INT NOT NULL DEFAULT 0,  -- 0 or 1
    FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE
);

-- table with tickets
CREATE TABLE IF NOT EXISTS tickets(
    ticket_id TEXT PRIMARY KEY,
    reservation_id TEXT NOT NULL UNIQUE,  --foreign key
    client_id TEXT NOT NULL,       --foreign key
    trip_id TEXT NOT NULL,        --foreign key
    passenger_name TEXT NOT NULL,
    fare_class TEXT NOT NULL,  --FIRST or SECOND
    total_price INT NOT NULL,
    issued_date TEXT NOT NULL,  -- ISO yyyy-mm-dd
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE,
    FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_trips_client ON trips(client_id);
CREATE INDEX IF NOT EXISTS idx_reservations_trip ON reservations(trip_id);
CREATE INDEX IF NOT EXISTS idx_tickets_trip ON tickets(trip_id);