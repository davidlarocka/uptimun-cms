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
	protected String regexSimpleIfNested = "[%][%][i][f][\\(]+[1-9a-zA-Z\\-\\_]+[\\)][%][%]+[1-9a-zA-Z\\-\\_\\s\\<\\>\\/\\*\\&\\$\\¿\\?\\´\\'\\\"\\[ \\] \\{ \\} \\| \\¨ \\]+[%][%][\\/][i][f][%][%]"; 
	protected String regexsimpleIf = "[%][%][i][f][\\(](.*)[\\)]%%(.*)%%[\\/][i][f][%][%]";
	protected String regexPatnnerTag = "[%][%](.*)[%][%]";
	
	private Map<String, String> mapInputsFid;

	// step 0
	public String processTags(String undig_content, Map<String, String> mapInputs) {

		mapInputsFid = mapInputs;

		output = undig_content;
		output = output.replaceAll("\n", "").replaceAll("\r", "");

		// #############simple tags
		mapInputs.forEach((k, v) -> {
			output = output.replaceAll("[%][%]" + k + "[%][%]", v);
			System.out.println("%%" + k + "%%" + v);

		});

		// ############### conditional Tags
		output = this.findBlockConditional(output);

		// loops tags

		// reference to landing tags
		
		//clean unprocess "%%"
		output = output.replaceAll(regexPatnnerTag, "");

		System.out.println("\n\n\n\n######################### salida final:" + output);
		return output;

	}

	// #### step 1
	private String findBlockConditional(String in) {

		List<String> blocks = new ArrayList<String>();
		//List<Integer> ifMatches = new ArrayList<Integer>();

		Matcher mifMatches = Pattern.compile(regexSimpleIfNested).matcher(in);
		int i = 0;

		while (mifMatches.find()) {

			System.out.println("Start index: " + mifMatches.start());
			System.out.println("End index: " + mifMatches.end());
			
			blocks.add(in.substring(mifMatches.start(), mifMatches.end()));
			i++;
		}

		if (i == 0) {
			return null;
		}
		// TODO: compare "if" starts equivalent to endings
		return this.processIfBlock(blocks);

	}

	//// #### step 2
	private String processIfBlock(List<String> blocks) {

		ArrayList<ArrayList<String>> processBlock = new ArrayList<>();

		for (String s : blocks) {
			System.out.println("\n\n\n ######## blocks result: " + s);
			ArrayList<String> idx = new ArrayList<String>();
			idx.add(s);
			idx.add(this.replaceBlockIF(s));
			processBlock.add(idx);
		}

		for (ArrayList<String> str : processBlock) {
			System.out.println("\n\n########## block: " + str.get(0)+"\n##########replace by: " + str.get(1));
			output = output.replace(str.get(0), str.get(1));
		}
		
		System.out.println("\n\n########## Output: " + output);
		//recursive to refind nested if
		this.findBlockConditional(output);
		
		return output;
	}

	// ##### step 3
	private String replaceBlockIF(String b) {
		replaceTemp = "";
		foundM = "";
		foundContent = "";

		matcherTemp = this.matcherIn(b, regexsimpleIf);

		while (matcherTemp.find()) {
			foundM = matcherTemp.group(1);
			foundContent = matcherTemp.group(2);
			System.out.println("Start index: " + matcherTemp.start() +"End index: " + matcherTemp.end() + "Match result: " + matcherTemp.group(0)+"Found tag: " + matcherTemp.group(1) + "Found text: " + matcherTemp.group(2));
		}

		// replace contents tags in Block
		mapInputsFid.forEach((k, v) -> {
			if (k.equals(foundM)) {
				System.out.println("\n\n\n\n ########## remplace: " + k + "POR: >> " + v);
				replaceTemp = b.replace(b, foundContent);
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
