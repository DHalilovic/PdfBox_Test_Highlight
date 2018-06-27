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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
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

	private void renderPage(int page, PDFRenderer pdfRenderer, PDFTextSearcher pdfTextSearcher)
	{

	}

	private void renderPages(int center, int radius, PDFRenderer pdfRenderer, PDFTextSearcher pdfTextSearcher)
	{
		renderPage(center, pdfRenderer, pdfTextSearcher); // Render center page

		// Render remaining pages outwards
		for (int i = 1; i < radius; i++)
		{
			if (true)
			{

			}
		}
	}

	private static void createPlaceholders(int numberPages, PDFRenderer pdfRenderer, Pane pageLayer) throws IOException
	{
		double nextPagePos = 0;
		double pageWidth = pdfRenderer.renderImage(0).getWidth();
		double pageHeight = pdfRenderer.renderImage(0).getWidth();
		ObservableList<Node> children = pageLayer.getChildren();

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

		StackPane pdfViewPane = new StackPane();
		Pane pageLayer = new Pane();
		Pane highlightLayer = new Pane();
		pdfViewPane.getChildren().addAll(pageLayer, highlightLayer);
		ZoomableScrollPane pdfScrollPane = new ZoomableScrollPane(pdfViewPane);
		pdfScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);

		createPlaceholders(numberPages, pdfRenderer, pageLayer);
		
		primaryStage.setTitle("Test");
		primaryStage.setScene(new Scene(pdfScrollPane));
		primaryStage.show();
	}

	public static void main(String[] args)
	{
		launch();
	}
}
