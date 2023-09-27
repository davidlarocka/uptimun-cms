package com.davidlarocka.optimumcms.controllers.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.davidlarocka.optimumcms.helpers.ParserHelper;
import com.davidlarocka.optimumcms.helpers.TemplatesHelper;
import com.davidlarocka.optimumcms.models.Art;
import com.davidlarocka.optimumcms.models.Landing;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class FileService {

	@Autowired
	ParserHelper parser;
	
	@Autowired
	TemplatesHelper template;
	
	@Value("${optimum.project-name}")
    private String project_name;
	@Value("${optimum.site}")
    private String site_dir;
	@Value("${optimum.site.art}")
    private String site_art;
	
	@Value("${optimum.site.art.webRoot}")
	protected String webRoot;
	
	@Value("${optimum.site.landing}")
    private String site_landing;
	
	@Value("${optimum.output-data-art}")
    private String type_outputs;
	
	@Value("${optimum.output-data-landing}")
    private String type_outputs_landings;
	
	@Value("${optimum.template.arts}")
    private String path_templates;
	
	@Value("${optimum.template.landings}")
    private String path_templates_landings;
	private  long ts;
	  
    public void defDirsName(long ts) {
        this.ts = ts;
    }

    public void generateOuputLanding(Landing landing)  throws IOException {
    
    	String view = (landing.getView() == null)?  "main" : landing.getView();
    	site_landing = parser.replaceTagForText(site_landing, "_view", view);
    	
    	//Phersisting data in json
    	Files.createDirectories(Paths.get(site_landing+"data/"));
		FileWriter output = new FileWriter(site_landing+"data/"+ landing.getName().replace(" ", "_").toLowerCase() + ".json");
		output.write(this.EntitylandingToStringJson(landing));
		output.close();
    
		
		if(landing.isPublished()) {
			Map<String, String> map = parser.StringJsonToMap(type_outputs_landings);
	    	map.forEach((k, v) -> {
	    		try {
					Files.createDirectories(Paths.get(site_landing+k));
					//System.out.println("creating output arts:"+site_art + k  + "/"+ ts + "."+ v);
					//Get templates tags for type to make outputs
					template.setName(landing.getTemplate() );
					template.setPath(path_templates_landings+view+"/"+k+"/");
					template.setPathMacros(path_templates_landings);
					//replace tags for content
					String content = template.generateOutputLanding(landing.getAreas());
					//only generate if template isn't empty and template exist = true
					if(content != null ) {
						//generate output
						FileWriter output_file = new FileWriter(site_landing + k+"/"+landing.getTemplate());
						/*if(v.equals("html")) {
							Document doc = Jsoup.parse(content);
							content = doc.toString();
						}//TODO:validar para que no formatee html para parciales */
						output_file.write(content);
						output_file.close();
					}
					//System.out.println("created arts:"+site_art + k  + "/"+ ts + "."+ v);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        });
		}else {
			Map<String, String> map = parser.StringJsonToMap(type_outputs_landings);
			map.forEach((k, v) -> {
				File file = new File(site_landing + k  +"/"+ ts + "."+ v);
				file.delete();
			});
			
		}
     
    }
    
    public void generateOuputFiles(Art art)  throws IOException {
    	
    	String path = parser.replaceTagForText(site_art, "_fechac", String.valueOf(ts).substring(0, 8));
    	
    	//Phersisting data in json
    	Files.createDirectories(Paths.get(path+String.valueOf(ts)+"/data/"));
		FileWriter output = new FileWriter(path+String.valueOf(ts)+"/data/"+ ts + ".json");
		output.write(this.EntityToStringJson(art));
		output.close();
    	
    	//create files to site (example: xml, json, html FROM optimum.output-data-art app prop)
		if(art.isPublished()) {
			Map<String, String> map = parser.StringJsonToMap(type_outputs);
	    	map.forEach((k, v) -> {
	    		
	    		try {
					Files.createDirectories(Paths.get(path+String.valueOf(ts)+"/"+k));
					System.out.println("creating output arts:"+path+String.valueOf(ts)+"/" + k  + "/"+ ts + "."+ v);
					//Get templates tags for type to make outputs
					template.setTs(ts);
					template.setName(art.getType_art() + "." +v);//v eq to extension. example general.html
					template.setPath(path_templates+k+"/");
					template.setPathMacros(path_templates);
					//replace tags for content
					String content = template.generateOutput(art.getAllInfo());
					//only generate if template isn't empty and template exist = true
					if(content != null ) {
						//generate output
						FileWriter output_file = new FileWriter(path+String.valueOf(ts)+"/" + k  +"/"+ ts + "."+ v);
						if(v.equals("html")) {
							Document doc = Jsoup.parse(content);
							content = doc.toString();
						}
						output_file.write(content);
						output_file.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
	        });
	    	
	    	
	    	//create output info for ssi by tag
			map = parser.StringJsonToMap(art.getAllInfo());
			map.forEach((k, v) -> {
				FileWriter separateData;
				try {
					separateData = new FileWriter(path+String.valueOf(ts)+"/data/"+k+".html");
					separateData.write(v);
					separateData.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
	    	
		}else {
			Map<String, String> map = parser.StringJsonToMap(type_outputs);
			map.forEach((k, v) -> {
				File file = new File(path+String.valueOf(ts)+"/" + k  +"/"+ ts + "."+ v);
				file.delete();
			});
			
		}
	}
    
    public String generateUrlFileByTs(long ts) {
    	String path = parser.replaceTagForText(webRoot, "_fechac", String.valueOf(ts).substring(0, 8));
    	return path+ts+"/pags/"+ts+".html";
    }
    
    private String EntityToStringJson(Art ent) throws JsonProcessingException {
		 ObjectMapper mapper = new ObjectMapper();
		 mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		 return mapper.writeValueAsString(ent);
	}
    
    private String EntitylandingToStringJson(Landing ent) throws JsonProcessingException {
		 ObjectMapper mapper = new ObjectMapper();
		 mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		 return mapper.writeValueAsString(ent);
	}
	
}
