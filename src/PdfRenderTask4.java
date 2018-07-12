import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class PdfRenderTask4<E> extends Task<E>
{
	PDFRenderer pdfRenderer;
	PDFTextSearcher pdfTextSearcher;
	Pane placeholderLayer;
	Pane pageLayer;
	int numberPages;
	int pageWidth;
	int dpi;
	Set<Integer> requestedPages;
	ArrayList<ImageView> pageImages;

	public PdfRenderTask4(PDDocument pdDocument, int numberPages, Pane pageLayer, Pane placeholderLayer, Set<Integer> requestedPages, int pageWidth, int dpi) throws IOException
	{
		this.pdfRenderer = new PDFRenderer(pdDocument);
		this.pdfTextSearcher = new PDFTextSearcher();
		this.numberPages = numberPages;
		this.pageLayer = pageLayer;
		this.placeholderLayer = placeholderLayer;
		pageImages = new ArrayList<ImageView>();
		this.requestedPages = requestedPages;
		this.pageWidth = pageWidth;
		this.dpi = dpi;
	}

	private void renderPage(int index, double xPosition) throws IOException
	{
		BufferedImage bim = pdfRenderer.renderImageWithDPI(index, dpi, ImageType.RGB);
		WritableImage wim = SwingFXUtils.toFXImage(bim, null);
		PageImageView pageImage = new PageImageView(wim, index);
		pageImage.setPreserveRatio(true);
		pageImage.setFitWidth(pageWidth);
		pageImage.relocate(xPosition, 0);
		pageImages.add(pageImage);
	}

	private void renderPages() throws IOException
	{
		ObservableList<Node> xPositions = placeholderLayer.getChildren();

		
		for (int index : requestedPages)
		{
			renderPage(index, ((Rectangle) xPositions.get(index)).xProperty().doubleValue());
		}
		
		if (isCancelled()) System.out.println("Task cancelled");
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
	}
}
