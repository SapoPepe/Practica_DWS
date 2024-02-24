package DWS.practica_dws.controller;

import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.ProductsService;
import DWS.practica_dws.service.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

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

    @PostMapping("/product/new")
    public String newProduct(Model model, HttpServletRequest request, @RequestParam("image") MultipartFile image) {
        Product p;
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String prize = request.getParameter("prize");

        if (name != null && Integer.parseInt(prize) >= 0) {
            if (description == null) {
                p = new Product(name, "Producto sin descripción", Integer.parseInt(prize));
            } else {
                p = new Product(name, description, Integer.parseInt(prize));
            }

            model.addAttribute("name", p.getName());
            this.productsService.saveProduct(p);
            return "saveProduct";
        } else {
            return "unsavedProduct";
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
        m.addAttribute("products", this.userSession.userProducts());
        return "shoppingCart";
    }

    @GetMapping("/product/{id}")
    public String showProduct(Model model, @PathVariable Long id){
        Product prod1 = productsService.getProduct(id);

        model.addAttribute("name", prod1.getName());
        model.addAttribute("price", prod1.getPrice());
        model.addAttribute("description", prod1.getDescription());
        model.addAttribute("image", "/images/products/" + id + ".jpeg"); // Ruta de la imagen

        return "showProduct";
    }

    @GetMapping("/product/{id}/delete")
    public String deleteProduct(Model model, @PathVariable long id) {
        productsService.deleteProduct(id);

        model.addAttribute("allProducts", productsService.getAll());

        return "deletedProduct";
    }

    @PostMapping("/removeProductFromCart")
    public String removeProductFromCart(@RequestParam long id, Model model) {
        productsService.removeProductFromCart(id, userSession);
        model.addAttribute("products", userSession.userProducts());
        return "shoppingCart";
    }

}