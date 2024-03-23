package DWS.practica_dws.service;

import DWS.practica_dws.model.Product;
import DWS.practica_dws.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ProductsService {
    //Each product have a diferent ID
    private Map<Long, Product> defaultProducts;
    //private AtomicLong nextId = new AtomicLong();

    public static long idNumber = 0;
    public ProductsService(){
        this.defaultProducts = new HashMap<>();
        Product p1 = new Product("Acetie homeopático", "Para cuando la vida te da limones, ¡tú le das CBD!  Un toque de tranquilidad para esos días estresantes. Relájate, respira profundo y disfruta de la vibra natural. ¡Dile adiós al estrés y hola a la buena energía!", 18.90);
        Product p2 = new Product("Botellín de cerveza", "¿Cansado de la misma rutina? ¡Relájate con la nueva Cerveza CBD! Suave, refrescante y con un toque especial que te hará sentir zen. Olvídate del estrés y disfruta de una experiencia única. ¡Salud!", 2.15);
        Product p3 = new Product("Crema de manos", "¿Manos secas y estresadas? ¡Dile adiós a la piel áspera con nuestra crema de manos con CBD! Enriquecida con aceite de cáñamo, esta crema mágica te dará la hidratación que necesitas para unas manos suaves como la seda. Además, su efecto relajante te ayudará a desestresarte después de un largo día. ¡Olvídate de las preocupaciones y disfruta de unas manos sanas y felices!", 24.1);
        Product p4 = new Product("Aspirina 500mg", "Dile adiós al dolor con un toque de paz interior. Prueba nuestra nueva aspirina con CBD, la combinación perfecta para calmar tu cuerpo y tu mente. Te sentirás tan bien que hasta las migrañas se irán de fiesta. ¡Es hora de darle un respiro a tu cabeza!", 10.32);
        Product p5 = new Product("Galletas con chocolate", "Olvídate del estrés, ¡dile hola a las galletas CBD!  Un dulce viaje a la relajación sin psicoactividad. Disfruta de un sabor delicioso y un efecto calmante que te hará sentir como flotando en una nube. ¡Perfectas para después de un largo día o para una tarde de Netflix!", 7);
        Product p6 = new Product("Gominolas", "¿Cansado del estrés? ¿Abrumado por la rutina? ¡No te preocupes! Prueba nuestras deliciosas gominolas con CBD, ¡el remedio natural para un día más zen! Con un toque de sabor a frutas tropicales y una dosis perfecta de CBD, estas gomitas te ayudarán a relajarte, mejorar tu sueño y encontrar la paz interior. Olvídate de las pastillas y las infusiones, ¡disfruta de los beneficios del CBD de la forma más divertida y deliciosa! Pide ya tus gominolas CBD y descubre una nueva forma de bienestar.", 13.33);
        p1.setPhoto(true);
        p2.setPhoto(true);
        p3.setPhoto(true);
        p4.setPhoto(true);
        p5.setPhoto(true);
        p6.setPhoto(true);
        saveProduct(p1);
        saveProduct(p2);
        saveProduct(p3);
        saveProduct(p4);
        saveProduct(p5);
        saveProduct(p6);
    }

    public void saveProduct(Product p){
        //long id = nextId.getAndIncrement();
        p.setID(idNumber);
        this.defaultProducts.put(idNumber, p);
        idNumber++;
    }

    public Collection<Product> getAll(){
        return this.defaultProducts.values();
    }

    public Product getProduct(Long id){
        return this.defaultProducts.get(id);
    }

    public Product deleteProduct(long id){
        if(this.defaultProducts.containsKey(id)){
            Product product= this.getProduct(id);
            this.defaultProducts.remove(id);
            return product;
        }
        return null;
    }

    public void removeProductFromCart(long id, UserSession userSession) {
        Product productToRemove = this.getProduct(id);
        userSession.unfollow(productToRemove);
    }

    public List<Product> getProductsByName(String productName) {
        List<Product> matchingProducts = new ArrayList<>();
        for (Product product : this.defaultProducts.values()) {
            if (product.getName().toLowerCase().contains(productName.toLowerCase())) {
                matchingProducts.add(product);
            }
        }
        return matchingProducts; // Devuelve una lista de productos cuyo nombre contiene la cadena de búsqueda
    }

    public Collection<Product> availableProducts(Collection<Product> cartProducts){
        List<Product> aux = new ArrayList<>();
        for (Product p : cartProducts){
            if(this.defaultProducts.containsKey(p.getId())) aux.add(p);
        }
        return aux;
    }

    public boolean hasPrincipals(String name, double price){
        return name!=null && price>0;
    }

    public boolean hasDescription(String description){
        return description!=null;
    }

    public boolean hasImage(MultipartFile image){
        return image!=null && !image.isEmpty();
    }

    public boolean correctComment(String name, Integer score){
        return name!=null && !name.isEmpty() && score!=null && !score.describeConstable().isEmpty() && score.intValue()>=0 && score.intValue()<=10;
    }
}
