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
	protected String regexPatnnerTag = "[%][%](.*)[%][%]";
	protected String ifBlock = "[%][%][i][f][\\(]+[a-zA-Z0-9_\\s\'\\=\\>\\<]+[\\)][%][%]";
	protected String endBlock = "[%][%][/][i][f][%][%]";
	protected String nifBlock = "[%][%][n][i][f][\\(]+[a-zA-Z0-9_\\s\']+[\\)][%][%]";
	protected String nendBlock = "[%][%][/][n][i][f][%][%]";

	private Map<String, String> mapInputsFid;

	// step 0
	public String processTags(String undig_content, Map<String, String> mapInputs) {
		boolean findMore = true;
		mapInputsFid = mapInputs;
		output = undig_content;
		output = output.replaceAll("\n", "").replaceAll("\r", "");
		// System.out.println("entrada >>>" + output);
		// #############simple tags
		mapInputs.forEach((k, v) -> {
			output = output.replaceAll("[%][%]" + k + "[%][%]", v);
			// System.out.println("%%" + k + "%%" + v);
		});

		// ############### conditional Tags
		while (findMore) {
			findMore = this.findBlockConditional(output);
		}

		// loops tags TODO

		// reference to landing tags TODO

		// clean unprocess "%%"
		output = output.replaceAll(regexPatnnerTag, "");
		// System.out.println("\n\n\n\n######################### salida final:" +
		// output);
		return output;

	}

	// #### step 1
	private boolean findBlockConditional(String in) {
		// System.out.println("\n\n\n\n######################### inicio in: " + in);
		// 1.1 buscar primer %%if%%
		Matcher mifMatches = this.matcherIn(in, ifBlock + "|" + nifBlock); // Pattern.compile(ifBlock).matcher(in);

		while (mifMatches.find()) {
			if (mifMatches.start() != 0) {
				// catch block if
				String textBlock = in.substring(mifMatches.start(), in.length());
				// find first end
				Matcher EndMatches = Pattern.compile(endBlock + "|" + nendBlock).matcher(textBlock);

				List<Integer> startTagend = new ArrayList<Integer>();
				List<Integer> endTagend = new ArrayList<Integer>();

				while (EndMatches.find()) {
					// System.out.println("Tag end : "+EndMatches.start());
					startTagend.add(EndMatches.start());
					endTagend.add(EndMatches.end());
				}

				// count nro %%if|%%nif( beetwen textBlock and first end
				int ocurrencies = ((int) this
						.matcherIn(textBlock.substring(0, startTagend.get(0)), ifBlock + "|" + nifBlock).results()
						.count());

				// find closed end
				// System.out.println("\n\n find in block: " + textBlock.substring(0,
				// startTagend.get(0) ) );
				// System.out.println("\n\n has : " + ocurrencies +" in " +
				// textBlock.substring(0, endTagend.get(0) ));
				// System.out.println("\n\n found block: " + textBlock.substring(0,
				// endTagend.get(ocurrencies -1) ) );
				output = output.replace(textBlock.substring(0, endTagend.get(ocurrencies - 1)),
						this.replaceBlockIF(textBlock.substring(0, endTagend.get(ocurrencies - 1))));
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
		matcherTemp = this.matcherIn(b, ifBlock + "|" + nifBlock);

		while (matcherTemp.find()) {
			foundM = b.substring(matcherTemp.start(), matcherTemp.end());
			int lenthEndBlock = (foundM.toLowerCase().indexOf("%%if(") != -1) ? 7 : 8; // 7 is length of "%%/if%% 
			foundContent = b.substring(matcherTemp.end(), (b.length() - lenthEndBlock));
			// System.out.println("\n\nStart index: " + matcherTemp.start() +"End index: " +
			// matcherTemp.end() + "\n in: " + b +"\nFound tag: " + foundM+ "\nFound text: "
			// + foundContent);
			break;
		}

		// replace contents tags in Block
		// CASES IF
		mapInputsFid.forEach((k, v) -> {
			// case simple IF true
			if (k.equals(foundM.replace("%%if(", "").replace(")%%", ""))) {
				// System.out.println("\n ########## replace: " + b + " by: >> " +
				// foundContent);
				replaceTemp = b.replace(b, foundContent);
			}
			// case IF EQ = true
			else if ((foundM.toLowerCase().indexOf(" eq ") != -1)
					&& (k.equals(foundM.toLowerCase().split("eq")[0].replace("%%if(", "").replace(" ", "")))) {
				// System.out.println("\n ########## replace EQ case: " + b + " POR: >> " +
				// foundContent + " >> " +foundM.toLowerCase().split("eq")[1].replace(")%%",
				// "").replace(" ", "").replace("\'", "").replace("\"", ""));
				if (v.replace(" ", "").equals(foundM.toLowerCase().split("eq")[1].replace(")%%", "").replace(" ", "")
						.replace("\'", "").replace("\"", ""))) {
					// delete case else if exist
					if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
						foundContent = this.deleteBlockElse(foundContent);
					}
					replaceTemp = b.replace(b, foundContent);
					// System.out.println("\n ########## replace EQ case OK: " + b + " by: >> " +
					// foundContent);
					// case IF EQ = false > ELSE
				} else if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
					replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
				}
			}

			// case IF "==" numeric true
			else if ((foundM.toLowerCase().indexOf("==") != -1)) {
				if (k.equals(foundM.toLowerCase().split("\\=\\=")[0].replace("%%if(", "").replace(" ", ""))) {
					if (v.replace(" ", "").equals(foundM.toLowerCase().split("\\=\\=")[1].replace(")%%", "")
							.replace(" ", "").replace("\'", "").replace("\"", ""))) {
						// delete case else if exist
						if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
							foundContent = this.deleteBlockElse(foundContent);
						}
						replaceTemp = b.replace(b, foundContent);
						//System.out.println("\n ########## replace == case OK: " + b + " by: >> " + foundContent);
						// case IF == false > ELSE
					} else if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
						replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
					}
				}
			}

			// case IF ">=" numeric true
			else if ((foundM.toLowerCase().indexOf(">=") != -1)) {
				if (k.equals(foundM.toLowerCase().split("\\>\\=")[0].replace("%%if(", "").replace(" ", ""))) {
					if (this.stringToInt(v.replace(" ", "")) >= this
							.stringToInt((foundM.toLowerCase().split("\\>\\=")[1].replace(")%%", "").replace(" ", "")
									.replace("\'", "").replace("\"", "")))) {
						// delete case else if exist
						if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
							foundContent = this.deleteBlockElse(foundContent);
						}
						replaceTemp = b.replace(b, foundContent);
						//System.out.println("\n ########## replace >= case OK: " + b + " by: >> " + foundContent);
						// case IF >= false > ELSE
					} else if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
						replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
					}
				}
			}

			// case IF "<=" numeric true
			else if ((foundM.toLowerCase().indexOf("<=") != -1)) {
				if (k.equals(foundM.toLowerCase().split("\\<\\=")[0].replace("%%if(", "").replace(" ", ""))) {
					if (this.stringToInt(v.replace(" ", "")) <= this
							.stringToInt((foundM.toLowerCase().split("\\<\\=")[1].replace(")%%", "").replace(" ", "")
									.replace("\'", "").replace("\"", "")))) {
						// delete case else if exist
						if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
							foundContent = this.deleteBlockElse(foundContent);
						}
						replaceTemp = b.replace(b, foundContent);
						//System.out.println("\n ########## replace <= case OK: " + b + " by: >> " + foundContent);
						// case IF >= false > ELSE
					} else if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
						replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
					}
				}
			}

			// case IF ">" numeric true
			else if ((foundM.toLowerCase().indexOf(">") != -1)) {
				if (k.equals(foundM.toLowerCase().split("\\>")[0].replace("%%if(", "").replace(" ", ""))) {
					if (this.stringToInt(v.replace(" ", "")) > this.stringToInt((foundM.toLowerCase().split("\\>")[1]
							.replace(")%%", "").replace(" ", "").replace("\'", "").replace("\"", "")))) {
						// delete case else if exist
						if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
							foundContent = this.deleteBlockElse(foundContent);
						}
						replaceTemp = b.replace(b, foundContent);
						// System.out.println("\n ########## replace > case OK: " + b + " by: >> " +
						// foundContent);
						// case IF > false > ELSE
					} else if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
						replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
					}
				}
			}

			// case IF ">" numeric true
			else if ((foundM.toLowerCase().indexOf("<") != -1)) {
				if (k.equals(foundM.toLowerCase().split("\\<")[0].replace("%%if(", "").replace(" ", ""))) {
					if (this.stringToInt(v.replace(" ", "")) < this.stringToInt((foundM.toLowerCase().split("\\<")[1]
							.replace(")%%", "").replace(" ", "").replace("\'", "").replace("\"", "")))) {
						// delete case else if exist
						if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
							foundContent = this.deleteBlockElse(foundContent);
						}
						replaceTemp = b.replace(b, foundContent);
						// System.out.println("\n ########## replace < case OK: " + b + " by: >> " +
						// foundContent);
						// case IF > false > ELSE
					} else if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
						replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
					}
				}
			}

			// case IF NE = true
			else if ((foundM.toLowerCase().indexOf(" ne ") != -1)
					&& (k.equals(foundM.toLowerCase().split("ne")[0].replace("%%if(", "").replace(" ", "")))) {
				if (!v.replace(" ", "").equals(foundM.toLowerCase().split("ne")[1].replace(")%%", "").replace(" ", "")
						.replace("\'", "").replace("\"", ""))) {
					// delete case else if exist
					if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
						foundContent = this.deleteBlockElse(foundContent);
					}
					replaceTemp = b.replace(b, foundContent);
					// System.out.println("\n ########## replace NE case OK: " + b + " by: >> " +
					// foundContent);

				} else if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
					// case IF NE = false > ELSE
					// System.out.println("\n ########## replace ELSE case OK: " + b + " POR: >> "
					// + foundContent.split("[%][%][e][l][s][e][%][%]")[1]);
					replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
				}
			}

		});

		// ############# Cases NIF
		// case simple NIF true
		if (foundM.toLowerCase().indexOf("%%nif(") != -1) {
			if (!mapInputsFid.containsKey(foundM.replace("%%nif(", "").replace(")%%", ""))) {
				// System.out.println("\n ########## CASE NIF: " + b + " by: >> "
				// +foundContent);
				replaceTemp = b.replace(b, foundContent);
			} else {
				if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
					replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
				}
			}
		}

		return replaceTemp;
	}

	private int stringToInt(String str) {
		str = str.replaceAll("[a-zA-Z\\=]", "");
		return Integer.parseInt(str);
	}

	private String replaceBlockElse(String in) {
		// System.out.println(
		// "\n ########## replace ELSE case OK: " + in + " POR: >> " +
		// in.split("[%][%][e][l][s][e][%][%]")[1]);
		return in.replace(in, in.split("[%][%][e][l][s][e][%][%]")[1]);
	}

	private String deleteBlockElse(String in) {
		// System.out.println(
		// "\n ########## delete ELSE case: " + in + " POR: >> " +
		// in.split("[%][%][e][l][s][e][%][%]")[0]);
		return in.replace(in, in.split("[%][%][e][l][s][e][%][%]")[0]);
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
