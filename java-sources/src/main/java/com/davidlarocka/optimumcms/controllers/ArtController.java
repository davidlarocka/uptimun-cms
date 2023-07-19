package com.davidlarocka.optimumcms.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	public List<Art> getAllArts(){
		return arts.findAll();
	}
	
	@PostMapping
	public void newArt(@RequestBody Art art) throws IOException  {
		//save on DB
		art.setCreatedCurrentEpoch();
		art.setTsCurrentEpoch();
		art.setUpdateCurrentEpoch();
		arts.save(art);
		
		//generate output json html xml
		files.defDirsName(art.getTs());
		files.generateOuputFiles(art);
	}
	
}


