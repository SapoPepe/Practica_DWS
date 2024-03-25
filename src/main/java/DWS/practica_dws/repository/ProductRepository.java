package DWS.practica_dws.repository;

import DWS.practica_dws.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //Search for products that contains somewhere the string that is needed
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:productName%")
    public List<Product> findProductByName(String productName);
}
