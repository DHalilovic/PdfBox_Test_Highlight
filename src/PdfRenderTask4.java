import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.TextPosition;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PdfRenderTask4<E> extends Task<E>
{
	PDDocument pdDocument;
	PDFRenderer pdfRenderer;
	PDFTextSearcher pdfTextSearcher;
	Pane placeholderLayer;
	Pane highlightLayer;
	Pane pageLayer;
	int numberPages;
	int pageWidth;
	int dpi;
	Set<Integer> requestedPages;
	ArrayList<ImageView> pageImages;
	HashMap<Integer, ArrayList<Rectangle>> highlightImages;

	public PdfRenderTask4(PDDocument pdDocument, int numberPages, Pane pageLayer, Pane highlightLayer, Pane placeholderLayer, Set<Integer> requestedPages, int pageWidth, int dpi) throws IOException
	{
		this.pdDocument = pdDocument;
		this.pdfRenderer = new PDFRenderer(pdDocument);
		this.pdfTextSearcher = new PDFTextSearcher();
		this.numberPages = numberPages;
		this.highlightLayer = highlightLayer;
		this.pageLayer = pageLayer;
		this.placeholderLayer = placeholderLayer;
		pageImages = new ArrayList<ImageView>();
		highlightImages = new HashMap<Integer, ArrayList<Rectangle>>();
		this.requestedPages = requestedPages;
		this.pageWidth = pageWidth;
		this.dpi = dpi;
	}

	private void renderPage(int page, double xPosition) throws IOException
	{
		BufferedImage bim = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
		WritableImage wim = SwingFXUtils.toFXImage(bim, null);
		PageImageView pageImage = new PageImageView(wim, page);
		pageImage.setPreserveRatio(true);
		pageImage.setFitWidth(pageWidth);
		pageImage.relocate(xPosition, 0);

		pageImages.add(pageImage);
	}

	private void renderHighlight(int page, double xPosition) throws IOException
	{
		pdfTextSearcher.setEndPage(page + 1);
		System.out.println("Page Highlighted: " + pdfTextSearcher.getEndPage());
		pdfTextSearcher.writeText(pdDocument, new OutputStreamWriter(new ByteArrayOutputStream()));
		ArrayList<List<TextPosition>> textPositions = pdfTextSearcher.getTextPositions();
		ArrayList<Index> indices = getIndex(textPositions, "java");
		ArrayList<Rectangle> pageHighlights = new ArrayList<Rectangle>();

		for (Index index : indices)
		{
			TextPosition startPos = textPositions.get(index.article).get(index.position);
			TextPosition endPos = textPositions.get(index.article).get(index.position + "java".length() - 1);
			Rectangle highlight = new Rectangle(startPos.getX() + xPosition, startPos.getY() - startPos.getHeight(), endPos.getEndX() - startPos.getX(), startPos.getHeight());
			highlight.setFill(Color.rgb(255, 0, 0, 0.5));
			//TODO Something wrong here
			pageHighlights.add(highlight);
			System.out.println("Working on highlight...");
		}

		highlightImages.put(page, pageHighlights);
	}

	private void renderPages() throws IOException
	{
		ObservableList<Node> xPositions = placeholderLayer.getChildren();

		for (int page : requestedPages)
		{
			double pagePosition = ((Rectangle) xPositions.get(page)).xProperty().doubleValue();
			renderPage(page, pagePosition);
			renderHighlight(page, pagePosition);
		}

		if (isCancelled())
			System.out.println("Task cancelled");
	}

	private static ArrayList<Index> getIndex(ArrayList<List<TextPosition>> textPositions, String target)
	{
		ArrayList<Index> result = new ArrayList<Index>();

		for (int h = 0; h < textPositions.size(); h++)
		{
			List<TextPosition> text = textPositions.get(h);

			for (int i = 0; i < text.size(); i++)
			{
				if (text.get(i).getUnicode().toLowerCase().charAt(0) == (target.charAt(0)))
				{
					boolean match = true;
					int j = 1;

					for (; i + j < text.size() && j < target.length(); j++)
					{
						if (text.get(i + j).getUnicode().toLowerCase().charAt(0) != target.charAt(j))
						{
							match = false;
							break;
						}
					}

					if (match)
					{
						result.add(new Index(h, i));
					}

					i = i + j + 1;
				}
			}
		}

		return result;
	}

	@Override
	protected E call() throws Exception
	{
		try
		{
			renderPages();
		} catch (IOException e)
		{
			System.out.println("Task failed to render pages");
		}

		return null;
	}

	@Override
	protected void succeeded()
	{
		for (ImageView pageImage : pageImages)
			pageLayer.getChildren().add(pageImage);

		for (Entry<Integer, ArrayList<Rectangle>> page : highlightImages.entrySet())
		{
			for (Rectangle highlight : page.getValue())
				highlightLayer.getChildren().add(highlight);
		}
	}
}
