package DWS.practica_dws.model;

import java.awt.*;

public class Product {
    private long id;
    private double prize;
    private String description;
    private String name;
    private Image image;    //Implement the img for the product

    public Product(String name, String description, double prize){
        this.name = name;
        this.description = description;
        this.prize = prize;
    }

    public void setID(Long l){
        this.id = l;
    }

    public String getName(){
        return this.name;
    }

}
