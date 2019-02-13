package centennial.ca.helpstack.Model;

public class User {

    private String id;
    private String centennial_id;
    private String firstname;
    private String lastname;
    private String role;
    private String email;
    private String department;
    private String phoneNo;
    private String ques;

    public User(String id, String centennial_id, String firstname, String lastname, String email, String phoneNo, String role, String department, String ques) {
        this.id = id;
        this.centennial_id = centennial_id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
        this.email = email;
        this.department = department;
        this.phoneNo = phoneNo;
        this.ques = ques;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCentennial_id() {
        return centennial_id;
    }

    public void setCentennial_id(String centennial_id) {
        this.centennial_id = centennial_id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getQues() {
        return ques;
    }

    public void setQues(String ques) {
        this.ques = ques;
    }
}
