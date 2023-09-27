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
import org.springframework.beans.factory.annotation.Value;
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
	private String outputTemplate;
	private String unprocessedArea;
	private String undig;
	private Matcher matcherTemp;
	private String replaceTemp;
	private String foundM;
	private String foundContent;
	protected String regexPatnnerTag = "[%][%]+(\\w|\\w.\\w|\\w_\\w)+[%][%]";
	protected String ifBlock = "[%][%][i][f][\\(]+[a-zA-Z0-9_\\s\'\\=\\>\\<]+[\\)][%][%]";
	protected String endBlock = "[%][%][/][i][f][%][%]";
	protected String nifBlock = "[%][%][n][i][f][\\(]+[a-zA-Z0-9_\\s\']+[\\)][%][%]";
	protected String nendBlock = "[%][%][/][n][i][f][%][%]";
	protected String loopArtic = "[%][%]_loop_artic\\([0-9\\s][,][0-9\\s][\\)][%][%]";
	protected String endloopArtic = "[%][%][/]_loop_artic[%][%]";
	protected String macros = "[%][%]macro[\\(]+[a-zA-Z0-9_\\s\\'\\=\\>\\<\\.\\/]+[\\)][%][%]";
	protected String areas = "[%][%]_area[\\(]+[a-zA-Z0-9_\\s\\'\\=\\>\\<\\.\\,\\á\\é\\í\\ó\\ú\\Á\\É\\Í\\Ó\\Ú]+[\\)][%][%]";
	protected String endAreas = "[%][%][/]_area[%][%]";

	private Map<String, String> mapInputsFid;

	@Value("${optimum.site.art}")
	private String site_art;

	@Value("${optimum.site.art.webRoot}")
	protected String webRoot;
	protected String art_path;
	protected String art_file;

	// step 0
	public String processTags(String undig_content, Map<String, String> mapInputs) {
		boolean findMore = true;
		mapInputsFid = mapInputs;
		output = undig_content;
		output = output.replaceAll("\n", "").replaceAll("\r", "");

		// loops tags
		while (findMore) {
			findMore = this.findLoopersBlock(output);
		}

		// #############simple tags
		mapInputs.forEach((k, v) -> {
			File f = new File(art_file + k + ".html");
			if (f.exists() && !f.isDirectory()) {
				//System.out.println("\n exite "+art_file + k + ".html");
				output = output.replaceAll("[%][%]" + k + "[%][%]",
						"<!--#include virtual='" + art_path + k + ".html' -->");// value is into file
			} else {
				//System.out.println("\n no exite");
				output = output.replaceAll("[%][%]" + k + "[%][%]", v);
			}
		});
		
		//vars empty to next invocation
		art_path = "";
		art_file = "";

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
				// System.out.println("\n macro: " + textBlock);
				// System.out.println("\n replace by: " + macroContent);
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
			// System.out.println("\n\nStart index: " + matcherTemp.start() + "End index: "
			// + matcherTemp.end() + "\n in: "
			// + b + "\nFound tag: " + foundM + "\nFound text: " + foundContent);
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

		// System.out.println("\n\nStart replace loop by: " + replaceTemp);
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

	private boolean findAreas(String in, String nameArea, String ids_arts) throws IOException {
		unprocessedArea = "";

		// System.out.println("\n\n\n\n## inicio areas: " + in+ " areas:" + areas);
		Matcher loopMatches = this.matcherIn(in, areas.replace("_area", nameArea));

		while (loopMatches.find()) {
			if (loopMatches.start() != 0) {
				// catch block if
				String textBlock = in.substring(loopMatches.start(), in.length());
				// find first end
				Matcher EndMatches = Pattern.compile(endAreas).matcher(textBlock);

				List<Integer> startTagend = new ArrayList<Integer>();
				List<Integer> endTagend = new ArrayList<Integer>();

				while (EndMatches.find()) {
					System.out.println("Tag end : " + EndMatches.start());
					startTagend.add(EndMatches.start());
					endTagend.add(EndMatches.end());
				}

				String foundAreas = textBlock.substring(0, endTagend.get(0));
				String[] ids = ids_arts.split(",");
				// System.out.println("\n\n has area: " + foundAreas);

				// get info arts
				for (String ts : ids) {

					String path = this.replaceTagForText(site_art, "_fechac", String.valueOf(ts).substring(0, 8));
					path = path.concat(String.valueOf(ts + "/"));

					// to parse includes info in tags template
					String pathWebRoot = this.replaceTagForText(webRoot, "_fechac", String.valueOf(ts).substring(0, 8));
					pathWebRoot = pathWebRoot.concat(String.valueOf(ts + "/"));

					String info_json = this.getContentJsonFile(path + "/data/" + ts + ".json");
					if (info_json != null) {
						Map<String, String> map = this.StringJsonToMap(info_json);

						String published = String.valueOf(map.get("published"));

						// System.out.println("\n antes by: " + outputTemplate);
						if (published.equals("true")) {
							this.art_path = pathWebRoot + "data/";
							this.art_file = path + "data/";
							this.processTags(foundAreas, this.StringJsonToMap(map.get("allInfo")));
							unprocessedArea = unprocessedArea.concat(output);
						}
					}
				}

				unprocessedArea = unprocessedArea.replaceAll(areas.replace("_area", nameArea), "").replaceAll(endAreas,
						"");
				outputTemplate = outputTemplate.replace(foundAreas, unprocessedArea);
				undig = outputTemplate;

				return true;
			}
			break;
		}
		return false;
	}

	public String replaceAreasForArtsInfo(String areas, String undig_content) throws IOException {
		undig = undig_content.replaceAll("\n", "").replaceAll("\r", "");
		outputTemplate = undig_content;
		// System.out.println("\n undigest: " + outputTemplate);

		Map<String, String> mapAreas = this.StringJsonToMap(areas);
		mapAreas.forEach((k, v) -> {
			try {
				Boolean findMore = true;
				while (findMore) {
					findMore = this.findAreas(undig, k, v);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// System.out.println(" >>> " + k + " >>> " + v);
		});

		// find area
		outputTemplate = outputTemplate.replaceAll(regexPatnnerTag, "");
		//System.out.println("\n queda by: " + outputTemplate);
		return outputTemplate;

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

	private String getContentJsonFile(String file) throws IOException {
		System.out.println("reading json: " + file);
		File f = new File(file);
		if (f.exists() && !f.isDirectory()) {
			String content = new String(Files.readAllBytes(Paths.get(file)));
			return content;
		} else {
			return null;
		}
	}

	public String getDir_data() {
		return site_art;
	}

	public void setDir_data(String site_art) {
		this.site_art = site_art;
	}

	public String getArt_path() {
		return art_path;
	}

	public void setArt_path(String art_path) {
		this.art_path = art_path;
	}

}
