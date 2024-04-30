package DWS.practica_dws.service;

import DWS.practica_dws.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {
    @Autowired
    private ProductsService productsService;
    //private long counter = 0;

    private static final Path FILES_FOLDER = Paths.get(System.getProperty("user.dir"), "files");
    private static final String PRODUCTS_FOLDER = "products";


    private Path createFilePath(String fileName, Path folder,long pId) {
        return folder.resolve(String.valueOf(pId)).resolve(fileName);
    }

    public void saveFile(Product p, MultipartFile file) throws IOException {
        //If the file has an ../../ for saving in other path that is not the supposed, we threw an exception
        if(file.getOriginalFilename().contains("/") || file.getOriginalFilename().contains("\\")) throw new IOException();
        Path folder = FILES_FOLDER.resolve(PRODUCTS_FOLDER);
        Files.createDirectories(folder.resolve(String.valueOf(p.getId())));
        Path newFile = createFilePath(file.getOriginalFilename(),folder,p.getId());
        file.transferTo(newFile);
        p.setFile(true, file.getOriginalFilename());
    }

    public ResponseEntity<Object> createResponseFromFile(long productID) throws MalformedURLException {
        Path folder = FILES_FOLDER.resolve(PRODUCTS_FOLDER);
        Product p = this.productsService.getProduct(productID).orElseThrow();

        Path filePath = createFilePath(p.getFileName(),folder,productID);

        Resource file = new UrlResource(filePath.toUri());

        if(!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/pdf").body(file);
        }
    }

    public void deleteFile(long productID) throws IOException {
        Path folder = FILES_FOLDER.resolve(PRODUCTS_FOLDER);
        Product p = this.productsService.getProduct(productID).orElseThrow();
        Path imageFile = createFilePath(p.getFileName(),folder,productID);
        Files.deleteIfExists(imageFile);
        p.setFile(false, null);
        this.productsService.saveProduct(p);
    }

    public boolean admitedFile(MultipartFile file){
        String originalName = file.getOriginalFilename();
        if(file!=null && !file.isEmpty() && (!originalName.matches(".*\\.(pdf)")|| originalName.contains(".php") ||originalName.contains(".jar")||originalName.contains(".sh"))) return false;
        return true;
    }

    public boolean hasFile(MultipartFile file){
        return file!=null && !file.isEmpty();
    }


}
