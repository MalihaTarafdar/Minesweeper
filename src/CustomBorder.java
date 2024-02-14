import java.awt.*;
import javax.swing.border.AbstractBorder;

public abstract class CustomBorder extends AbstractBorder {
	private static final long serialVersionUID = 2366119804324391625L;

	private static final int WIDTH = 10;
	private static final int HSWIDTH = WIDTH / 3;
	private static final int TOTAL_WIDTH = WIDTH + 2 * HSWIDTH;

	private static Color primaryColor, highlightColor, shadowColor;

	private static boolean dither;
	private static Graphics2D g2;

	public CustomBorder(Theme theme) {
		super();
		CustomBorder.primaryColor = theme.getPrimaryColor();
		CustomBorder.highlightColor = theme.getHighlightColor();
		CustomBorder.shadowColor = theme.getShadowColor();
		dither = primaryColor == null;
	}

	private static void paintDitheredBorder(Color color1, Color color2, int cWidth, int cHeight,
			Rectangle bounds) {
		for (int x = 0; x < cWidth; x += HSWIDTH) {
			for (int y = 0; y < cHeight; y += HSWIDTH) {
				if (bounds.contains(new Point(x, y))) continue;
				if (x % 2 == y % 2) g2.setColor(color1);
				else g2.setColor(color2);
				g2.fillRect(x, y, HSWIDTH, HSWIDTH);
			}
		}
	}

	private static void paintRect(Color color, int x, int y, int width, int height) {
		g2.setColor(color);
		g2.fillRect(x, y, width, height);
	}
	private static void paintTrap(Color color, int[] xPoints, int[] yPoints) {
		g2.setColor(color);
		g2.fillPolygon(xPoints, yPoints, 4);
	}
	private static void paintLine(Color color, int x1, int y1, int x2, int y2) {
		g2.setColor(color);
		g2.drawLine(x1, y1, x2, y2);
	}

	public static int getHSWidth() {
		return HSWIDTH;
	}

	public static int getWidth() {
		return TOTAL_WIDTH;
	}

	public static class ScoreboardBorder extends CustomBorder {
		private static final long serialVersionUID = -1332761673315174647L;

		private int padding;

		public ScoreboardBorder(int padding, Theme theme) {
			super(theme);
			this.padding = padding;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int cWidth, int cHeight) {
			cWidth += 2 * (TOTAL_WIDTH + padding);
			cHeight += 2 * (TOTAL_WIDTH + padding) - HSWIDTH;

			super.paintBorder(c, g, x, y, cWidth, cHeight);
			g2 = (Graphics2D)g;

			if (!dither) {
				paintRect(primaryColor, 0, 0, cWidth, cHeight);
			} else {
				paintDitheredBorder(shadowColor, highlightColor, cWidth, cHeight,
						new Rectangle(WIDTH + HSWIDTH, WIDTH + HSWIDTH, cWidth - 2 * TOTAL_WIDTH,
						cHeight - 2 * TOTAL_WIDTH));
				paintRect(highlightColor, WIDTH + HSWIDTH, WIDTH + HSWIDTH,
						cWidth - 2 * (WIDTH + HSWIDTH), cHeight - 2 * WIDTH - HSWIDTH);
			}

			paintTrap(highlightColor, new int[]{0, cWidth, cWidth - HSWIDTH, 0},
					new int[]{0, 0, HSWIDTH, HSWIDTH}); // TOP HIGHTLIGHT
			paintRect(shadowColor, WIDTH + HSWIDTH, WIDTH + HSWIDTH,
					cWidth - 2 * (WIDTH + HSWIDTH), HSWIDTH); // TOP SHADOW
			paintTrap(shadowColor, new int[]{cWidth - HSWIDTH, cWidth, cWidth, cWidth - HSWIDTH},
					new int[]{HSWIDTH, 0, cHeight, cHeight}); // RIGHT SHADOW
			paintRect(highlightColor, cWidth - TOTAL_WIDTH, WIDTH + HSWIDTH, HSWIDTH,
					cHeight - 2 * (WIDTH + HSWIDTH)); // RIGHT HIGHLIGHT
			paintRect(highlightColor, TOTAL_WIDTH, cHeight - TOTAL_WIDTH,
					cWidth - 2 * (TOTAL_WIDTH), HSWIDTH); // BOTTOM HIGHLIGHT
			paintTrap(highlightColor, new int[]{0, HSWIDTH, HSWIDTH, 0},
					new int[]{0, 0, cHeight - HSWIDTH, cHeight}); // LEFT HIGHLIGHT
			paintRect(shadowColor, WIDTH + HSWIDTH, WIDTH + HSWIDTH, HSWIDTH,
					cHeight - 2 * (WIDTH + HSWIDTH)); // LEFT SHADOW

			if (dither) {
				paintLine(shadowColor, cWidth - TOTAL_WIDTH, WIDTH + HSWIDTH,
						cWidth - TOTAL_WIDTH, cHeight - WIDTH - HSWIDTH);
				paintLine(shadowColor, TOTAL_WIDTH, cHeight - (WIDTH + HSWIDTH),
						cWidth - TOTAL_WIDTH, cHeight - (WIDTH + HSWIDTH));
			}
		}
	}

	public static class GameBorder extends CustomBorder {
		private static final long serialVersionUID = -122874985631083954L;

		public GameBorder(Theme theme) {
			super(theme);
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int cWidth, int cHeight) {
			cWidth += 2 * TOTAL_WIDTH;
			cHeight += 2 * TOTAL_WIDTH - TOTAL_WIDTH / 4;

			super.paintBorder(c, g, x, y, cWidth, cHeight);
			g2 = (Graphics2D)g;

			if (!dither) {
				paintRect(primaryColor, 0, 0, cWidth, cHeight);
			} else {
				paintDitheredBorder(highlightColor, shadowColor, cWidth, cHeight,
						new Rectangle(WIDTH + HSWIDTH, WIDTH + HSWIDTH, cWidth - 2 * TOTAL_WIDTH,
						cHeight - 2 * TOTAL_WIDTH));
			}

			paintRect(shadowColor, WIDTH + HSWIDTH, WIDTH + HSWIDTH - TOTAL_WIDTH / 4,
					cWidth - 2 * (WIDTH + HSWIDTH), HSWIDTH); // TOP SHADOW
			paintRect(shadowColor, cWidth - HSWIDTH, 0, HSWIDTH, cHeight); // RIGHT SHADOW
			paintRect(highlightColor, cWidth - TOTAL_WIDTH, WIDTH + HSWIDTH - TOTAL_WIDTH / 3,
					HSWIDTH, cHeight - 2 * (WIDTH + HSWIDTH)); // RIGHT HIGHLIGHT
			paintTrap(shadowColor, new int[]{HSWIDTH, cWidth, cWidth, 0},
					new int[]{cHeight - 2 * HSWIDTH, cHeight - 2 * HSWIDTH, cHeight,
					cHeight}); // BOTTOM SHADOW
			paintRect(highlightColor, TOTAL_WIDTH, cHeight - TOTAL_WIDTH * 4 / 3,
					cWidth - 2 * TOTAL_WIDTH, HSWIDTH); // BOTTOM HIGHLIGHT
			paintTrap(highlightColor, new int[]{0, HSWIDTH, HSWIDTH, 0},
					new int[]{0, 0, cHeight - HSWIDTH, cHeight}); // LEFT HIGHLIGHT
			paintRect(shadowColor, WIDTH + HSWIDTH, WIDTH, HSWIDTH,
					cHeight - 2 * (WIDTH + HSWIDTH) - TOTAL_WIDTH / 6); // LEFT SHADOW

			if (dither) {
				paintLine(shadowColor, cWidth - TOTAL_WIDTH, WIDTH + HSWIDTH - TOTAL_WIDTH / 3,
						cWidth - TOTAL_WIDTH, cHeight - TOTAL_WIDTH - 2 * HSWIDTH);
				paintLine(shadowColor, TOTAL_WIDTH, cHeight - TOTAL_WIDTH * 4 / 3,
						cWidth - TOTAL_WIDTH, cHeight - TOTAL_WIDTH * 4 / 3);
			}
		}
	}
}