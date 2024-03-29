package DWS.practica_dws.controller;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.ProductsService;
import DWS.practica_dws.service.PersonSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@Controller
public class ProductController {
    @Autowired
    private PersonSession personSession;
    @Autowired
    private ProductsService productsService;

    private static final String PRODUCTS_FOLDER = "products";
    private ImageService imageService = new ImageService();

    @GetMapping("/")
    public String showProducts(Model model, HttpSession httpSession){
        //if(httpSession.isNew()) this.userSession = new PersonSession();
        model.addAttribute("products", this.productsService.getAll());
        return "index";
    }


    @GetMapping("/newProduct")
    public String showNewProductForm(){
        return "newProduct";
    }


    @PostMapping("/product/new")
    public String newProduct(Model model, @RequestParam String name, @RequestParam (required = false) String description,
                             @RequestParam(required = false) String price, @RequestParam MultipartFile image, HttpServletRequest request) {

        Product p;

        try{
            double priceD = Double.parseDouble(price);
            //If it doens't have the principal of the product
            if(this.productsService.hasPrincipals(name, priceD) && this.imageService.admitedImage(image)){

                if(!this.productsService.hasDescription(description)) p = new Product(name, "Producto sin descripción", priceD);
                else p = new Product(name, description, priceD);

                model.addAttribute("name", name);
                if(this.productsService.hasImage(image)){
                    p.setPhoto(true);
                    this.imageService.saveImage(p, image);
                }
                this.productsService.saveProduct(p);
                return "saveProduct";
            } else{
                model.addAttribute("noPrincipals", true);
                return "newProduct";
            }

        } catch (NumberFormatException e){
            model.addAttribute("noPrincipals", true);
            return "newProduct";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/product/{id}/image")
    public ResponseEntity<Object> downloadImage(@PathVariable long id) throws MalformedURLException, SQLException {
        //return imageService.createResponseFromImage(PRODUCTS_FOLDER, id);
        Product p = this.productsService.getProduct(id).orElseThrow();
        return this.imageService.getImage(p);
    }


    @GetMapping("/product/{id}")
    public String showProduct(Model model, @PathVariable Long id) throws MalformedURLException {
        Optional<Product> p = productsService.getProduct(id);
        Product product = p.get();
        model.addAttribute("product", product);
        model.addAttribute("commentList", product.getComments());
        if (product.hasImage()) model.addAttribute("containsPhoto", true);
        else model.addAttribute("containsPhoto", false);

        return "showProduct";
    }


    @PostMapping("/product/{id}/delete")
    public String deleteProduct(Model model, @PathVariable long id){
        Optional<Product> aux = this.productsService.getProduct(id);

        //If aux contains something we delete that product
        if(aux.isPresent()){
            Product p = aux.get();
            //We first delete the image from the DB and then the product entity
            this.imageService.deleteImage(p);
            this.productsService.saveProduct(p);
            this.productsService.deleteProduct(id, this.personSession);
            model.addAttribute("product", p.getName());
            model.addAttribute("exist", true);
        } else model.addAttribute("exist", false);

        return "deletedProduct";
    }


    @PostMapping("/product/{id}/edit")
    public String showEditForm(Model model, @PathVariable long id) {
        Optional<Product> aux = this.productsService.getProduct(id);
        Product p = aux.get();
        model.addAttribute("product", p);
        return "editProduct";
    }

    @PostMapping("/product/{id}/modify")
    public String editProduct(Model model, @PathVariable long id, @RequestParam(required = false) String name,
                              @RequestParam(required = false) double price, @RequestParam(required = false) String description,
                              @RequestParam(required = false) MultipartFile image) throws IOException {
        Optional<Product> aux = this.productsService.getProduct(id);
        Product p = aux.get();
        p.updateInfo(name, description, price);

        if(this.imageService.admitedImage(image)){
            this.imageService.saveImage(p, image);
        }

        this.productsService.saveProduct(p);
        model.addAttribute("product", p);
        return "showProduct";
    }

    @PostMapping("/followProduct")
    public String follow(Model m, @RequestParam String id){
        long identification = Long.parseLong(id);
        this.personSession.follow(identification);
        this.productsService.getProduct(identification).ifPresent(product -> {
            m.addAttribute("name", product.getName());
        });
        return "followProduct";
    }

    @PostMapping("/removeProductFromCart")
    public String removeProductFromCart(Model model, @RequestParam String id) {
        long identification = Long.parseLong(id);
        this.personSession.unfollow(identification);
        model.addAttribute("products", this.productsService.availableProducts(this.personSession.personProducts()));
        return "shoppingCart";
    }


    @GetMapping("/shoppingCart")
    public String userProducts(Model m){
        m.addAttribute("products", this.productsService.availableProducts(this.personSession.personProducts()));
        return "shoppingCart";
    }


    @PostMapping("/product/{id}/newComment")
    public String newComment(Model model, @PathVariable long id, @RequestParam(required = false) String userName,
                             @RequestParam(required = false) Integer score, @RequestParam(required = false) String opinion){
        Optional<Product> aux = productsService.getProduct(id);
        if(aux.isPresent()){
            Product p = aux.get();
            if(this.productsService.correctComment(userName, score)) {
                Comment comment = new Comment(userName, score.intValue(), opinion);
                p.addComment(comment);
                this.productsService.saveProduct(p);
                this.personSession.addComment(comment);
                model.addAttribute("error", false);
            }
            else model.addAttribute("error", true);

            model.addAttribute("product", p);
            model.addAttribute("commentList", p.getComments());
        }
        return "showProduct";
    }

    @PostMapping("/product/{id}/comment/{CID}")
    public String deleteComment(Model model, @PathVariable long id, @PathVariable long CID) {
        Optional<Product> aux = this.productsService.getProduct(id);

        if(aux.isPresent()){
            Product p = aux.get();
            this.productsService.deleteCommentFromProduct(CID, id);
            this.personSession.deleteComment(CID);
            this.productsService.deleteComment(CID);
            model.addAttribute("product", p);
            model.addAttribute("commentList", p.getComments());
        }

        return "showProduct";
    }

    @GetMapping("/searchProduct")
    public String searchProduct(Model model, @RequestParam String productName) {
        List<Product> matchingProducts = this.productsService.getProductsByName(productName);
        model.addAttribute("products", matchingProducts);
        return "searchResults";
    }

}