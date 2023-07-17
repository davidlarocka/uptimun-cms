package com.davidlarocka.optimumcms.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davidlarocka.optimumcms.interfaces.ArtInterface;
import com.davidlarocka.optimumcms.models.Art;

@RestController
@RequestMapping("/art")
public class ArtController {

	@Autowired
	ArtInterface arts;
	
	@GetMapping
	public List<Art> getAllArts(){
		return arts.findAll();
	}
	
	@PostMapping
	public void newArt(@RequestBody Art art) {
		art.setCreatedCurrentEpoch();
		art.setTsCurrentEpoch();
		art.setUpdateCurrentEpoch();
		arts.save(art);
	}
	
}


