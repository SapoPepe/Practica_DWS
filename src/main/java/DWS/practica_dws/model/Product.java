package DWS.practica_dws.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.sql.Blob;
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
    private String type;
    private String fileName;
    private boolean containsFile;
    @Lob
    private String description;
    //If the Product is being deleted, all their comments are deleted too
    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments;
    @ManyToMany
    private List<Person> inUsersShoppingCart;

    private String imageLocation;
    @Lob
    @JsonIgnore
    private Blob image;
    private boolean containsPhoto;



    public Product() {
    }

    public Product(String name, String description, double price, String type, String fileName){
        this.name = name;
        this.description = description;
        this.price = price;
        this.numProductsInCarts = 0;
        this.comments = new ArrayList<>();
        this.inUsersShoppingCart = new ArrayList<>();
        this.containsPhoto = false;
        this.imageLocation = null;
        this.image = null;
        this.type = type;
        this.fileName = fileName;
        this.containsFile = false;
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

    public List<Comment> getComments(){
        return this.comments;
    }

    public void removeComment(Comment c){
        this.comments.remove(c);
    }

    public void removeAllComments(){
        this.comments.clear();
    }

    public Blob getImageFile(){
        return this.image;
    }

    public String getType(){
        return this.type;
    }


    public List<Person> getInUsersShoppingCart() {
        return inUsersShoppingCart;
    }

    public void updateInfo(String name, String description, double price, String type){
        if(name!=null) this.name = name;
        if(price!=0) this.price = price;
        if(type!=null && !type.isEmpty()) this.type = type;
        if(description!= null){
            if(description.isEmpty()){
                this.description = "Producto sin descripción";
            }else{
                this.description = description;
            }
        }
     }


     public void setImageLocation(String loc){
        this.imageLocation =  loc;
     }

     public void setImageFile(Blob img){
        this.image = img;
     }

    public void removePerson(Person u){
        this.inUsersShoppingCart.remove(u);
        this.numProductsInCarts--;
    }

    public void addUsers(Person u){
        this.inUsersShoppingCart.add(u);
        this.numProductsInCarts++;
    }

    public void setPhoto(boolean hasPhoto){
        this.containsPhoto = hasPhoto;
    }

    public boolean hasImage(){
        return this.containsPhoto;
    }

    public void setFile(boolean hasFile, String fileName){
        this.containsFile = hasFile;
        this.fileName = fileName;
    }

    public boolean hasFile(){
        return this.containsFile;
    }

    public String getFileName(){
        return this.fileName;
    }
}
