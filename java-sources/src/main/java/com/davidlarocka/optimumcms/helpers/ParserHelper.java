package com.davidlarocka.optimumcms.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.davidlarocka.optimumcms.helpers.lang.LangHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ParserHelper {
	
	@Autowired
	LangHelper langs;
	private String output;
	private Matcher matcherTemp;
	private String replaceTemp;
	private String foundM;
	private String foundContent;
	protected String regexSimpleIfNested = "[%][%][i][f][\\(]+[1-9a-zA-Z\\-\\_]+[\\)][%][%]+[1-9a-zA-Z\\-\\_\\s\\<\\>\\/\\*\\&\\$\\¿\\?\\´\\'\\\"\\[ \\] \\{ \\} \\| \\¨ \\. \\%]+[%][%][\\/][i][f][%][%]"; 
	protected String regexsimpleIf = "[%][%][i][f][\\(](.*)[\\)]%%(.*)%%[\\/][i][f][%][%]";
	protected String regexPatnnerTag = "[%][%](.*)[%][%]";
	protected String ifBlock = "[%][%][i][f][\\(]+[a-zA-Z0-9_\\s]+[\\)][%][%]";
	protected String endBlock = "[%][%][/][i][f][%][%]";
	
	private Map<String, String> mapInputsFid;

	// step 0
	public String processTags(String undig_content, Map<String, String> mapInputs) {
		boolean findMore = true;
		mapInputsFid = mapInputs;
		output = undig_content;
		output = output.replaceAll("\n", "").replaceAll("\r", "");

		
		// #############simple tags
		mapInputs.forEach((k, v) -> {
			output = output.replaceAll("[%][%]" + k + "[%][%]", v);
			//System.out.println("%%" + k + "%%" + v);
		});

		// ############### conditional Tags		
		while (findMore) {
	    	findMore = this.findBlockConditional(output);
	    }
		
	    
		// loops tags TODO

		// reference to landing tags TODO
		
		//clean unprocess "%%"
		output = output.replaceAll(regexPatnnerTag, "");
		System.out.println("\n\n\n\n######################### salida final:" + output);
		return output;

	}

	// #### step 1
	private boolean findBlockConditional(String in) {
		//System.out.println("\n\n\n\n######################### inicio in: " + in);
		//1.1 buscar primer %%if%%
		Matcher mifMatches = this.matcherIn(in, ifBlock); //Pattern.compile(ifBlock).matcher(in);
		
		while (mifMatches.find()) {
			if( mifMatches.start() != 0 ) {
				//catch block if
				String textBlock = in.substring(mifMatches.start(), in.length());
				//find first end
				Matcher EndMatches = Pattern.compile(endBlock).matcher(textBlock);
				
				List<Integer> startTagend = new ArrayList<Integer>();
				List<Integer>  endTagend = new ArrayList<Integer>();
				
				while (EndMatches.find()) { 
			         //System.out.println("Tag end : "+EndMatches.start());    
			         startTagend.add(EndMatches.start()) ;
			         endTagend.add(EndMatches.end()) ;
				}
						
				//count nro %%if( beetwen textBlock and first end
				int ocurrencies =  (int) this.matcherIn(textBlock.substring(0, startTagend.get(0)), ifBlock).results().count();
				
				//find closed end
				//System.out.println("\n\n find in block: " + textBlock.substring(0, startTagend.get(0) ) );
				//System.out.println("\n\n has : " + ocurrencies +" in " + textBlock.substring(0, endTagend.get(0) ));
				//System.out.println("\n\n found block: " + textBlock.substring(0, endTagend.get(ocurrencies -1) ) );
				output = output.replace(textBlock.substring(0, endTagend.get(ocurrencies -1) ), 
									this.replaceBlockIF(textBlock.substring(0, endTagend.get(ocurrencies -1))));
				return true;
			}
			break;
		}
		return false;
	}


	// ##### step 2
	private String replaceBlockIF(String b) {
		replaceTemp = "";
		foundM = "";
		foundContent = "";
		matcherTemp = this.matcherIn(b, "[%][%][i][f][\\\\(]+[a-zA-Z0-9_\\\\s]+[\\\\)][%][%]");

		while (matcherTemp.find()) {
			foundM = b.substring(matcherTemp.start(), matcherTemp.end());
			foundContent = b.substring(matcherTemp.end(), (b.length()-7));
			
			System.out.println("\n\nStart index: " + matcherTemp.start() +"End index: " + matcherTemp.end() + "\n in: " + b +"\nFound tag: " + foundM+ "\nFound text: " + foundContent);
			break;
		}

		// replace contents tags in Block
		mapInputsFid.forEach((k, v) -> {
			if (k.equals(foundM.replace("%%if(", "").replace(")%%", ""))) {
				System.out.println("\n ########## remplace: " + b + " POR: >> " + foundContent);
				replaceTemp = b.replace(b , foundContent);
			}
		});
		return replaceTemp;//
	}

	private Matcher matcherIn(String in, String regenx) {
		return Pattern.compile(regenx).matcher(in);
	}
	
	public String replaceTagForText(String textEndigest, String tag, String value) {
		String res = textEndigest.replace("%%" + tag + "%%", value);
		return res;
	}

	public Map<String, String> StringJsonToMap(String json) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, String> map = mapper.readValue(json, Map.class);
		return map;
	}

}
