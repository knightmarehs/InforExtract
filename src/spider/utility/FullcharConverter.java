package spider.utility;

import java.io.UnsupportedEncodingException;

import javax.print.DocFlavor.INPUT_STREAM;

public class FullcharConverter
{
	public static final String full2HalfChange(String QJstr)
	{
		char[] outCharArr = QJstr.toCharArray();
		for(int i = 0; i < QJstr.length(); i++)
		{
			if(outCharArr[i] == 12288)
			{
				outCharArr[i] = (char)32; 
				continue;
			}
			if(outCharArr[i] > 65280 && outCharArr[i] < 65375)
			{
				outCharArr[i] = (char)(outCharArr[i] - 65248);
			}
		}
		return new String(outCharArr);
	}
	public static final String half2FullChange(String BJstr)
	{
		char[] outCharArr = BJstr.toCharArray();
		for(int i = 0; i < BJstr.length(); i++)
		{
			if(outCharArr[i] == 32)
			{
				outCharArr[i] = 12288;
				continue;
			}
			if(outCharArr[i] < 127 && outCharArr[i] > 32)
			{
				outCharArr[i] = (char)(outCharArr[i] + 65248);
			}
		}
		return new String(BJstr);
	}
	public static void main(String[] args)
	{
		String inputString = "１２　３‘’；“、's";
		String outputString = FullcharConverter.full2HalfChange(inputString);
		System.out.println(inputString);
		System.out.println(outputString);
	}
}
