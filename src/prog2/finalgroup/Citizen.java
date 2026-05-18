package prog2.finalgroup;

public class Citizen implements Comparable<Citizen> {
    private String fullName;
    private String email;
    private String address;
    private int age;
    private boolean resident;
    private int district;
    private char gender;

    public Citizen() {
        fullName = "";
        email = "";
        address = "";
        age = 0;
        resident = true;
        district = 1;
        gender = 'M';
    }

    public Citizen(String fullName, String email, String address, int age,
                   boolean resident, int district, char gender) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.age = age;
        this.resident = resident;
        this.district = district;
        this.gender = gender;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isResident() {
        return resident;
    }

    public void setResident(boolean resident) {
        this.resident = resident;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public String getResidencyLabel() {
        return resident ? "Resident" : "Non-Resident";
    }

    public String getGenderLabel() {
        return gender == 'M' ? "Male" : "Female";
    }

    @Override
    public int compareTo(Citizen other) {
        return this.fullName.compareToIgnoreCase(other.fullName);
    }

    @Override
    public String toString() {
        return fullName + " (" + getGenderLabel() + ", age " + age + ", District " + district + ")";
    }
}
