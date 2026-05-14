

package prog2.finalgroup;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class MyProgram {
    public static void main(String[] args) {
    }
}


class Program extends JFrame {

}


class CSVReader {
    public static void main(String[] args) throws Exception {
        List<String[]> rows = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader("data.csv"));
        String line;

        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            rows.add(values);
        }
        br.close();

        // Access like a 2D array
        for (String[] row : rows) {
            System.out.println(Arrays.toString(row));
        }
    }
}


