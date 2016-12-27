package spider.utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

public class InfoExtractDoubanCelebrity
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
		if(!flag)
			System.out.println("basic info not found");
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
				System.out.println("link pair not found");
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
				.compile("<div class=\"bd\">[\\s]*(.+?)[\\s]*</div>");
		Pattern patternLong = Pattern
				.compile("<span class=\"all hidden\">[\\s]*(.+?)[\\s]*</span>");
		
		Matcher matcher = patternLong.matcher(content);
		if (matcher.find())
		{
			resultString = matcher.group(1);
		}
		else
		{
			matcher = pattern.matcher(content);
			if(matcher.find())
				resultString = matcher.group(1);
		}
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
				System.out.println("姓名: " + titleString);
				
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
	public Boolean extractCelebrityurls()
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
	
	public Boolean extractCelebrityInfo()
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
				NodeFilter headFilter = new TagNameFilter("head");
				NodeFilter filterContentIntro = new org.htmlparser.filters.HasAttributeFilter(
						"id", "intro");
				
				// filter for clebrity
				NodeFilter filter = new org.htmlparser.filters.HasAttributeFilter(
						"id", "info");
				NodeFilter filterReinfo = new org.htmlparser.filters.HasAttributeFilter(
						"class", "all hidden");
				
				// node list for celebrity
				parser.reset();
				NodeList headList = parser.extractAllNodesThatMatch(headFilter);
				parser.reset();
				NodeList celebrityNodesInfo = parser.extractAllNodesThatMatch(filterCelebrityInfo);
				parser.reset();
				NodeList nodesContentIntro = parser.extractAllNodesThatMatch(filterContentIntro);
				
				// node list for movie
				parser.reset();
				NodeList nodesInfo = parser.extractAllNodesThatMatch(filter);
				parser.reset();
				NodeList nodesReinfo = parser
						.extractAllNodesThatMatch(filterReinfo);
				
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
						System.out.println("姓名: " + titleString);
					}
				} else
				{
					System.err.println(currentUrl);
					System.err.println("Detecting entity name error!");
					return false;
				}
				if (celebrityNodesInfo.size() == 1)
				{
					Node Info = celebrityNodesInfo.elementAt(0);
					String infoHtml = Info.toHtml();
					//System.out.println(infoHtml);
					Pattern basicP = Pattern
							.compile("<li>[\\s]*<span>([^<]+?)</span>[:]?[\\s]*([^<]+?)[\\s]*</li>");
					Pattern linkP = Pattern
							.compile("<li>[\\s]*<span>([^<]+?)</span>[:]?[\\s]*<a href=\"[^<]+?\">([^<]+?)</a>");
					Pattern time2P =Pattern.compile("<span class=['\"]pl['|\"]>[\\s]*([^<]+?):?[\\s]*</span>[\\s]*:?[\\s]*"
							+"<span property=\"v:runtime\" [^<]*>([^<]+?)</span>[\\s]*/[\\s]*([^<]+?)[\\s]*<br[/]?>");
					extractPairFromHtml(infoHtml, basicP);
					//System.out.println(infoHtml);
					extractLinkPairFromHtml(infoHtml, linkP);
					extractTime2FromHtml(infoHtml, time2P);
				} else
				{
					System.err.println(currentUrl);
					System.err.println("There exits " + celebrityNodesInfo.size()+ " infoboxs");
					return false;
				}
				if (nodesContentIntro.size() == 1)
				{
					Node Intro = nodesContentIntro.elementAt(0);
					String Introstr = Intro.toHtml();
					//System.out.println(Introstr);
					String contentString = extractContentInfo(Introstr);
					System.out.println("影人简介" + " : " + contentString);
					infoMap.put("影人简介", contentString);
				} else
				{
					System.err.println(currentUrl);
					System.err.println("There exits "
							+ nodesContentIntro.size()
							+ " Content Introduction");
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

	public void setUrlList()
	{
		int minId = 1112113;
		int maxId = 1345607;
		int currentId = minId;
		String urlBase = "http://movie.douban.com/celebrity/";
		String url;
		for(int id=minId;id<=maxId;id++)
		{
			url = urlBase + id;
			listOfURL.add(url);
		}
		//System.out.println("url:"+url);
		
	}
	public void readUrlList(String filename)
	{
		try
		{
			String urlBase = "http://movie.douban.com/celebrity/";
			FileInputStream fileInputStream = new FileInputStream(filename);
			InputStreamReader inputStreamReader = new InputStreamReader(
					fileInputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String url = "";
			while ((url = bufferedReader.readLine()) != null)
			{
				listOfURL.add(urlBase+url);
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

	public static void main(String[] args)
	{
		try
		{
			// TODO Auto-generated method stub
			InfoExtractDoubanCelebrity infoExtractDoubanCelebrity = new InfoExtractDoubanCelebrity();
			String celebrityInfoFile = "E:\\Husen\\data\\douban_celebrity\\celebrityInfo2.txt";
			PrintStream celebrityInfoPrintStream = new PrintStream(celebrityInfoFile);
			 
			  String failedFileString = "E:\\Husen\\data\\douban_celebrity\\douban_celebrity_failedUrl2.txt";
			  String logFile = "E:\\Husen\\data\\douban_celebrity\\log2.txt";
			  String errorFile = "E:\\Husen\\data\\douban_celebrity\\error2.txt";
			  String errorUrlFile = "E:\\Husen\\data\\douban_celebrity\\errorUrl.txt";
			  PrintStream logPrintStream = new PrintStream(logFile); 
			  PrintStream errorPrintStream = new PrintStream(errorFile); 
			  PrintStream failedPrintStream = new PrintStream(failedFileString);
			  System.setOut(logPrintStream); 
			  System.setErr(errorPrintStream);
			  
			long t1 = System.currentTimeMillis();
			//infoExtractDoubanCelebrity.setUrlList(); 
			infoExtractDoubanCelebrity.readUrlList(errorUrlFile);
			for(String urlString : infoExtractDoubanCelebrity.getListOfURL()) 
			{ 
				infoExtractDoubanCelebrity.setCurrentUrl(urlString);
				System.out.println("urlString:"+urlString);
				if(infoExtractDoubanCelebrity.extractCelebrityInfo()) 
				{
					infoExtractDoubanCelebrity.saveCurrentInfo(celebrityInfoPrintStream); 
				}
				else 
				{ 
					System.err.println("URL:" + urlString);
					infoExtractDoubanCelebrity.getFailedUrlList().add(urlString); 
				}
				Thread.sleep(2000); 
			} 
			
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
