import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

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

public class PdfRenderTask<E> extends Task<E>
{
	PDFRenderer pdfRenderer;
	PDFTextSearcher pdfTextSearcher;
	Pane placeholderLayer;
	Pane pageLayer;
	int numberPages;
	double lastRenderStartPage;
	ArrayList<ImageView> pageImages;

	public PdfRenderTask(PDDocument pdDocument, int numberPages, double lastRenderStartPage, Pane pageLayer, Pane placeholderLayer) throws IOException
	{
		this.pdfRenderer = new PDFRenderer(pdDocument);
		this.pdfTextSearcher = new PDFTextSearcher();
		this.numberPages = numberPages;
		this.lastRenderStartPage = lastRenderStartPage;
		this.pageLayer = pageLayer;
		this.placeholderLayer = placeholderLayer;
		pageImages = new ArrayList<ImageView>();
	}

	private void renderPage(int page, double xPosition, PDFRenderer pdfRenderer, PDFTextSearcher pdfTextSearcher, Pane pageLayer) throws IOException
	{
		BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 72, ImageType.RGB);
		WritableImage wim = SwingFXUtils.toFXImage(bim, null);
		ImageView pageImage = new ImageView(wim);
		pageImage.setPreserveRatio(true);
		pageImage.relocate(xPosition, 0);
		pageImages.add(pageImage);
	}

	private void renderPages(int center, int radius, int numberPages, PDFRenderer pdfRenderer, PDFTextSearcher pdfTextSearcher, Pane pageLayer, Pane placeholderLayer) throws IOException
	{
		if (center < 0 || center >= numberPages)
			return;

		ObservableList<Node> xPositions = placeholderLayer.getChildren();
		int currentPage;

		// Render remaining pages outwards
		for (int i = -radius; i <= radius; i++)
		{
			currentPage = center + i;
			if (currentPage < 0 || currentPage >= numberPages)
				continue;

			renderPage(currentPage, ((Rectangle) xPositions.get(currentPage)).xProperty().doubleValue(), pdfRenderer, pdfTextSearcher, pageLayer);
		}
	}

	@Override
	protected E call() throws Exception
	{
		try
		{
			renderPages((int) (lastRenderStartPage * numberPages), 10, numberPages, pdfRenderer, pdfTextSearcher, pageLayer, placeholderLayer);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void succeeded()
	{
		System.out.println("poop running");
		for (ImageView pageImage : pageImages)
			pageLayer.getChildren().add(pageImage);
	}
}
