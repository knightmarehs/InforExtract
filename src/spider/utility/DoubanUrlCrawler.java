package spider.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.omg.CosNaming.NamingContextExtPackage.URLStringHelper;


public class DoubanUrlCrawler
{

	public DoubanUrlCrawler(String urlbaseString)
	{
		urlBase = urlbaseString;
	}

	public DoubanUrlCrawler()
	{
	}

	/**
	 * ����url��ǰ׺
	 */
	private String urlBase = "http://movie.douban.com";

	/**
	 * ��ʼҳ����
	 */
	private String urlStart = "?start=";

	/**
	 * ҳ�水ʱ������,�п���ԭ����30ҳ�ģ�����ʱ��������ٵ�20ҳ
	 */

	private String urlTypeWithAll = "&type=T";

	private ArrayList<String> failedUrlList = new ArrayList<String>();
	
	private ArrayList<String> subjectList = new ArrayList<String>();
	public ArrayList<String> updateUrl = new ArrayList<String>();
	
	private String[] movieStyleList =
		{
			"爱情","喜剧","动画","科幻","经典","剧情","动作","青春","悬疑","惊悚","犯罪","纪录片",
			"文艺","励志","搞笑","恐怖","短片","战争","魔幻","黑色幽默","动画短片","情色","传记",
			"感人","暴力","童年","音乐","同志","黑帮","浪漫","女性","家庭","史诗","童话","烂片","cult",
			"美国","日本","香港","英国","中国","法国","韩国","台湾","德国","意大利","内地","泰国",
			"西班牙","印度","欧洲","加拿大","中国大陆","澳大利亚","俄罗斯","伊朗","瑞典","巴西",
			"爱尔兰","波兰","捷克","丹麦","阿根廷","比利时","墨西哥","奥地利","荷兰","匈牙利",
			"土耳其","新西兰","新加坡","以色列","宫崎骏","周星驰","王家卫","JohnnyDepp","岩井俊二",
			"张国荣","梁朝伟","尼古拉斯·凯奇","张艺谋","刘德华","冯小刚","斯皮尔伯格","成龙","杜琪峰",
			"李连杰","姜文","徐克","TimBurton","周迅","周润发","桂纶镁","李安","奥黛丽·赫本","金城武",
			"AnneHathaway","徐静蕾","舒淇","刘青云","吴彦祖","JimCarrey","彭浩翔","希区柯克","汤姆·汉克斯",
			"大卫·芬奇","王晶","新海诚","2011","2012","2010","2013","2009","2008","2007","2006","2004","2005",
			"2003","2001","2002","2000","1997","1994","1999","1998","2014","1995","1996","1993","1993","1992",
			"1990","1991","1988","1989","1989","2015"
		};
	// For book url extraction
	private String[] bookStyleList =
	{ "小说", "外国文学", "文学", "随笔", "中国文学", "经典", "散文", "日本文学", "村上春树", "童话", "诗歌",
			"王小波", "张爱玲", "杂文", "名著", "儿童文学", "古典文学", "余华", "钱钟书", "鲁迅",
			"当代文学", "外国名著", "诗词", "杜拉斯", "茨威格", "米兰·昆德拉", "港台", "漫画", "绘本",
			"推理", "青春", "言情", "科幻", "韩寒", "亦舒", "武侠", "耽美", "日本漫画", "悬疑",
			"安妮宝贝", "奇幻", "东野圭吾", "三毛", "郭敬明", "几米", "网络小说", "穿越", "金庸",
			"阿加莎·克里斯蒂", "轻小说", "推理小说", "张小娴", "魔幻", "高木直子", "张悦然", "沧月",
			"青春文学", "落落", "JK罗琳", "蔡康永", "古龙", "余秋雨", "历史", "哲学", "心理学", "传记",
			"文化", "社会学", "设计", "艺术", "政治", "建筑", "社会", "宗教", "电影", "数学", "政治学",
			"思想", "回忆录", "国学", "中国历史", "人文", "音乐", "戏剧", "人物传记", "佛教", "绘画",
			"艺术史", "西方哲学", "军事", "自由主义", "二战", "近代史", "考古", "美术", "爱情", "旅行",
			"生活", "励志", "摄影", "心理", "成长", "职场", "游记", "女性", "教育", "美食", "灵修",
			"情感", "健康", "手工", "养生", "两性", "家居", "人际关系", "自助游", "经济学", "管理",
			"经济", "金融", "商业", "投资", "营销", "广告", "理财", "创业", "股票", "企业史", "策划",
			"科普", "互联网", "编程", "科学", "交互设计", "用户体验", "算法", "web", "UE", "UCD",
			"科技", "通信", "交互", "程序", "神经网络" };
	
	private String[] hotStyleList = 
		{
			"2011","2012","BL","爱情","编程","成长","J·K·罗琳","传记","耽美","德国","儿童文学","法国","村上春树",
			"高木直子","古典文学","管理","郭敬明","国学","哈利波特","韩寒","回忆录","绘本","计算机","建筑","教材",
			"教育","金庸","经典","经济","经济学","科幻","科普","励志","历史","灵修","龙应台","旅行","落落","漫画",
			"美国","美国文学","美食","名著","魔幻","女性","奇幻","钱钟书","青春","青春文学","轻小说","人类学","人生",
			"人文","日本","日本漫画","日本文学","三毛","散文","商业","摄影","社会","社会学","设计","生活","诗歌","数学",
			"思维","思想","随笔","台湾","童话","投资","推理","推理小说","外国文学","王小波","网络小说","文化","文学",
			"武侠","香港","小说","心理","心理学","悬疑","爱情","言情","艺术","亦舒","音乐","英国","英国文学","英语",
			"营销","游记","余华","杂文","张爱玲","张小娴","张悦然","哲学","政治学","政治哲学","职场","中国","中国历史",
			"中国文学","宗教"
		};


	public String[] getHotStyleList()
	{
		return hotStyleList;
	}

	public void setHotStyleList(String[] hotStyleList)
	{
		this.hotStyleList = hotStyleList;
	}


	/**
	 * ����һ����𣬻�ô����������ҳ���url�б?
	 * 
	 * @param type
	 * @param urlList
	 * @param urlMap
	 * @param failedUrlList
	 * @return
	 */
	public ArrayList<String> crawleByType_Music(String type,
			HashMap<String, String> urlMap, ArrayList<String> failedUrlList,
			int maxPage, String rankType)
	{
		// �ӵ�0ҳ��ʼ��ȡ��
		String nextPageUrl = urlBase + type + urlStart + "0" + rankType;
		ArrayList<String> urlList = crawleByStartUrl_Music(nextPageUrl, urlMap,
				failedUrlList, maxPage);
		return urlList;
	}

	public void crawleByType_Book(String type,int maxPage, String rankType)
	{
		String nextPageUrl = urlBase + "/tag/" + type + urlStart + "0"
				+ rankType;
		crawleByStartUrl_Book(nextPageUrl,type, maxPage, rankType);
	}

	private ArrayList<String> crawleByStartUrl_Music(String nextPageUrl,
			HashMap<String, String> urlMap, ArrayList<String> failedUrlList,
			int maxPage)
	{

		ArrayList<String> result = new ArrayList<String>();
		// Pattern urlPattern = Pattern
		// .compile("href=\"(http://movie.douban.com/subject/[0-9]+/)\"");

		Pattern urlPattern = Pattern
				.compile("href=\"(http://music.douban.com/subject/[0-9]+/)\"[ ]*title=\"(.+?)\"[ ]*>");
		Pattern nextPattern = Pattern
				.compile("<span class=\"next\"><a href=\"(http://music.douban.com/tag/.+?start=[0-9]+.*)\">��ҳ");
		int pageNum = 0;
		while (nextPageUrl != null && pageNum < maxPage)
		{
			pageNum++;
			System.out.println("������ȡ" + nextPageUrl);
			String page = null;
			int addNum = 0;
			int allNum = 0;
			int doubleNum = 0;
			try
			{
				page = crawleByURL(nextPageUrl, "utf-8");
				System.out.println(page);
				if (page != null)
				{
					Matcher matcher = urlPattern.matcher(page);
					while (matcher.find())
					{
						String url = matcher.group(1);
						allNum++;
						if (!urlMap.containsKey(url))
						{
							urlMap.put(url, "");
							result.add(url);
							System.out.println(url);
							addNum++;
						} else
						{
							doubleNum++;
						}
					}
					matcher = nextPattern.matcher(page);
					if (matcher.find())
					{
						nextPageUrl = matcher.group(1);
					} else
					{
						nextPageUrl = null;
					}
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				failedUrlList.add(nextPageUrl);
				return result;
			}
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException e)
			{
				System.out.println(e.getMessage());
				return result;
			}
		}

		return result;
	}

	private void crawleByStartUrl_Book(String nextPageUrl,String type,int maxPage,String rankType)
	{
		//Pattern urlPattern = Pattern.compile("href=\"(http://book.douban.com/subject/[0-9]+/)\"[\\s]*onclick=.+?[\\s]*"+ "title=\"(.+?)\"[\\s]*>");
		Pattern urlPattern = Pattern.compile("<a class=\"nbg\" href=\"([^\"]+?)\"[\\s]*title");
		//Pattern nextPattern = Pattern.compile("<span class=\"next\">[\\s]*<link rel=\"next\"[\\s]*href=\"(/tag/[^>]+?)\"/>");
		NodeFilter filterItem = new org.htmlparser.filters.HasAttributeFilter(
				"class", "item");
		int pageNum = 0;
		while (nextPageUrl != null && pageNum < maxPage)
		{
			pageNum++;
			System.out.println("nextURL:" + nextPageUrl);
			String page = null;
			int addNum = 0;
			int allNum = 0;
			int doubleNum = 0;
			try
			{
				page = crawleByURL(nextPageUrl, "utf-8");
			    //System.out.println(page);
			    Parser parser = Parser.createParser(page, "utf-8");
				
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
						//System.out.println("----------- "+i+" --------------");
						//System.out.println(itemString);
						
						Matcher itemMatcher = urlPattern.matcher(itemString);
						if(itemMatcher.find())
						{
							String urlString = itemMatcher.group(1);
							//System.out.println(urlString);
							allNum++;
							if (!subjectList.contains(urlString))
							{
								subjectList.add(urlString);
								//System.out.println("addNum:"+addNum+" "+urlString);
								addNum++;
							} else
							{
								doubleNum++;
							}
						}
						else
							System.out.println("Nt");
					}
				}
				if(allNum > 0)
				{
					int tmp=pageNum*20;
					nextPageUrl = urlBase + "/tag/" + type + urlStart + tmp
							+ rankType;
				} 
				else
				{
					nextPageUrl = null;
				}
				
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				failedUrlList.add(nextPageUrl);
//				return result;
			}
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException e)
			{
				System.out.println(e.getMessage());
//				return result;
			}
		}

//		return result;
	}

	public byte[] crawleByURL(String url)
	{
		HttpClient client = new HttpClient();
		client.getParams()
				.setParameter(
						HttpMethodParams.USER_AGENT,
						"Mozilla/5.0 (compatible; YodaoBot_0529/1.0; http://www.yodao.com/help/webmaster/spider/)");
		client.getParams().setSoTimeout((int) (5 * 1000));

		byte[] content = null;
		HttpMethod method = new GetMethod(url);
		method.setFollowRedirects(true);
		try
		{
			client.executeMethod(method);
			content = method.getResponseBody();
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			return null;
		} finally
		{
			method.releaseConnection();
		}
		return content;

	}
	
	public String crawleByURL(String webpageURL, String type)
			throws IOException
	{
		
		String sourceCode = HttpRequest.sendPost(webpageURL,"");;
		
		if (sourceCode == null || sourceCode.equals(""))
			throw new IOException("sourceCode : null");
		return sourceCode;
	}

	/**
	 * ���ҳ������:һ���򵥵����ӹ��ߡ�
	 * 
	 * @param webpageURL
	 * @param type
	 * @return
	 * @throws IOException
	 */
//	public String crawleByURL(String webpageURL, String type)
//			throws IOException
//	{
//		BufferedReader br;
//		String sourceCode = "";
//		for (int i = 0; i < 10; i++)
//		{
//			URL url;
//			try
//			{
//				url = new URL(webpageURL);
//				HttpURLConnection connection = (HttpURLConnection) url
//						.openConnection();
//				//set agent to cheat server
//				connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//				
//				connection.setDoInput(true);
//				connection.setConnectTimeout(5000);
//				connection.setReadTimeout(5000);
//				//System.out.println("NewUrl:"+url);
//				br = new BufferedReader(new InputStreamReader(
//						connection.getInputStream(), type));
//				String temp;
//				while ((temp = br.readLine()) != null)
//				{
//					sourceCode += temp;
//				}
//				br.close();
//				break;
//			} catch (Exception e)
//			{
//				System.out.println(e.getMessage());
//				continue;
//			}
//		}
//		if (sourceCode == null || sourceCode.equals(""))
//			throw new IOException("sourceCode : null");
//		return sourceCode;
//	}


	public ArrayList<String> fetchSubjectForMovie()
	{
		for (String movieType : movieStyleList)
		{
			//ArrayList<String> urlListWithinType = new ArrayList<String>();
			crawleByType_Book(movieType, 10000, urlTypeWithAll);
		}
		int tagSubjectSize = subjectList.size();
		System.out.println("the size of subject is" + tagSubjectSize);
		return subjectList;
	}
	
	public ArrayList<String> updateSubjectForMovie()
	{
		String page = null;
		String url = "https://movie.douban.com/nowplaying/beijing/";
		try
		{
			page = crawleByURL(url, "utf-8");
		    
			System.out.println(page);
		    
			Parser parser = Parser.createParser(page, "utf-8");
			NodeFilter filterItem = new org.htmlparser.filters.HasAttributeFilter(
					"id", "nowplaying");
			Pattern urlPattern = Pattern.compile("data-subject=\"([^\"]+?)\"");
			parser.reset();
			NodeList itemList = parser.extractAllNodesThatMatch(filterItem);
			
			if(itemList.size()>0)
			{
				Node itemNode;
				for(int i=0;i<itemList.size();i++)
				{
					itemNode = itemList.elementAt(i);
					String itemString = itemNode.toHtml();
					
//					System.out.println("----------- "+i+" --------------");
//					System.out.println(itemString);
					
					Matcher itemMatcher = urlPattern.matcher(itemString);
					while(itemMatcher.find())
					{
						String idString = itemMatcher.group(1);
			
						String urlString = "http://movie.douban.com/subject/"+idString+"/";
						System.out.println(urlString);
						
						if (!subjectList.contains(urlString))
						{
							subjectList.add(urlString);
							updateUrl.add(urlString);
							//System.out.println("addNum:"+addNum+" "+urlString);
						} 
					}
				}
			}
			
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
//			return result;
		}
		
		return updateUrl;
	}
	/**
	 * ��ȡ����ҳ���subject id.
	 * 
	 * @param type
	 * @param urlList
	 * @param urlMap
	 * @param failedUrlList
	 * @return movie��uri.
	 */
	private ArrayList<String> crawleSinglePage(String url)
	{

		ArrayList<String> result = new ArrayList<String>();
		Pattern urlPattern = Pattern
				.compile("href=\"(http://music.douban.com/subject/[0-9]+/)\"");
		try
		{
			String page = crawleByURL(url, "utf-8");

			if (page != null)
			{
				Matcher matcher = urlPattern.matcher(page);
				while (matcher.find())
				{
					String subjectUrl = matcher.group(1);
					if (!result.contains(subjectUrl))
						result.add(subjectUrl);
				}
			}

		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			failedUrlList.add(url);
			return result;
		}
		try
		{
			Thread.sleep(2000);
		} catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
			return result;
		}
		return result;
	}

	public void saveSubjectList(String subjectFile, Boolean flag)
	{
		try
		{
			OutputStream outputStream = new FileOutputStream(subjectFile,flag);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			for (String url : subjectList)
			{
				bufferedWriter.write(url + "\n");
			}
			bufferedWriter.flush();
			bufferedWriter.close();
			outputStreamWriter.close();
			outputStream.close();
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public void saveList(ArrayList<String> list, String outputFile)
	{
		try
		{
			System.out.println("save begin.");
			OutputStream outputStream = new FileOutputStream(outputFile);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			for (String url : list)
			{
				bufferedWriter.write(url + "\n");
			}
			bufferedWriter.flush();
			bufferedWriter.close();
			outputStreamWriter.close();
			outputStream.close();
			System.out.println("save done.");
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public void saveFailedList(String failedListFile, Boolean flag)
	{
		try
		{
			OutputStream outputStream = new FileOutputStream(failedListFile,flag);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			for (String url : failedUrlList)
			{
				bufferedWriter.write(url + "\n");
			}
			bufferedWriter.flush();
			bufferedWriter.close();
			outputStreamWriter.close();
			outputStream.close();
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	public void readSubjectFile(String filename)
	{
		try
		{
			FileInputStream fis = new FileInputStream(filename);
		    InputStreamReader isr = new InputStreamReader(fis);
		    BufferedReader bufferedReader = new BufferedReader(isr);
		    String urlString;
		    subjectList.clear();
		    while((urlString = bufferedReader.readLine())!= null)
			{
		    	if(!subjectList.contains(urlString))
		    		subjectList.add(urlString);
			}
		    System.out.println(subjectList.size());
		    bufferedReader.close();
		    isr.close();
		    fis.close();
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public void fetchFailedSubjList_Book(String failedListFile,String subjectListFile)
	{
		try
		{
			readSubjectFile(subjectListFile);
			FileInputStream fileInputStream = new FileInputStream(failedListFile);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
			BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader);
			ArrayList<String> urlList = new ArrayList<String>();
			String urlString;
			while((urlString = bufferedReader2.readLine()) != null)
			{
				urlList.add(urlString);
			}
			bufferedReader2.close();
			inputStreamReader.close();
			fileInputStream.close();
			failedUrlList.clear();
			for(String url:urlList)
			{
				//crawleByStartUrl_Book(url,10000);
			}
			System.out.println(subjectList.size());
			saveFailedList(failedListFile, false);
			saveSubjectList(subjectListFile, true);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args)
	{
		long t1 = System.currentTimeMillis(); 
		DoubanUrlCrawler doubanURLCrawlerTool = new DoubanUrlCrawler("http://movie.douban.com");
		ArrayList<String> result = doubanURLCrawlerTool.updateSubjectForMovie();
		String out = "E:\\Husen\\data\\douban_movie\\douban_movie_subjectid_update.txt";
		
		try
		{
			//doubanURLCrawlerTool.readSubjectFile(out);
			long t2 = System.currentTimeMillis();
			System.out.print("Scraw time is:");
			System.out.print(t2-t1);
			System.out.println("ms.");
			doubanURLCrawlerTool.saveSubjectList(out, false);
		} 
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}
