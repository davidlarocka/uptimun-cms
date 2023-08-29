package com.davidlarocka.optimumcms.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davidlarocka.optimumcms.controllers.services.FileService;
import com.davidlarocka.optimumcms.interfaces.ArtInterface;
import com.davidlarocka.optimumcms.models.Art;

@RestController
@RequestMapping("/art")
public class ArtController {

	@Autowired
	ArtInterface arts;

	@Autowired
	FileService files;

	@GetMapping
	public List<Art> getAllArts() {
		return arts.findAll();
	}

	@PostMapping
	public HttpStatus saveArt(@RequestBody Art savedArt) throws IOException {
		Art art = null;
		if (savedArt.getId() != null) {// UPDATE art
			if (arts.existsById(savedArt.getId())) {
				art = arts.getReferenceById(savedArt.getId());
				art.setTitle(savedArt.getTitle());
				art.setFile_url(savedArt.getFile_url());
				art.setUrl(savedArt.getUrl());
				art.setUpdateCurrentEpoch();
				art.setPublished(savedArt.isPublished());
				art.setInputs(savedArt.getInputs());
				art.setFile_url(savedArt.getFile_url());
			} else {
				return HttpStatus.NOT_FOUND;
			}
		} else {// NEW art
			art = savedArt;
			art.setCreatedCurrentEpoch();
			art.setTsCurrentDate();
			art.setUpdateCurrentEpoch();
			art.setFile_url( files.generateUrlFileByTs(art.getTs()));
		}
		// save on DB
		arts.save(art);
		// generate output json html xml
		files.defDirsName(art.getTs());
		files.generateOuputFiles(art);
		return HttpStatus.OK;
	}
	
	@DeleteMapping
	public HttpStatus deleteArt(@RequestBody Art deletedArt) throws IOException {
		Art art = null;
		if (arts.existsById(deletedArt.getId())) {
			art = arts.getReferenceById(deletedArt.getId());
			art.setPublished(false);
		} else {
			return HttpStatus.NOT_FOUND;
		}
		
		files.defDirsName(art.getTs());
		files.generateOuputFiles(art);
		arts.delete(deletedArt);
		return HttpStatus.OK;
	}

}
