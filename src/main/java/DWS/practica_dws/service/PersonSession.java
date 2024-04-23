package DWS.practica_dws.service;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Person;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.repository.CommentRepository;
import DWS.practica_dws.repository.PersonRepository;
import DWS.practica_dws.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
//@SessionScope //Makes an instance of each componet for each user
public class PersonSession {
    private Person person;
    @Autowired
    private PersonRepository persons;
    @Autowired
    private ProductsService productsService;

    /*@PostConstruct
    public void init(){
        this.person = new Person("Pepe");
        this.persons.save(this.person);
    }*/


    public void follow(Long identification, Principal principal){
        Optional<Product> o = this.productsService.getProduct(identification);
        Person per = this.persons.findByPersonName(principal.getName()).orElseThrow();


        if(o.isPresent()){
            Product p = o.get();
            per.followProduct(p);
            this.persons.save(per);
            p.addUsers(per);
            this.productsService.saveProduct(p);
        }
    }

    public void unfollow(Long id){
        Optional<Product> o = this.productsService.getProduct(id);
        Person per = this.persons.findById(Long.valueOf(1)).orElseThrow();

        if(o.isPresent()){
            Product p = o.get();
            p.removePerson(per);
            this.productsService.saveProduct(p);

            per.unfollowProduct(p);
            this.persons.save(per);
        }
    }

    public Collection<Product> personProducts(Principal principal){
        Person per = this.persons.findByPersonName(principal.getName()).orElseThrow();
        return per.cartProducts();
    }

    public Person getUser(){
        return this.person;
    }



    public void deleteProductFromCarts(Product p){
        List<Person> aux = this.persons.findAll();
        for(Person per : aux){
            per.unfollowProduct(p);
            this.persons.save(per);
        }
    }


    public void addComment(Comment c){
        Person per = this.persons.findById(Long.valueOf(1)).orElseThrow();
        per.addComment(c);
        this.persons.save(per);
        this.productsService.saveComment(c);
    }

    public void deleteComment(Long CID){
        Person per = this.persons.findById(Long.valueOf(1)).orElseThrow();
        Comment c = this.productsService.getComment(CID);
        per.deleteComment(c);
        this.persons.save(per);
        this.productsService.deleteComment(CID);
    }

    public void deleteCommentsFromUsers(List<Comment> comments){
        List<Person> aux = this.persons.findAll();
        for(Person per : aux){
            for (Comment c : comments){
                per.deleteComment(c);
            }
            this.persons.save(per);
        }
    }

    public boolean ownsComment(String name, long CID){
        Person p = this.persons.findByPersonName(name).orElseThrow();
        Comment c = this.productsService.getComment(CID);
        return p.hasComment(c) && c.hasPerson(name);
    }
}
