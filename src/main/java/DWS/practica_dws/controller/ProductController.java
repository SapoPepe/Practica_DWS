package DWS.practica_dws.controller;

import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.ProductsService;
import DWS.practica_dws.service.UserSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {
    @Autowired
    private UserSession userSession;
    @Autowired
    private ProductsService productsService;

    private static final String PRODUCTS_IMAGES_FOLDER = "products";
    private ImageService imageService;

    @GetMapping("/")
    public String showProducts(Model model, HttpSession httpSession){
            if(httpSession.isNew()) this.userSession = new UserSession(httpSession);
            model.addAttribute("products", this.productsService.getAll());
        return "index";
    }

    @PostMapping("/product/new")
    public String newPost(Model model, Product p) {

        model.addAttribute("name", p.getName());
        this.productsService.saveProduct(p);

        return "saveProduct";
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
        //model.addAttribute("image", prod1.getImage());

        return "showProduct";
    }

    @GetMapping("/product/{id}/delete")
    public String deleteProduct(Model model, @PathVariable long id) {
        if(productsService.getProduct(id)!=null){
            productsService.deleteProduct(id);
        }
        model.addAttribute("allProducts",productsService.getAll());

        return "deletedProduct";
    }

}
