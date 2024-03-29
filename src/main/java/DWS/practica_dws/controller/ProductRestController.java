package DWS.practica_dws.controller;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.CustomError;
import DWS.practica_dws.model.Image;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.PersonSession;
import DWS.practica_dws.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProductRestController {
    @Autowired
    private ProductsService productsService;
    @Autowired
    private PersonSession userSession;
    @Autowired
    private ImageService imageService;

    private static final Path IMAGES_FOLDER = Paths.get(System.getProperty("user.dir"), "images");

    @GetMapping("/products")
    public ResponseEntity<Collection<Product>> showAllProducts(@RequestParam(required = false) String search){
        Collection<Product> list;
        if(search==null) {
            list = this.productsService.getAll();
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
        if(this.productsService.hasPrincipals(product.getName(), product.getPrice())){

            if(this.productsService.hasDescription(product.getDescription())) product.setDescription("Producto sin descripción");

            this.productsService.saveProduct(product);
            return new ResponseEntity<>(product, HttpStatus.OK);
        }

        else return new ResponseEntity<>(new CustomError("No name or price"), HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity deleteProduct(@PathVariable long id){
        if(!productsService.getProduct(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productsService.deleteProduct(id, userSession);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity editProduct(@RequestBody Product product, @PathVariable long id){
        Optional <Product> oldProduct;
        if(product.getName()!=null || product.getPrice()>0 || product.getDescription()!=null){
            oldProduct=productsService.getProduct(id);
            if(!oldProduct.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            oldProduct.get().updateInfo(product.getName(), product.getDescription(), product.getPrice(), product.getType());
            this.productsService.saveProduct(product);
            //imageService.saveImage(PRODUCTS_FOLDER, p.getId(), image, model);
            return new ResponseEntity<>(oldProduct, HttpStatus.OK);
        } else{
            return new ResponseEntity<>(new CustomError("Nothing to change"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity showProduct(@PathVariable long id){
    if(!productsService.getProduct(id).isPresent()) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }else {
        Optional<Product> product = productsService.getProduct(id);
        return new ResponseEntity<>(product.get(), HttpStatus.OK);
    }
    }

    //followProduct
    @GetMapping("/user/shoppingCart")
    public ResponseEntity showShoppingCart(){
        Collection<Product> list = productsService.availableProducts(userSession.personProducts());
        if(!list.isEmpty()){
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/user/shoppingCart")
    public ResponseEntity addShoppingCart(@RequestParam long productId){
        //Long identification = Long.parseLong(id);
        if(productsService.getProduct(productId).isPresent()){
            this.userSession.follow(productId);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/user/shoppingCart")
    public ResponseEntity deleteShoppingCart(@RequestParam long productId){
        //Long identification = Long.parseLong(id);
        if(productsService.getProduct(productId).isPresent()){
            productsService.removeProductFromCart(productId, userSession);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/products/{productId}/comments")
    public ResponseEntity addComment(@PathVariable long productId, @RequestBody Comment comment){
        Optional<Product> product = this.productsService.getProduct(productId);

        //If the product exist and the comment is well-formed, it's add to the product
        if(product.isPresent() && this.productsService.correctComment(comment.getUserName(), comment.getScore())){
            product.get().addComment(comment);
            userSession.getUser().addComment(comment);
            return new ResponseEntity<>(comment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new CustomError("Algo ha salido mal. Revisa los datos."), HttpStatus.BAD_REQUEST);
        }
    }
    //Faltan comprobaciones


    @DeleteMapping("/products/{id}/comments/{CID}")
    public ResponseEntity deleteComment(@PathVariable long id, @PathVariable int CID){
        if(!productsService.getProduct(id).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productsService.deleteCommentFromProduct(id, CID);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/products/{id}/image")
    public ResponseEntity addImage(@RequestBody Image image, @PathVariable long id){
        Optional <Product> p;
        try {
             p = productsService.getProduct(id);
             if(p.isPresent()){
                 imageService.saveImage(p.get(), image);
                 return new ResponseEntity<>(HttpStatus.NO_CONTENT);
             }else{
                 return new ResponseEntity<>(HttpStatus.NOT_FOUND);
             }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity getImage(@PathVariable long id){
        Optional <Product> p;
        Image image = new Image();
        try {
            p = productsService.getProduct(id);
            if(p.isPresent()){
                Blob blob = p.get().getImageFile();
                byte[] bytes = blob.getBytes(0,(int)blob.length());
                image.setImageBase64(Base64.getEncoder().encodeToString(bytes));
                return new ResponseEntity<>(image, HttpStatus.OK);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/products/{id}/image")
    public ResponseEntity deleteImage(@PathVariable long id){
        Optional <Product> p;
        try{
            p = productsService.getProduct(id);
            if(p.isPresent()) {
                imageService.deleteImage(p.get());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/products/{id}/image")
    public ResponseEntity editImage(@PathVariable long id, @RequestBody Image image){
        Optional <Product> p;
        try {
            p = productsService.getProduct(id);
            if(p.isPresent()){
                imageService.saveImage(p.get(), image);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}