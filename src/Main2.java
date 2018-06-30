import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import com.sun.javafx.scene.control.skin.ScrollPaneSkin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;

public class Main2 extends Application
{
	final static String path = "C:\\Users\\Denis Halilovic\\Documents\\Office Documents\\PDF_Files\\JavaStructures.pdf";
	final static int pagePadding = 16;
	double lastRenderStartPage = 0;

	private static ArrayList<Index> getIndex(ArrayList<List<TextPosition>> textPositions, String target)
	{
		ArrayList<Index> result = new ArrayList<Index>();

		for (int h = 0; h < textPositions.size(); h++)
		{
			List<TextPosition> text = textPositions.get(h);
			for (int i = 0; i < text.size(); i++)
			{
				if (text.get(i).getUnicode().charAt(0) == (target.charAt(0)))
				{
					boolean match = true;
					int j = 1;

					for (; i + j < text.size() && j < target.length(); j++)
					{
						if (text.get(i + j).getUnicode().charAt(0) != target.charAt(j))
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

	private void renderPage(int page, double xPosition, PDFRenderer pdfRenderer, PDFTextSearcher pdfTextSearcher, Pane pageLayer) throws IOException
	{
		BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 72, ImageType.RGB);
		WritableImage wim = SwingFXUtils.toFXImage(bim, null);
		ImageView pageImage = new ImageView(wim);
		pageImage.setPreserveRatio(true);
		pageImage.relocate(xPosition, 0);
		pageLayer.getChildren().add(pageImage);
	}

	/*
	private void renderPages(int center, int radius, int numberPages, PDFRenderer pdfRenderer, PDFTextSearcher pdfTextSearcher, Pane pageLayer, Pane placeholderLayer) throws IOException
	{
		//System.out.println(center);
		if (center < 0 || center >= numberPages)
			return;
	
		pageLayer.getChildren().clear();
	
		ObservableList<Node> xPositions = placeholderLayer.getChildren();
		renderPage(center, ((Rectangle) xPositions.get(center)).xProperty().doubleValue(), pdfRenderer, pdfTextSearcher, pageLayer);
		int currentPage;
	
		// Render remaining pages outwards
		for (int i = 1; i < radius; i++)
		{
			currentPage = center + i;
			if (currentPage < numberPages)
			{
				//System.out.println(((Rectangle) xPositions.get(currentPage)).xProperty().doubleValue());
				renderPage(currentPage, ((Rectangle) xPositions.get(currentPage)).xProperty().doubleValue(), pdfRenderer, pdfTextSearcher, pageLayer);
			} else
				break;
	
		}
		for (int i = -1; i > 0 - radius; i--)
		{
			currentPage = center + i;
			if (currentPage >= 0)
			{
				//System.out.println(((Rectangle) xPositions.get(currentPage)).xProperty().doubleValue());
				renderPage(currentPage, ((Rectangle) xPositions.get(currentPage)).xProperty().doubleValue(), pdfRenderer, pdfTextSearcher, pageLayer);
			} else
				break;
		}
	}
	*/

	synchronized private void renderPages(int center, int radius, int numberPages, PDFRenderer pdfRenderer, PDFTextSearcher pdfTextSearcher, Pane pageLayer, Pane placeholderLayer) throws IOException
	{
		if (center < 0 || center >= numberPages)
			return;

		pageLayer.getChildren().clear();
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

	private static void createPlaceholders(int numberPages, double pageWidth, double pageHeight, Pane placeholderLayer) throws IOException
	{
		double nextPagePos = 0;
		ObservableList<Node> children = placeholderLayer.getChildren();

		// Render remaining pages outwards
		for (int i = 0; i < numberPages; i++)
		{
			Rectangle placeHolder = new Rectangle(nextPagePos, 0, pageWidth, pageHeight);
			placeHolder.setFill(Color.WHITE);
			nextPagePos += pageWidth + pagePadding;
			children.add(i, placeHolder);
		}
	}

	public void start(Stage primaryStage) throws Exception
	{
		PDDocument pdDocument = PDDocument.load(new File(path));
		PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
		PDFTextSearcher pdfTextSearcher = new PDFTextSearcher();
		int numberPages = pdDocument.getNumberOfPages();

		int pageWidth;
		int pageHeight;
		{
			BufferedImage tempImage = pdfRenderer.renderImage(0);
			pageWidth = tempImage.getWidth();
			pageHeight = tempImage.getHeight();
		}

		StackPane pdfViewPane = new StackPane();
		Pane placeholderLayer = new Pane();
		Pane pageLayer = new Pane();
		Pane highlightLayer = new Pane();		
		
		pdfViewPane.getChildren().addAll(placeholderLayer, pageLayer, highlightLayer);
		NewZoomableScrollPane pdfScrollPane = new NewZoomableScrollPane(pdfViewPane);
		pdfScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		
		// Breaks mouse-based zooming, due to changing Hscale from [0, 1] to [0, numberPages]
		//pdfScrollPane.setHmax(numberPages);
		
		pdfScrollPane.hvalueProperty().addListener(new ChangeListener<Number>()
		{
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal)
			{
				double newValInt = newVal.doubleValue();

				if (Math.abs((newValInt - lastRenderStartPage) * numberPages) > 4)
				{
					lastRenderStartPage = newValInt;

					//TODO Only one thread can access a single PDFBox instance at a time; distribute instances across threads?
					Task<Void> renderTask = new Task<Void>()
					{
						@Override
						protected Void call()
						{
							Platform.runLater(() -> 
							{
								try
								{
									renderPages((int) (lastRenderStartPage * numberPages), 10, numberPages, pdfRenderer, pdfTextSearcher, pageLayer, placeholderLayer);
								} catch (IOException e)
								{
									e.printStackTrace();
								}
							});

							return null;
						}
					};

					Thread renderHandler = new Thread(renderTask);
					renderHandler.setDaemon(true);
					renderHandler.start();

					//System.out.println(newValInt);
				}
			}
		});
		/*
		pdfScrollPane.getContent().setOnScroll(e ->
		{
			if (e.isControlDown())
			{
				e.consume();
				pdfScrollPane.onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
			} else
			{
				System.out.println(pdfScrollPane.getHvalue());
			}
		});
		*/

		createPlaceholders(numberPages, pageWidth, pageHeight, placeholderLayer);

		
		
		primaryStage.setTitle("Test");
		primaryStage.setScene(new Scene(pdfScrollPane));
		primaryStage.show();
	}

	public static void main(String[] args)
	{
		launch();
	}
}
