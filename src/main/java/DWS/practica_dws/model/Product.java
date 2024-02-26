package DWS.practica_dws.model;

import DWS.practica_dws.service.UserSession;

import java.util.*;

public class Product {
    //Each product have an ID
    private long id;
    private int numProductsInCarts;
    private double prize;
    private String description;
    private String name;
    private List<Comment> comments;
    private List<User> inUsersShoppingCart;


    public Product(String name, String description, double prize){
        this.name = name;
        this.description = description;
        this.prize = prize;
        this.numProductsInCarts = 0;
        this.comments = new ArrayList<>();
        this.inUsersShoppingCart = new ArrayList<>();
    }

    public void setID(Long l){
        this.id = l;
    }

    public long getId() {
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public double getPrice() {
        return prize;
    }
    public String getDescription() {
        return description;
    }

    public void addComment(Comment c){
        c.setID(this.comments.size());
        this.comments.add(c);
    }

    public void removeComment(int CID){
        this.comments.remove(CID);
    }

    public Collection<Comment> getComments(){
        return this.comments;
    }

    public void updateInfo(String name, String description, double prize){
        this.name = name;
        this.description = description;
        this.prize = prize;
    }

    public void removeUsers(User u){
        this.inUsersShoppingCart.remove(u);
        this.numProductsInCarts--;
    }

    public void addUsers(User u){
        this.inUsersShoppingCart.add(u);
        this.numProductsInCarts++;
    }
}
