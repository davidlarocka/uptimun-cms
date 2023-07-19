package com.davidlarocka.optimumcms.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TemplatesHelper {

	
	private String name;
	
	private String path;
	
	Map<String, String> MapTemplateTags;
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public String generateOutput() throws IOException {
		return this.getContentTemplate();
	}
	
	public String getTagsByTemplate() throws IOException{
		
		
		
		String content = this.getContentTemplate();
		
		
		return content;
	
	}
	
	private String getContentTemplate() throws IOException {
		String file = path+name;
		//System.out.println("reading template: "+file);
		
		File f = new File(file);
		if(f.exists() && !f.isDirectory()) { 
			String content =  new String(
		            Files.readAllBytes(Paths.get(file)));
			return content;
		}else {
			return null;
		}
		
		
	}
	
	
}
