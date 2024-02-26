package DWS.practica_dws.controller;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.CustomError;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ProductsService;
import DWS.practica_dws.service.UserSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class ProductRestController {
    @Autowired
    private ProductsService productsService;
    @Autowired
    private UserSession userSession;


    @GetMapping("/products")
    public ResponseEntity<Collection<Product>> showAllProducts(@RequestParam(required = false) String search){
        Collection<Product> list;
        if(search==null) {
            list = productsService.getAll();
        }else {
            list = productsService.getProductsByName(search);
        }
        if(list != null){
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/products")
    public ResponseEntity addProduct(@RequestBody Product product){
        if(product.getName()!=null && product.getPrice()>0){        //0 isn´t considered a valid price
            if(product.getDescription()==null){
                product.setDescription("Producto sin descripción");
            }
            this.productsService.saveProduct(product);
            //imageService.saveImage(PRODUCTS_FOLDER, p.getId(), image, model);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else{
            return new ResponseEntity<>(new CustomError("No name or price"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity deleteProduct(@PathVariable long id){
        if(productsService.getProduct(id)==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productsService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity editProduct(@RequestBody Product product, @PathVariable long id){
        Product oldProduct;
        if(product.getName()!=null || product.getPrice()>0 || product.getDescription()!=null){
            oldProduct=productsService.getProduct(id);
            if(oldProduct==null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            oldProduct.updateInfo(product.getName(), product.getDescription(), product.getPrice());
            this.productsService.saveProduct(product);
            //imageService.saveImage(PRODUCTS_FOLDER, p.getId(), image, model);
            return new ResponseEntity<>(oldProduct, HttpStatus.OK);
        } else{
            return new ResponseEntity<>(new CustomError("Nothing to change"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity showProduct(@PathVariable long id){
    if(productsService.getProduct(id)==null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
        Product product = productsService.getProduct(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    //followProduct
    @GetMapping("/user/shoppingCart")
    public ResponseEntity showShoppingCart(){
        Collection<Product> list = userSession.userProducts();
        if(!list.isEmpty()){
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/user/shoppingCart")
    public ResponseEntity addShoppingCart(@RequestParam long productId){
        //Long identification = Long.parseLong(id);
        if(productsService.getProduct(productId)!=null){
            this.userSession.follow(productsService.getProduct(productId));
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/user/shoppingCart")
    public ResponseEntity deleteShoppingCart(@RequestParam long productId){
        //Long identification = Long.parseLong(id);
        if(productsService.getProduct(productId)!=null){
            this.userSession.unfollow(productsService.getProduct(productId));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/products/{productId}/comments")
    public ResponseEntity addComment(@PathVariable long productId, @RequestBody Comment comment){
        //Long identification = Long.parseLong(id);
        if(productsService.getProduct(productId)!=null){
            Product product = productsService.getProduct(productId);
            product.addComment(comment);
            return new ResponseEntity<>(comment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new CustomError("Algo ha salido mal. Revisa los datos."), HttpStatus.NOT_FOUND);
        }
    }
    //Faltan comprobaciones


    @DeleteMapping("/products/{id}/comments/{CID}")
    public ResponseEntity deleteComment(@PathVariable long id, @PathVariable int CID){
        if(productsService.getProduct(id)==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Product product = productsService.getProduct(id);
        product.removeComment(CID);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }




}
