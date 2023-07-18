package com.davidlarocka.optimumcms.controllers.services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.davidlarocka.optimumcms.models.Art;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class FileService {

	private  String site_dir;
	private  long ts;
	  
    public FileService(String site_dir, long ts) {
        this.site_dir = site_dir;
        this.ts = ts;
    }

	private String EntityToStringJson(Art ent) throws JsonProcessingException {
		
		 ObjectMapper mapper = new ObjectMapper();  
		 return mapper.writeValueAsString(ent);
		
	}
	

    public void generateOuputFiles(Art art)  throws IOException {
    	
    	System.out.print("el directorio es: " + site_dir);
    	Files.createDirectories(Paths.get(site_dir));
    	
		//create files to site
		FileWriter outputJson = new FileWriter(site_dir + ts + ".json");
		outputJson.write(this.EntityToStringJson(art));
		outputJson.close();
		
	}
	
}
