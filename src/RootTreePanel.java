import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.DataFormatException;

import tools.srsoftware.ObjectComparator;

public class RootTreePanel extends TreePanel {

	private static final long serialVersionUID = 1L;
	private int dist = 10;
	protected int distance = 50; // Basis-Distanz zwischen den Knoten des Mindmap
	
	public RootTreePanel() {
		super();
		MindmapNode.setCentered(false);
	}
	
	public RootTreePanel(TreePanel mindmapPanel) {
		this();
		setParametersFrom(mindmapPanel);
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (mindmap != null) {
			g.setFont(g.getFont().deriveFont(fontSize));
			boolean wasFolded=mindmap.isFolded();
			mindmap.setFolded(false);
			if (mindmap.nodeFile() != null) {
				try {
					mindmap.loadFromFile();
				} catch (FileNotFoundException e) {
					System.out.println("Datei nicht gefunden: " + e.getMessage());
				} catch (IOException e) {
					System.out.println("Fehler beim Laden von: " + e.getMessage());
				} catch (DataFormatException e) {
					System.out.println("Dateityp wird nicht unterstützt: " + e.getMessage());
				}
			}
			MindmapNode child;
			Dimension d=mindmap.nodeDimension(g, this);
			Point center=new Point((getWidth()-d.width)/3,getHeight()/2);
			moveNodeTowards(mindmap, center);
			if (wasFolded){
				child=mindmap.firstChild();
				while (child!=null) {
					child.setOrigin(mindmap.getOrigin());
					child=child.next();
				}
			}
			if (!updatedSinceLastChange) mindmap.resetDimension();
			Point leftCenter=mindmap.getOrigin();
			leftCenter.y+=d.height/2;
			d.height=paint((Graphics2D)g,mindmap,true).height;
			paintFamily((Graphics2D) g,mindmap,leftCenter,d);
		}
	}

	private void paintFamily(Graphics2D g, MindmapNode mindmap,Point leftCenter, Dimension mindmapDimension) {
		MindmapNode parent=mindmap.parent();
		TreeSet<Point> points=new TreeSet<Point>(ObjectComparator.get());
		if (parent!=null){
			int x=leftCenter.x;
			int y=leftCenter.y;
			mindmap.resetDimension();
			Dimension mindmapDim = mindmap.nodeDimension(g, this);
			Point p=mindmap.getOrigin();
			p.y+=mindmapDim.height/2;
			points.add(p);
			
			MindmapNode sibling=mindmap;
			int height=mindmapDimension.height;
			y+=(mindmapDimension.height)/2+dist;
			while ((sibling=sibling.next())!=null){
				Dimension siblingDim=sibling.nodeDimension(g, this);
				moveNodeTowards(sibling, x, y);
				p=sibling.getOrigin();
				p.y+=siblingDim.height/2;
				points.add(p);
				sibling.paint(g, this);
				if (sibling.firstChild()!=null){ 
					Stroke str = g.getStroke();
					g.setStroke(new BasicStroke(g.getFont().getSize()/5));
					g.drawOval(sibling.getOrigin().x+siblingDim.width, sibling.getOrigin().y+siblingDim.height/2-5, 7, 7);
					g.setStroke(str);
				}
				height+=siblingDim.height+dist;
				y+=siblingDim.height+dist;
			}
			
			sibling=mindmap;
			y=leftCenter.y-(mindmapDimension.height)/2;
			
			while ((sibling=sibling.prev())!=null){
				Dimension siblingDim=sibling.nodeDimension(g, this);
				height+=siblingDim.height+dist;
				y-=siblingDim.height+dist;
				moveNodeTowards(sibling, x, y);
				p=sibling.getOrigin();
				p.y+=siblingDim.height/2;
				points.add(p);
				sibling.paint(g, this);
				
				if (sibling.firstChild()!=null){ 
					Stroke str = g.getStroke();
					g.setStroke(new BasicStroke(g.getFont().getSize()/5));
					g.drawOval(sibling.getOrigin().x+siblingDim.width, sibling.getOrigin().y+siblingDim.height/2-5, 7, 7);
					g.setStroke(str);
				}
				
			}			
			mindmapDimension=new Dimension(10,height);
			parent.resetDimension();
			Dimension parentDimension=parent.nodeDimension(g, this);
			leftCenter.y=y+height/2+parentDimension.height;
			leftCenter.x-=parentDimension.width+distance;
			moveNodeTowards(parent, leftCenter);
			p=parent.getOrigin();
			p.x+=parentDimension.width;
			p.y+=parentDimension.height/2;
			for (Iterator<Point> it = points.iterator();it.hasNext();) drawConnection(g, p, it.next());
			parent.paint(g, this);
			paintFamily(g, parent, leftCenter, mindmapDimension);
		}
	}

	private Dimension paint(Graphics2D g, MindmapNode mindmap, boolean draw) {
		if (!this.contains(mindmap.getOrigin())){
			mindmap.setFolded(true);
			return new Dimension(0,0);
		}
		if (!updatedSinceLastChange) mindmap.resetDimension();
		Dimension ownDim = mindmap.nodeDimension(g, this);
		if (draw) mindmap.paint(g, this); 
		g.setFont(g.getFont().deriveFont(fontSize*0.8f));

		Dimension childDim = mindmap.isFolded()?new Dimension(0,0):paintChildren(g, null, mindmap, false);
		Dimension result = new Dimension(ownDim.width + childDim.width + distance, Math.max(ownDim.height, childDim.height));
		if (draw) {
			Point leftCenter = mindmap.getOrigin();
						
			leftCenter.x += ownDim.width;
			leftCenter.y += ownDim.height/2;
			if (mindmap.isFolded()){ 
				Stroke str = g.getStroke();
				g.setStroke(new BasicStroke(g.getFont().getSize()/5));
				g.drawOval(leftCenter.x, leftCenter.y-dist/2, 7, 7);
				g.setStroke(str);
			} else paintChildren(g, leftCenter, mindmap, true);
		}
		return result;
	}

	private Dimension paintChildren(Graphics2D g, Point leftCenter, MindmapNode mindmap, boolean draw) {
		int width = 0;
		int height = dist;
		MindmapNode child = mindmap.firstChild();
		while (child != null) {
			Dimension d = paint(g,child, false);
			height += d.height+dist;
			width = Math.max(width, d.width);
			child = child.next();
		}
		if (draw) {
			height-=dist;
			Point currentOrigin=new Point(leftCenter);
			currentOrigin.y-=height/2;
			currentOrigin.x+=distance;
			child = mindmap.firstChild();
			while (child != null) {
				Dimension d=paint(g,child,false);
				Dimension d2=child.nodeDimension(g, this);
				Point target=new Point(currentOrigin);
				target.y+=(d.height-d2.height)/2;
				moveNodeTowards(child, target);
				paint(g,child, true);
				currentOrigin.y+=d.height+dist;
				Point p=child.getOrigin();
				p.y+=d2.height/2;
				p.x-=2;
				drawConnection(g,leftCenter,p);
				child = child.next();
			}
		}
		return new Dimension(width, height);
	}

	private void drawConnection(Graphics gr, Point p1, Point p2) {
		Graphics2D g = (Graphics2D) gr;
		Stroke str = g.getStroke();
		g.setStroke(new BasicStroke(g.getFont().getSize()/5));
		g.setColor(connectionColor);
		int dx=p2.x-p1.x;
		int dy=p1.y-p2.y;
		if (dy>0){
			g.drawArc(p1.x-dx/2, p2.y, dx, dy, 270, 90);
			g.drawArc(p1.x+dx/2, p2.y, dx, dy, 90, 90);
		} else {
			g.drawArc(p1.x-dx/2, p1.y, dx, -dy, 0, 90);
			g.drawArc(p1.x+dx/2, p1.y, dx, -dy, 180, 90);
		}
		g.setStroke(str);
	}
}
