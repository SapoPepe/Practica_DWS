package DWS.practica_dws.repository;

import DWS.practica_dws.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //Search for products that contains somewhere the string that is needed
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:productName%")
    public List<Product> findProductByName(String productName);

    //Search for products that contains somewhere the string that is needed and matched with the filters
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:productName% AND (:min IS NULL OR p.price >= :min) AND (:max IS NULL OR p.price <= :max) AND (:type IS NULL OR :type = '' OR p.type = :type)")
    public List<Product> findProductByNameWithFilter(@Param("productName") String productName, @Param("min") Double min, @Param("max") Double max, @Param("type") String type);


    //Filter products
    @Query("SELECT p FROM Product p WHERE (:min IS NULL OR p.price >= :min) AND (:max IS NULL OR p.price <= :max) AND (:type IS NULL OR :type = '' OR p.type = :type)")
    public List<Product> findByPriceRangeAndType(@Param("min") Double min, @Param("max") Double max, @Param("type") String type);

}
