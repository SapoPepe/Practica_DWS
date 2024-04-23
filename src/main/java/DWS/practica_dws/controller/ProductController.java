package DWS.practica_dws.controller;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.model.User;
import DWS.practica_dws.repository.UserRepository;
import DWS.practica_dws.service.FileService;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.ProductsService;
import DWS.practica_dws.service.PersonSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@Controller
public class ProductController {
    @Autowired
    private PersonSession personSession;
    @Autowired
    private ProductsService productsService;
    @Autowired
    private FileService fileService;

     private ImageService imageService = new ImageService();

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
            model.addAttribute("products", this.productsService.getAll(min, max, type));
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
    public String showProduct(Model model, @PathVariable Long id) throws MalformedURLException {
        Optional<Product> p = productsService.getProduct(id);
        Product product = p.get();
        model.addAttribute("product", product);
        model.addAttribute("commentList", product.getComments());
        if (product.hasImage()) model.addAttribute("containsPhoto", true);
        else model.addAttribute("containsPhoto", false);

        return "showProduct";
    }


    @PostMapping("/product/{id}/delete")
    public String deleteProduct(Model model, @PathVariable long id) throws IOException {
        Optional<Product> aux = this.productsService.getProduct(id);

        //If aux contains something we delete that product
        if(aux.isPresent()){
            Product p = aux.get();
            //We first delete the image from the DB and then the product entity
            this.imageService.deleteImage(p);
            this.fileService.deleteFile(p.getId());
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
    public String follow(Model m, @RequestParam String id){
        long identification = Long.parseLong(id);
        this.personSession.follow(identification);
        this.productsService.getProduct(identification).ifPresent(product -> {
            m.addAttribute("name", product.getName());
        });
        return "followProduct";
    }

    @PostMapping("/removeProductFromCart")
    public String removeProductFromCart(Model model, @RequestParam String id) {
        long identification = Long.parseLong(id);
        this.personSession.unfollow(identification);
        model.addAttribute("products", this.productsService.availableProducts(this.personSession.personProducts()));
        return "shoppingCart";
    }


    @GetMapping("/shoppingCart")
    public String userProducts(Model m){
        m.addAttribute("products", this.productsService.availableProducts(this.personSession.personProducts()));
        return "shoppingCart";
    }


    @PostMapping("/product/{id}/newComment")
    public String newComment(Model model, @PathVariable long id, @RequestParam(required = false) String userName,
                             @RequestParam(required = false) Integer score, @RequestParam(required = false) String opinion){
        Optional<Product> aux = productsService.getProduct(id);
        if(aux.isPresent()){
            Product p = aux.get();
            if(this.productsService.correctComment(userName, score)) {
                Comment comment = new Comment(userName, score.intValue(), opinion);
                p.addComment(comment);
                this.productsService.saveProduct(p);
                this.personSession.addComment(comment);
                model.addAttribute("error", false);
            }
            else model.addAttribute("error", true);

            model.addAttribute("product", p);
            model.addAttribute("commentList", p.getComments());
        }
        return "showProduct";
    }

    @PostMapping("/product/{id}/comment/{CID}")
    public String deleteComment(Model model, @PathVariable long id, @PathVariable long CID) {
        Optional<Product> aux = this.productsService.getProduct(id);

        if(aux.isPresent()){
            Product p = aux.get();
            this.productsService.deleteCommentFromProduct(CID, id);
            this.personSession.deleteComment(CID);
            this.productsService.deleteComment(CID);
            model.addAttribute("product", p);
            model.addAttribute("commentList", p.getComments());
        }

        return "showProduct";
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




    // This could go in a WebController.java

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/loginerror")
    public String loginerror() {
        return "loginerror";
    }

    @GetMapping("/private")
    public String privatePage(Model model, HttpServletRequest request) {

        String name = request.getUserPrincipal().getName();

        UserRepository userRepository = new UserRepository() {
            @Override
            public Optional<User> findByName(String name) {
                return Optional.empty();
            }

            @Override
            public void flush() {

            }

            @Override
            public <S extends User> S saveAndFlush(S entity) {
                return null;
            }

            @Override
            public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) {
                return null;
            }

            @Override
            public void deleteAllInBatch(Iterable<User> entities) {

            }

            @Override
            public void deleteAllByIdInBatch(Iterable<Long> longs) {

            }

            @Override
            public void deleteAllInBatch() {

            }

            @Override
            public User getOne(Long aLong) {
                return null;
            }

            @Override
            public User getById(Long aLong) {
                return null;
            }

            @Override
            public User getReferenceById(Long aLong) {
                return null;
            }

            @Override
            public <S extends User> List<S> findAll(Example<S> example) {
                return null;
            }

            @Override
            public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
                return null;
            }

            @Override
            public <S extends User> List<S> saveAll(Iterable<S> entities) {
                return null;
            }

            @Override
            public List<User> findAll() {
                return null;
            }

            @Override
            public List<User> findAllById(Iterable<Long> longs) {
                return null;
            }

            @Override
            public <S extends User> S save(S entity) {
                return null;
            }

            @Override
            public Optional<User> findById(Long aLong) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(Long aLong) {
                return false;
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public void deleteById(Long aLong) {

            }

            @Override
            public void delete(User entity) {

            }

            @Override
            public void deleteAllById(Iterable<? extends Long> longs) {

            }

            @Override
            public void deleteAll(Iterable<? extends User> entities) {

            }

            @Override
            public void deleteAll() {

            }

            @Override
            public List<User> findAll(Sort sort) {
                return null;
            }

            @Override
            public Page<User> findAll(Pageable pageable) {
                return null;
            }

            @Override
            public <S extends User> Optional<S> findOne(Example<S> example) {
                return Optional.empty();
            }

            @Override
            public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
                return null;
            }

            @Override
            public <S extends User> long count(Example<S> example) {
                return 0;
            }

            @Override
            public <S extends User> boolean exists(Example<S> example) {
                return false;
            }

            @Override
            public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
                return null;
            }
        };
        User user = userRepository.findByName(name).orElseThrow();

        model.addAttribute("username", user.getName());
        model.addAttribute("admin", request.isUserInRole("ADMIN"));

        return "private";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }



}