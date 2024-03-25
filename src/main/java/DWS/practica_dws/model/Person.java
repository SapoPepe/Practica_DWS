package DWS.practica_dws.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity
//@Table(name = "users") //Changes the name of the table to "users" due to an error in SQL if the table names "user"
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String personName;
    //private ArrayList<Comment> comments = new ArrayList<>();
    @ManyToMany
    private List<Product> userProducts = new ArrayList<>();

    public Person() {
    }

    public Person(String name){
        this.personName = name;
    }


    public String getName() {
        return personName;
    }
    public void setName(String name) {
        this.personName = name;
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
/*
    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

 */
}