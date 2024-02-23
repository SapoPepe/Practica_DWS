package DWS.practica_dws.controller;

import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ProductsService;
import DWS.practica_dws.service.UserSession;
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


    @GetMapping("/")
    public String showProducts(Model model){
            model.addAttribute("products", this.productsService.getAll());
        return "index";
    }

    @PostMapping("/product/new")
    public String newPost(Model model, Product p) {

        model.addAttribute("name", p.getName());
        this.productsService.saveProduct(p);

        return "saveProduct";
    }

    @PostMapping("followProduct?id={id}")
    public String follow(Model m, @RequestParam Long id){
        this.userSession.follow(this.productsService.getProduct(id));
        m.addAttribute(m);
        return "followProduct";
    }


}
