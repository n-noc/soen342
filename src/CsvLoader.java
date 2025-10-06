import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import domain.TrainType;

public class CsvLoader {

    public static List<TrainConnection> load(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        if (lines.isEmpty()) return Collections.emptyList();

        //splitting csv headers into separate cells
        List<String> headerCells = splitCsvLine(lines.getFirst());
        Map<String, Integer> idx = buildHeaderIndex(headerCells); //lookup map from header name

        //list that stores each train connection object starting from row 2
        List<TrainConnection> results = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) continue;

            List<String> cells = splitCsvLine(line); //splitting rows again into cells

            try {
                TrainConnection tc = parseRow(cells, idx);
                if (tc != null) {
                    results.add(tc);
                }
            } catch (Exception ex) {
                System.out.println("Error parsing row: " + line);
            }
        }
        return results;
    }

    private static TrainConnection parseRow(List<String> cells, Map<String, Integer> idx) {
        String routeId = get(cells, idx, "Route ID");
        String departureCity = get(cells, idx, "Departure City");
        String arrivalCity = get(cells, idx, "Arrival City");

        LocalTime departureTime = parseTime(get(cells, idx, "Departure Time"));
        String arrivalRaw = get(cells, idx, "Arrival Time");
        int arrivalDayOffset = arrivalRaw != null && arrivalRaw.contains("(+1d)") ? 1 : 0;

        String arrivalClean = arrivalRaw;
        if (arrivalClean != null) {
            int paren = arrivalClean.indexOf('('); // strips " (+1d)" or anything after '('
            if (paren >= 0) arrivalClean = arrivalClean.substring(0, paren).trim();
        }
        LocalTime arrivalTime = parseTime(arrivalClean);


        //TrainType uses its own method that recognizes strings and matches them with their enum
        TrainType trainType = TrainType.fromString(get(cells, idx, "Train Type"));
        Set<DayOfWeek> days = parseDays(get(cells, idx, "Days of Operation"));

        int firstClass = parseEuroInt(get(cells, idx, "First Class ticket rate (in euro)"));
        int secondClass = parseEuroInt(get(cells, idx, "Second Class ticket rate (in euro)"));

        //new train connection object is created with listed characteristics and trip duration is internally computed inside constructor
        return new TrainConnection(
                routeId,
                trimOrNull(departureCity),
                trimOrNull(arrivalCity),
                departureTime,
                arrivalTime,
                trainType,
                days,
                firstClass,
                secondClass,
                arrivalDayOffset
        );
    }

    //extract header string names from list and add them to map along with their indexes
    private static Map<String, Integer> buildHeaderIndex(List<String> headerCells) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerCells.size(); i++) {
            map.put(headerCells.get(i).trim(), i);
        }
        return map;
    }

    private static String get(List<String> cells, Map<String, Integer> idx, String header) {
        Integer i = idx.get(header); //get key value from map
        if (i == null || i < 0 || i >= cells.size()) {
            return "";
        }
        return cells.get(i); //get the value in cell for the same index as map obtained from header string
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static LocalTime parseTime(String raw) {
        if (raw == null) throw new IllegalArgumentException("Time is null");
        String t = raw.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Time is empty");
        return LocalTime.parse(t); //string time into Local Time
    }

    private static int parseEuroInt(String raw) {
        if (raw == null) throw new IllegalArgumentException("Price is null");
        String t = raw.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Price is empty");
        //prices appear as whole euros
        return Integer.parseInt(t);
    }

    private static Set<DayOfWeek> parseDays(String raw) {
        if (raw == null) return EnumSet.noneOf(DayOfWeek.class);
        String s = raw.trim();
        if (s.isEmpty()) return EnumSet.noneOf(DayOfWeek.class);

        if (equalsIgnoreCase(s, "Daily")) {
            return EnumSet.allOf(DayOfWeek.class);
        }

        if (equalsIgnoreCase(s, "Sat-Sun")) {
            return EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        }

        //For ranges like "Fri-Sun"
        if (s.contains("-") && !s.contains(",")) {
            String[] parts = s.split("-");
            if (parts.length == 2) {
                DayOfWeek start = parseSingleDay(parts[0]);
                DayOfWeek end = parseSingleDay(parts[1]);
                if (start != null && end != null) {
                    EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
                    int cur = start.getValue();
                    set.add(start);
                    while (cur != end.getValue()) {
                        cur = (cur % 7) + 1; // 6 % 7 + 1 = 7 example
                        set.add(DayOfWeek.of(cur));
                    }
                    return set;
                }
            }
        }

        //comma seperated
        EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
        for (String token : s.split(",")) {
            DayOfWeek d = parseSingleDay(token);
            if (d != null) set.add(d);
        }
        return set;
    }

    private static DayOfWeek parseSingleDay(String token) {
        if (token == null) return null;
        String t = token.trim().toLowerCase(Locale.ROOT);
        return switch (t) {
            case "mon", "monday" -> DayOfWeek.MONDAY;
            case "tue", "tues", "tuesday" -> DayOfWeek.TUESDAY;
            case "wed", "weds", "wednesday" -> DayOfWeek.WEDNESDAY;
            case "thu", "thur", "thurs", "thursday" -> DayOfWeek.THURSDAY;
            case "fri", "friday" -> DayOfWeek.FRIDAY;
            case "sat", "saturday" -> DayOfWeek.SATURDAY;
            case "sun", "sunday" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }

    private static List<String> splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) return out; //empty list

        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {  //csv file sees -> "mon,tue,wed" so this should be added into one cell all together
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++; //skip next char since its known it's a quotation mark and it has already been appended
                } else {
                    inQuotes = !inQuotes; //toggles once we hit a quotation mark
                }
            } else if (c == ',' && !inQuotes) {
                out.add(cell.toString()); //added to out to make space for next cell info
                cell.setLength(0); //reset builder for next cell
            } else {
                cell.append(c);
            }
        }
        out.add(cell.toString());
        return out;
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a.equalsIgnoreCase(b);
    }
}
