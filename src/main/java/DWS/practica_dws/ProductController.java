package DWS.practica_dws;

import DWS.practica_dws.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {
    @Autowired
    private ProductsService productsService;


    //How to specify if the user is not created, redirect to index for creating a new user
    @GetMapping("/home")
    public String showProducts(Model model){

            model.addAttribute("products", this.productsService.getAll());
        return "home";
    }



}
