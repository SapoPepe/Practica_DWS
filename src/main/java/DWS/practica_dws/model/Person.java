package DWS.practica_dws.model;

import java.util.HashSet;
import java.util.List;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;


@Entity
//@Table(name = "users") //Changes the name of the table to "users" due to an error in SQL if the table names "user"
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String personName;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();
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
        for(int i = this.userProducts.size()-1; i >= 0; i--){
            Product aux = this.userProducts.get(i);
            if(p.equals(aux)) this.userProducts.remove(i);
        }
    }

    public Collection<Product> cartProducts(){
        return this.userProducts;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }

    public void deleteComment(Comment c){
        this.comments.remove(c);
    }
    public Long getID() {
        return id;
    }
}