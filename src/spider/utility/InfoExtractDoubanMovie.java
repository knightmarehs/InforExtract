package spider.utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class InfoExtractDoubanMovie
{
	private HashMap<String, String> infoMap = new HashMap<String, String>();
	
	public HashMap<String, String> urlListMap = new HashMap<String, String>();
	
	private ArrayList<String> listOfIntro = new ArrayList<String>();

	private ArrayList<String> failedUrlList = new ArrayList<String>();

	public ArrayList<String> getFailedUrlList()
	{
		return failedUrlList;
	}

	public void setFailedUrlList(ArrayList<String> failedUrlList)
	{
		this.failedUrlList = failedUrlList;
	}

	private ArrayList<String> listOfURL = new ArrayList<String>();

	public ArrayList<String> getListOfURL()
	{
		return listOfURL;
	}

	public void setListOfURL(ArrayList<String> listOfURL)
	{
		this.listOfURL = listOfURL;
	}

	private String currentEntityName;

	private String currentUrl;

	public String getCurrentUrl()
	{
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl)
	{
		this.currentUrl = currentUrl;
	}

	public void saveCurrentInfo(PrintStream ps)
	{
		try
		{
			if (!infoMap.isEmpty())
			{
				ps.println("doubanURL:" + currentUrl);
				ps.println("片名:" + currentEntityName);
				for (String key : infoMap.keySet())
				{
					ps.println(key + ":" + infoMap.get(key));
				}
//				ps.println("书籍目录:");
//				for (String string : listOfIntro)
//				{
//					ps.println(string);
//				}
				ps.println();
			}
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	public void saveFailedUrlList(PrintStream printStream)
	{
		for (String url : failedUrlList)
		{
			printStream.println(url);
		}
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
				infoMap.put(attribute, value);
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return flag;
	}
	public boolean extractTime2FromHtml(String html, Pattern p)
	{
		boolean flag=false;
		try
		{
			Matcher matcher = p.matcher(html);
			while(matcher.find())
			{
				flag = true;
				String attribute = matcher.group(1);
				String value = matcher.group(2) + '/' + matcher.group(3);
				System.out.println(attribute + " : " + value);
				infoMap.put(attribute, value);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return flag;
	}
	public boolean extractLinkPairFromHtml(String html, Pattern pattern)
	{
		boolean flag = false;
		//System.out.println("html:"+html);
		try
		{
			Matcher matcher = pattern.matcher(html);
			while (matcher.find())
			{
				//System.out.println("Found:"+matcher.groupCount());
				//System.out.println("group0 : "+matcher.group());
				flag = true;
				String attribute = matcher.group(1);
				String value = matcher.group(2);
				int i = 4;
				while (i <= matcher.groupCount())
				{
					String temp = matcher.group(i);
					//System.out.println("group"+i+" : "+temp);
					if (temp == null)
						break;
					value = value + '/' + temp;
					i+=2;
				}
				System.out.println(attribute + " : " + value);
				infoMap.put(attribute, value);
			}
			if(!flag)
				System.out.println("NOTfound");
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return flag;
	}

	public String extractContentInfo(String content)
	{
		String resultString = "";
		Pattern pattern = Pattern
				.compile("<span property=\"v:summary\" class=\"\">[\\s]*(.+?)</span>");
		
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
		{
			resultString = matcher.group(1);
		}
		return resultString;
	}

	public String extractScoreInfo(String content)
	{
		String resultString = "";
		Pattern pattern = Pattern
				.compile("<strong class=\"ll rating_num\" property=\"v:average\">(.+?)</strong>");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
		{
			resultString = matcher.group(1);
		}
		
		String scorerNum = "0";
		Pattern p1 = Pattern.compile("<span property=\"v:votes\">(.+?)</span>");
		matcher = p1.matcher(content);
		if(matcher.find())
		{
			scorerNum = matcher.group(1);
		}
		System.out.println("评价人数" + " : " + scorerNum);
		infoMap.put("评价人数", scorerNum);
		return resultString;
	}
	public void extractMulu(String html)
	{
		Pattern pattern = Pattern.compile(">[\\s]*([^/]+?)<br/");
		Matcher matcher = pattern.matcher(html);
		while (matcher.find())
		{
			String qumuName = matcher.group(1);
			System.out.println(qumuName);
			listOfIntro.add(qumuName);
		}
	}
	public String specifyUrl() throws IOException, ParserException
	{
		String type = "Null";
		if(currentUrl != null)
		{
			DoubanUrlCrawler doubancr = new DoubanUrlCrawler();
			String page = doubancr.crawleByURL(currentUrl, "utf-8");
			if(page==null)
				System.out.println("page = NULL");
			Parser parser = Parser.createParser(page, "utf-8");	
			
			NodeFilter headFilter = new TagNameFilter("head");
			NodeList headList = parser.extractAllNodesThatMatch(headFilter);
			Node node = headList.elementAt(0);
			String headString = node.toHtml();
			Pattern headPattern = Pattern
					.compile("<title>[\\s]*(.+?)[\\s]*\\(豆瓣\\)[\\s]*</title>");
			Matcher matcher = headPattern.matcher(headString);
			if(matcher.find())
			{
				String titleString = matcher.group(1);
				//currentEntityName = titleString;
				System.out.println("title: " + titleString);
				
				NodeFilter filter = new org.htmlparser.filters.HasAttributeFilter(
						"class", "top-nav-info");
				parser.reset();
				NodeList topList = parser.extractAllNodesThatMatch(filter);
				Node topNode = topList.elementAt(0);
				//System.out.println(topNode);
				String topString = topNode.toHtml();
				//System.out.println(topString);
				Pattern topPattern = Pattern.compile("source=(.+?)\" class=\"nav-login\"");
				Matcher topMatcher = topPattern.matcher(topString);
				if(topMatcher.find())
				{
					String typeString = topMatcher.group(1);
					type = typeString;
				}
				else
					System.out.println("Nt");
			}
		}
		return type;
	}
	public Boolean extractMovieurls()
	{
		Boolean flag =true;
		try
		{
			if (currentUrl != null)
			{
				if (!infoMap.isEmpty())
				{
					// saveCurrentInformation();
					infoMap.clear();
					listOfIntro.clear();
				}
				System.out
						.println("-------------------------------------------------------------");
				System.out.println("URL: " + currentUrl);
				DoubanUrlCrawler doubancr = new DoubanUrlCrawler();
				String page = doubancr.crawleByURL(currentUrl, "utf-8");
				Parser parser = Parser.createParser(page, "utf-8");
				
				NodeFilter filterItem = new org.htmlparser.filters.HasAttributeFilter(
						"class", "item");
				
				parser.reset();
				NodeList itemList = parser
						.extractAllNodesThatMatch(filterItem);
				
				if(itemList.size()>0)
				{
					Node itemNode;
					for(int i=0;i<itemList.size();i++)
					{
						itemNode = itemList.elementAt(i);
						String itemString = itemNode.toHtml();
						System.out.println("----------- "+i+" --------------");
						//System.out.println(itemString);
						Pattern urlPattern = Pattern.compile("<a class=\"nbg\" href=\"([^\"]+?)\"[\\s]*title");
						Matcher itemMatcher = urlPattern.matcher(itemString);
						if(itemMatcher.find())
						{
							String urlString = itemMatcher.group(1);
							//System.out.println(urlString);
							urlListMap.put(urlString, "1");
						}
						else
							System.out.println("Nt");
						//listOfURL
					}
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
	public Boolean extractMovieInfo()
	{
		Boolean flag = true;
		try
		{
			if (currentUrl != null)
			{
				if (!infoMap.isEmpty())
				{
					// saveCurrentInformation();
					infoMap.clear();
					listOfIntro.clear();
				}
				System.out
						.println("-------------------------------------------------------------");
				System.out.println("URL: " + currentUrl);
				DoubanUrlCrawler doubancr = new DoubanUrlCrawler();
				String page = doubancr.crawleByURL(currentUrl, "utf-8");
				Parser parser = Parser.createParser(page, "utf-8");
				
				// filter for celebrity
				NodeFilter filterCelebrityInfo = new org.htmlparser.filters.HasAttributeFilter(
						"id", "headline");
				
				// filter for movie
				NodeFilter filter = new org.htmlparser.filters.HasAttributeFilter(
						"id", "info");
				NodeFilter filterContentIntro = new org.htmlparser.filters.HasAttributeFilter(
						"id", "link-report");
				NodeFilter filterReinfo = new org.htmlparser.filters.HasAttributeFilter(
						"class", "all hidden");
				NodeFilter headFilter = new TagNameFilter("head");
				NodeFilter filterScore = new org.htmlparser.filters.HasAttributeFilter("id","interest_sectl");
				
				// node list for celebrity
				NodeList celebrityNodesInfo = parser.extractAllNodesThatMatch(filterCelebrityInfo);
				parser.reset();
				
				// node list for movie
				NodeList nodesInfo = parser.extractAllNodesThatMatch(filter);
				parser.reset();
				NodeList nodesContentIntro = parser
						.extractAllNodesThatMatch(filterContentIntro);
				parser.reset();
				NodeList nodesReinfo = parser
						.extractAllNodesThatMatch(filterReinfo);
				parser.reset();
				NodeList headList = parser.extractAllNodesThatMatch(headFilter);
				parser.reset();
				NodeList scoreList = parser.extractAllNodesThatMatch(filterScore);
				
				if (headList.size() == 1)
				{
					Node node = headList.elementAt(0);
					String headString = node.toHtml();
					//System.out.println(headString);
					Pattern headPattern = Pattern
							.compile("<title>[\\s]*(.+?)[\\s]*\\(豆瓣\\)[\\s]*</title>");
					Matcher matcher = headPattern.matcher(headString);
					flag = false;
					while (matcher.find())
					{
						flag = true;
						String titleString = matcher.group(1);
						currentEntityName = titleString;
						System.out.println("title: " + titleString);
					}
				} else
				{
					System.err.println(currentUrl);
					System.err.println("Detecting entity name error!");
					return false;
				}
				//if(celebrityNodesInfo.size() == 1)
				if (nodesInfo.size() == 1)
				{
					Node Info = nodesInfo.elementAt(0);
					String infoHtml = Info.toHtml();
					Pattern basicP = Pattern
							.compile("<span class=['\"]pl['|\"]>[\\s]*([^<]+?):?[\\s]*</span>[\\s]*:?[\\s]*([^<]+?)[\\s]*<br[/]?>");
					Pattern linkP = Pattern
							.compile("<span class=['\"]pl['|\"]>[\\s]*([^<]+?):?[\\s]*</span>[\\s]*:?[\\s]*(?:<span class='attrs'>)?<a href=\"[^>]+\">([^<]+?)</a>[\\s]*"
									+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"
									+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"
									+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"
									+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"
									+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"
									+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"+"(/[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>[\\s]*)?"
									+"(</span></span>)?<br[/]?>"
									);
					Pattern typeP = Pattern.compile("<span class=['\"]pl['|\"]>[\\s]*([^<]+?):?[\\s]*</span>[\\s]*:?[\\s]*"
									+"<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*"
									+"(/[\\s]*<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*)?"+"(/[\\s]*<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*)?"
									+"(/[\\s]*<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*)?"+"(/[\\s]*<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*)?"
									+"(/[\\s]*<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*)?"+"(/[\\s]*<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*)?"
									+"(/[\\s]*<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*)?"+"(/[\\s]*<span property=\"[^<]+?\">([^<]+?)</span>[\\s]*)?"
									+"<br[/]?>");
					Pattern time2P =Pattern.compile("<span class=['\"]pl['|\"]>[\\s]*([^<]+?):?[\\s]*</span>[\\s]*:?[\\s]*"
							+"<span property=\"v:runtime\" [^<]*>([^<]+?)</span>[\\s]*/[\\s]*([^<]+?)[\\s]*<br[/]?>");
					extractPairFromHtml(infoHtml, basicP);
					//System.out.println(infoHtml);
					extractLinkPairFromHtml(infoHtml, linkP);
					extractLinkPairFromHtml(infoHtml, typeP);
					extractTime2FromHtml(infoHtml, time2P);
				} else
				{
					System.err.println(currentUrl);
					//System.err.println("There exits " + celebrityNodesInfo.size()+ " infoboxs");
					System.err.println("There exits " + nodesInfo.size()+ " infoboxs");
					return false;
				}
				if (nodesContentIntro.size() == 1)
				{
					Node Intro = nodesContentIntro.elementAt(0);
					String Introstr = Intro.toHtml();
					//System.out.println(Introstr);
					String contentString = extractContentInfo(Introstr);
					System.out.println("内容简介" + " : " + contentString);
					infoMap.put("内容简介", contentString);
				} else
				{
					System.err.println(currentUrl);
					System.err.println("There exits "
							+ nodesContentIntro.size()
							+ " Content Introduction");
				}
				if(scoreList.size() == 1)
				{
					Node Score = scoreList.elementAt(0);
					String Scorestr = Score.toHtml();
					//System.out.println(Scorestr);
					String scoreString = extractScoreInfo(Scorestr);
					System.out.println("评分" + " : " + scoreString);
					infoMap.put("评分", scoreString);
				}
				else
				{
					System.err.println(currentUrl);
					System.err.println("There exits "
							+ scoreList.size()
							+ " moive's score");
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

	public void readUrlList(String filename)
	{
		try
		{
			FileInputStream fileInputStream = new FileInputStream(filename);
			InputStreamReader inputStreamReader = new InputStreamReader(
					fileInputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String url = "";
			while ((url = bufferedReader.readLine()) != null)
			{
				listOfURL.add(url);
			}
			bufferedReader.close();
			inputStreamReader.close();
			fileInputStream.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void transformDataForm(String filename)
	{
		try
		{
			String fileNameConcept = "E:\\Husen\\data\\douban_movie\\concept_movie.txt";
			String fileNameTriples = "E:\\Husen\\data\\douban_movie\\triples_movie.txt";
			String fileNameOrigin = "E:\\Husen\\data\\douban_movie\\origin_movie.txt";
			PrintStream conceptPS = new PrintStream(fileNameConcept);
			PrintStream triplesPS = new PrintStream(fileNameTriples);
			PrintStream originPS = new PrintStream(fileNameOrigin);
			InputStream inputStream = new FileInputStream(filename);
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String line = "";
			Boolean flag = true;
			line = bufferedReader.readLine();
			int concept_num = 245734;
			int origin_num = 245734;
			int triple_num = 3130469;
			String movieNameString = "";
			while (flag)
			{
				// String[] arrayStrings = line.split(":");
				int index = line.indexOf(':');
				String propertyString = line.substring(0, index);
				String valueString = "";
				valueString = line.substring(index + 1);
				triple_num++;
				if (propertyString.equals("书籍名称"))
				{
					concept_num++;
					origin_num++;
					movieNameString = valueString;
					// conceptPS.println('"' + concept_num + '"' + "\t" + '"' +
					// valueString + '"');
					System.out.println("\"" + concept_num + "\"\t\""
							+ valueString + '"');
				}
				if (propertyString.equals("URL"))
				{
					// originPS.println('"' + origin_num + '"' + "\t" + '"' +
					// concept_num + '"' + "\t" +
					// "\"Extract From Douban_movie struct_info\"" + "\t" +
					// valueString + "\t" + "2012-12-20");
					System.out
							.println("\""
									+ origin_num
									+ "\"\t\""
									+ concept_num
									+ "\"\t\"Extract From Douban_movie struct_info\"\t\""
									+ valueString + "\"\t\"" + "2012-12-20\"");
				}
				if (propertyString.equals("书籍目录"))
				{
					while ((line = bufferedReader.readLine()) != null)
					{
						// String[] arrayStrings2 = line.split(":");
						index = line.indexOf(':');
						if (index > 0)
						{
							String propertyString2 = line.substring(0, index);
							String valueString2 = line.substring(index + 1);
							if (propertyString2.equals("书籍名称"))
							{
								break;
							}
						}
						valueString += line;
					}
				} else
				{
					line = bufferedReader.readLine();
				}
				// triplesPS.println('"' + triple_num + '"' + "\t" + '"' +
				// movieNameString + "\t" + '"' + propertyString + '"' + "\t" +
				// '"' + valueString + '"' + "\t" + '"' + concept_num + '"' +
				// "\t" + '"' + " " + '"');
				if(!valueString.equals(""))
					System.out.println("\"" + triple_num + "\"\t\""
						+ movieNameString + "\"\t\"" + propertyString + "\"\t\""
						+ valueString + "\"\t\"" + concept_num + "\"\t\" \"");

				System.out.println(propertyString);
				System.out.println(valueString);
				// System.out.println(arrayStrings[2]);
				if (line == null)
					flag = false;
			}
		} catch (Exception e)
		{
			// TODO: handle exception
		}

	}

	public static void main(String[] args)
	{
		try
		{
			// TODO Auto-generated method stub
			InfoExtractDoubanMovie infoExtractDoubanmovie = new InfoExtractDoubanMovie();
			String movieInfoFile = "E:\\Husen\\data\\douban_movie\\movieInfoUpdate1.txt";
			//infoExtractDoubanmovie.transformDataForm(filename);
			String subjectFile = "E:\\Husen\\data\\douban_movie\\need_to_update.txt";
			PrintStream movieInfoPrintStream = new PrintStream(movieInfoFile);
			 
			  String logFile = "E:\\Husen\\data\\douban_movie\\updatelog1.txt";
			  String errorFile = "E:\\Husen\\data\\douban_movie\\updateerror1.txt";
			  PrintStream logPrintStream = new PrintStream(logFile); 
			  PrintStream errorPrintStream = new PrintStream(errorFile); 
			  System.setOut(logPrintStream); 
			  System.setErr(errorPrintStream);
			  
			long t1 = System.currentTimeMillis();
			infoExtractDoubanmovie.readUrlList(subjectFile); 
			for(String urlString : infoExtractDoubanmovie.getListOfURL()) 
			{ 
				urlString = urlString.replace("http:", "https:");
				infoExtractDoubanmovie.setCurrentUrl(urlString);
				System.out.println("urlString:"+urlString);
				if(infoExtractDoubanmovie.extractMovieInfo()) 
				{
					infoExtractDoubanmovie.saveCurrentInfo(movieInfoPrintStream); 
				}
				else 
				{ 
					System.err.println("URL:" + urlString);
					infoExtractDoubanmovie.getFailedUrlList().add(urlString); 
				}
				Thread.sleep(2000); 
			} 
			//infoExtractDoubanmovie.extractMovieInfo();
			long t2 = System.currentTimeMillis();
			System.out.print("Crawle time:");
			System.out.print(t2-t1);
			System.out.println("ms");
			
			 
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}


}
