package prog2.finalgroup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyProgramUtility {

    private static final String DEFAULT_PATH = "res/data.csv";

    // ── Public API ────────────────────────────────────────────────────────────

    public static List<Citizen> loadCitizens() throws Exception {
        return loadCitizens(DEFAULT_PATH);
    }

    static List<Citizen> loadCitizens(String filePath) throws Exception {
        List<Citizen> citizens = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int id = 1;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = parseCSVLine(line);

                // Trim every field
                for (int i = 0; i < values.length; i++) values[i] = values[i].trim();

                // Need at least 8 columns (indices 0-7)
                if (values.length < 8) continue;

                try {
                    String firstName = values[0];
                    String lastName  = values[1];
                    String email     = values[2];
                    String address   = values[3];
                    int    age       = Integer.parseInt(values[4]);
                    boolean resident = values[5].equalsIgnoreCase("Resident");
                    int    district  = Integer.parseInt(values[6]);
                    char   gender    = values[7].isEmpty() ? 'M' : Character.toUpperCase(values[7].charAt(0));

                    citizens.add(new Citizen(
                            firstName + " " + lastName,
                            email, address, age, resident, district, gender, id++
                    ));
                } catch (NumberFormatException e) {
                    // Skip rows with unparseable numbers (e.g. header row)
                }
            }
        }

        return citizens;
    }

    public static String[] citizenToRow(Citizen c) {
        return new String[]{
                c.getFirstName(),
                c.getLastName(),
                c.getEmail(),
                c.getAddress(),
                String.valueOf(c.getAge()),
                c.isResident() ? "Resident" : "Non-Resident",
                String.valueOf(c.getDistrict()),
                c.getGender() == 'F' ? "Female" : "Male"
        };
    }

    public static List<String[]> citizensToRows(List<Citizen> citizens) {
        List<String[]> rows = new ArrayList<>();
        for (Citizen c : citizens) rows.add(citizenToRow(c));
        return rows;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString());
        return tokens.toArray(new String[0]);
    }
}