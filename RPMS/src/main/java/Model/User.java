package Model;

public abstract class User {
    private String userId;
    private String name;
    private String email;
    private String contactNumber;
    private String passwordHash;


    public User(String userId,String name,String email,String contactNumber,String password,boolean isHash) {
        if(userId==null||name==null||!email.contains("@")||password==null||contactNumber==null){
            throw new IllegalArgumentException(" User fields can not be null!");
        }
        this.email = email;
        this.name = name;
        this.userId = userId;
        this.passwordHash = isHash?password:hashPassword(password);
        this.contactNumber=contactNumber;
    }
    private String hashPassword(String password){
        return Integer.toString(password.hashCode());
    }
    // common method
    public abstract  void login();
    public  abstract  void logOut();
    // getter methods

    public String getContactNumber() {return contactNumber;}
    public String getUserId(){return  userId;}
    public String getName(){return  name;}
    public String getEmail(){return  email;}
    public boolean checkPassword(String password ){
        return hashPassword(password).equals(passwordHash);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // setter method
    public void setName(String name){this.name=name;}
    public void setUserId(String userId){this.userId=userId;}
    public  void setEmail(String email){this.email=email;}
    public void  setPassword(String password){this.passwordHash=hashPassword(password);}
}
