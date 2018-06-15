import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application
{
	final static String path = "C:\\Users\\Denis Halilovic\\Documents\\Office Documents\\PDF_Files\\apache.pdf";

	public static void basicHighlight(Stage primaryStage) throws IOException
	{
		File file = new File(path);
		PDDocument pdDocument = PDDocument.load(file);
		PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
		PDFTextSearcher pdfTextSearcher = new PDFTextSearcher();
		pdfTextSearcher.setStartPage(0);
		pdfTextSearcher.setEndPage(1);
		pdfTextSearcher.writeText(pdDocument, new OutputStreamWriter(new ByteArrayOutputStream()));

		BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
		WritableImage wim = SwingFXUtils.toFXImage(bim, null);
		ImageView pageImage = new ImageView(wim);
		pageImage.setPreserveRatio(true);

		Rectangle rec = new Rectangle(26.004425, 22.003723 - 5.0907116, 5.833024, 5.0907116);
		rec.setFill(Color.rgb(255, 0, 0, 0.5));

		/*
		HBox pdfHBox = new HBox();
		pdfHBox.setSpacing(8);
		pdfHBox.getChildren().addAll(pageImage, rec);
		
		rec.setLayoutX(100 - pdfHBox.getLayoutBounds().getMinX());
		rec.setLayoutY(100 - pdfHBox.getLayoutBounds().getMinY());
		*/

		Pane pane = new Pane();
		pane.getChildren().addAll(pageImage, rec);

		VBox vBox = new VBox();
		vBox.setPadding(new Insets(10));
		vBox.setSpacing(8);
		vBox.getChildren().add(pane);

		primaryStage.setTitle("Test");
		primaryStage.setScene(new Scene(vBox));
		primaryStage.show();
	}

	public static void wordHighlight(Stage primaryStage) throws IOException
	{
		Scanner scan = new Scanner(System.in);
		File file = new File(path);
		PDDocument pdDocument = PDDocument.load(file);
		PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
		PDFTextSearcher pdfTextSearcher = new PDFTextSearcher();
		pdfTextSearcher.setStartPage(0);
		pdfTextSearcher.setEndPage(1);
		pdfTextSearcher.writeText(pdDocument, new OutputStreamWriter(new ByteArrayOutputStream()));
		//pdfTextSearcher.getText(pdDocument);
		ArrayList<List<TextPosition>> textPositions = pdfTextSearcher.getTextPositions();

		System.out.print("Pattern: ");
		String target = scan.nextLine();
		ArrayList<Index> indices = getIndex(textPositions, target);

		BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
		WritableImage wim = SwingFXUtils.toFXImage(bim, null);
		ImageView pageImage = new ImageView(wim);
		pageImage.setPreserveRatio(true);

		Pane pane = new Pane();
		pane.getChildren().add(pageImage);

		for (Index index : indices)
		{
			TextPosition startPos = textPositions.get(index.article).get(index.position);
			TextPosition endPos = textPositions.get(index.article).get(index.position + target.length() - 1);
			Rectangle highlight = new Rectangle(startPos.getX(), startPos.getY() - startPos.getHeight(), endPos.getEndX() - startPos.getX(), startPos.getHeight());
			highlight.setFill(Color.rgb(255, 0, 0, 0.5));
			pane.getChildren().add(highlight);
		}

		VBox vBox = new VBox();
		vBox.setPadding(new Insets(10));
		vBox.setSpacing(8);
		vBox.getChildren().add(pane);

		primaryStage.setTitle("Test");
		primaryStage.setScene(new Scene(vBox));
		primaryStage.show();
	}

	public static void wordHighlightMultiPage(Stage primaryStage) throws IOException
	{
		Scanner scan = new Scanner(System.in);
		File file = new File(path);
		PDDocument pdDocument = PDDocument.load(file);
		PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
		PDFTextSearcher pdfTextSearcher = new PDFTextSearcher();
		
		pdfTextSearcher.setStartPage(0);
		pdfTextSearcher.setEndPage(1);
		pdfTextSearcher.writeText(pdDocument, new OutputStreamWriter(new ByteArrayOutputStream()));
		
		pdfTextSearcher.writeText(pdDocument, new OutputStreamWriter(new ByteArrayOutputStream()));
		ArrayList<List<TextPosition>> textPositions = pdfTextSearcher.getTextPositions();
		String target = scan.nextLine();
		ArrayList<Index> indices = getIndex(textPositions, target);
	
		BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
		WritableImage wim = SwingFXUtils.toFXImage(bim, null);
		ImageView pageImage = new ImageView(wim);
		pageImage.setPreserveRatio(true);
		
		Pane innerPane = new Pane();
		innerPane.getChildren().add(pageImage);
		
		for (Index index : indices)
		{
			TextPosition startPos = textPositions.get(index.article).get(index.position);
			TextPosition endPos = textPositions.get(index.article).get(index.position + target.length() - 1);
			Rectangle highlight = new Rectangle(startPos.getX(), startPos.getY() - startPos.getHeight(), endPos.getEndX() - startPos.getX(), startPos.getHeight());
			highlight.setFill(Color.rgb(255, 0, 0, 0.5));
			innerPane.getChildren().add(highlight);
		}
		
		ZoomableScrollPane pane = new ZoomableScrollPane(innerPane);
		
		VBox vBox = new VBox();
		vBox.setPadding(new Insets(10));
		vBox.setSpacing(8);
		vBox.getChildren().add(pane);

		primaryStage.setTitle("Test");
		primaryStage.setScene(new Scene(vBox));
		primaryStage.show();
	}

	private static ArrayList<Index> getIndex(ArrayList<List<TextPosition>> textPositions, String target)
	{
		ArrayList<Index> result = new ArrayList<Index>();

		for (int h = 0; h < textPositions.size(); h++)
		{
			List<TextPosition> text = textPositions.get(h);
			for (int i = 0; i < text.size(); i++)
			{
				//System.out.println("i: " + text.get(i));
				//System.out.println("target: " + target.charAt(0));

				if (text.get(i).getUnicode().charAt(0) == (target.charAt(0)))
				{
					//System.out.println("Match");
					boolean match = true;
					int j = 1;

					// i + target.length() OR i + 1 + target.length()?
					for (; i + j < text.size() && j < target.length(); j++)
					{
						//System.out.println("j: " + text.get(i + j));
						//System.out.println("target: " + target.charAt(j));

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

	public static void main(String[] args)
	{
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		//basicHighlight(primaryStage);
		//wordHighlight(primaryStage);
		wordHighlightMultiPage(primaryStage);
	}

}
