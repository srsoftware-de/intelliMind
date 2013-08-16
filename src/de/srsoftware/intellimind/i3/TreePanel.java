package de.srsoftware.intellimind.i3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.DataFormatException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tools.srsoftware.GenericFileFilter;
import tools.srsoftware.Tools;
import de.srsoftware.formula.FormulaInputDialog;

/**
 * @author Stephan Richter
 * 
 */
public class TreePanel extends JPanel implements MouseListener, MouseWheelListener {
	private static final long serialVersionUID = -9127677905556355410L;
	private static final int UP = 1;
	private static final int DOWN = -1;
	protected static Color backgroundTraceColor = null;
	protected static Color foregroundTraceColor = null;
	private Vector<ActionListener> actionListeners;
	protected int distance = 100; // Basis-Distanz zwischen den Knoten des Mindmap
	private float distanceRatio = 6.5f;
	protected static float fontSize = 18f;
	private TreeSet<String> exportedFiles = null;
	protected MindmapNode mindmap; // das Mindmap, das vom Panel dargestellt wird
	protected Color connectionColor;
	protected static MindmapNode cuttedNode = null;
	private TreeThread organizerThread; // Thread, der in regelmäßigen Abständen das Layout aktualisiert
	protected static MindmapLanguagePack languagePack=null;
	protected boolean updatedSinceLastChange = false;
	protected int fileLoadLevelLimit = 2; // maximale Tiefe von aktuellem Knoten ausgehend, bei der verlinkte Mindmaps geladen werden
	private Image backgroundImage;



	public TreePanel(boolean arg0) {
		super(arg0);
		init();
	}

	public TreePanel(LayoutManager arg0) {
		super(arg0);
		init();
	}

	public TreePanel(LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
		init();
	}

	public TreePanel() {
		super();		
		init();
		organizerThread = new TreeThread();
		organizerThread.setTreeMapper(this);
		organizerThread.start();
		addMouseListener(this);
	}
	
	protected void moveNodeTowardsY(MindmapNode dummy, int y) {
		if (dummy !=null){
			Point origin=dummy.getOrigin();
			origin.y=(origin.y+y)/2;
			dummy.setOrigin(origin);			
		}
	}
	
	public void moveNodeTowards(MindmapNode node, Point target) {
		moveNodeTowards(node, target.x, target.y);
	}

	public void moveNodeTowards(MindmapNode node, int x, int y) {
		if (node != null) {
//			if (node == mindmap) {
//				x = this.getWidth() / 2;
//				y = this.getHeight() / 2;
//			}
			int dx = (x - node.getOrigin().x) / 4;
			int dy = (y - node.getOrigin().y) / 4;
			moveNode(node, dx, dy);
		}
	}
	
	public void moveNode(MindmapNode node, int dx, int dy) {
		if (dx != 0 || dy != 0) {
			Point oldOrigin = node.getOrigin();
			Point newOrigin = new Point(oldOrigin.x + dx, oldOrigin.y + dy);
			node.setOrigin(newOrigin);
			if (dx>5 || dy>5) pushThread();
		}
	}
	
	public Point center(){
		return new Point(getWidth()/2,getHeight()/2);
	}
	
	public void pushThread() {
		if (organizerThread != null) organizerThread.go();
	}

	private void init() {
		if (Tools.language.equals("English")){
			languagePack=new MindmapLanguagePack_English(); // auch verwendet in StarTreePanel und MindmapNode; FormulaInputDialog verwendet FormulaLanguagePack 
		} else languagePack = new MindmapLanguagePack_German(); // auch verwendet in StarTreePanel und MindmapNode; FormulaInputDialog verwendet FormulaLanguagePack
		actionListeners = new Vector<ActionListener>();
		this.setBackground(new Color(0, 155, 255));
		addMouseWheelListener(this);
		URL myurl = this.getClass().getResource("/intelliMind.gif");
		backgroundImage = this.getToolkit().getImage(myurl);
	}
	
  public void paint(Graphics g){
    super.paint(g);
    if (mindmap==null && backgroundImage!=null) g.drawImage(backgroundImage,(this.getWidth()-backgroundImage.getWidth(this))/2,(this.getHeight()-backgroundImage.getHeight(this))/2,this);
  }

	public void addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
	}

	public void navigateLeft() {
		if (mindmap.parent() != null) setMindmapTo(mindmap.parent());
	}

	public void navigateRight() {
		//System.out.println("navigateRight");
		if (mindmap.getLink() != null) Tools.execute(mindmap.getLink());
		if (mindmap.firstChild() != null) setMindmapTo(mindmap.firstChild());
	}

	public void navigateDown() {
		if (mindmap.next() != null)
			setMindmapTo(mindmap.next());
		else {			
			MindmapNode dummy=mindmap.parent();
			if (dummy!=null) {
				setMindmapTo(dummy);
			sheduleNavigation(DOWN);
		}
	}
	}
	
	private class NavigationThread extends Thread{
		private int direction;
		public NavigationThread(int direction) {
			this.direction=direction;
		}
		
		@Override
		public void run() {			
			super.run();
			try {
				sleep(100);
				switch (direction) {
				case DOWN:
					navigateDown();
					break;

				case UP:
						navigateUp();
						break;
				}
			} catch (InterruptedException e) {
			}
		}
	}

	private void sheduleNavigation(int direction) {
		(new NavigationThread(direction)).start();
		
	}

	public void navigateUp() {
		if (mindmap.prev() != null)
			setMindmapTo(mindmap.prev());
		else {
			MindmapNode dummy=mindmap.parent();
			if (dummy!=null) {
				setMindmapTo(dummy);
			sheduleNavigation(UP);
			}
			

		}
	}

	public void navigateToEnd() {
		if (mindmap.firstChild() != null) {
			MindmapNode dummy = mindmap.firstChild();
			while (dummy.next() != null)
				dummy = dummy.next();
			setMindmapTo(dummy);
		}
	}

	public void deleteActive() {
		MindmapNode dummy = mindmap.prev();
		if (dummy == null) dummy = mindmap.next();
		if (dummy == null) dummy = mindmap.parent();
		if (dummy != null) {
			mindmap.cutoff();
			mindmap.parent().mindmapChanged();
			setMindmapTo(dummy);
		}
	}

	protected void setMindmapTo(MindmapNode newNode) {
		//System.out.println("setMindmapTo("+newNode.getText()+")");
		if (newNode == null) return; // falls kein Knoten zum zentrieren übergeben wurde: abbrechen
		if (backgroundTraceColor != null) newNode.setBGColor(backgroundTraceColor); // falls die Hintergrundfarbe verschleppt wird: Hintergrundfarbe zum Ziel-Knoten übertragen
		if (foregroundTraceColor != null) newNode.setForeColor(foregroundTraceColor); // falls die Vordergrundfarbe verschleppt wird: Vordergrundfarbe zum Ziel-Knoten übertragen
		mindmap.shrinkImages();// falls Bild des alten Zentrums groß war: verkleinern
		mindmap = newNode; // neuen Knoten zentrieren
		propagateCurrentFile(); // die frohe Nachricht vom neuen Knoten verbreiten
		updateView(); // Ansicht aktualisieren
	}

	protected void sendActionEvent(ActionEvent actionEvent) {
		for (ActionListener a : actionListeners)
			a.actionPerformed(actionEvent);
	}

	protected void propagateCurrentFile() {
		//System.out.println("propagateCurrentFile");
		URL path = mindmap.getRoot().nodeFile();
		if (path != null) {
			String title = path.toString();
			if (mindmap.hasUnsavedChanges()) title += " (*)";
			//System.out.println("set title to "+title);
			sendActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SetTitle:" + title));
		}
	}


	public void setMindmap(MindmapNode root) {
		mindmap = root;
		organizerThread.go();		
	}

	public void appendNewChild(MindmapNode newChild) {
		if (newChild != null) {
			mindmap.addChild(newChild);
			mindmap.mindmapChanged();
			updateView();
		}
	}

	public void editMindmap(MindmapNode node) {
		String oldText = node.getFormulaCode();
		String text=oldText;
		if (text.endsWith(".imf")||text.endsWith("{.imf}"))	{
			text=text.replace("}\\small{", "").replace("\\small{", "").replace("}\\bold{","");
			text=text.substring(text.lastIndexOf('/')+1);
			text=text.substring(0,text.lastIndexOf('.'));
		}
		String newText = FormulaInputDialog.readInput(null, languagePack.CHANGE_CURRENT_NODES_TEXT(), text);
		if ((newText != null) && !newText.equals(oldText)) node.setText(newText);
		updateView();
	}

	protected void showMindmapImage() {
		if (mindmap.getNodeImage() != null) {
			mindmap.changeImageShrinkOption();
			updateView();
		}
	}

	public void editMindmap() {
		editMindmap(mindmap);
		requestFocus();
	}

	public void questForFileToSaveMindmap(MindmapNode node) {
		String guessedName = Tools.deleteNonFilenameChars(node.getText() + ".imf");
		String choosenFilename = Tools.saveDialog(this, languagePack.SAVE_AS(), guessedName, new GenericFileFilter(languagePack.MINDMAP_FILE(), "*.imf"));
		if (choosenFilename == null)
			node.mindmapChanged();
		else {
			if (!choosenFilename.toUpperCase().endsWith(".IMF") && !choosenFilename.toUpperCase().endsWith(".MM")) {
				choosenFilename += ".imf";
			}
			if (!(new File(choosenFilename)).exists() || JOptionPane.showConfirmDialog(null, languagePack.ASK_FOR_OVERWRITE(), languagePack.WARNING(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				try {
					URL u = new URL("file://" + choosenFilename);
					System.out.println(u);
					if (!node.saveTo(u))
						JOptionPane.showMessageDialog(null, languagePack.SAVE_FAILED().replace("##", choosenFilename), languagePack.SAVE_ERROR(), JOptionPane.OK_OPTION);
					else {
						sendActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SetTitle:" + node.getRoot().nodeFile()));
					}
				} catch (MalformedURLException e) {
					System.out.println("Konnte keine URL aus \"" + choosenFilename + "\" basteln!");
					node.mindmapChanged();
				}
			}
		}
	}

	private MindmapNode pollFirst(TreeSet<MindmapNode> ts) {
		MindmapNode result = ts.first();
		ts.remove(result);
		return result;
	}

	public void saveMindmaps() {
		TreeSet<MindmapNode> unsavedMindmaps = MindmapNode.saveChangedMindmaps();
		while (!unsavedMindmaps.isEmpty())
			questForFileToSaveMindmap(pollFirst(unsavedMindmaps));
		propagateCurrentFile();
	}

	public boolean hasUnsavedMindmap() {
		return MindmapNode.existUnsavedMindmaps();
	}

	public void flushMindmapChanges() {
		MindmapNode.flushUnsavedChanges();
	}

	public void saveCurrentFork() {
		questForFileToSaveMindmap(mindmap);
		if (mindmap.parent() != null) mindmap.parent().mindmapChanged();
	}

	public void setImageOfCurrentNode(NodeImage nodeImage) {
		if (nodeImage != null) mindmap.setNodeImage(nodeImage);
	}

	public void appendNewBrother(MindmapNode createNewNode) {
		mindmap.addBrother(createNewNode);
		setMindmapTo(createNewNode);
	}

	public MindmapNode currentMindmap() {
		return mindmap;
	}

	public void cut() {
		if (mindmap.parent() != null) {
			cuttedNode = mindmap;
			mindmap.cutoff();
			mindmap.parent().mindmapChanged();
			if (mindmap.prev() != null) {
				setMindmapTo(mindmap.prev());
			} else {
				if (mindmap.next() != null) {
					setMindmapTo(mindmap.next());
				} else {
					setMindmapTo(mindmap.parent());
				}
			}
		}
	}

	protected void copyToClipboard(MindmapNode node) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(node.getText() + '\n'), null);
	}

	private void copyToClipboard() {
		copyToClipboard(mindmap);
	}

	public void copy() {
		cuttedNode = mindmap.clone();
		copyToClipboard();
	}

	public void paste() {
		if (mindmap.parent() != null && cuttedNode != null) {
			appendNewBrother(cuttedNode);
			cuttedNode = cuttedNode.clone();
		}
	}

	public void setCurrentBackgroundColor(Color c) {
		if (c != null) mindmap.setBGColor(c);
	}

	public void showNodeDetails() {
		JOptionPane.showMessageDialog(this, mindmap.getFullInfo(), "Information", JOptionPane.INFORMATION_MESSAGE);
		this.requestFocus();
	}

	public void refreshView() {
		if (mindmap.getNodeImage() != null) {
			mindmap.getNodeImage().reload();
			updateView();
		}
	}

	public boolean traceBGColor() {
		if (backgroundTraceColor == null) {
			backgroundTraceColor = mindmap.getBGColor();
			return true;
		}
		backgroundTraceColor = null;
		return false;
	}

	public boolean traceForeColor() {
		if (foregroundTraceColor == null) {
			foregroundTraceColor = mindmap.getForeColor();
			return true;
		}
		foregroundTraceColor = null;
		return false;
	}

	public void deleteActiveImage() {
		mindmap.setImage(null);
	}

	public void navigateToRoot() {
		while (mindmap.parent() != null) setMindmapTo(mindmap.parent());
	}

	public void setCurrentForegroundColor(Color c) {
		if (c != null) mindmap.setForeColor(c);
	}

	public void setTextSmaller() {
		fontSize *= 0.9;
		updateView();
	}

	public void setTextLarger() {
		fontSize *= 1.1;
		updateView();
	}

	public float getTextSize() {
		return fontSize;
	}

	public void setTextSize(float fs) {
		fontSize = fs;
	}

	public void startHtmlExport(String folder, boolean onlyCurrent, int maxDepth, boolean interactive, boolean singleFile, boolean noMultipleFollow) throws IOException, DataFormatException, URISyntaxException {
		exportedFiles = new TreeSet<String>();
		MindmapNode root = mindmap.getSuperRoot();
		writeHtmlFile(root, folder, 1, onlyCurrent, maxDepth, interactive, singleFile, noMultipleFollow);
		this.setMindmap(root.reload());
	}

	public String writeHtmlFile(MindmapNode root, String folder, int depth, boolean onlyCurrent, int maxDepth, boolean interactive, boolean singleFile, boolean noMultipleFollow) throws IOException {
		String filename = root.nodeFile().getFile();
		filename = folder + filename.substring(filename.lastIndexOf("/") + 1) + ".html";
		if ((maxDepth == 0 || depth < maxDepth) && !exportedFiles.contains(filename)) {
			exportedFiles.add(filename);
			BufferedWriter htmlFile = new BufferedWriter(new FileWriter(filename));
			writeHtmlHeader(htmlFile);
			htmlFile.write("<body>\n");
			exportNodeToHtml(root, htmlFile, folder, depth, onlyCurrent, maxDepth, interactive, singleFile, noMultipleFollow);
			htmlFile.write("</body>\n");
			closeHtmlFile(htmlFile);
		}
		return filename;
	}

	private void exportNodeToHtml(MindmapNode node, BufferedWriter htmlFile, String folder, int depth, boolean onlyCurrent, int maxDepth, boolean interactive, boolean singleFile, boolean noMultipleFollow) throws IOException {
		htmlFile.write(node.getText());
		if (node.firstChild() != null) {
			htmlFile.write("<ul>\n");
			MindmapNode child = node.firstChild();
			while (child != null) {
				htmlFile.write("<li>");
				if (child.nodeFile() == null) {
					exportNodeToHtml(child, htmlFile, folder, depth, onlyCurrent, maxDepth, interactive, singleFile, noMultipleFollow);
				} else {
					if (onlyCurrent) {
						htmlFile.write(child.getText());
					} else {
						boolean include=depth<maxDepth;
						if (interactive){
							System.out.println("Warning: interactive export not supported, yet.");
							// include = Abfrage
						}
						if (include) {
							try {
								child.loadFromFile();
							} catch (FileNotFoundException fnfe){								
							} catch (NullPointerException npwe){								
							} catch (DataFormatException e) {
							} catch (URISyntaxException e) {
							}
						}
						if (singleFile) {
							exportNodeToHtml(child, htmlFile, folder, depth+1, onlyCurrent, maxDepth, interactive, singleFile, noMultipleFollow);
						} else {
							if (child.hasBeenLoadedFromFile()) {
								String lnk = writeHtmlFile(child, folder, depth + 1, onlyCurrent, maxDepth, interactive, singleFile, noMultipleFollow);
								htmlFile.write("<a href=\"file://" + lnk + "\">" + child.getText() + "</a>");
							} else {
								htmlFile.write(child.getText());
							}
						}
					}
				}
				htmlFile.write("</li>");
				child = child.next();
			}
			htmlFile.write("</ul>\n");
			node.cutoff();
		}
	}

	private void writeHtmlHeader(BufferedWriter htmlFile) throws IOException {
		htmlFile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		htmlFile.write("<html>\n<head>\n<title>" + mindmap.getText() + "</title>\n</head>\n");
	}

	private void closeHtmlFile(BufferedWriter htmlFile) throws IOException {
		htmlFile.write("</html>");
		htmlFile.close();
	}

	public void saveRoot() {
		questForFileToSaveMindmap(mindmap.getSuperRoot());
	}

	public void setLinkOfCurrentNode(URL link) {
		mindmap.setLink(link);
	}

	public void deleteActiveLink() {
		mindmap.setLink(null);
	}
	
	public void setBackground(Color bg){
		super.setBackground(bg);
		connectionColor = Tools.colorComplement(bg);
	}
	
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		distance = (int) (width / distanceRatio);
		updateView();
	}

	public void setBounds(Rectangle r) {
		setBounds(r.x, r.y, r.width, r.height);
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
		distance = (int) (width / distanceRatio);
		updateView();
	}

	public void setSize(Dimension d) {
		setSize(d.width, d.height);
	}

	public void updateView() {
		updatedSinceLastChange = false;
		organizerThread.go();
		repaint();
	}
	
	public void mouseClicked(MouseEvent arg0) {}

	public void mouseEntered(MouseEvent arg0) {}

	public void mouseExited(MouseEvent arg0) {}

	public void mousePressed(MouseEvent arg0) {
		// Bestimmen des geklcikten Knotens
		MindmapNode clickedNode = getNodeAt(arg0.getPoint());

		// bei Doppelklick: Aktion auslösen
		if (arg0.getClickCount() > 1) {
			if (mindmap.getLink() != null)
				// Ausführen, falls Verknüpfung
				Tools.execute(mindmap.getLink());
			else
				// Bearbeiten, falls normaler Knoten
				editMindmap(clickedNode);
		} else {
			if (arg0.getButton() == MouseEvent.BUTTON2) {
				// Knoten-Text in Zwischenablage kopieren
				copyToClipboard(clickedNode);
			} else {
				// zu Knoten wechseln oder Bild vergrößern
				if (mindmap != null) {
					if (mindmap == clickedNode)
						// wenn geklickter Knoten schon im Zentrum ist: Bild ggf. vergrößern
						showMindmapImage();
					else {
						// wenn geklickter Knoten in der Peripherie: zentrieren
						setMindmapTo(clickedNode);
					}
				}
			}
		}
	}

	private MindmapNode getNodeAt(Point point) {
		// Start: Distanz von Click zu zentralem Knoten prüfen
		if (mindmap == null) return null;
		MindmapNode clickedNode = mindmap;
		double minDistance = point.distance(mindmap.getOrigin());

		// Dann alle direkten Kinder des Zentralen Knotens prüfen
		MindmapNode dummy = mindmap.firstChild();
		while (dummy != null) {
			double distance = point.distance(dummy.getOrigin());
			if (distance < minDistance) {
				clickedNode = dummy;
				minDistance = distance;
			}

			// alle Enkel des zentralen Knotens prüfen
			MindmapNode dummy2 = dummy.firstChild();
			while (dummy2 != null) {
				distance = point.distance(dummy2.getOrigin());
				if (distance < minDistance) {
					clickedNode = dummy2;
					minDistance = distance;
				}
				dummy2 = dummy2.next();
			}

			dummy = dummy.next();
		}

		// Elter des zentralen Knotens prüfen
		if (mindmap.parent() != null) {
			dummy = mindmap.parent();

			// groß-Elter prüfen
			if (dummy.parent() != null) {
				double distance = point.distance(dummy.parent().getOrigin());
				if (distance < minDistance) {
					clickedNode = dummy.parent();
					minDistance = distance;
				}
			}

			// Kinder des Elter prüfen
			MindmapNode dummy2 = dummy.firstChild();
			while (dummy2 != null) {
				double distance = point.distance(dummy2.getOrigin());
				if (distance < minDistance) {
					clickedNode = dummy2;
					minDistance = distance;
				}
				dummy2 = dummy2.next();
			}

			double distance = point.distance(dummy.getOrigin());
			if (distance < minDistance) {
				clickedNode = dummy;
				minDistance = distance;
			}
		}
		return clickedNode;
	}

	public void mouseReleased(MouseEvent arg0) {}

	public void stopOrganizing() {
		organizerThread.die();		
	}

	protected void setParametersFrom(TreePanel mindmapPanel) {
		mindmap=mindmapPanel.currentMindmap();
		connectionColor=mindmapPanel.connectionColor;
		this.setBackground(mindmapPanel.getBackground());
	}

	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (arg0.getWheelRotation()==-1){
			this.setTextLarger();
			distance*=1.1;
		} else
		if (arg0.getWheelRotation()==1){
			this.setTextSmaller();
			distance*=0.9;
		}
	}

	public void decreaseDistance() {
		distance -= 10;
		updateView();
	}

	public void increaseDistance() {
		distance += 10;
		updateView();
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int d) {
		distance = d;
	}
}
