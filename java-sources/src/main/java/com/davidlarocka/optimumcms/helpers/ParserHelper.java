package com.davidlarocka.optimumcms.helpers;

import org.springframework.stereotype.Service;

@Service
public class ParserHelper {
	
	public String replaceTemplateTags(String textEndigest, String tag, String value) {
		String res = textEndigest.replace("%%"+tag+"%%" , value);
		return res;
	}
	
}
