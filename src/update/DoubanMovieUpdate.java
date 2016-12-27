package update;

import java.io.PrintStream;
import java.util.ArrayList;

import spider.utility.*;

public class DoubanMovieUpdate
{
// 每次shut down后要手动将douban_movie_subjectid_update中的内容添加到douban_movie_subjectid的末尾，并更新need_to_update.txt
	public static void main(String[] args)
	{
		long t1 = System.currentTimeMillis(); 
		DoubanUrlCrawler doubanURLCrawlerTool = new DoubanUrlCrawler(
				"http://movie.douban.com");
		ArrayList<String> result = new ArrayList<String>();
		String subjectidFile = "E:\\Husen\\Data\\douban_movie\\douban_movie_subjectid.txt";
		String out = "E:\\Husen\\data\\douban_movie\\douban_movie_subjectid_update.txt";
		
		System.out.println("read url begin.");
		doubanURLCrawlerTool.readSubjectFile(subjectidFile);
		System.out.println("read url end.");
		
		while(true)
		{
			try
			{
				result = doubanURLCrawlerTool.updateSubjectForMovie();
				long t2 = System.currentTimeMillis();
				System.out.print("Scraw time is:");
				System.out.print(t2-t1);
				System.out.println("ms.");
				doubanURLCrawlerTool.saveList(result,out);
			} 
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
			try
			{
				Thread.sleep(86400000);
			} catch (InterruptedException e)
			{
				System.out.println(e.getMessage());
			}
		}
		
	}
}
