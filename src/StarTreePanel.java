import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.DataFormatException;

public class StarTreePanel extends TreePanel {
	private static final long serialVersionUID = -3898710876180470991L;
	private int levelLimit = 5; // Zahl der maximalen Tiefe, die ausgehend vom aktuellen Knoten angezeigt wird
	private float parentDistanceFactor = 1.7f;

	public StarTreePanel() {
		super();
		MindmapNode.setCentered(true);
	}

	public StarTreePanel(TreePanel mindmapPanel) {
		this();
		setParametersFrom(mindmapPanel);
	}

	private void paint(Graphics2D g, MindmapNode node, MindmapNode doNotTraceThis, int level) {
		if (node != null && level < levelLimit) {
			Font oldFont = g.getFont();
			float oldSize = oldFont.getSize();
			if (doNotTraceThis != null) {
				if (doNotTraceThis.parent() == node) {
					g.setFont(oldFont.deriveFont(oldSize * 5 / 6));
				} else
					g.setFont(oldFont.deriveFont(oldSize / 2));
			}
			Point origin = node.getOrigin();
			/*
			 * MindmapNode dummy = node.firstChild(); while (dummy != null) { if (dummy != doNotTraceThis) { Point org = dummy.getOrigin(); if (level < levelLimit - 1) g.drawLine(origin.x, origin.y, org.x, org.y); paint(g, dummy, node, level + 1); } dummy = dummy.next(); } dummy = node.parent(); if (dummy != null && dummy != doNotTraceThis) { Point org = dummy.getOrigin(); if (level < levelLimit - 1) g.drawLine(origin.x, origin.y, org.x, org.y); paint(g, dummy, node, level + 1); }
			 */
			MindmapNode dummy = node.lastChild();
			while (dummy!=null) {
				if (dummy != doNotTraceThis) {
					Point org = dummy.getOrigin();
					g.setStroke(new BasicStroke(g.getFont().getSize()/4));
					if (level < levelLimit - 1) drawConnection(g,origin.x, origin.y, org.x, org.y);
					paint(g, dummy, node, level + 1);
				}
				dummy = dummy.prev();
			}
			dummy = node.parent();
			if (dummy != null && dummy != doNotTraceThis) {
				Point org = dummy.getOrigin();
				g.setStroke(new BasicStroke(g.getFont().getSize()/3));
				if (level < levelLimit - 1) drawConnection(g,origin.x, origin.y, org.x, org.y);
				paint(g, dummy, node, level + 1);
			}
			g.setFont(oldFont);
			g.setColor(connectionColor);
			g.setStroke(new BasicStroke(g.getFont().getSize()/7));
			if (!updatedSinceLastChange) node.resetDimension();
			if (level < 2)
				node.paint(g, this);
			else
				node.paintWithoutImages(g, this);
		}
	}

	private void drawConnection(Graphics g, int x, int y, int x2, int y2) {
		g.setColor(connectionColor);
		g.drawLine(x, y, x2, y2);
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.setFont(g.getFont().deriveFont(fontSize));
		paint((Graphics2D)g, mindmap, null, 0);
		updatedSinceLastChange = true;
	}

	public static double sin(double pDegree) {
		return Math.sin(Math.PI * pDegree / 180);
	}

	public static double cos(double pDegree) {
		return Math.cos(Math.PI * pDegree / 180);
	}

	private Point getTargetPos(Point origin, double angle, int dist) {
		int x = (int) (origin.x + 2 * dist * -cos(angle));
		int y = (int) (origin.y + dist * -sin(angle));
		return new Point(x, y);
	}

	private void organize(MindmapNode node, MindmapNode comingFrom, Point origin, int distance, double angle, int level) {
		if (level < fileLoadLevelLimit) {
			if (node.nodeFile() != null) {
				try {
					node.loadFromFile();
				} catch (FileNotFoundException e) {
					System.out.println("Datei nicht gefunden: " + e.getMessage());
				} catch (IOException e) {
					System.out.println("Fehler beim Laden von: " + e.getMessage());
				} catch (DataFormatException e) {
					System.out.println("Dateityp wird nicht unterstützt: " + e.getMessage());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		if (level <= levelLimit) {
			boolean hasParent = node.parent() != null;
			int numLinks = node.getNumChildren() + ((hasParent) ? 1 : 0);
			double angleDiff = (numLinks == 0) ? 0 : 360 / numLinks;

			if (comingFrom == null || comingFrom == node.parent()) {
				if (hasParent && (node.parent() != comingFrom)) {
					Point targetPos = getTargetPos(origin, angle, (int) (distance * parentDistanceFactor));
					organize(node.parent(), node, targetPos, distance / 3, angle + 180, level + 1);
				}
				angle += angleDiff;

				MindmapNode child = node.firstChild();
				while (child != null) {
					if (child != comingFrom) {
						Point targetPos = getTargetPos(origin, angle, distance);
						organize(child, node, targetPos, distance / 3, angle + 180, level + 1);
						angle += angleDiff;
					}
					child = child.next();
				}
			} else { // comingFrom ist eines der Kinder
				MindmapNode child = comingFrom.next();
				angle += angleDiff;
				while (child != null) {
					if (child != comingFrom) {
						Point targetPos = getTargetPos(origin, angle, distance);
						organize(child, node, targetPos, distance / 3, angle + 180, level + 1);
						angle += angleDiff;
					}
					child = child.next();
				}
				if (hasParent) {
					Point targetPos = getTargetPos(origin, angle, (int) (distance * parentDistanceFactor));
					organize(node.parent(), node, targetPos, distance / 3, angle + 180, level + 1);
					angle += angleDiff;
				}
				child = comingFrom;
				while (child.prev() != null)
					child = child.prev();
				while (child != null && child != comingFrom) {
					if (child != comingFrom) {
						Point targetPos = getTargetPos(origin, angle, distance);
						organize(child, node, targetPos, distance / 3, angle + 180, level + 1);
						angle += angleDiff;
					}
					child = child.next();
				}

			}
			moveNodeTowards(node, origin);
		}
	}

	public boolean organize() {
		float angle = 50f;
		if (mindmap.parent() == null && mindmap.getNumChildren() == 1) angle += 180f;
		organize(mindmap, null, new Point(this.getWidth() / 2, this.getHeight() / 2), distance, angle, 0);
		return true;
	}

	public void repaint() {
		if (mindmap != null) organize();
		super.repaint();
	}

}
