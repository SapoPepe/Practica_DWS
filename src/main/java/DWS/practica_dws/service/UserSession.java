package DWS.practica_dws.service;

import DWS.practica_dws.model.Product;
import DWS.practica_dws.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Collection;

@Service
@SessionScope //Makes an instance of each componet for each user
public class UserSession {
    private User user;

    public UserSession(HttpSession httpSession){
        if(httpSession.isNew()) this.user = new User();
    }

    public void follow(Product p){
        this.user.followProduct(p);
    }

    public Collection<Product> userProducts(){
        return this.user.cartProducts();
    }


}
