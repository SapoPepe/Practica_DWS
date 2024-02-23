package DWS.practica_dws;


import DWS.practica_dws.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SesionController {

    @Autowired
    private User user;
    @Autowired
    private ProductsService productsService;

    @PostMapping("/userRegistration")
    public String proccessForm(Model model, @RequestParam String user_name){
        this.user.createUser(user_name);

        model.addAttribute("user_name", user_name);
        model.addAttribute("products", this.productsService.getAll());

        return "home";
    }
}
