package com.davidlarocka.optimumcms.controllers.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.davidlarocka.optimumcms.interfaces.MediaMethodsInterface;


@Service
public class MediaService implements MediaMethodsInterface {

	
	@Value("${optimum.site.uploads}")
    private String site_dir_uploads;
	
 
	  public void init() {
	    try {
	    	Files.createDirectories(Paths.get(site_dir_uploads));
	    } catch (IOException e) {
	      throw new RuntimeException("Could not initialize folder for upload!");
	    }
	  }

	  public String save(MultipartFile file) {
	    try {
	    	
	      String nameFile = this.getNameMedia();
	      Files.copy(file.getInputStream(), Paths.get(site_dir_uploads+nameFile));
	      System.out.println("created media in:"+ Paths.get(site_dir_uploads+nameFile) );
	      return site_dir_uploads+nameFile;
	    } catch (Exception e) {
	      if (e instanceof FileAlreadyExistsException) {
	        throw new RuntimeException("A file of that name already exists.");
	      }

	      throw new RuntimeException(e.getMessage());
	    }
	  }
	
	  public Resource load(String filename) {
		    try {
		      Path file = Paths.get(site_dir_uploads).resolve(filename);
		      Resource resource = new UrlResource(file.toUri());

		      if (resource.exists() || resource.isReadable()) {
		        return resource;
		      } else {
		        throw new RuntimeException("Could not read the file!");
		      }
		    } catch (MalformedURLException e) {
		      throw new RuntimeException("Error: " + e.getMessage());
		    }
		  }
	  
	  private String getNameMedia() throws IOException {
		  Files.createDirectories(Paths.get(site_dir_uploads));
		  String mediaName = "photo_"+Instant.now().getEpochSecond(); //TODO: set name with media type 
		  return mediaName;
	  }
	
	
}
