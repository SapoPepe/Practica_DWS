package DWS.practica_dws.service;

import DWS.practica_dws.model.Person;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.repository.PersonRepository;
import DWS.practica_dws.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Optional;

@Service
//@SessionScope //Makes an instance of each componet for each user
public class PersonSession {

    @Autowired
    private PersonRepository persons;
    @Autowired
    private ProductRepository products;

    @PostConstruct
    public void init(){
        Person u = new Person("Pepe");
        this.persons.save(u);
    }


    public void follow(Optional<Product> o){
        o.ifPresent(p -> {
            Person u = this.persons.findAll().get(0);
            u.followProduct(p);
            this.persons.save(u);
            p.addUsers(u);
            this.products.save(p);
        });
    }
/*
    public void unfollow(Product productToRemove){
        Collection<Product> userProducts = this.user.cartProducts();

        if (productToRemove != null && userProducts.contains(productToRemove)) {
            userProducts.remove(productToRemove);
            this.user.unfollowProduct(productToRemove);
            productToRemove.removeUsers(this.user);
        }
    }

    public Collection<Product> userProducts(){
        return this.user.cartProducts();
    }

    public Person getUser(){
        return this.user;
    }

*/

}
