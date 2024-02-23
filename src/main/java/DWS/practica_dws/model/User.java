package DWS.practica_dws.model;

import DWS.practica_dws.model.Product;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;

@Component
@SessionScope //Makes an instance of each componet for each user
public class User {
    private String name;
    private ArrayList<Product> userProducts;

    public void createUser(String name){
        this.name = name;
        this.userProducts = new ArrayList<Product>();
    }

    public void followProduct(Product p){
        this.userProducts.add(p);
    }

}
