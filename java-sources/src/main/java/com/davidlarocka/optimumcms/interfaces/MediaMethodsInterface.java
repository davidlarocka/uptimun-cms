package com.davidlarocka.optimumcms.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface MediaMethodsInterface{
	
	public void init();
	public String save(MultipartFile file);
	public Resource load(String filename);

}
