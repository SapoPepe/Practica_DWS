package DWS.practica_dws.controller;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Person;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.repository.PersonRepository;
import DWS.practica_dws.service.FileService;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.PersonSession;
import DWS.practica_dws.service.ProductsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class ProductController {
    @Autowired
    private PersonSession personSession;
    @Autowired
    private ProductsService productsService;
    @Autowired
    private FileService fileService;

    private PersonRepository personRepository;

     private ImageService imageService = new ImageService();

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        if(principal != null) {

            model.addAttribute("notLogged", false);
            model.addAttribute("logged", true);
            model.addAttribute("userName", principal.getName());
            model.addAttribute("admin", request.isUserInRole("ADMIN"));

        } else model.addAttribute("notLogged", true);
    }


    @GetMapping("/")
    public String showProducts(Model model, HttpSession httpSession, @RequestParam(required = false) Double min,
                               @RequestParam(required = false) Double max, @RequestParam(required = false) String type){
        //if(httpSession.isNew()) this.userSession = new PersonSession();
        if(max==null && min==null && (type==null || type.isEmpty())){
            model.addAttribute("incorrect_filter", false);
            model.addAttribute("products", this.productsService.getAll());
        }
        else if(this.productsService.correctFilter(min, max)){
            model.addAttribute("incorrect_filter", false);
            Collection<Product> aux = this.productsService.getAll(min, max, type);
            model.addAttribute("products", aux);
        } else {
            model.addAttribute("incorrect_filter", true);
            model.addAttribute("products", this.productsService.getAll());
        }

        return "index";
    }


    @GetMapping("/newProduct")
    public String showNewProductForm(){
        return "newProduct";
    }


    @PostMapping("/product/new")
    public String newProduct(Model model, @RequestParam String name, @RequestParam (required = false) String description,
                             @RequestParam(required = false) String price, @RequestParam(required = false) String type,
                             @RequestParam MultipartFile image, @RequestParam MultipartFile file, HttpServletRequest request) {

        Product p;

        try{
            double priceD = Double.parseDouble(price);
            //If it doens't have the principal of the product
            if(this.productsService.hasPrincipals(name, priceD) && this.imageService.admitedImage(image) && this.fileService.admitedFile(file)){

                //Create the product if it has description or not
                if(!this.productsService.hasDescription(description)) p = new Product(name, "Producto sin descripción", priceD, "", null);
                else p = new Product(name, description, priceD, type, null);

                //If it has image, it will be upload to the database and the product will include the direction of the image
                if(this.imageService.hasImage(image)) this.imageService.saveImage(p, image);

                //If it has a PDF file, it will be save in memory and the product will have the name of it's file
                if(this.fileService.hasFile(file)) this.fileService.saveFile(p, file);

                this.productsService.saveProduct(p);
                model.addAttribute("name", name);
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
    public ResponseEntity<Object> downloadImage(@PathVariable long id) throws MalformedURLException, SQLException {
        //return imageService.createResponseFromImage(PRODUCTS_FOLDER, id);
        Product p = this.productsService.getProduct(id).orElseThrow();
        return this.imageService.getImage(p);
    }

    @GetMapping("/product/{id}/file")
    public ResponseEntity<Object> downloadFile(@PathVariable long id) throws MalformedURLException{
        return fileService.createResponseFromImage(id);
    }


    @GetMapping("/product/{id}")
    public String showProduct(Model model, HttpServletRequest request, @PathVariable Long id) {
        Optional<Product> p = productsService.getProduct(id);
        Product product = p.get();
        model.addAttribute("product", product);

        if (product.hasImage()) model.addAttribute("containsPhoto", true);
        else model.addAttribute("containsPhoto", false);
        if(product.getComments().isEmpty()) model.addAttribute("noComments", true);

        //If the user isn't logged he can't see the delete comment button
        if(request.getUserPrincipal() == null) model.addAttribute("generalCommentList", product.getComments());
        else{
            model.addAttribute("logged", true);
            Person per = this.personSession.getUser(request.getUserPrincipal().getName());
            //If the user is an ADMIN he can delete any comment
            if(this.personSession.isAdmin(per)) model.addAttribute("personCommentList", product.getComments());
            else {
                List<Set<Comment>> sets = this.productsService.commentSeparator(product, per);
                Object[] personComments = sets.get(0).toArray();
                Object[] elseComments = sets.get(1).toArray();

                model.addAttribute("personCommentList", personComments);
                model.addAttribute("generalCommentList", elseComments);
            }
        }

        return "showProduct";
    }


    @PostMapping("/product/{id}/delete")
    public String deleteProduct(Model model, @PathVariable long id) throws IOException {
        Optional<Product> aux = this.productsService.getProduct(id);

        //If aux contains something we delete that product
        if(aux.isPresent()){
            Product p = aux.get();
            //We first delete the image from the DB and then the product entity
            if(p.hasImage()) this.imageService.deleteImage(p);
            if(p.hasFile()) this.fileService.deleteFile(p.getId());
            this.productsService.saveProduct(p);
            this.productsService.deleteProduct(id, this.personSession);
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
                              @RequestParam(required = false) String type, @RequestParam(required = false) MultipartFile file,
                              @RequestParam(required = false) MultipartFile image) throws IOException {
        Optional<Product> aux = this.productsService.getProduct(id);
        Product p = aux.get();

        //For the information
        p.updateInfo(name, description, price, type);

        //For the image
        if(image!=null && !image.isEmpty() && this.imageService.admitedImage(image)) this.imageService.saveImage(p, image);

        //For the file
        if(file!=null && !file.isEmpty() && this.fileService.admitedFile(file)){
            if(p.hasFile()) this.fileService.deleteFile(p.getId());    //Delete the old file
            this.fileService.saveFile(p, file);                        //Keep the new file
        }

        this.productsService.saveProduct(p);
        model.addAttribute("product", p);
        return "showProduct";
    }

    @PostMapping("/followProduct")
    public String follow(Model m, @RequestParam String id, HttpServletRequest request){
        long identification = Long.parseLong(id);
        this.personSession.follow(identification, request.getUserPrincipal());
        this.productsService.getProduct(identification).ifPresent(product -> {
            m.addAttribute("name", product.getName());
        });
        return "followProduct";
    }

    @PostMapping("/removeProductFromCart")
    public String removeProductFromCart(Model model, @RequestParam String id, HttpServletRequest request) {
        long identification = Long.parseLong(id);
        this.personSession.unfollow(identification, request.getUserPrincipal().getName());
        model.addAttribute("products", this.productsService.availableProducts(this.personSession.personProducts(request.getUserPrincipal())));
        return "shoppingCart";
    }


    @GetMapping("/shoppingCart")
    public String userProducts(Model m, HttpServletRequest request){
        m.addAttribute("products", this.productsService.availableProducts(this.personSession.personProducts(request.getUserPrincipal())));
        return "shoppingCart";
    }



    @PostMapping("/product/{id}/newComment")
    public String newComment(Model model, HttpServletRequest request, @PathVariable long id, @RequestParam(required = false) String userName,
                             @RequestParam(required = false) Integer score, @RequestParam(required = false) String opinion){
        Optional<Product> aux = productsService.getProduct(id);
        if(aux.isPresent()){
            Product p = aux.get();
            if(this.productsService.correctComment(userName, score)) {
                Comment comment = new Comment(request.getUserPrincipal(), score.intValue(), opinion);
                p.addComment(comment);
                this.productsService.saveProduct(p);
                this.personSession.addComment(comment, request.getUserPrincipal().getName());
                model.addAttribute("error", false);
            }
            else model.addAttribute("error", true);

            model.addAttribute("product", p);
        }
        return showProduct(model, request, id);
    }

    @PostMapping("/product/{id}/comment/{CID}")
    public String deleteComment(Model model, HttpServletRequest request, @PathVariable long id, @PathVariable long CID) {
        Optional<Product> aux = this.productsService.getProduct(id);

        if(aux.isPresent()){
            Product p = aux.get();
            Person per = this.personSession.getUser(request.getUserPrincipal().getName());

            if(this.personSession.ownsComment(request.getUserPrincipal().getName(), CID)){
                this.productsService.deleteCommentFromProduct(CID, id);
                this.personSession.deleteComment(CID, per);
                this.productsService.deleteComment(CID);
            } else if(this.personSession.isAdmin(per)){
                this.productsService.deleteCommentFromProduct(CID, id);
                this.personSession.deleteComment(CID);
                this.productsService.deleteComment(CID);

            }
            model.addAttribute("product", p);
        }

        return showProduct(model, request, id);
    }

    @GetMapping("/searchProduct")
    public String searchProduct(Model model, @RequestParam String productName, @RequestParam(required = false) Double min,
                                @RequestParam(required = false) Double max, @RequestParam(required = false) String type) {
        if(max==null && min==null && (type==null || type.isEmpty())){
            model.addAttribute("incorrect_filter", false);
            model.addAttribute("productName", productName);
            model.addAttribute("products", this.productsService.getProductsByName(productName));
        }
        else if(this.productsService.correctFilter(min, max)){
            model.addAttribute("incorrect_filter", false);
            model.addAttribute("productName", productName);
            model.addAttribute("products", this.productsService.getProductByNameWithFilter(productName, min, max, type));
        } else {
            model.addAttribute("incorrect_filter", true);
            model.addAttribute("productName", productName);
            model.addAttribute("products", this.productsService.getProductsByName(productName));
        }

        return "searchResults";
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }


    @GetMapping("/loginerror")
    public String loginerror(){
        return "loginerror";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(Model m, @RequestParam String username, @RequestParam String password){
        //If the username already exist we cant create a new one with the same name
        if(this.personSession.exists(username)) {
            m.addAttribute("usedUser", true);
            return "register";
        } else if (!this.personSession.correctNameAndPass(username, password)) {
            m.addAttribute("invalidUser", true);
            return "register";
        } else {
            this.personSession.newPerson(username, password);
            m.addAttribute("name", username);
            return "registerConfirmed";
        }
    }

    @GetMapping("/profile")
    public String privatePage(Model model, HttpServletRequest request) {

        String name = request.getUserPrincipal().getName();

        Person user = this.personSession.getUser(name);

        model.addAttribute("username", user.getName());
        model.addAttribute("admin", request.isUserInRole("ADMIN"));

        return "profile";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }
}