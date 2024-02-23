package DWS.practica_dws.service;

import DWS.practica_dws.model.Product;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductsService {
    //Each product have a diferent ID
    private Map<Long, Product> defaultProducts;
    //private AtomicLong nextId = new AtomicLong();

    public static long idNumber = 0;
    public ProductsService(){
        this.defaultProducts = new HashMap<>();
        saveProduct(new Product("Producto1", "Descripción de producto 1", 30.23));
        saveProduct(new Product("Producto2", "Descripción de producto 2", 12));
        saveProduct(new Product("Producto3", "Descripción de producto 3", 823));
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
}
