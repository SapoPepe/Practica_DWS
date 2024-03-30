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
    private long counter = 0;

    private static final Path FILES_FOLDER = Paths.get(System.getProperty("user.dir"), "files");

    private Path createFilePath(String fileName, Path folder) {
        this.counter++;
        return folder.resolve("("+ counter + ")-product-" + fileName);
    }

    public void saveFile(String folderName, Product p, MultipartFile file) throws IOException {
        Path folder = FILES_FOLDER.resolve(folderName);
        Files.createDirectories(folder);
        Path newFile = createFilePath(file.getOriginalFilename(), folder);
        file.transferTo(newFile);

        p.setFile(true, file.getOriginalFilename());
    }

    public ResponseEntity<Object> createResponseFromImage(String folderName, long productID) throws MalformedURLException {
        Path folder = FILES_FOLDER.resolve(folderName);
        Product p = this.productsService.getProduct(productID).orElseThrow();

        Path filePath = createFilePath(p.getFileName(), folder);

        Resource file = new UrlResource(filePath.toUri());

        if(!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/pdf").body(file);
        }
    }

    public void deleteFile(String folderName, long productID) throws IOException {
        Path folder = FILES_FOLDER.resolve(folderName);
        Product p = this.productsService.getProduct(productID).orElseThrow();
        Path imageFile = createFilePath(p.getFileName(), folder);
        Files.deleteIfExists(imageFile);
        p.setFile(false, null);
        this.productsService.saveProduct(p);
    }

    public boolean admitedFile(MultipartFile file){
        String originalName = file.getOriginalFilename();
        if(file!=null && !file.isEmpty() && !originalName.matches(".*\\.(pdf)")) return false;
        return true;
    }

    public boolean hasFile(MultipartFile file){
        return file!=null && !file.isEmpty();
    }


}
