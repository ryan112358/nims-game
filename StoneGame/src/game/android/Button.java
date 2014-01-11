package game.android;

/**
 * Stone Game:
 * Ryan McKenna & Jacob Aimino
 * 
 * Button: Template for a button.
 * Attributes: 
 *  - x and y position (center of button)
 *	- text to be put on button
 *	- width and height (scaled in tableview)
 */

public class Button {
	private float x;
	private float y;
	private String text;
	private float width;
	private float height;
	
	
	/**
	 * Returns true if touch coordinates are inside button bounds
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onButton(float x, float y) {
		return x >= this.x && x <= this.x + this.width &&
				y >= this.y && y <= this.y + this.width;
	}
	
	public Button(float x, float y, String text, float width, float height) {
		this.x = x;
		this.y = y;
		this.text = text;
		this.width = width;
		this.height = height;
	}
	
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public float getWidth() {
		return width;
	}
	public void setWidth(float width) {
		this.width = width;
	}
	public float getHeight() {
		return height;
	}
	public void setHeight(float height) {
		this.height = height;
	}
	
	

}
