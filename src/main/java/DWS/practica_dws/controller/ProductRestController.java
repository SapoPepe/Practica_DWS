package DWS.practica_dws.controller;

import DWS.practica_dws.model.Comment;
import DWS.practica_dws.model.CustomError;
import DWS.practica_dws.model.Person;
import DWS.practica_dws.model.Product;
import DWS.practica_dws.service.FileService;
import DWS.practica_dws.service.ImageService;
import DWS.practica_dws.service.PersonSession;
import DWS.practica_dws.service.ProductsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.GrantedAuthority;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProductRestController {
    @Autowired
    private ProductsService productsService;
    @Autowired
    private PersonSession userSession;
    @Autowired
    private ImageService imageService;
    @Autowired
    private FileService fileService;

    private static final Path IMAGES_FOLDER = Paths.get(System.getProperty("user.dir"), "images");



    @GetMapping("/products")
    public ResponseEntity<Collection<Product>> showAllProducts(@RequestParam(required = false) String name, @RequestParam(required = false) Double min,
                                                               @RequestParam(required = false) Double max, @RequestParam(required = false) String type){
        Collection<Product> list;
        if(name==null) {
            if(max==null && min==null && (type==null || type.isEmpty())) {
                list = this.productsService.getAll();
            }else if(this.productsService.correctFilter(min, max)){
                list = this.productsService.getAll(min, max, type);
            }else{
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }else {
            if(max==null && min==null && (type==null || type.isEmpty())) {
                list = productsService.getProductsByName(name);
            }else if(this.productsService.correctFilter(min, max)){
                list = productsService.getProductByNameWithFilter(name, min, max, type);
            }else{
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if(list != null){
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }



    }

    @PostMapping("/products")
    public ResponseEntity addProduct(@RequestBody Product product){
        if(this.productsService.hasPrincipals(product.getName(), product.getPrice())){

            if(this.productsService.hasDescription(product.getDescription())) product.setDescription("Producto sin descripción");

            this.productsService.saveProduct(product);
            return new ResponseEntity<>(product, HttpStatus.OK);
        }

        else return new ResponseEntity<>(new CustomError("No name or price"), HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity deleteProduct(@PathVariable long id){
        if(!productsService.getProduct(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productsService.deleteProduct(id, userSession);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity editProduct(@RequestBody Product product, @PathVariable long id){
        Optional <Product> oldProduct;
        if(productsService.comprobationProductIsNotEmpty(product)){
            oldProduct=productsService.getProduct(id);
            if(!oldProduct.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            oldProduct.get().updateInfo(product.getName(), product.getDescription(), product.getPrice(), product.getType());
            this.productsService.saveProduct(product);
            return new ResponseEntity<>(oldProduct, HttpStatus.OK);
        } else{
            return new ResponseEntity<>(new CustomError("Nothing to change"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity showProduct(@PathVariable long id){
    if(!productsService.getProduct(id).isPresent()) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }else {
        Optional<Product> product = productsService.getProduct(id);
        return new ResponseEntity<>(product.get(), HttpStatus.OK);
    }
    }

    //followProduct
    @GetMapping("/user/shoppingCart")
    public ResponseEntity showShoppingCart(HttpServletRequest request){
        Collection<Product> list = productsService.availableProducts(userSession.personProducts(request.getUserPrincipal()));
        if(!list.isEmpty()){
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/user/shoppingCart")
    public ResponseEntity addShoppingCart(@RequestParam long productId, HttpServletRequest request){
        //Long identification = Long.parseLong(id);
        if(productsService.getProduct(productId).isPresent()){
            this.userSession.follow(productId, request.getUserPrincipal());
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/user/shoppingCart")
    public ResponseEntity deleteShoppingCart(@RequestParam long productId, HttpServletRequest request){
        if(productsService.getProduct(productId).isPresent()){
            //productsService.removeProductFromCart(productId, userSession);
            this.userSession.unfollow(productId, request.getUserPrincipal().getName());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/products/{productId}/comments")
    public ResponseEntity addComment(@PathVariable long productId, @RequestBody Comment comment, HttpServletRequest request){
        Optional<Product> product = this.productsService.getProduct(productId);
        //If the product exist and the comment is well-formed, it's add to the product
        comment.setUserName(request.getUserPrincipal().getName());
        if(product.isPresent() && this.productsService.correctComment(comment.getScore())){
            product.get().addComment(comment);
            productsService.saveProduct(product.get());
            this.userSession.addComment(comment, request.getUserPrincipal().getName());
            //userSession.getUser(request.getUserPrincipal().getName()).addComment(comment);
            return new ResponseEntity<>(comment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new CustomError("Algo ha salido mal. Revisa los datos."), HttpStatus.BAD_REQUEST);
        }
    }
    //Faltan comprobaciones


    @DeleteMapping("/products/{id}/comments/{CID}")
    public ResponseEntity deleteComment(HttpServletRequest request, @PathVariable long id, @PathVariable long CID){
        if(!productsService.getProduct(id).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Person per = this.userSession.getUser(request.getUserPrincipal().getName());
        if(this.userSession.ownsComment(request.getUserPrincipal().getName(), CID)){
            this.productsService.deleteCommentFromProduct(CID, id);
            this.userSession.deleteComment(CID, per);
            this.productsService.deleteComment(CID);
        } else if(this.userSession.isAdmin(per)){
            this.productsService.deleteCommentFromProduct(CID, id);
            this.userSession.deleteComment(CID);
            this.productsService.deleteComment(CID);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/products/{id}/image")
    public ResponseEntity addImage(@RequestBody MultipartFile image, @PathVariable long id){
        Optional <Product> p;
        try {
             p = productsService.getProduct(id);
             if(p.isPresent() && imageService.hasImage(image) && imageService.admitedImage(image)){
                 imageService.saveImage(p.get(), image);
                 productsService.saveProduct(p.get());
                 return new ResponseEntity<>(HttpStatus.NO_CONTENT);
             }else{
                 return new ResponseEntity<>(HttpStatus.NOT_FOUND);
             }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity getImage(@PathVariable long id){
        Optional <Product> p;
        try {
            p = productsService.getProduct(id);
            return this.imageService.getImage(p.get());
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/products/{id}/image")
    public ResponseEntity deleteImage(@PathVariable long id){
        Optional <Product> p;
        try{
            p = productsService.getProduct(id);
            if(p.isPresent()) {
                imageService.deleteImage(p.get());
                productsService.saveProduct(p.get());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/products/{id}/image")
    public ResponseEntity editImage(@PathVariable long id, @RequestBody MultipartFile image){
        Optional <Product> p;
        try {
            p = productsService.getProduct(id);
            if(p.isPresent() && imageService.hasImage(image) && imageService.admitedImage(image)){
                imageService.saveImage(p.get(), image);
                productsService.saveProduct(p.get());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/products/{id}/file")
    public ResponseEntity addFile(@RequestBody MultipartFile file, @PathVariable long id){
        Optional <Product> p;
        try {
            p = productsService.getProduct(id);
            if(p.isPresent() && fileService.hasFile(file) && fileService.admitedFile(file)){
                fileService.saveFile(p.get(), file);
                productsService.saveProduct(p.get());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products/{id}/file")
    public ResponseEntity getFile(@PathVariable long id){
        try {
            return fileService.createResponseFromFile(id);
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/products/{id}/file")
    public ResponseEntity deleteFile(@PathVariable long id){
        try{
            fileService.deleteFile(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/products/{id}/file")
    public ResponseEntity modifyFile(@RequestBody MultipartFile file, @PathVariable long id){
        Optional <Product> p;
        try {
            p = productsService.getProduct(id);
            if(p.isPresent() && fileService.hasFile(file) && fileService.admitedFile(file)){
                if(p.get().hasFile()){
                    this.fileService.deleteFile(p.get().getId());    //Delete the old file
                }
                fileService.saveFile(p.get(), file);
                productsService.saveProduct(p.get());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception exception){
            return new ResponseEntity<>(new CustomError("Algo ha salido mal."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/persons")
    public ResponseEntity adminShowAllUsers(HttpServletRequest request) throws Exception {
        //Person per = this.userSession.getUser(request.getUserPrincipal().getName());
        List<Person> users = this.userSession.getUsers();
        //users.remove(per);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/person")
    public ResponseEntity registerUser(@RequestParam String username, @RequestParam String password){
        //If the username already exist we cant create a new one with the same name
        if(this.userSession.exists(username)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } else if (!this.userSession.correctNameAndPass(username, password)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } else {
            this.userSession.newPerson(username, password);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @GetMapping("/person")
    public ResponseEntity showPerson(HttpServletRequest request) throws Exception {
        Person per = this.userSession.getUser(request.getUserPrincipal().getName());
        return new ResponseEntity<>(per, HttpStatus.OK);
    }


    @PutMapping("/person")
    public ResponseEntity editUser(HttpServletRequest request, @RequestParam String username, @RequestParam String password){
        //If the username already exist we cant create a new one with the same name
        if(this.userSession.exists(username)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } else if (!this.userSession.correctNameAndPass(username, password)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }else {
            Person admin = this.userSession.getUser(request.getUserPrincipal().getName());
            this.userSession.editPerson(admin.getID(), username, password);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @DeleteMapping("/person/{id}")
    public ResponseEntity deletePerson(HttpServletRequest request, @PathVariable long id) throws Exception {
        Person admin = this.userSession.getUser(request.getUserPrincipal().getName());

        if(this.userSession.isAdmin(admin) && !admin.samePerson(id)){
            this.userSession.deletePerson(id);
        } else if(!this.userSession.isAdmin(admin) && admin.samePerson(id)){
            this.userSession.deletePerson(id);
        } else return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
