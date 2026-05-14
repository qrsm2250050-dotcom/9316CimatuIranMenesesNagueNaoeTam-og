package prog2.finalgroup;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class MyProgram {
    public static void main(String[] args) throws Exception {
        List<String[]> rawRows = CSVReader.readRaw("data.csv");

        String[] headers = {"Last Name", "First Name", "Email", "Address", "Age", "Residency", "Gender"};

        String[][] data = new String[rawRows.size()][headers.length];

        for (int i = 0; i < rawRows.size(); i++) {
            String[] r = rawRows.get(i);
            data[i][0] = r.length > 0 ? r[0].trim() : ""; // Last Name
            data[i][1] = r.length > 1 ? r[1].trim() : ""; // First Name
            data[i][2] = r.length > 2 ? r[2].trim() : ""; // Email
            data[i][3] = r.length > 3 ? r[3].trim() : ""; // Address
            data[i][4] = r.length > 4 ? r[4].trim() : ""; // Age
            data[i][5] = r.length > 5 ? r[5].trim() : ""; // Residency
            data[i][6] = r.length > 7 ? r[7].trim() : ""; // Gender (skip index 6)
        }

        // Print headers
        System.out.println(String.join(" | ", headers));
        System.out.println("-".repeat(80));

        // Print each row
        for (String[] row : data) {
            System.out.println(String.join(" | ", row));
        }
    }
}



class CSVReader {
    public static List<String[]> readRaw(String filename) throws Exception {
        List<String[]> rows = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] values = line.split(",", -1);
            rows.add(values);
        }
        br.close();

        return rows;
    }
}