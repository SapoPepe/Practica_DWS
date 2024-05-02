package DWS.practica_dws.service;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Person;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.repository.CommentRepository;
import DWS.practica_dws.repository.ProductRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.hibernate.engine.jdbc.BlobProxy;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

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
    public void init() {
        Product p1 = new Product("Acetie homeopático", "Para cuando la vida te da limones, ¡tú le das CBD!  Un toque de tranquilidad para esos días estresantes. Relájate, respira profundo y disfruta de la vibra natural. ¡Dile adiós al estrés y hola a la buena energía!", 18.90, "Farmacia", "1.jpg");
        Product p2 = new Product("Botellín de cerveza", "¿Cansado de la misma rutina? ¡Relájate con la nueva Cerveza CBD! Suave, refrescante y con un toque especial que te hará sentir zen. Olvídate del estrés y disfruta de una experiencia única. ¡Salud!", 2.15, "Comida", "2.jpg");
        Product p3 = new Product("Crema de manos", "¿Manos secas y estresadas? ¡Dile adiós a la piel áspera con nuestra crema de manos con CBD! Enriquecida con aceite de cáñamo, esta crema mágica te dará la hidratación que necesitas para unas manos suaves como la seda.", 24.1, "Farmacia", "3.jpg");
        Product p4 = new Product("Aspirina 500mg", "Dile adiós al dolor con un toque de paz interior. Prueba nuestra nueva aspirina con CBD, la combinación perfecta para calmar tu cuerpo y tu mente. Te sentirás tan bien que hasta las migrañas se irán de fiesta.", 10.32, "Farmacia", "4.jpg");
        Product p5 = new Product("Galletas con chocolate", "Olvídate del estrés, ¡dile hola a las galletas CBD!  Un dulce viaje a la relajación sin psicoactividad. Disfruta de un sabor delicioso y un efecto calmante que te hará sentir como flotando en una nube.", 7, "Comida", "5.jpg");
        Product p6 = new Product("Gominolas", "¿Cansado del estrés? Prueba nuestras deliciosas gominolas con CBD, con un toque de sabor a frutas tropicales y una dosis perfecta de CBD, estas gomitas te ayudarán a relajarte, mejorar tu sueño y encontrar la paz interior.", 13.33, "Comida", "6.jpg");

        try {
            File f=new File("./images/products/1.jpeg");
            p1.setImageFile(BlobProxy.generateProxy(new FileInputStream(f), f.length()));
            f=new File("./images/products/2.jpeg");
            p2.setImageFile(BlobProxy.generateProxy(new FileInputStream(f), f.length()));
            f=new File("./images/products/3.jpeg");
            p3.setImageFile(BlobProxy.generateProxy(new FileInputStream(f), f.length()));
            f=new File("./images/products/4.jpeg");
            p4.setImageFile(BlobProxy.generateProxy(new FileInputStream(f), f.length()));
            f=new File("./images/products/5.jpeg");
            p5.setImageFile(BlobProxy.generateProxy(new FileInputStream(f), f.length()));
            f=new File("./images/products/6.jpeg");
            p6.setImageFile(BlobProxy.generateProxy(new FileInputStream(f), f.length()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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

    public void saveProduct(Product p) {
        if(p.getComments()!=null){
            for (Comment comment : p.getComments()) {
                comment.setOpinion(stripXSS(comment.getOpinion()));
            }
        }
        this.products.saveAndFlush(p);
    }

    public Collection<Product> getAll() {
        return this.products.findAll();
    }

    public Collection<Product> getAll(Double min, Double max, String type) {
        return this.products.findByPriceRangeAndType(min, max, type);
    }

    public Optional<Product> getProduct(Long id) {
        return this.products.findById(id);
    }

    public Product deleteProduct(long id, PersonSession session) {
        Optional<Product> aux = this.products.findById(id);
        //If aux contains something we remove it from the DB
        if (aux.isPresent()) {
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

    public void saveComment(Comment c) {
        c.setOpinion(stripXSS(c.getOpinion()));
        this.comments.saveAndFlush(c);
    }

    public void deleteComment(long CID) {
        this.comments.deleteById(CID);
    }

    public void deleteCommentFromProduct(long CID, long id) {
        Product p = this.products.findById(id).orElseThrow();
        Comment c = this.comments.findById(CID).orElseThrow();
        p.removeComment(c);
    }

    public Comment getComment(long CID) {
        return this.comments.findById(CID).orElseThrow();
    }

    //It returns a list of two sets, the first one contains the person comments and the second the rest
    public List<Set<Comment>> commentSeparator(Product p, Person per){
        List<Comment> aux = p.getComments();
        Set<Comment> perComments = new HashSet<>();
        Set<Comment> elseComments = new HashSet<>();
        List<Set<Comment>> finalList = new ArrayList<>();

        for(Comment c : aux){
            if(c.hasPerson(per.getName())) perComments.add(c);
            else elseComments.add(c);
        }

        finalList.add(perComments);
        finalList.add(elseComments);

        return finalList;
    }

    public List<Product> getProductsByName(String productName) {
        return this.products.findProductByName(productName);
    }

    public List<Product> getProductByNameWithFilter(String productName, Double min, Double max, String type) {
        return this.products.findProductByNameWithFilter(productName, min, max, type);
    }

    public Collection<Product> availableProducts(Collection<Product> cartProducts) {
        List<Product> aux = new ArrayList<>();
        for (Product p : cartProducts) {
            if (this.products.findById(p.getId()).isPresent()) aux.add(p);
        }
        return aux;
    }

    public boolean hasPrincipals(String name, double price) {
        return name != null && !name.isEmpty() && price > 0;
    }

    public boolean hasDescription(String description) {
        return description != null && !description.isEmpty();
    }


    public boolean correctComment(Integer score) {
        return score != null && !score.describeConstable().isEmpty() && score.intValue() >= 0 && score.intValue() <= 10;
    }

    public boolean correctFilter(Double min, Double max) {
        if (min != null && max == null) return min >= 0;
        if (min == null && max != null) return max >= 0;
        if (min != null && max != null) return min <= max && min >= 0 && max >= 0;
        return true;
    }

    private boolean isNotEmptyField(String field) {
        return field != null && !field.isEmpty();
    }

    public boolean comprobationProductIsNotEmpty(Product product) {
        if (product.getName() != null || product.getPrice() > 0 || product.getDescription() != null) {
            return true;
        }
        return false;
    }


    public static String stripXSS(String value) {
        //This safelist allows only simple text formatting: b, em, i, strong, u. All other HTML (tags and attributes) will be removed.
        Safelist safeList=Safelist.simpleText();

        if(StringUtils.isBlank(value))
            return value;

        safeList.addTags("p");
        safeList.addTags("h1");
        safeList.addTags("h2");
        safeList.addTags("h3");
        safeList.addTags("ol");
        safeList.addTags("li");
        safeList.addTags("ul");
        safeList.addTags("br");

        // Use the ESAPI library to avoid encoded attacks.
        value = ESAPI.encoder().canonicalize( value );

        // Avoid null characters
        value = value.replaceAll("\0", "");

        value = Jsoup.clean( value,  safeList );

        return value;
    }

    public void deleteCommentsFromProductsPerson(String personName){
        List<Product> products = this.products.findAll();
        for(Product p : products){
            p.deleteCommentsFromPerson(personName);
            this.saveProduct(p);
        }
    }
}
