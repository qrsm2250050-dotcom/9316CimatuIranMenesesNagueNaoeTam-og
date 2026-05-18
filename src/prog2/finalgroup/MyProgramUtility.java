package prog2.finalgroup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MyProgramUtility {
    private static final String FILE_PATH = "res/data.csv";

    public static List<Citizen> loadCitizens() throws IOException {
        List<Citizen> citizens = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] values = parseCsvLine(line);
                if (values.length < 8) {
                    continue;
                }
                citizens.add(buildCitizen(values));
            }
        }
        return citizens;
    }

    private static Citizen buildCitizen(String[] values) {
        String lastName = values[0].trim();
        String firstName = values[1].trim();
        String fullName = firstName + " " + lastName;
        String email = values[2].trim();
        String address = values[3].trim();
        int age = Integer.parseInt(values[4].trim());

        boolean resident = values[5].trim().equalsIgnoreCase("Resident");
        int district = Integer.parseInt(values[6].trim());

        String genderText = values[7].trim();
        char gender = genderText.equalsIgnoreCase("Male") ? 'M' : 'F';

        return new Citizen(fullName, email, address, age, resident, district, gender);
    }

    private static String[] parseCsvLine(String line) {
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

    public static int[] countByGender(List<Citizen> citizens) {
        int males = 0;
        int females = 0;
        for (Citizen c : citizens) {
            if (c.getGender() == 'M') {
                males++;
            } else {
                females++;
            }
        }
        return new int[]{males, females};
    }

    public static Map<Integer, Integer> countResidentsPerDistrict(List<Citizen> citizens) {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        for (Citizen c : citizens) {
            if (!c.isResident()) {
                continue;
            }
            counts.merge(c.getDistrict(), 1, Integer::sum);
        }
        return counts;
    }

    public static List<Citizen> getSeniorCitizens(List<Citizen> citizens) {
        List<Citizen> seniors = new ArrayList<>();
        for (Citizen c : citizens) {
            if (c.getAge() >= 60) {
                seniors.add(c);
            }
        }
        return seniors;
    }

    public static double computeAverageAge(List<Citizen> citizens) {
        if (citizens.isEmpty()) {
            return 0;
        }
        int totalAge = 0;
        for (Citizen c : citizens) {
            totalAge += c.getAge();
        }
        return (double) totalAge / citizens.size();
    }
    public static List<Citizen> sortByFullName(List<Citizen> citizens) {
        List<Citizen> sorted = new ArrayList<>(citizens);
        Collections.sort(sorted);
        return sorted;
    }
    public static int[] countByResidency(List<Citizen> citizens) {
        int residents = 0;
        int nonResidents = 0;
        for (Citizen c : citizens) {
            if (c.isResident()) {
                residents++;
            } else {
                nonResidents++;
            }
        }
        return new int[]{residents, nonResidents};
    }

    public static String formatGenderReport(List<Citizen> citizens) {
        int[] counts = countByGender(citizens);
        return "Male citizens: " + counts[0] + "\nFemale citizens: " + counts[1]
                + "\nTotal: " + citizens.size();
    }

    public static String formatDistrictReport(List<Citizen> citizens) {
        Map<Integer, Integer> perDistrict = countResidentsPerDistrict(citizens);
        StringBuilder sb = new StringBuilder("Residents per district:\n");
        for (Map.Entry<Integer, Integer> entry : perDistrict.entrySet()) {
            sb.append("District ").append(entry.getKey())
                    .append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public static String formatSeniorReport(List<Citizen> citizens) {
        List<Citizen> seniors = getSeniorCitizens(citizens);
        StringBuilder sb = new StringBuilder("Senior citizens (age 60+): ")
                .append(seniors.size()).append("\n\n");
        int limit = Math.min(seniors.size(), 25);
        for (int i = 0; i < limit; i++) {
            sb.append(seniors.get(i).toString()).append("\n");
        }
        if (seniors.size() > limit) {
            sb.append("... and ").append(seniors.size() - limit).append(" more.");
        }
        return sb.toString();
    }

    public static String formatAverageAgeReport(List<Citizen> citizens) {
        return String.format("Average age of all citizens: %.2f years", computeAverageAge(citizens));
    }

    public static String formatSortedNamesReport(List<Citizen> citizens) {
        List<Citizen> sorted = sortByFullName(citizens);
        StringBuilder sb = new StringBuilder("Citizens sorted by name (A-Z):\n\n");
        int limit = Math.min(sorted.size(), 30);
        for (int i = 0; i < limit; i++) {
            sb.append(sorted.get(i).getFullName()).append("\n");
        }
        if (sorted.size() > limit) {
            sb.append("... and ").append(sorted.size() - limit).append(" more.");
        }
        return sb.toString();
    }

    public static String formatResidencyReport(List<Citizen> citizens) {
        int[] counts = countByResidency(citizens);
        return "Residents: " + counts[0] + "\nNon-Residents: " + counts[1]
                + "\nTotal: " + citizens.size();
    }
}
