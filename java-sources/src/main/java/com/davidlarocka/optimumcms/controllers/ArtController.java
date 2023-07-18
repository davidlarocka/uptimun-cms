package com.davidlarocka.optimumcms.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davidlarocka.optimumcms.controllers.helpers.FileHerper;
import com.davidlarocka.optimumcms.interfaces.ArtInterface;
import com.davidlarocka.optimumcms.models.Art;

@RestController
@RequestMapping("/art")
public class ArtController {

	@Autowired
	ArtInterface arts;
	
	@Value("${optimum.site}")
    private String site_dir;
	
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
		FileHerper files = new FileHerper(site_dir, art.getTs() );
		files.generateOuputFiles(art);
		
		 
	}
	
	
	
}


