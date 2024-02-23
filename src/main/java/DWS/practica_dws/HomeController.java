package DWS.practica_dws;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /*@GetMapping("/home")
    public String showHome(HttpSession httpSession){
        if(httpSession.isNew()){
            return "index";
        }

        else{
            return "home";
        }
    }*/
}
