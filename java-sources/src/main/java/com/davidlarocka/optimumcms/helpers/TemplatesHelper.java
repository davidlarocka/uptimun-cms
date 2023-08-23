package com.davidlarocka.optimumcms.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplatesHelper {

	private String name;
	private String path;
	private String pathMacros;
	Map<String, String> MapTemplateTags;

	@Autowired
	ParserHelper parser;

	public void setPath(String path) {
		this.path = path;
	}

	public void setPathMacros(String pathMacros) {
		this.pathMacros = pathMacros;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String generateOutputLanding(String areas) throws IOException {
		// get content undigest on template
		String undig_content = this.getContentTemplate();
		System.out.println("Areas: "+ areas);
		
		if (undig_content == null) {
			return "";
		}
		
		//System.out.println("content template: "+ undig_content);
		undig_content = this.getContentMacros(undig_content);
		//find areas
		String processed = parser.replaceAreasForArtsInfo(areas, undig_content);
		
		
		return processed;//TODO: process content
	}
	


	public String generateOutput(String inputs) throws IOException {

		// get content undigest on template
		String undig_content = this.getContentTemplate();
		if (undig_content == null) {
			return "";
		}
		undig_content = this.getContentMacros(undig_content);

		// get macros content

		// process tags
		Map<String, String> mapInputs = parser.StringJsonToMap(inputs);
		return parser.processTags(undig_content, mapInputs);
	}

	public String getTagsByTemplate() throws IOException {
		String content = this.getContentTemplate();
		return content;
	}

	private String getContentTemplate() throws IOException {
		String file = path + name;
		System.out.println("reading template: "+file);
		File f = new File(file);
		if (f.exists() && !f.isDirectory()) {
			String content = new String(Files.readAllBytes(Paths.get(file)));
			return content;
		} else {
			return null;
		}
	}

	public String getContentMacros(String content_template) throws IOException {
		return parser.getMacros(content_template, pathMacros);
	}

}
