package DWS.practica_dws.controller;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.ProductsService;
import DWS.practica_dws.service.UserSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {
    @Autowired
    private UserSession userSession;
    @Autowired
    private ProductsService productsService;

    private static final String PRODUCTS_FOLDER = "products";
    private ImageService imageService = new ImageService();

    @GetMapping("/")
    public String showProducts(Model model, HttpSession httpSession){
        if(httpSession.isNew()) this.userSession = new UserSession(httpSession);
        model.addAttribute("products", this.productsService.getAll());
        return "index";
    }


    @GetMapping("/newProduct")
    public String showNewProductForm(){
        return "newProduct";
    }


    @PostMapping("/product/new")
    public String newProduct(Model model, @RequestParam String name, @RequestParam String description,
                             @RequestParam(required = false) String price, @RequestParam MultipartFile image) {

        Product p;

        try{
            double priceD = Double.parseDouble(price);
            //If it doens't have the principal of the product
            if(!name.isEmpty() && priceD>=0){
                if(description!=null){
                    p = new Product(name, "Producto sin descripción", priceD);
                } else {
                    p = new Product(name, description, priceD);
                }

                model.addAttribute("name", name);
                this.productsService.saveProduct(p);
                if(!image.isEmpty()){
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
        this.userSession.follow(this.productsService.getProduct(identification));
        m.addAttribute("name", this.productsService.getProduct(identification).getName());
        return "followProduct";
    }


    @GetMapping("/shoppingCart")
    public String userProducts(Model m){
        m.addAttribute("products", this.productsService.availableProducts(this.userSession.userProducts()));
        return "shoppingCart";
    }

    @GetMapping("/product/{id}")
    public String showProduct(Model model, @PathVariable Long id) throws MalformedURLException {
        Product prod1 = productsService.getProduct(id);
        model.addAttribute("product", prod1);
        model.addAttribute("commentList", prod1.getComments());
        if (this.imageService.imageExist(PRODUCTS_FOLDER, id)) model.addAttribute("containsPhoto", true);
        else model.addAttribute("containsPhoto", false);

        return "showProduct";
    }

    @PostMapping("/product/{id}/delete")
    public String deleteProduct(Model model, @PathVariable long id) throws IOException {
        Product p = productsService.getProduct(id);

        if(p!=null){
            productsService.deleteProduct(id);
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
        Product p = productsService.getProduct(id);
        model.addAttribute("product", p);
        return "editProduct";
    }

    @PostMapping("/product/{id}/modify")
    public String editProduct(Model model, @PathVariable long id, @RequestParam(required = false) String name,
                              @RequestParam(required = false) double price, @RequestParam(required = false) String description,
                              @RequestParam(required = false) MultipartFile image) {
        Product p = productsService.getProduct(id);
        p.updateInfo(name, description, price);
        model.addAttribute("product", p);

        try {
            if (image != null && !image.isEmpty()) {
                imageService.modifyImage("products", id, image, model);
            }
        } catch (IOException e) {
            // Handle exception
            e.printStackTrace();
        }

        return "showProduct";
    }

    @PostMapping("/removeProductFromCart")
    public String removeProductFromCart(@RequestParam long id, Model model) {
        productsService.removeProductFromCart(id, userSession);
        model.addAttribute("products", this.productsService.availableProducts(this.userSession.userProducts()));
        return "shoppingCart";
    }


    @PostMapping("/product/{id}/newComment")
    public String newComment(Model model, @PathVariable long id, @RequestParam String userName,
                             @RequestParam int score, @RequestParam String opinion){
        Product p = productsService.getProduct(id);
        if(p!=null){
            if(!userName.isEmpty() && score >= 0 && score <= 10) {
                Comment comment = new Comment(userName, score, opinion);
                p.addComment(comment);
                userSession.getUser().addComment(comment);
                model.addAttribute("error", false);
            }
            else model.addAttribute("error", true);

            model.addAttribute("product", p);
            model.addAttribute("commentList", p.getComments());
        }
        return "showProduct";
    }

    @PostMapping("/product/{id}/comment/{CID}")
    public String deleteComment(Model model, @PathVariable long id, @PathVariable int CID) {
        Product p = productsService.getProduct(id);

        if(p!=null){
            p.removeComment(CID);
            model.addAttribute("product", p);
            model.addAttribute("commentList", p.getComments());
        }

        return "showProduct";
    }

    @GetMapping("/searchProduct")
    public String searchProduct(Model model, @RequestParam String productName) {
        List<Product> matchingProducts = productsService.getProductsByName(productName);
        model.addAttribute("products", matchingProducts);
        return "searchResults";
    }

}