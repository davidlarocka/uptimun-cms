package com.davidlarocka.optimumcms.controllers.services;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	@Value("${optimum.output-data-art}")
    private String type_outputs;
	@Value("${optimum.template.arts}")
    private String path_templates;
	private  long ts;
	  
    public void defDirsName(long ts) {
        this.ts = ts;
    }

    public void generateOuputFiles(Art art)  throws IOException {
    	
    	site_art = parser.replaceTagForText(site_art, "_fechac", String.valueOf(ts).substring(0, 8));
    	
    	//Phersisting data in json
    	Files.createDirectories(Paths.get(site_art+"data/"));
		FileWriter output = new FileWriter(site_art+"data/"+ ts + ".json");
		output.write(this.EntityToStringJson(art));
		output.close();
    	
    	//create files to site (example: xml, json, html FROM optimum.output-data-art app prop)
    	Map<String, String> map = parser.StringJsonToMap(type_outputs);
    	map.forEach((k, v) -> {
    		try {
				Files.createDirectories(Paths.get(site_art+k));
				System.out.println("creating output arts:"+site_art + k  + "/"+ ts + "."+ v);
				//Get templates tags for type to make outputs
				template.setName("general."+v);//TODO: buscar nombre de template de tipo de arts
				template.setPath(path_templates+k+"/");
				//replace tags for content
				String content = template.generateOutput(art.getInputs());
				//only generate if template isn't empty and template exist = true
				if(content != null) {
					//generate output
					FileWriter output_file = new FileWriter(site_art + k  +"/"+ ts + "."+ v);
					if(v.equals("html")) {
						Document doc = Jsoup.parse(content);
						content = doc.toString();
					}
					output_file.write(content);
					output_file.close();
				}
				//System.out.println("created arts:"+site_art + k  + "/"+ ts + "."+ v);
			} catch (IOException e) {
				e.printStackTrace();
			}
        });
	}
    
    private String EntityToStringJson(Art ent) throws JsonProcessingException {
		 ObjectMapper mapper = new ObjectMapper();  
		 return mapper.writeValueAsString(ent);
	}
	
}
