package DWS.practica_dws.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Product {
    private long id;
    private double prize;
    private String description;
    private String name;
    private Map<String, Comment> comments;


    public Product(String name, String description, double prize){
        this.name = name;
        this.description = description;
        this.prize = prize;
        this.comments = new HashMap<>();

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
        this.comments.put(c.getUser(), c);
    }

    public Collection<Comment> getComments(){
        return this.comments.values();
    }
}
