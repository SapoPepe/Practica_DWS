package DWS.practica_dws.service;

import DWS.practica_dws.model.Product;
import DWS.practica_dws.model.User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductsService {
    //Each product have a diferent ID
    private Map<Long, Product> defaultProducts;
    //private AtomicLong nextId = new AtomicLong();

    public static long idNumber = 0;
    public ProductsService(){
        this.defaultProducts = new HashMap<>();
        Product p1 = new Product("Producto1", "Descripción de producto 1", 30.23);
        Product p2 = new Product("Producto2", "Descripción de producto 2", 12);
        Product p3 = new Product("Producto3", "Descripción de producto 3", 823);
        p1.setPhoto(true);
        p2.setPhoto(true);
        p3.setPhoto(true);
        saveProduct(p1);
        saveProduct(p2);
        saveProduct(p3);
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

    public boolean contains(Long id){
        return this.defaultProducts.get(id)!=null;
    }

    public String getProductName(Long id){
        return this.defaultProducts.get(id).getName();
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

}
