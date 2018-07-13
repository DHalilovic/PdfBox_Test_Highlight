import com.sun.javafx.scene.control.skin.ScrollPaneSkin;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class NewZoomableScrollPane extends ScrollPane
{
	private double scaleValue = 1;
	private double minScaleValue;
	private double maxScaleValue;
	private double zoomIntensity = 0.02;
	private Node target;
	private Node zoomNode;

	public NewZoomableScrollPane(Node target, double minScaleValue, double maxScaleValue)
	{
		super();
		this.target = target;
		this.minScaleValue = minScaleValue;
		this.maxScaleValue = maxScaleValue;
		zoomNode = new Group(target);
		setContent(outerNode(zoomNode));

		setPannable(true);
		setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		setFitToHeight(true); //center
		setFitToWidth(true); //center

		updateScale();

		this.setSkin(new ScrollPaneSkin(this)
		{
			@Override
			public void onTraverse(Node n, Bounds b)
			{
			}
		});
	}

	private Node outerNode(Node node)
	{
		Node outerNode = centeredNode(node);

		outerNode.setOnScroll(e ->
		{
			if (e.isControlDown())
			{
				e.consume();
				onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
			}
		});

		return outerNode;
	}

	private Node centeredNode(Node node)
	{
		// TODO Switch to Pane
		VBox vBox = new VBox(node);
		vBox.setAlignment(Pos.CENTER);
		return vBox;
	}

	private void updateScale()
	{
		target.setScaleX(scaleValue);
		target.setScaleY(scaleValue);
	}

	public double getScaleValue()
	{
		return scaleValue;
	}

	public double getMinScaleValue()
	{
		return minScaleValue;
	}

	public double getMaxScaleValue()
	{
		return maxScaleValue;
	}

	private void onScroll(double wheelDelta, Point2D mousePoint)
	{
		double zoomFactor = Math.exp(wheelDelta * zoomIntensity);
		double nextScaleValue = scaleValue * zoomFactor;

		//System.out.println(nextScaleValue);

		if (nextScaleValue < minScaleValue || nextScaleValue > maxScaleValue)
			return;

		Bounds innerBounds = zoomNode.getLayoutBounds();
		Bounds viewportBounds = getViewportBounds();

		// calculate pixel offsets from [0, 1] range
		double valX = getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
		double valY = getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

		scaleValue = nextScaleValue;

		// convert target coordinates to zoomTarget coordinates
		Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

		// calculate adjustment of scroll position (pixels)
		Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

		updateScale();
		layout(); // refresh ScrollPane scroll positions & target bounds

		// convert back to [0, 1] range
		// (too large/small values are automatically corrected by ScrollPane)
		Bounds updatedInnerBounds = zoomNode.getLayoutBounds();
		this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
		this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
	}
}