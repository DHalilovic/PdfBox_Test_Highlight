import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PDFTextSearcher extends PDFTextStripper
{

	public PDFTextSearcher() throws IOException
	{
		super();
	}

	/**
	 * Override the default functionality of PDFTextStripper.writeString()
	 */
	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException
	{
		/*
		for (TextPosition text : textPositions)
		{
			System.out.println(text.getUnicode() + " [(X=" + text.getXDirAdj() + ",Y=" +
					text.getYDirAdj() + ") height=" + text.getHeightDir() + " width=" +
					text.getWidthDirAdj() + "]");
		}
		*/
	}
	
	public ArrayList<List<TextPosition>> getTextPositions()
	{
		return charactersByArticle;
	}
}
