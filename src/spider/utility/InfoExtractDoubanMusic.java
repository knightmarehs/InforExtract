package spider.utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AbstractDocument.BranchElement;

import org.htmlparser.*;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.*;

import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.input.*;
import org.jdom2.output.*;
import org.omg.CORBA.portable.ValueBase;

/**
 * Extract infomation from Douban Music
 */

public class InfoExtractDoubanMusic
{

	/**
	 * @param currentUrl
	 *            :the analyzing URL
	 * @param QumuForCurrentURL
	 * @param mapForCurrentURL
	 *            To save information of (attribute,value)
	 */
	InfoExtractDoubanMusic(String url)
	{
		currentUrl = url;
	}

	InfoExtractDoubanMusic()
	{
		currentUrl = null;
	}

	private String currentUrl;

	private String currentEntityName;

	private HashMap<String, String> mapForCurrentURL = new HashMap<String, String>();

	private ArrayList<String> failedUrlList = new ArrayList<String>();

	private ArrayList<String> QumuForCurrentURL = new ArrayList<String>();

	private HashSet<String> allAttributehHashSet = new HashSet<String>();


	/**
	 * @param osw
	 *            To output the information of an uniform entity in the form of
	 *            XML
	 */
	public boolean outputSingleEntity(PrintStream ps)
	{
		try
		{
			if (!mapForCurrentURL.isEmpty() && !QumuForCurrentURL.isEmpty())
			{ 
				ps.println("名称:" + currentEntityName);	
				ps.println("URL:" + currentUrl);
				for (String key : mapForCurrentURL.keySet())
				{
					ps.println(key + ":" + mapForCurrentURL.get(key));
				}
				ps.println("曲目:");
				for (String string : QumuForCurrentURL)
				{
					ps.println(string);
				}
				ps.println();
				
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	public void saveFailedUrlList(PrintStream ps)
	{
		for(String url:failedUrlList)
		{
			ps.println(url);
		}
	}

	/**
	 * To convert the uniform entity to all entities & to save the current
	 * information in the XML document tree & to save the attributes.
	 * 
	 * @return TODO
	 */
	public boolean saveCurrentInformation(PrintStream ps)
	{
		boolean flag = true;
		try
		{
			String[] attributeOfQumu =
				{
					"流派", "发行时间", "表演者"
				};
			if (!mapForCurrentURL.isEmpty() && !QumuForCurrentURL.isEmpty())
			{ 
				ps.println("专辑名称:" + currentEntityName);	
				ps.println("URL:" + currentUrl);
				for (String key : mapForCurrentURL.keySet())
				{
					ps.println(key + ":" + mapForCurrentURL.get(key));
				}
				ps.println("曲目:");
				for (String string : QumuForCurrentURL)
				{
					ps.println(string);
				}
				ps.println();
				
			}
			// Uniform entity & all entities
			for (String entityname : QumuForCurrentURL)
			{
				ps.println("曲目名称:" + entityname);
				ps.println("所属专辑:" + currentEntityName);
				for (String attributeNameString : attributeOfQumu)
				{
					if (mapForCurrentURL.containsKey(attributeNameString))
					{
						ps.println(attributeNameString + ":" + mapForCurrentURL.get(attributeNameString));
					}
				}
				ps.println();
			}
			for (String string : mapForCurrentURL.keySet())
			{
				allAttributehHashSet.add(string);
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return flag;
	}

	/**
	 * @param urlList
	 *            To extract information from a list of URL.
	 */
	public void extractInfoUrlList(ArrayList<String> urlList, OutputStream osw)
	{
		for (String urlString : urlList)
		{
			setCurrentUrl(urlString);
			if (ExtractAlbumInfo())
			{
				//saveCurrentInformation();
			} else
			{
				failedUrlList.add(urlString);
			}
		}
		return;
	}

	public boolean extractLinkPairFromHtml(String html, Pattern pattern)
	{
		boolean flag = false;
		try
		{
			Matcher matcher = pattern.matcher(html);
			while (matcher.find())
			{
				flag = true;
				String attribute = matcher.group(1);
				String value = matcher.group(2);
				int i = 4;
				while(i <= matcher.groupCount())
				{
					String temp = matcher.group(i);
					if(temp == null)
						break;
					value = value + '/' + temp;
					i += 2;
				}
				System.out.println(attribute + " : " + value);
				mapForCurrentURL.put(attribute, value);
			}							
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return flag;
	}
	
	public boolean extractPairFromHtml(String html, Pattern p)
	{
		boolean flag = false;
		try
		{
			Matcher matcher = p.matcher(html);
			while (matcher.find())
			{
				flag = true;
				String attribute = matcher.group(1);
				String value = matcher.group(2);
				System.out.println(attribute + " : " + value);
				mapForCurrentURL.put(attribute, value);
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return flag;
	}

	
	/**
	 * @param Node
	 *            To get the Qumu Information in the format <div
	 *            class="indent">,three
	 *            pattern:prefix+name+suffix;'>'+name+suffix;prifixname+suffix;
	 * @return TODO
	 * @return
	 */
	public boolean extractQu_Indent(Node node)
	{
		String[] prefixStrings =
		{ 
				">0?[12]\\-[12]?[\\d]\\([\\d]:[\\d]{1,2}\\)[\\s]*",
				">\\[[\\d]{1,2}\\][\\s]*[IVX]{0,3}[\\.]?[\\s]*",
				">\\d{1,2}[\\s]*[.，\\-][\\s]*[\\d]*[\\s]*",
				">\\d[\\s]+", ">[12034]\\d[\\s]+",
				">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?[\\s]*" };
		String[] nameofQumuStrings =
		{ 
				"\\[CD\\][\\s]*([^<]+?)", "\\[DVD\\][\\s]*([^<]+?)",
				"\\[Disc [12]\\][\\s]*([^<]+?)", "([^/]+?)", "([^<]+?)" 
				};
		String[] prefixAndName =
		{ ">\\d\\[CD\\][\\s]*([^<]+?)", ">\\d\\[DVD\\][\\s]*([^<]+?)",
				">\\d\\[Disc [12]\\][\\s]*([^<]+?)", ">\\d([^\\d][^<]+?)" };
		String[] suffixStrings =
		{ "\\-[12][\\d]{3}[\\s]*<br/", "\\-[12][\\d]{3}[\\s]*</div>",//-1997$
				"[\\s]*[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]][\\s]*<br/","[\\s]*[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]][\\s]*</div>",//[12:34]
				"[\\s]*[\\(\\[]-[\\)\\]][\\s]*<br/","[\\s]*[\\(\\[]-[\\)\\]][\\s]*</div>",//[-]
				"[\\s]*[\\d]{1,2}[\\.:：][\\d]{1,2}[\\s]*<br/","[\\s]*[\\d]{1,2}[\\.:：][\\d]{1,2}[\\s]*</div>",// 7.34、7:34
				"[\\s]*<br/", "[\\s]*</div>" };
		String[] forspecialPattern =
		{};
		String patternString = null;
		try
		{
			QumuForCurrentURL.clear();
			String str = node.toHtml();
			// System.out.println(str);
			Pattern p = Pattern
					.compile("<div class=\"indent\">([^/]+?)(<br/>([^/]+?))*</div>|" +
							"<div class=\"indent\">([^<]+?)(<br/>([^<]+?))*</div>|" +
							"<div class=\"indent\">([^<]+?)<br/>(.+?)<br/>([^<]+?)</div>");
			Matcher m = p.matcher(str);
			// if(!m.find()){}
			for (String prefix : prefixStrings)
				for (String name : nameofQumuStrings)
					for (String suffix : suffixStrings)
					{
						if (patternString != null)
						{
							patternString += '|';
						}
						patternString = patternString + prefix + name + suffix;
					}
			for (String prefixName : prefixAndName)
				for (String suffix : suffixStrings)
				{
					patternString = patternString + '|' + prefixName + suffix;
				}
			for (String name : nameofQumuStrings)
				for (String suffix : suffixStrings)
				{
					patternString = patternString + "|>" + name + suffix;
				}
			while (m.find())
			{
				String divStr = m.group();
				divStr = FullcharConverter.full2HalfChange(divStr);
//				System.out.println(divStr);
				Pattern pat = Pattern.compile(patternString);
				/*
				 * Pattern pat = Pattern .compile("" +
				 * ">\\d{1,2}[.，\\- \\s]*([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|>\\d{1,2}[.， \\-\\s]*([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|"
				 * +
				 * ">\\d{1,2}[.，\\- \\s]*([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|>\\d{1,2}[.， \\-\\s]*([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|"
				 * +
				 * ">\\d{1,2}[.，\\- \\s]*([^/]+?)<br/|>\\d{1,2}[.， \\-\\s]*([^/]+?)</div>|"
				 * +
				 * ">\\d{1,2}[.，\\- \\s]*([^<]+?)<br/|>\\d{1,2}[.， \\-\\s]*([^<]+?)</div>|"
				 * +
				 * ">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?\\s([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|"
				 * + ">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?\\s([^/]+?)<br/|" +
				 * ">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?\\s([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|"
				 * + ">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?\\s([^<]+?)<br/|" +
				 * ">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?\\s([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|"
				 * + ">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?\\s([^/]+?)</div>|" +
				 * ">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?\\s([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|"
				 * + ">第[一二三四五六七八九十]{1}[一二三四五六七八九十]?首歌?\\s([^<]+?)</div>|" +
				 * ">\\d([^\\d][^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|>[12]\\d([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|>\\d([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|"
				 * +
				 * ">\\d([^\\d][^/]+?)<br/|>[12]\\d([^/]+?)<br/|>\\d([^/]+?)<br/|"
				 * +
				 * ">\\d([^\\d][^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|>[12]\\d([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|>\\d([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|"
				 * +
				 * ">\\d([^\\d][^<]+?)<br/|>[12]\\d([^<]+?)<br/|>\\d([^<]+?)<br/|"
				 * +
				 * ">\\d([^\\d][^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|>[12]\\d([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|>\\d([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<div>/|"
				 * +
				 * ">\\d([^\\d][^/]+?)</div>|>[12]\\d([^/]+?)</div>|>\\d([^/]+?)<div>/|"
				 * +
				 * ">\\d([^\\d][^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|>[12]\\d([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|>\\d([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<div>/|"
				 * +
				 * ">\\d([^\\d][^<]+?)</div>|>[12]\\d([^<]+?)</div>|>\\d([^<]+?)<div>/|"
				 * +
				 * ">([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|>([^/]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|"
				 * + ">([^/]+?)<br/|>([^/]+?)</div>|" +
				 * ">([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]<br/|>([^<]+?)[\\(\\[][\\d]{1,2}:[\\d]{1,2}[\\)\\]]</div>|"
				 * + ">([^<]+?)<br/|>([^<]+?)</div>");
				 */
				Matcher mat = pat.matcher(divStr);
				while (mat.find())
				{
					int i = 1;
					int size = prefixStrings.length * nameofQumuStrings.length
							* suffixStrings.length;
					size = size + prefixAndName.length * suffixStrings.length
							+ nameofQumuStrings.length * suffixStrings.length;
					for (; i <= size && mat.group(i) == null; i++)
						;
					if (i == (size + 1))
						return false;
					String temp = mat.group(i);
					Pattern temPattern = Pattern
							.compile("[\\s]*[Dd][Ii][Ss][Cc][\\s]*[\\d][\\s]*|作曲[:：]|演奏[:：]|编曲[:：]|曲目[：:]|" +
									"[^A-Z]DVD$|[Cc][dD][\\s]*[\\d]{1,2}|^[tT][Rr][Aa][Cc][Kk][\\s]*[\\d]{1,2}$|" +
									"指挥[：:]");//※<<>>()
					Matcher temMatcher = temPattern.matcher(temp);
					if (!temMatcher.find())
					{
						QumuForCurrentURL.add(temp);
						// mapForCurrentURL.put("曲目", temp);
						System.out.println(temp);
					}
				}
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	};

	public boolean extractQumu(Node node)
	{
		NodeList nodelist = new NodeList();
		NodeFilter filterBr = new org.htmlparser.filters.HasAttributeFilter(
				"class", "olts");
		node.collectInto(nodelist, filterBr);
		System.out.println("曲目:");
		if (nodelist.size() == 1)
		{
			Pattern pTable = Pattern.compile("<tr>[\\s]*<td>([^/]+?)</td>");
			String pageStr = nodelist.elementAt(0).toHtml();
//			System.out.println(pageStr);
			pageStr = FullcharConverter.full2HalfChange(pageStr);
			Matcher matcher = pTable.matcher(pageStr);
			boolean flag = false;
			while (matcher.find())
			{
				flag = true;
				String qumuString = matcher.group(1);
				System.out.println(qumuString);
				QumuForCurrentURL.add(qumuString);
			}
			return flag;
		} else
		{
			return extractQu_Indent(node);
		}
	}

	/**
	 * To extract the album information from the current URL after saving the
	 * previous information: saveCurrentInformation();
	 */
	public boolean ExtractAlbumInfo()
	{
		boolean flag = true;
		try
		{
			if (currentUrl != null)
			{
				if (!mapForCurrentURL.isEmpty())
				{
					// saveCurrentInformation();
					mapForCurrentURL.clear();
					QumuForCurrentURL.clear();
				}
				System.out
						.println("-------------------------------------------------------------");
				System.out.println("URL: " + currentUrl);
				DoubanUrlCrawler doubancr = new DoubanUrlCrawler();
				String page = doubancr.crawleByURL(currentUrl, "utf-8");
				Parser parser = Parser.createParser(page, "utf-8");
				NodeFilter filter = new org.htmlparser.filters.HasAttributeFilter(
						"id", "info");

				NodeFilter filterIntro = new org.htmlparser.filters.HasAttributeFilter(
						"property", "v:summary");
				NodeFilter filterReinfo = new org.htmlparser.filters.HasAttributeFilter(
						"class", "related_info");
				NodeFilter headFilter = new TagNameFilter("head");
				NodeList nodesInfo = parser.extractAllNodesThatMatch(filter);
				// NodeList nodesInfo = null;
				parser.reset();
				NodeList nodesIntroduction = parser
						.extractAllNodesThatMatch(filterIntro);
				parser.reset();
				NodeList nodesReinfo = parser
						.extractAllNodesThatMatch(filterReinfo);
				parser.reset();
				NodeList headList = parser.extractAllNodesThatMatch(headFilter);
				if (headList.size() == 1)
				{
					Node node = headList.elementAt(0);
					String headString = node.toHtml();
					// System.out.println(headString);
					Pattern headPattern = Pattern
							.compile("<title>[\\s]*(.+?)[\\s]*\\(豆瓣\\)[\\s]*</title>");
					Matcher matcher = headPattern.matcher(headString);
					while (matcher.find())
					{
						flag = false;
						flag = true;
						String titleString = matcher.group(1);
						currentEntityName = titleString;
						System.out.println("title: " + titleString);
					}
				} else
				{
					System.out.println(currentUrl);
					System.out.println("Detecting entity name error!");
					return false;
				}
				if (nodesInfo.size() == 1)
				{
					Node Info = nodesInfo.elementAt(0);
					String infoHtml = Info.toHtml();
//					System.out.println(infoHtml.toCharArray());
					Pattern pattern = Pattern
							.compile("<span class=\"pl\">([^/]+?):</span>&nbsp;(.+?)\\s*<br>");
					/*Pattern linkp = Pattern
							.compile("<span class=\"pl\">\\s*([^/]+?):\\s*<a href=\".+?\">([^/]+?)</a>");*/
					Pattern linkp = Pattern
							.compile("<span class=\"pl\">\\s*([^/]+?):\\s*<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*" +
									"(<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*)?" +
									"(<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*)?" +
									"(<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*)?" +
									"(<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*)?" +
									"(<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*)?" +
									"(<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*)?" +
									"(<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*)?" +
									"(<a href=\".+?\">([^/]+?)</a>[\\s]*/?[\\s]*)?</span>");
					extractPairFromHtml(infoHtml, pattern);
					extractLinkPairFromHtml(infoHtml, linkp);
				} else
				{
					System.out.println(currentUrl);
					System.out.println("There exits " + nodesInfo.size()
							+ " infoboxs");
					return false;
				}
				if (nodesIntroduction.size() == 1)
				{
					Node Intro = nodesIntroduction.elementAt(0);
					String Introstr = Intro.toPlainTextString();
					System.out.println("简介: " + Intro.toPlainTextString());
					mapForCurrentURL.put("简介", Introstr);
				} else
				{
					System.out.println(currentUrl);
					System.out.println("There exits "
							+ nodesIntroduction.size() + " Introduction");
				}
				if (nodesReinfo.size() == 1)
				{
					Node nodeReinfo = nodesReinfo.elementAt(0);
					flag = extractQumu(nodeReinfo);
				} else
				{
					System.out.println(currentUrl);
					System.out.println("There exits " + nodesReinfo.size()
							+ " tables of Qumu");
					// return false;
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			flag = false;
		}
		try
		{
			Thread.sleep(2000);
		} catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
			return false;
		} finally
		{
			System.out
					.println("-------------------------------------------------------------");
		}
		return flag;
	}

	public String getCurrentUrl()
	{
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl)
	{
		this.currentUrl = currentUrl;
	}

	public ArrayList<String> getFailedUrlList()
	{
		return failedUrlList;
	}

	public void addFailedUrl(String url)
	{
		failedUrlList.add(url);
	}

	public static void main(String[] args)
	{
		try
		{
			FileInputStream fis = new FileInputStream(
					"E:\\Husen\\data\\douban_music\\douban_music_subjectid.txt");
			OutputStream osLog = new FileOutputStream("E:\\Husen\\data\\douban_music\\Log.txt",false);
			PrintStream logPrintStream = new PrintStream(osLog);
			OutputStream osErr = new FileOutputStream("E:\\Husen\\data\\douban_music\\Error.txt",false);
			PrintStream errPrintStream = new PrintStream(osErr);
			OutputStream osInfo = new FileOutputStream("E:\\Husen\\data\\douban_music\\douban_musicInfo.txt",false);
			PrintStream infoPrintStream = new PrintStream(osInfo);
			PrintStream failedUrlList = new PrintStream("E:\\Husen\\data\\douban_music\\failedUrlList.txt");
			System.setOut(logPrintStream);
			System.setErr(errPrintStream);
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String urlString;
			ArrayList<String> urlList = new ArrayList<String>();
			while ((urlString = bufferedReader.readLine()) != null)
			{
				urlList.add(urlString);
			}
			InfoExtractDoubanMusic infoExtractDoubanMusic = new InfoExtractDoubanMusic();
			for (String url : urlList)
			{
				infoExtractDoubanMusic.setCurrentUrl(url);
				if (infoExtractDoubanMusic.ExtractAlbumInfo())
				{
					infoExtractDoubanMusic.saveCurrentInformation(infoPrintStream);
					try
					{
						Thread.sleep(2000);
					} catch (InterruptedException e)
					{
						System.out.println(e.getMessage());	// return result;
					}
				} else
				{
					System.err.append("URL:" + url);
					infoExtractDoubanMusic.addFailedUrl(url);
				}
			}
			infoExtractDoubanMusic.saveFailedUrlList(failedUrlList);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
