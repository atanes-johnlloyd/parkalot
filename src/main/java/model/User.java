package model;

public class User {
    private String fullName;
    private String username;
    private String email;
    private String contact;
    private String password;
    private String position;
    private String status;
    
    public User(String fullName, String username, String email, String contact, String password,String position, String status){
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.contact = contact;
        this.password = password;
        this.position = position;
        this.status = status;
    }
    
    public void setFullName(String fullName){
        this.fullName = fullName;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public void setContact(String contact){
        this.contact = contact;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public void setPosition(String position){
        this.position = position;
    }
    
    public void setStatus(String status){
        this.status = status;
    }
    
    public String getFullName(){
        return this.fullName;
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public String getEmail(){
        return this.email;
    }
    
    public String getContact(){
        return this.contact;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public String getPosition(){
        return this.position;
    }
    
    public String getStatus(){
        return this.status;
    }
    
}
