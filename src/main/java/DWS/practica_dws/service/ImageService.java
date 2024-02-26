package DWS.practica_dws.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {
	private static final Path IMAGES_FOLDER = Paths.get(System.getProperty("user.dir"), "images");

	private Path createFilePath(long imageId, Path folder) {
		return folder.resolve(imageId + ".jpeg");
	}

	public void saveImage(String folderName, long productId, MultipartFile image, Model model) throws IOException {
		Path folder = IMAGES_FOLDER.resolve("products");
		Files.createDirectories(folder);
		Path imagePath = folder.resolve(String.valueOf(productId) + ".jpeg");
		image.transferTo(imagePath);
		model.addAttribute("imageId", String.valueOf(productId));
	}

	public void deleteImage(String folderName, long imageId) throws IOException {
		Path folder = IMAGES_FOLDER.resolve(folderName);
		Path imageFile = createFilePath(imageId, folder);
		Files.deleteIfExists(imageFile);
	}

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
}