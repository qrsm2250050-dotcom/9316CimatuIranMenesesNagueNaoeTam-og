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

    public static void separateValues() {
        try {
            lineCount = Files.lines(Paths.get("data.csv")).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<Citizen> citizens = new ArrayList<>();
            for (int i = 1; i <= lineCount; i++) {
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    firstName = values[1];
                    lastName = values[2];
                    email = values[3];
                    address = values[4];
                    age = Integer.parseInt(values[5]);

                    if (values[6].equalsIgnoreCase("Resident")) {
                        resident = true;
                    } else if (values[6].equalsIgnoreCase("Non-Resident")) {
                        resident = false;
                    }

                    district = Integer.parseInt(values[7]);

                    gender = values[8].charAt(0);

                    id = i;

                    citizens.add(new Citizen(firstName + " " + lastName, email, address, age, resident, district, gender, id));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
