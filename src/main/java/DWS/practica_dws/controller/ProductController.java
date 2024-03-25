package DWS.practica_dws.controller;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.ProductsService;
import DWS.practica_dws.service.PersonSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

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
                             @RequestParam(required = false) String price, @RequestParam MultipartFile image) {

        Product p;

        try{
            double priceD = Double.parseDouble(price);
            //If it doens't have the principal of the product
            if(this.productsService.hasPrincipals(name, priceD)){

                if(!this.productsService.hasDescription(description)) p = new Product(name, "Producto sin descripción", priceD);
                else p = new Product(name, description, priceD);

                model.addAttribute("name", name);
                this.productsService.saveProduct(p);
                if(this.productsService.hasImage(image)){
                    imageService.saveImage(PRODUCTS_FOLDER, p.getId(), image, model);
                    p.setPhoto(true);
                }
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
    public ResponseEntity<Object> downloadImage(@PathVariable int id) throws MalformedURLException {
        return imageService.createResponseFromImage(PRODUCTS_FOLDER, id);
    }

    @PostMapping("/followProduct")
    public String follow(Model m, @RequestParam String id){
        Long identification = Long.parseLong(id);
        this.personSession.follow(this.productsService.getProduct(identification));

        this.productsService.getProduct(identification).ifPresent(product -> {
            m.addAttribute("name", product.getName());
        });
        return "followProduct";
    }

/*
    @GetMapping("/shoppingCart")
    public String userProducts(Model m){
        m.addAttribute("products", this.productsService.availableProducts(this.userSession.userProducts()));
        return "shoppingCart";
    }*/

    @GetMapping("/product/{id}")
    public String showProduct(Model model, @PathVariable Long id) throws MalformedURLException {
        Optional<Product> p = productsService.getProduct(id);
        Product product = p.get();
        model.addAttribute("product", product);
        model.addAttribute("commentList", product.getComments());
        if (this.imageService.imageExist(PRODUCTS_FOLDER, id)) model.addAttribute("containsPhoto", true);
        else model.addAttribute("containsPhoto", false);

        return "showProduct";
    }


    @PostMapping("/product/{id}/delete")
    public String deleteProduct(Model model, @PathVariable long id) throws IOException {
        Optional<Product> aux = this.productsService.getProduct(id);

        //If aux contains something we delete that product
        if(aux.isPresent()){
            Product p = aux.get();
            this.productsService.deleteProduct(id);
            try {
                imageService.deleteImage(PRODUCTS_FOLDER, p.getId());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
                              @RequestParam(required = false) MultipartFile image) {
        Optional<Product> aux = this.productsService.getProduct(id);
        Product p = aux.get();
        p.updateInfo(name, description, price);
        this.productsService.saveProduct(p);
        model.addAttribute("product", p);

        try {
            if (this.productsService.hasImage(image)) this.imageService.modifyImage("products", id, image, model);
        } catch (IOException e) {
            // Handle exception
            e.printStackTrace();
        }

        return "showProduct";
    }
/*
    @PostMapping("/removeProductFromCart")
    public String removeProductFromCart(@RequestParam long id, Model model) {
        productsService.removeProductFromCart(id, userSession);
        model.addAttribute("products", this.productsService.availableProducts(this.userSession.userProducts()));
        return "shoppingCart";
    }
*/

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
                //userSession.getUser().addComment(comment);
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
            p.removeComment(CID);
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