package model;

public class User {

	  private int id;
	    private String name;
	    private String email;
	    private String password;

	    // Constructor for inserting new user (without ID)
	    public User(String name, String email, String password) {
	        this.name = name;
	        this.email = email;
	        this.password = password;
	    }

	    // Constructor with ID (e.g., from DB)
	    public User(int id, String name, String email, String password) {
	        this.id = id;
	        this.name = name;
	        this.email = email;
	        this.password = password;
	    }

	    // Getters and Setters (required for data access)
	    public int getId() { return id; }
	    public void setId(int id) { this.id = id; }

	    public String getName() { return name; }
	    public void setName(String name) { this.name = name; }

	    public String getEmail() { return email; }
	    public void setEmail(String email) { this.email = email; }

	    public String getPassword() { return password; }
	    public void setPassword(String password) { this.password = password; }
}
