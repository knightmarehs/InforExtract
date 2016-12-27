package spider.utility;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.htmlparser.util.ParserException;

public class TestSpider {

	/**
	 * To test the douban Extraction
	 * @param args
	 * @throws IOException 
	 * @throws ParserException 
	 */
	public void scanSignelUrl(String url) throws ParserException, IOException
	{
		String urlBase = "http://movie.douban.com/tag/爱情?start=";
		url = urlBase + url;
		InfoExtractDoubanMovie infoExt = new InfoExtractDoubanMovie();
		
		infoExt.setCurrentUrl(url);
		infoExt.extractMovieurls();
		
		infoExt.extractMovieurls();
		
		Iterator<String> it = infoExt.urlListMap.keySet().iterator();
		while(it.hasNext())
		{
			System.out.println(it.next());
		}
			
	}
	public void testForSingleUrl(String url)
	{
		String urlBase = "http://movie.douban.com/subject/";
		String urlBase1 = "http://movie.douban.com/celebrity/";
		url = urlBase + url + "/";
		//InfoExtractDoubanCelebrity infoExt = new InfoExtractDoubanCelebrity();
		InfoExtractDoubanMovie infoExt = new InfoExtractDoubanMovie();
		//InfoExtractDoubanMusic infoExt = new InfoExtractDoubanMusic();
		infoExt.setCurrentUrl(url);
		infoExt.extractMovieInfo();
		//infoExt.outputSingleEntity(System.out);
	}
	public void testForListUrl(String[] urlList) 
	{
		try{
		for(String item:urlList)
		{
			testForSingleUrl(item);
			System.out.println("if continue:y?n");
			InputStreamReader inputreader = new InputStreamReader(System.in);
			char[] getin = new char[2];
			inputreader.read(getin);
			if(getin[0] == 'y')
			{
				continue;
			}
			else
				break;
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws ParserException, IOException 
	{
		String[] urlList = 
			{
				"1394547","20378879","1462686","1407919","3435496","1924543","4060882","5259254","6855941","3576679",
				"20269194","6047523","20257188","11504490","1790638","3035043","3141335","11504430","10742528","4090465",
				"6957521","10618796","11504490","11621675","19965722","11610864","20271083","3277240","6439864","10565154",
				"19970961","3002544","3072330","2362976","7063434","3991279","10565154","1404953","2375834","3810299",
				"2154082","2326045","199749","1415125","1403126","1776436","2211438","11535674"
			};
		String[] FailedList = 
			{
				"21359619","24404677","1048027","1019001","1306464","1011820","3006424"
			};
		TestSpider ts = new TestSpider();
		//ts.testForListUrl(urlList);
		ts.testForSingleUrl(FailedList[FailedList.length - 1]);
		//for(String str:urlList)
		//ts.scanSignelUrl("0");
	}

}
