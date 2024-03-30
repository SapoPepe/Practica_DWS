package DWS.practica_dws.service;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Person;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.repository.CommentRepository;
import DWS.practica_dws.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ProductsService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductRepository products;
    @Autowired
    private CommentRepository comments;
    //It has to be desactivated due to a ciclic iteration between ProductService and PersonSession
    /*@Autowired
    private PersonSession personSession;*/


    @PostConstruct
    public void init(){
        Product p1 = new Product("Acetie homeopático", "Para cuando la vida te da limones, ¡tú le das CBD!  Un toque de tranquilidad para esos días estresantes. Relájate, respira profundo y disfruta de la vibra natural. ¡Dile adiós al estrés y hola a la buena energía!", 18.90, "Farmacia");
        Product p2 = new Product("Botellín de cerveza", "¿Cansado de la misma rutina? ¡Relájate con la nueva Cerveza CBD! Suave, refrescante y con un toque especial que te hará sentir zen. Olvídate del estrés y disfruta de una experiencia única. ¡Salud!", 2.15, "Comida");
        Product p3 = new Product("Crema de manos", "¿Manos secas y estresadas? ¡Dile adiós a la piel áspera con nuestra crema de manos con CBD! Enriquecida con aceite de cáñamo, esta crema mágica te dará la hidratación que necesitas para unas manos suaves como la seda. Además, su efecto relajante te ayudará a desestresarte después de un largo día. ¡Olvídate de las preocupaciones y disfruta de unas manos sanas y felices!", 24.1, "Farmacia");
        Product p4 = new Product("Aspirina 500mg", "Dile adiós al dolor con un toque de paz interior. Prueba nuestra nueva aspirina con CBD, la combinación perfecta para calmar tu cuerpo y tu mente. Te sentirás tan bien que hasta las migrañas se irán de fiesta. ¡Es hora de darle un respiro a tu cabeza!", 10.32, "Farmacia");
        Product p5 = new Product("Galletas con chocolate", "Olvídate del estrés, ¡dile hola a las galletas CBD!  Un dulce viaje a la relajación sin psicoactividad. Disfruta de un sabor delicioso y un efecto calmante que te hará sentir como flotando en una nube. ¡Perfectas para después de un largo día o para una tarde de Netflix!", 7, "Comida");
        Product p6 = new Product("Gominolas", "¿Cansado del estrés? ¿Abrumado por la rutina? ¡No te preocupes! Prueba nuestras deliciosas gominolas con CBD, ¡el remedio natural para un día más zen! Con un toque de sabor a frutas tropicales y una dosis perfecta de CBD, estas gomitas te ayudarán a relajarte, mejorar tu sueño y encontrar la paz interior. Olvídate de las pastillas y las infusiones, ¡disfruta de los beneficios del CBD de la forma más divertida y deliciosa! Pide ya tus gominolas CBD y descubre una nueva forma de bienestar.", 13.33, "Comida");
        p1.setPhoto(true);
        p2.setPhoto(true);
        p3.setPhoto(true);
        p4.setPhoto(true);
        p5.setPhoto(true);
        p6.setPhoto(true);
        this.products.save(p1);
        this.products.save(p2);
        this.products.save(p3);
        this.products.save(p4);
        this.products.save(p5);
        this.products.save(p6);

    }

    public void saveProduct(Product p){
        this.products.saveAndFlush(p);
    }

    public Collection<Product> getAll(){
        return this.products.findAll();
    }

    public List<Product> getAll(Double min, Double max, String type){
        return this.products.findByPriceRangeAndType(min, max, type);
    }

    public Optional<Product> getProduct(Long id){
        return this.products.findById(id);
    }

    public Product deleteProduct(long id, PersonSession session){
        Optional<Product> aux = this.products.findById(id);
        //If aux contains something we remove it from the DB
        if(aux.isPresent()){
            Product p = aux.get();
            session.deleteProductFromCarts(p);
            //We need to delete the comments form this product in middle table that associate users with comments because if not it jump off an error
            session.deleteCommentsFromUsers(p.getComments());
            //We delete all the comments of the product
            p.removeAllComments();
            saveProduct(p);
            this.products.delete(p);
            return p;
        }
        return null;
    }

    public void saveComment(Comment c){
        this.comments.saveAndFlush(c);
    }

    public void deleteComment(long CID){
        this.comments.deleteById(CID);
    }

    public void deleteCommentFromProduct(long CID, long id){
        Product p = this.products.findById(id).orElseThrow();
        Comment c = this.comments.findById(CID).orElseThrow();
        p.removeComment(c);
    }

    public Comment getComment(long CID){
        return this.comments.findById(CID).orElseThrow();
    }

    public void removeProductFromCart(long id, PersonSession userSession) {
        Optional<Product> productToRemove = this.products.findById(id);
        if(productToRemove.isPresent()){
            userSession.unfollow(id);
        }
    }

    public List<Product> getProductsByName(String productName) {
        return this.products.findProductByName(productName);
    }

    public List<Product> getProductByNameWithFilter(String productName, Double min, Double max, String type){
        return this.products.findProductByNameWithFilter(productName, min, max, type);
    }

    public Collection<Product> availableProducts(Collection<Product> cartProducts){
        List<Product> aux = new ArrayList<>();
        for (Product p : cartProducts){
            if(this.products.findById(p.getId()).isPresent()) aux.add(p);
        }
        return aux;
    }

    public boolean hasPrincipals(String name, double price){
        return name!=null && price>0;
    }

    public boolean hasDescription(String description){
        return description!=null && !description.isEmpty();
    }

    public boolean hasImage(MultipartFile image){
        return image!=null && !image.isEmpty();
    }

    public boolean correctComment(String name, Integer score){
        return name!=null && !name.isEmpty() && score!=null && !score.describeConstable().isEmpty() && score.intValue()>=0 && score.intValue()<=10;
    }

    public boolean correctFilter(Double min, Double max){
        if(min!=null && max==null) return min>=0;
        if(min==null && max!=null) return max>=0;
        if(min!=null && max!=null) return min <= max && min >= 0 && max >= 0;
        return true;
    }

    private boolean isNotEmptyField(String field) {
        return field != null && !field.isEmpty();
    }

    public boolean comprobationProductIsNotEmpty(Product product){
        if(product.getName()!=null || product.getPrice()>0 || product.getDescription()!=null){
            return true;
        }
        return false;
    }

}
