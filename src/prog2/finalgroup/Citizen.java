package prog2.finalgroup;
import java.lang.Comparable;
public class Citizen implements Comparable<Citizen> {
    private String fullName;
    private String email;
    private String address;
    private int age;
    private boolean resident;
    private int district;
    private char gender;
    private int id;

    public Citizen() {
        fullName = "";
        email = "";
        address = "";
        age = 0;
        resident = true;
        district = 1;
        gender = 'M';
        id = 0;
    }

    public Citizen(String fullName, String email, String address, int age, boolean resident, int district, char gender, int id) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.age = age;
        this.resident = resident;
        this.district = district;
        this.gender = gender;
        this.id = id;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getFullName() {
        return fullName;
    }
    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Citizen other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + fullName;
    }
}