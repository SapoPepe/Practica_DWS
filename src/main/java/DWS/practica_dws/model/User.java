package DWS.practica_dws.model;

import java.util.ArrayList;

public class User {
    private ArrayList<Product> userProducts = new ArrayList<>();

    public void followProduct(Product p){
        this.userProducts.add(p);
    }

}
