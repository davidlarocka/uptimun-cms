package com.davidlarocka.optimumcms.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	protected String regexPatnnerTag = "[%][%]+(\\w|\\w.\\w)+[%][%]";
	protected String ifBlock = "[%][%][i][f][\\(]+[a-zA-Z0-9_\\s\'\\=\\>\\<]+[\\)][%][%]";
	protected String endBlock = "[%][%][/][i][f][%][%]";
	protected String nifBlock = "[%][%][n][i][f][\\(]+[a-zA-Z0-9_\\s\']+[\\)][%][%]";
	protected String nendBlock = "[%][%][/][n][i][f][%][%]";
	protected String loopArtic = "[%][%]_loop_artic\\([0-9\\s][,][0-9\\s][\\)][%][%]";
	protected String endloopArtic = "[%][%][/]_loop_artic[%][%]";
	protected String macros = "[%][%]macro[\\(]+[a-zA-Z0-9_\\s\\'\\=\\>\\<\\.]+[\\)][%][%]";

	private Map<String, String> mapInputsFid;

	// step 0
	public String processTags(String undig_content, Map<String, String> mapInputs) {
		boolean findMore = true;
		mapInputsFid = mapInputs;
		output = undig_content;
		output = output.replaceAll("\n", "").replaceAll("\r", "");
		//System.out.println("entrada >>>" + output);

		// loops tags
		while (findMore) {
			findMore = this.findLoopersBlock(output);
		}

		// #############simple tags
		mapInputs.forEach((k, v) -> {
			output = output.replaceAll("[%][%]" + k + "[%][%]", v);
			//System.out.println("%%" + k + "%%" + v);
		});

		findMore = true;
		// ############### conditional Tags
		while (findMore) {
			findMore = this.findBlockConditional(output);
		}

		// reference to landing tags TODO

		// clean unprocess "%%"
		// System.out.println("\n\n\n\n######################### salida final antes de
		// limpiar: " + output);
		output = output.replaceAll(regexPatnnerTag, "");
		// System.out.println("\n\n\n\n######################### salida final:" +
		// output);
		return output;

	}

	public String getMacros(String content, String path) throws IOException {
		boolean findMore = true;
		output = content;
		output = output.replaceAll("\n", "").replaceAll("\r", "");

		while (findMore) {
			findMore = this.findMacro(output, path);
		}

		return output;
	}

	private boolean findMacro(String in, String path) throws IOException {

		//System.out.println("\n\n\n\n######################### inicio in: " + in);
		Matcher macrosMatches = this.matcherIn(in, macros);

		while (macrosMatches.find()) {
			if (macrosMatches.start() != 0) {
				// get content
				String textBlock = in.substring(macrosMatches.start(), macrosMatches.end());
				path = path + "macros/" + textBlock.replace(" ", "").replace("%%macro(", "").replace(")%%", "");
				//System.out.println("\n\n\n\n###########Find content macro in: " + path);
				String macroContent = "macro: " + path + " Not found";
				File f = new File(path);
				if (f.exists() && !f.isDirectory()) {
					macroContent = new String(Files.readAllBytes(Paths.get(path))).replaceAll("\n", "").replaceAll("\r",
							"");
				}
				//System.out.println("\n macro: " + textBlock);
				//System.out.println("\n replace by: " + macroContent);
				output = output.replace(textBlock, macroContent);
				return true;
			}
			break;
		}
		return false;
	}

	// #### step 1
	private boolean findLoopersBlock(String in) {
		// System.out.println("\n\n\n\n######################### inicio in: " + in);
		// 1.1 buscar primer %%if%%
		Matcher loopMatches = this.matcherIn(in, loopArtic);

		while (loopMatches.find()) {
			if (loopMatches.start() != 0) {
				// catch block if
				String textBlock = in.substring(loopMatches.start(), in.length());
				// find first end
				Matcher EndMatches = Pattern.compile(endloopArtic).matcher(textBlock);

				List<Integer> startTagend = new ArrayList<Integer>();
				List<Integer> endTagend = new ArrayList<Integer>();

				while (EndMatches.find()) {
					// System.out.println("Tag end : "+EndMatches.start());
					startTagend.add(EndMatches.start());
					endTagend.add(EndMatches.end());
				}

				// count nro %%loop_artic beetwen textBlock and first end
				int ocurrencies = ((int) this.matcherIn(textBlock.substring(0, startTagend.get(0)), loopArtic).results()
						.count());

				// find closed end
				// System.out.println("\n\n find in block: " + textBlock.substring(0,
				// startTagend.get(0) ) );
				// System.out.println("\n\n has : " + ocurrencies +" in " +
				// textBlock.substring(0, endTagend.get(0) ));
				// System.out.println("\n\n found block: " + textBlock.substring(0,
				// endTagend.get(ocurrencies -1) ) );
				output = output.replaceFirst(Pattern.quote(textBlock.substring(0, endTagend.get(ocurrencies - 1))),
						this.replaceBlockLooper(textBlock.substring(0, endTagend.get(ocurrencies - 1))));
				return true;
			}
			break;
		}
		return false;
	}

	// ##### step 2
	private String replaceBlockLooper(String b) {
		replaceTemp = "";
		foundM = "";
		foundContent = "";
		String currentLoop = "";
		matcherTemp = this.matcherIn(b, loopArtic);

		while (matcherTemp.find()) {
			foundM = b.substring(matcherTemp.start(), matcherTemp.end());
			foundContent = b.substring(matcherTemp.end(), (b.length() - 16)); // 16 is length of "%%/_loop_artic%%"
			//System.out.println("\n\nStart index: " + matcherTemp.start() + "End index: " + matcherTemp.end() + "\n in: "
			//		+ b + "\nFound tag: " + foundM + "\nFound text: " + foundContent);
			break;
		}

		// get param to do loop
		int i = Integer.valueOf(foundM.toLowerCase().split("[,]")[0].replace(" ", "").replace("%%_loop_artic(", ""));
		int e = Integer.valueOf(foundM.toLowerCase().split("[,]")[1].replace(" ", "").replace(")%%", ""));

		// do
		do {
			String varName = foundContent.split("\\#\\#")[1];
			varName = varName.split("\\#\\#")[0];
			currentLoop = foundContent.replace("##" + varName + "##", String.valueOf(i));
			replaceTemp = replaceTemp.concat(currentLoop);
			i++;
		} while (i <= e);

		//System.out.println("\n\nStart replace loop by: " + replaceTemp);
		return replaceTemp;
	}

	// #### step 1
	private boolean findBlockConditional(String in) {
		// System.out.println("\n\n\n\n######################### inicio in: " + in);
		// 1.1 find first %%if|%%nif
		Matcher mifMatches = this.matcherIn(in, ifBlock + "|" + nifBlock);

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
						// System.out.println("\n ########## replace == case OK: " + b + " by: >> " +
						// foundContent);
						// case IF == false > ELSE
					} else if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
						replaceTemp = b.replace(b, this.replaceBlockElse(foundContent));
					}
				}
			}

			// case IF "!=" numeric true
			else if ((foundM.toLowerCase().indexOf("!=") != -1)) {
				if (k.equals(foundM.toLowerCase().split("\\!\\=")[0].replace("%%if(", "").replace(" ", ""))) {
					if (!v.replace(" ", "").equals(foundM.toLowerCase().split("\\!\\=")[1].replace(")%%", "")
							.replace(" ", "").replace("\'", "").replace("\"", ""))) {
						// delete case else if exist
						if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
							foundContent = this.deleteBlockElse(foundContent);
						}
						replaceTemp = b.replace(b, foundContent);
						// System.out.println("\n ########## replace == case OK: " + b + " by: >> " +
						// foundContent);
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
						// System.out.println("\n ########## replace >= case OK: " + b + " by: >> " +
						// foundContent);
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
						// System.out.println("\n ########## replace <= case OK: " + b + " by: >> " +
						// foundContent);
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

			// case IF "==" numeric true
			else if ((foundM.toLowerCase().indexOf("=") != -1)) {
				if (k.equals(foundM.toLowerCase().split("\\=")[0].replace("%%if(", "").replace(" ", ""))) {
					if (v.replace(" ", "").equals(foundM.toLowerCase().split("\\=")[1].replace(")%%", "")
							.replace(" ", "").replace("\'", "").replace("\"", ""))) {
						// delete case else if exist
						if (foundContent.toLowerCase().indexOf("%%else%%") != -1) {
							foundContent = this.deleteBlockElse(foundContent);
						}
						replaceTemp = b.replace(b, foundContent);
						// System.out.println("\n ########## replace == case OK: " + b + " by: >> " +
						// foundContent);
						// case IF == false > ELSE
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
		return in.replace(in, in.split("[%][%][e][l][s][e][%][%]")[1]);
	}

	private String deleteBlockElse(String in) {
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
