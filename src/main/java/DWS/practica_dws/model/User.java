package DWS.practica_dws.model;

import java.util.ArrayList;
import java.util.Collection;

public class User {
    private String name = "Pepe";
    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<Product> userProducts = new ArrayList<>();

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void followProduct(Product p){
        this.userProducts.add(p);
    }

    public void unfollowProduct(Product p){
        this.userProducts.remove(p);
    }

    public Collection<Product> cartProducts(){
        return this.userProducts;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }

}