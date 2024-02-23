package DWS.practica_dws.controller;

import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProductController {
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



}
