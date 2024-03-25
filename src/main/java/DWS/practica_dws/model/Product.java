package DWS.practica_dws.model;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Product {
    //Each product have an ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;


    private int numProductsInCarts;
    private double price;
    private String name;
    @Lob
    private String description;
    //If the Product is being deleted, all their comments are deleted too
    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments;
    @ManyToMany
    private List<Person> inUsersShoppingCart;
    public boolean containsPhoto;


    public Product() {
    }

    public Product(String name, String description, double price){
        this.name = name;
        this.description = description;
        this.price = price;
        this.numProductsInCarts = 0;
        this.comments = new ArrayList<>();
        this.inUsersShoppingCart = new ArrayList<>();
        this.containsPhoto = false;
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
        return price;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getNumProductsInCarts() {
        return numProductsInCarts;
    }

    public void addComment(Comment c){
        this.comments.add(c);
    }

    public Collection<Comment> getComments(){
        return this.comments;
    }

    public void removeComment(long CID){
        this.comments.remove((int) (CID-1));
    }

/*
    public List<User> getInUsersShoppingCart() {
        return inUsersShoppingCart;
    }
*/
    public void updateInfo(String name, String description, double price){
        if(name!=null){
            this.name = name;
        }if(price!=0){
            this.price = price;
        }if(description!= null){
            if(description.isEmpty()){
                this.description = "Producto sin descripción";
            }else{
                this.description = description;
            }
        }
     }

     /*
    public void removeUsers(User u){
        this.inUsersShoppingCart.remove(u);
        this.numProductsInCarts--;
    }
*/
    public void addUsers(Person u){
        this.inUsersShoppingCart.add(u);
        this.numProductsInCarts++;
    }

    public void setPhoto(boolean hasPhoto){
        this.containsPhoto = hasPhoto;
    }
}
