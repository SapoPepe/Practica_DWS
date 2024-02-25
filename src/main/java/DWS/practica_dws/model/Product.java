package DWS.practica_dws.model;

import java.util.*;

public class Product {
    //Each product have an ID
    private long id;
    private double prize;
    private String description;
    private String name;
    private List<Comment> comments;


    public Product(String name, String description, double prize){
        this.name = name;
        this.description = description;
        this.prize = prize;
        this.comments = new ArrayList<>();

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
        this.comments.add(c);
    }

    public Collection<Comment> getComments(){
        return this.comments;
    }

    public void updateInfo(String name, String description, double prize){
        this.name = name;
        this.description = description;
        this.prize = prize;
    }
}
