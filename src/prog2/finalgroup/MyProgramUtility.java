package prog2.finalgroup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class MyProgramUtility {
    private static String filePath = "res/data.csv";
    private static String line = "";
    private static String firstName;
    private static String lastName;
    private static String email;
    private static String address;
    private static int age;
    private static boolean resident;
    private static int district;
    private static char gender;
    private static long lineCount;
    private static int id;
    public static List<Citizen> citizens = new ArrayList<>();

    public static List<Citizen> separateValues() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].replace("\"", "").trim();
                }
                String firstName = values[0].trim();
                String lastName = values[1].trim();
                String email = values[2].trim();
                String address = values[3].trim();
                int age = Integer.parseInt(values[4].trim());
                boolean resident = values[5].trim().equalsIgnoreCase("Resident");
                int district = Integer.parseInt(values[6].trim());
                char gender = values[7].trim().charAt(0);
                int id = citizens.size() + 1;

                Citizen citizen = new Citizen(firstName + " " + lastName, email, address, age, resident, district, gender, id);

                citizens.add(citizen);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return citizens;
    }

    class CSVReader {
        public static List<String[]> readCSV(String filename) throws Exception {
            List<String[]> rows = new ArrayList<>();

            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = parseCSVLine(line);

                // Trim whitespace from each field
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }

                if (values.length >= 8) {
                    rows.add(values);
                }
            }
            br.close();
            return rows;
        }

        // Parses a CSV line respecting quoted fields
        // e.g. Colleen,Joyner,"Ap #697, Nullam Road",30 → 4 tokens, not 5
        private static String[] parseCSVLine(String line) {
            List<String> tokens = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean inQuotes = false;

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);

                if (c == '"') {
                    inQuotes = !inQuotes; // toggle quote mode, don't add the quote char
                } else if (c == ',' && !inQuotes) {
                    tokens.add(current.toString()); // comma outside quotes = new field
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            tokens.add(current.toString()); // add last field

            return tokens.toArray(new String[0]);
        }
    }
}
