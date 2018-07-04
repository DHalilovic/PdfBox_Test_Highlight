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

public class Main4 extends Application
{
	final static String path = "C:\\Users\\Denis Halilovic\\Documents\\Office Documents\\PDF_Files\\JavaStructures.pdf";
	final static int pagePadding = 16;
	int lastRenderStartPage = 0;
	int lastRenderLowBound = 0;
	int lastRenderHighBound = 0;
	ArrayList<Task> tasks;

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
		tasks = new ArrayList<Task>();
		int numberPages = pdDocument.getNumberOfPages();
		int pageWidth;
		int pageHeight;
		
		{
			PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
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
		
		createPlaceholders(numberPages, pageWidth, pageHeight, placeholderLayer);
		
		pdfScrollPane.hvalueProperty().addListener(new ChangeListener<Number>()
		{
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal)
			{
				int newValInt = (int) (newVal.doubleValue() * numberPages);

				if (Math.abs(newValInt - lastRenderStartPage) > 4)
				{
					for (Task task : tasks)
						task.cancel();
					
					ObservableList<Node> renderedPages = pageLayer.getChildren();
					
					if (lastRenderStartPage < newValInt)
						for (int i = 0; i < renderedPages.size() && i < 2; i++)
							renderedPages.remove(0);
					else
						for (int i = 7; i < renderedPages.size(); i++)
							renderedPages.remove(renderedPages.size() - 1);
					
					lastRenderLowBound = lastRenderStartPage - 4;
					lastRenderHighBound = lastRenderStartPage + 4;
					lastRenderStartPage = newValInt;
					
					
					//TODO Only one thread can access a single PDFBox instance at a time; distribute instances across threads?
					PdfRenderTask4<Void> renderTask;
					try
					{
						renderTask = new PdfRenderTask4<Void>(pdDocument, numberPages, lastRenderStartPage, lastRenderLowBound, lastRenderHighBound, pageLayer, placeholderLayer);
						Thread renderHandler = new Thread(renderTask);
						renderHandler.setDaemon(true);
						tasks.add(renderTask);
						renderHandler.start();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		});

		primaryStage.setTitle("Test");
		primaryStage.setScene(new Scene(pdfScrollPane));
		primaryStage.show();
	}

	public static void main(String[] args)
	{
		launch();
	}
}
