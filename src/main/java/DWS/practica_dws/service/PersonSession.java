package DWS.practica_dws.service;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Person;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.repository.CommentRepository;
import DWS.practica_dws.repository.PersonRepository;
import DWS.practica_dws.repository.ProductRepository;
import DWS.practica_dws.security.SecurityConfiguration;
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
    @Autowired
    private SecurityConfiguration securityConfiguration;

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

    public void unfollow(Long id, String userName){
        Optional<Product> o = this.productsService.getProduct(id);
        Person per = this.persons.findByPersonName(userName).orElseThrow();

        if(o.isPresent()){
            Product p = o.get();
            p.removePerson(per);
            this.productsService.saveProduct(p);

            per.unfollowProduct(p);
            this.persons.save(per);
        }
    }

    public void unfollow(Product p, Person per){
        p.removePerson(per);
        this.productsService.saveProduct(p);
        per.unfollowProduct(p);
        this.persons.save(per);
    }

    public Collection<Product> personProducts(Principal principal){
        Person per = this.persons.findByPersonName(principal.getName()).orElseThrow();
        return per.cartProducts();
    }

    public Person getUser(String userName){
        return this.persons.findByPersonName(userName).orElseThrow();
    }

    public List<Person> getUsers(){
        return this.persons.findAll();
    }

    public void deleteProductFromCarts(Product p){
        List<Person> aux = this.persons.findAll();
        for(Person per : aux){
            per.deleteProductFromCart(p);
            this.persons.save(per);
        }
    }


    public void addComment(Comment c, String personName){
        Person per = this.persons.findByPersonName(personName).orElseThrow();
        per.addComment(c);
        this.persons.save(per);
        this.productsService.saveComment(c);
    }

    public void deleteComment(Long CID, Person per){
        Comment c = this.productsService.getComment(CID);
        per.deleteComment(c);
        this.persons.save(per);
    }

    public void deleteComment(Long CID){
        Comment c = this.productsService.getComment(CID);
        Person per = this.persons.findByPersonName(c.getUserName()).orElseThrow();
        per.deleteComment(c);
        this.persons.save(per);
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

    public boolean exists(String username){
        Optional<Person> p = this.persons.findByPersonName(username);
        return p.isPresent();
    }

    public void newPerson(String username, String password){
        this.persons.save(new Person(username, this.securityConfiguration.passwordEncoder().encode(password), "USER"));
    }

    public void deletePerson(long id){
        Person p = this.persons.findById(id).orElseThrow();
        //Delete the comments of the user in the hall application
        this.productsService.deleteCommentsFromProductsPerson(p.getName());
        //Unfollowing all the products that he followed
        Object[] products = p.cartProducts().toArray();
        for(int i = products.length - 1; i>=0; i--){
            Product pro = (Product) products[i];
            unfollow(pro, p);
        }

        this.persons.deleteById(id);
    }

    public boolean isAdmin(Person p){
        List<String> roles = p.getRoles();
        for(String s : roles) if(s.equals("ADMIN")) return true;
        return false;
    }

    public boolean correctNameAndPass(String name, String pass){
        return name!=null && !name.isEmpty() && pass!=null && !pass.isEmpty();
    }

    public boolean correctName(String name){
        return name!=null && !name.isEmpty();
    }

    public boolean correctPass(String pass){
        return pass!=null && !pass.isEmpty();
    }
    public boolean correctPassForPerson(String username, String pass){
        Person p = this.persons.findByPersonName(username).orElseThrow();
        String encodePass = p.getEncodedPassword();
        return this.securityConfiguration.passwordEncoder().matches(pass, encodePass);
    }

    public void editPerson(long id, String username, String password) {
        Person p = this.persons.findById(id).orElseThrow();
        p.setName(username);
        p.setEncodedPassword(this.securityConfiguration.passwordEncoder().encode(password));
        this.persons.save(p);
    }

    public boolean hasValue(String value){
        return value!=null && !value.isEmpty();
    }
}
