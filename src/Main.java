import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

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
	
	public static void main(String[] args)
	{
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		File file = new File(path);
		PDDocument pdDocument = PDDocument.load(file);
		PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
		PDFTextSearcher pdfTextSearcher = new PDFTextSearcher();
		pdfTextSearcher.setStartPage(0);
		pdfTextSearcher.setEndPage(1);
		pdfTextSearcher.writeText(pdDocument, new OutputStreamWriter(new ByteArrayOutputStream()));
		
		BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 75, ImageType.RGB);
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

}
