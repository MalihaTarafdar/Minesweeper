import java.awt.Color;

public class Theme {
	private String iconSet;
	private Color primaryColor, highlightColor, shadowColor;
	private Color menuForeground, menuBackground, menuSelectionForeground, menuSelectionBackground;

	public Theme(String iconSet, Color primaryColor, Color highlightColor, Color shadowColor,
			Color menuForeground, Color menuBackground, Color menuSelectionForeground,
			Color menuSelectionBackground) {
		this.iconSet = iconSet;
		this.primaryColor = primaryColor;
		this.highlightColor = highlightColor;
		this.shadowColor = shadowColor;
		this.menuForeground = menuForeground;
		this.menuBackground = menuBackground;
		this.menuSelectionForeground = menuSelectionForeground;
		this.menuSelectionBackground = menuSelectionBackground;
	}

	public String getIconSet() {
		return iconSet;
	}
	public void setIconSet(String iconSet) {
		this.iconSet = iconSet;
	}

	public Color getPrimaryColor() {
		return primaryColor;
	}
	public void setPrimaryColor(Color primaryColor) {
		this.primaryColor = primaryColor;
	}

	public Color getHighlightColor() {
		return highlightColor;
	}
	public void setHighlightColor(Color highlightColor) {
		this.highlightColor = highlightColor;
	}

	public Color getShadowColor() {
		return shadowColor;
	}
	public void setShadowColor(Color shadowColor) {
		this.shadowColor = shadowColor;
	}

	public Color getMenuForeground() {
		return menuForeground;
	}
	public void setMenuForeground(Color menuForeground) {
		this.menuForeground = menuForeground;
	}

	public Color getMenuBackground() {
		return menuBackground;
	}
	public void setMenuBackground(Color menuBackground) {
		this.menuBackground = menuBackground;
	}

	public Color getMenuSelectionForeground() {
		return menuSelectionForeground;
	}
	public void setMenuSelectionForeground(Color menuSelectionForeground) {
		this.menuSelectionForeground = menuSelectionForeground;
	}

	public Color getMenuSelectionBackground() {
		return menuSelectionBackground;
	}
	public void setMenuSelectionBackground(Color menuSelectionBackground) {
		this.menuSelectionBackground = menuSelectionBackground;
	}
}