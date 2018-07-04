import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PageImageView extends ImageView
{
	private int index;

	public PageImageView(Image image, int index)
	{
		super(image);
		this.index = index;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}
}
