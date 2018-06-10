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
		ArrayList<List<TextPosition>> textPositions = pdfTextSearcher.getTextPositions();

		String target = scan.nextLine();
		ArrayList<Integer> indices = getIndex(textPositions, target);

		BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
		WritableImage wim = SwingFXUtils.toFXImage(bim, null);
		ImageView pageImage = new ImageView(wim);
		pageImage.setPreserveRatio(true);

		Pane pane = new Pane();
		pane.getChildren().addAll(pageImage);

		VBox vBox = new VBox();
		vBox.setPadding(new Insets(10));
		vBox.setSpacing(8);
		vBox.getChildren().add(pane);

		primaryStage.setTitle("Test");
		primaryStage.setScene(new Scene(vBox));
		primaryStage.show();
	}

	public static ArrayList<Integer> getIndex(ArrayList<List<TextPosition>> textPositions, String target)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		getIndexRec(textPositions, target, result);
		return result;
	}

	private static void getIndexRec(ArrayList<List<TextPosition>> textPositions, String target, ArrayList<Integer> result)
	{
		for (int h = 0; h < textPositions.size(); h++)
		{
			List<TextPosition> text = textPositions.get(h);
			for (int i = 0; i < text.size(); i++)
			{
				if (text.get(i).getUnicode().equals(target.charAt(0)))
				{
					boolean match = true;
					int j = 1;

					// i + target.length() OR i + 1 + target.length()?
					for (; i + j < text.size() && j < target.length(); j++)
					{
						if (!text.get(i + j).getUnicode().equals(target.charAt(j)))
						{
							match = false;
							break;
						}
					}

					if (match)
					{
						result.add(i);
					}

					i = i + j + 1;
				}
			}
		}
	}

	public static void main(String[] args)
	{
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		//basicHighlight(primaryStage);
		wordHighlight(primaryStage);
	}

}
