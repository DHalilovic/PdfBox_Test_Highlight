import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

public class Main4 extends Application
{
	final static String path = "C:\\Users\\Denis Halilovic\\Documents\\Office Documents\\PDF_Files\\JavaStructures.pdf";
	final static int pagePadding = 16;
	final static int dpi = 144;
	final double mapScale = 0.01;
	boolean isMapView = false;
	int lastIndex = 0;
	double lastScaleValue = 1.0;
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
		NewZoomableScrollPane pdfScrollPane = new NewZoomableScrollPane(pdfViewPane, 0.8, 2.4);
		//pdfScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);

		createPlaceholders(numberPages, pageWidth, pageHeight, placeholderLayer);

		PdfRenderTask4<Void> renderTask;
		HashSet<Integer> requestedPages = new HashSet<Integer>();

		for (int i = 0; i < 11; i++)
		{
			requestedPages.add(i);
		}

		try
		{
			renderTask = new PdfRenderTask4<Void>(pdDocument, numberPages, pageLayer, highlightLayer, placeholderLayer, requestedPages, pageWidth, dpi);
			Thread renderHandler = new Thread(renderTask);
			renderHandler.setDaemon(true);
			tasks.add(renderTask);
			renderHandler.start();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		HBox docHBox = new HBox(8);
		docHBox.setPadding(new Insets(4));
		docHBox.setAlignment(Pos.CENTER);
		docHBox.getChildren().addAll(new Text(path));

		TextField pageTextField = new TextField("1");
		pageTextField.prefColumnCountProperty().bind(pageTextField.textProperty().length());
		pageTextField.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			// If in focus
			if (newValue)
			{
				pageTextField.setUserData(Integer.parseInt(pageTextField.getText()));
				return;
			}

			if (isMapView)
				return;

			int newPageValue = 0;
			boolean canScroll = true;

			try
			{
				newPageValue = Integer.parseInt(pageTextField.getText());
			} catch (NumberFormatException e)
			{
				canScroll = false;
			}

			if (!canScroll)
				return;

			int lastPageValue = (int) pageTextField.getUserData();

			if (lastPageValue != newPageValue && newPageValue > 0 && newPageValue <= numberPages)
			{
				pdfScrollPane.setHvalue(newPageValue / (double) numberPages);
			}
		});

		HBox pageHBox = new HBox(8);
		pageHBox.setPadding(new Insets(4));
		pageHBox.setAlignment(Pos.CENTER);
		pageHBox.getChildren().addAll(pageTextField, new Text("/"), new Text("" + numberPages));

		TextField searchTextField = new TextField();

		HBox searchHBox = new HBox(8);
		searchHBox.setPadding(new Insets(4));
		searchHBox.setAlignment(Pos.CENTER);
		searchHBox.getChildren().addAll(new Text("Search"), searchTextField);

		BorderPane pdfHeaderPane = new BorderPane();
		pdfHeaderPane.setLeft(docHBox);
		pdfHeaderPane.setCenter(pageHBox);
		pdfHeaderPane.setRight(searchHBox);

		Button zoomButton = new Button("Zoom");
		zoomButton.setPadding(new Insets(4));
		zoomButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				{
					if (isMapView)
					{
						zoomButton.setText("Zoom Out");
						placeholderLayer.toBack();
						int newIndex = lastIndex;

						try
						{
							newIndex = Integer.parseInt(pageTextField.getText()) + 1;
						} catch (NumberFormatException ex)
						{
						}

						pdfScrollPane.forceZoom(pdfScrollPane.getMinScaleValue());
						pdfScrollPane.setHvalue((double) newIndex / numberPages);
					} else
					{
						zoomButton.setText("Zoom In");
						pageLayer.getChildren().clear();
						highlightLayer.getChildren().clear();
						pdfScrollPane.forceZoom(mapScale);
						placeholderLayer.toFront();
					}

					isMapView = !isMapView;
				}
			}
		});

		pdfScrollPane.hvalueProperty().addListener(new ChangeListener<Number>()
		{
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal)
			{

				// TODO Separate isMapView to separate condition which handles Map page highlighting
				if (/*isMapView && */pdfScrollPane.getScaleValue() != lastScaleValue)
				{
					lastScaleValue = pdfScrollPane.getScaleValue();
					return;
				}

				if (pdfScrollPane.getScaleValue() >= pdfScrollPane.getMinScaleValue())
				{
					if (!tasks.isEmpty() && tasks.get(0).isRunning())
						return;

					int newIndex = (int) (newVal.doubleValue() * numberPages);

					pageTextField.setText("" + (newIndex + 1));

					if (!isMapView && Math.abs(newIndex - lastIndex) < 3)
						return;

					lastIndex = newIndex;

					for (Task task : tasks)
						task.cancel();

					HashSet<Integer> requestedPages = new HashSet<Integer>();

					for (int i = (newIndex - 6 >= 0) ? newIndex - 6 : 0; i <= newIndex + 5 && i < numberPages; i++)
					{
						requestedPages.add(i);
					}

					// TODO Group highlights in group layers, make highlight layer stackpane, delete like pages
					//highlightLayer.getChildren().clear();
					
					ObservableList<Node> renderedPages = pageLayer.getChildren();					
					Iterator<Node> iterator = renderedPages.listIterator();

					while (iterator.hasNext())
					{
						PageImageView child = (PageImageView) iterator.next();
						int pageIndex = child.getIndex();

						if (requestedPages.contains(pageIndex))
							requestedPages.remove(pageIndex);
						else
							iterator.remove();
					}

					PdfRenderTask4<Void> renderTask;
					try
					{
						renderTask = new PdfRenderTask4<Void>(pdDocument, numberPages, pageLayer, highlightLayer, placeholderLayer, requestedPages, pageWidth, dpi);
						Thread renderHandler = new Thread(renderTask);
						renderHandler.setDaemon(true);
						tasks.add(renderTask);
						renderHandler.start();
					} catch (IOException e)
					{
						e.printStackTrace();
					}

				} else
				{
					// TODO General Page Coloring Here
				}
			}
		});

		BorderPane pdfFooterPane = new BorderPane();
		pdfFooterPane.setCenter(zoomButton);

		VBox pdfVBox = new VBox(8);
		pdfVBox.setPadding(new Insets(4));

		pdfVBox.getChildren().addAll(pdfHeaderPane, pdfScrollPane, pdfFooterPane);
		pdfVBox.setVgrow(pdfScrollPane, Priority.ALWAYS);

		primaryStage.setTitle("Test");
		primaryStage.setScene(new Scene(pdfVBox));
		primaryStage.show();

		/*
		for (Node node : pdfScrollPane.getChildrenUnmodifiable())
		{
			if (node instanceof ScrollBar)
			{
				ScrollBar scrollBar = (ScrollBar) node;
		
				if (scrollBar.getOrientation() == Orientation.HORIZONTAL)
				{
					System.out.println("SOMETHING");
					scrollBar.setBlockIncrement(0);
		
				}
			}
		}
		*/
	}

	public static void main(String[] args)
	{
		launch();
	}
}
