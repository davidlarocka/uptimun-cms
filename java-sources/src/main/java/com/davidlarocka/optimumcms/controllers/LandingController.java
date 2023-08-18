package com.davidlarocka.optimumcms.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davidlarocka.optimumcms.controllers.services.FileService;
import com.davidlarocka.optimumcms.interfaces.LandingInterface;
import com.davidlarocka.optimumcms.models.Landing;

@RestController
@RequestMapping("/landing")
public class LandingController {

	@Autowired
	LandingInterface landings;
	
	@Autowired
	FileService files;
	
	@GetMapping
	public List<Landing> getAllLandings() {
		return landings.findAll();
	}
	
	@PostMapping
	public HttpStatus saveLandig(@RequestBody Landing savedlanding) throws IOException {
		Landing landing = null;
		if (savedlanding.getId() != null) {// update art
			if (landings.existsById(savedlanding.getId())) {
				landing = landings.getReferenceById(savedlanding.getId());
				landing.setName(savedlanding.getName());
				landing.setFile_url(savedlanding.getFile_url());
				landing.setTemplate(savedlanding.getTemplate());
				landing.setView(savedlanding.getView());
				landing.setUpdateCurrentEpoch();
				landing.setUrl(savedlanding.getUrl());
				landing.setPublished(savedlanding.isPublished());
				landing.setAreas(savedlanding.getAreas());
			} else {
				return HttpStatus.NOT_FOUND;
			}

		} else {// new landing
			landing = savedlanding;
			landing.setCreatedCurrentEpoch();
			//landing.setTsCurrentDate();
			landing.setUpdateCurrentEpoch();
		}
		// save on DB
		landings.save(landing);
		// generate output json html xml
		//files.defDirsName(art.getTs());
		files.generateOuputLanding(landing);
		return HttpStatus.OK;
	}
	
	
}
