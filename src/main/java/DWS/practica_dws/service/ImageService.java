package DWS.practica_dws.service;

import DWS.practica_dws.model.Product;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@Service
public class ImageService {
	@Autowired
	private ProductsService products;

	private static final String basePath = "/product";
	private static final Path IMAGES_FOLDER = Paths.get(System.getProperty("user.dir"), "images");

	private URI createFilePath(long id) {
		return fromCurrentRequest().replacePath(basePath).path("/{id}/image").buildAndExpand(id).toUri();
	}

	public boolean admitedImage(MultipartFile image){
		String originalName = image.getOriginalFilename();
		if(image!=null && !image.isEmpty() && !originalName.matches(".*\\.(jpg|jpeg|gif|png)")) return false;
		return true;
	}

	public void saveImage(Product p, MultipartFile image) throws IOException {
		p.setPhoto(true);
		URI location = this.createFilePath(p.getId());
		p.setImageLocation(location.toString());
		p.setImageFile(BlobProxy.generateProxy(image.getInputStream(), image.getSize()));
	}

	public ResponseEntity<Object> getImage(Product p) throws SQLException {
		if(p.hasImage()){
			Resource file = new InputStreamResource(p.getImageFile().getBinaryStream());
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
					.contentLength(p.getImageFile().length())
					.body(file);
		} else return ResponseEntity.notFound().build();
	}


	public void deleteImage(Product p){
		p.setPhoto(false);
		p.setImageFile(null);
		p.setImageLocation(null);
	}

	/*
	public void modifyImage(String folderName, long productId, MultipartFile newImage, Model model) throws IOException {
		deleteImage(folderName, productId);
		saveImage(folderName, productId, newImage, model);
	}

	public ResponseEntity<Object> createResponseFromImage(String folderName, long imageId) throws MalformedURLException {
		Path folder = IMAGES_FOLDER.resolve(folderName);
		Path imagePath = createFilePath(imageId, folder);
		Resource file = new UrlResource(imagePath.toUri());
		if(!Files.exists(imagePath)) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg").body(file);
		}
	}

	public boolean imageExist(String folderName, long imageId) throws MalformedURLException {
		Path folder = IMAGES_FOLDER.resolve(folderName);
		Path imagePath = createFilePath(imageId, folder);
		Resource file = new UrlResource(imagePath.toUri());
        return Files.exists(imagePath);
	}*/


	public boolean hasImage(MultipartFile image){
		return image!=null && !image.isEmpty();
	}
}