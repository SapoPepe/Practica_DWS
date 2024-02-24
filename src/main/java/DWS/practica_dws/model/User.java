package DWS.practica_dws.model;

import java.util.ArrayList;
import java.util.Collection;

public class User {
    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<Product> userProducts = new ArrayList<>();

    public void followProduct(Product p){
        this.userProducts.add(p);
    }

    public Collection<Product> cartProducts(){
        return this.userProducts;
    }

}
