package com.davidlarocka.optimumcms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.davidlarocka.optimumcms.interfaces.MediaMethodsInterface;
import com.davidlarocka.optimumcms.interfaces.MediaStorageInterface;
import com.davidlarocka.optimumcms.models.Media;
@RestController
@RequestMapping("/media")
public class MediaController {

	@Autowired
	MediaStorageInterface mediaStorage;
	@Autowired
	MediaMethodsInterface mediaMethods;
	private Media media;
	
	  @PostMapping
	  public HttpStatus uploadFile(@RequestParam("file") MultipartFile file, @RequestParam String title, long ts) {
		 
		  try {
			media = new Media();
	    	String filename = mediaMethods.save(file);
	    	media.setUrl(filename);
	    	media.setTs(ts);
	    	media.setTitle(title);
	    	mediaStorage.save(media);
	      return HttpStatus.OK;
	    } catch (Exception e) {
	    System.out.println("err:"+ e );
	      return HttpStatus.NOT_FOUND;
	    }
	  }
	
	
}
