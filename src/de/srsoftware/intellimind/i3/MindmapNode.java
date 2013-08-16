package de.srsoftware.intellimind.i3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.DataFormatException;

import tools.srsoftware.FileRecoder;
import tools.srsoftware.ObjectComparator;
import tools.srsoftware.Tools;
import de.srsoftware.formula.Formula;
import de.srsoftware.xmlformatter.XmlFormatter;

public class MindmapNode {
	private static TreeSet<MindmapNode> changedMindmaps = new TreeSet<MindmapNode>(new ObjectComparator());
	private MindmapNode parent = null; // this variable holds the pointer to the parent node, if given
	private MindmapNode firstChild = null;// this variable holds the pointer to the current node's first child
	private MindmapNode lastChild = null;// this variable holds the pointer to the current node's last child
	private MindmapNode nextBrother = null;// this variable holds the pointer to the current node's next brother, if given
	private MindmapNode previousBrother = null;// this variable holds the pointer to the current node's previous brother, if given
	private MindmapNode referencedNode = null;
	@SuppressWarnings("unused")
	private NodeId nodeId = null; // this holds the node's id
	private boolean folded = true; // determines whether a node's children shall be shown
	private NodeImage nodeImage = null; // holds the node's image, if given
	private Color foregroundColor = null; // color for text
	private Color backgroundColor = null; // color for filling the nodes
	private URL nodeFile = null; // hold the URL of the file, which saves this node
	private boolean nodeFileHasBeenLoaded = false; // is set to false, for nodes, that only point to files, and to true, if the file has been loaded
	private URL link = null; // holds a given link
	private Point origin = null; // the node's current drawing position
	private Formula formula = null; // the node's formula i.e. text
	private int numChildren = 0; // the number of children the node has
	private static boolean centered = false; // determines, whether =>origin specifies the center or the upper left corner of the node
	private static Color swappedColor = Color.white;
	private boolean shrinkLargeImages = true;
	private int maxBackupNumber = 10;
	/* private static MindmapLanguagePack languagePack=new MindmapLanguagePack_English(); / */private static MindmapLanguagePack languagePack = new MindmapLanguagePack_German();

	// */

	/**
	 * internal method to initialize the MindmapNode
	 * 
	 * @param text the text, which is passed to the node's formula
	 */
	private void initialize(String text, Point origin) {
		if (text != null) this.formula = new Formula(text);
		this.origin = (origin != null) ? origin : new Point(3000, 500);
		this.foregroundColor = Color.black;
		this.backgroundColor = Color.white;
		this.nodeId = new NodeId();
		nodeFileHasBeenLoaded = false;
	}

	/**
	 * create a new node with empty formula
	 */
	public MindmapNode() {
		initialize(null, null);
	}

	/**
	 * create a new node with empty formula at position origin
	 */
	public MindmapNode(Point origin) {
		initialize(null, origin);
	}

	/**
	 * create a new node with the given text
	 * 
	 * @param text the text for the formula
	 */
	public MindmapNode(String text) {
		initialize(text, null);
	}

	/**
	 * create a new node with the given text
	 * 
	 * @param text the text for the formula
	 */
	public MindmapNode(String text,Point origin) {
		initialize(text, origin);
	}

	/**
	 * append the given MindmapNode to the current one
	 * 
	 * @param newChild the MindmapNode to be appended
	 */
	public void addChild(MindmapNode newChild) {
		
		newChild.parent = this;
		lastChild = newChild;
		if (firstChild == null) {
			firstChild = newChild;
			lastChild.previousBrother=null;
		} else {
			MindmapNode dummy = firstChild;
			while (dummy.nextBrother != null)	dummy = dummy.nextBrother;
			dummy.nextBrother = newChild;
			lastChild.previousBrother = dummy;
		}
		numChildren++;
	}

	public Point getOrigin() {
		return new Point(origin);
	}

	public void setOrigin(Point newOrigin) {
		origin = newOrigin;
	}

	public int getNumChildren() {
		// TODO Auto-generated method stub
		return numChildren;
	}

	public MindmapNode parent() {
		return parent;
	}

	public MindmapNode firstChild() {
		return firstChild;
	}

	public MindmapNode lastChild() {
		return lastChild;
	}

	public MindmapNode next() {
		return nextBrother;
	}

	public MindmapNode prev() {
		return previousBrother;
	}

	public static void swapColor(Graphics g) {
		Color dummy = g.getColor();
		g.setColor(swappedColor);
		swappedColor = dummy;
	}

	public void doNotShrinkImages() {
		shrinkLargeImages = false;
	}

	public void shrinkImages() {
		shrinkLargeImages = true;
	}

	public Dimension paint(Graphics g, ImageObserver obs) {
		return paint(g, obs, true);
	}

	public Dimension paint(Graphics g, ImageObserver obs, boolean draw) {
		
		if (formula != null) {
			Dimension formulaDimension = formula.getSize(g);
			if (formulaDimension.width < 10 && nodeImage != null) formulaDimension.width = 300;
			Dimension imageDimension = (nodeImage != null) ? ((shrinkLargeImages) ? nodeImage.getResizedDimension(formulaDimension.width, obs) : nodeImage.getDimension(obs)) : (new Dimension());
			Dimension nodeDimension = new Dimension(Math.max(formulaDimension.width, imageDimension.width)+4, formulaDimension.height + imageDimension.height+4);
			Point upperLeft = (centered) ? new Point(origin.x - nodeDimension.width / 2, origin.y - nodeDimension.height / 2) : origin;
			if (draw) {
				// the following lines draw arcs besides the node, if the node contains a link
				if (link != null) {
					g.drawArc(upperLeft.x - (nodeDimension.height / 2), upperLeft.y - 2, nodeDimension.height , nodeDimension.height , 90, 180);
					g.drawArc(upperLeft.x + nodeDimension.width  - (nodeDimension.height / 2), upperLeft.y - 2, nodeDimension.height , nodeDimension.height, 270, 180);
				}
				swapColor(g);
				if (this.backgroundColor != null) g.setColor(this.backgroundColor);
				g.fillRoundRect(upperLeft.x - 2, upperLeft.y - 2, nodeDimension.width, nodeDimension.height , 5, 5);
				swapColor(g);
				if (this.foregroundColor != null) g.setColor(this.foregroundColor);
				g.drawRoundRect(upperLeft.x - 2, upperLeft.y - 2, nodeDimension.width, nodeDimension.height , 5, 5);
				
				if (formulaDimension.width > imageDimension.width) {
					formula.draw(g, upperLeft.x, upperLeft.y + imageDimension.height);
				} else {
					formula.draw(g, upperLeft.x + (imageDimension.width - formulaDimension.width) / 2, upperLeft.y + imageDimension.height);
				}
				if (this.nodeImage != null) {
					g.drawString("\u270D", upperLeft.x + 2, upperLeft.y + g.getFontMetrics().getHeight() + 2);
					nodeImage.paint(g, obs, upperLeft, imageDimension);
				}
			}
			return nodeDimension;
		}
		return null;
	}

	public void paintWithoutImages(Graphics g, ImageObserver obs) {
		// TODO Auto-generated method stub
		if (formula != null) {
			Dimension d = formula.getSize(g);
			Point upperLeft = (centered) ? new Point(origin.x - d.width / 2, origin.y - d.height / 2) : origin;
			swapColor(g);
			if (this.backgroundColor != null) g.setColor(this.backgroundColor);
			g.fillRoundRect(upperLeft.x - 2, upperLeft.y - 2, d.width + 2, d.height + 2, 5, 5);
			swapColor(g);
			if (this.foregroundColor != null) g.setColor(this.foregroundColor);
			g.drawRoundRect(upperLeft.x - 2, upperLeft.y - 2, d.width + 2, d.height + 2, 5, 5);
			formula.draw(g, upperLeft);
		}
	}

	public String getFormulaCode() { // get the code
		return (formula != null) ? formula.toString() : null;
	}

	public String getText() { // get the text only, without formatting
		return (formula != null) ? formula.getText() : null;
	}

	private String toString(int l) {
		if (l > 10) return "...";
		String result = "{";
		if (formula != null) result += formula;
		MindmapNode dummy = firstChild();
		while (dummy != null) {
			result += dummy.toString(l + 1);
			dummy = dummy.next();
		}
		result += "}";
		return result;
	}

	public String toString() {
		return toString(0);
	}

	public static void setCentered(boolean c) {
		centered = c;
	}

	public void resetDimension() {
		if (formula != null) formula.resetDimension();
	}

	public void cutoff() {
		MindmapNode parentOfNodeToIsolate = parent();
		MindmapNode nextBrotherOfNodeToIsolate = next();
		MindmapNode previousBrotherOfNodeToIsolate = prev();
		if (nextBrotherOfNodeToIsolate != null) nextBrotherOfNodeToIsolate.previousBrother = previousBrotherOfNodeToIsolate;
		if (previousBrotherOfNodeToIsolate != null) previousBrotherOfNodeToIsolate.nextBrother = nextBrotherOfNodeToIsolate;
		if (parentOfNodeToIsolate != null) {
			if (this == parentOfNodeToIsolate.firstChild) parentOfNodeToIsolate.firstChild = nextBrotherOfNodeToIsolate;
			if (this == parentOfNodeToIsolate.lastChild) parentOfNodeToIsolate.lastChild = previousBrotherOfNodeToIsolate;
			parentOfNodeToIsolate.numChildren--;
		}
	}

	public boolean saveTo(URL fileUrl) {
		// TODO Auto-generated method stub
		nodeFile = fileUrl;
		String s = fileUrl.toString();
		s = s.substring(s.indexOf(":") + 1);
		// System.out.println("Versuche nach " + s + " zu speichern");
		File f = new File(s);
		try {
			f.createNewFile();
			this.save();
			this.nodeFileHasBeenLoaded = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}

	public URL getLink() {
		return link;
	}

	public void setLink(URL link) {
		this.link = link;
		mindmapChanged();
	}

	public void setImage(URL fileUrl) {
		this.nodeImage = (fileUrl == null) ? null : new NodeImage(fileUrl);
		mindmapChanged();
	}

	public void loadFromFile() throws FileNotFoundException, IOException, DataFormatException, URISyntaxException {	
		if (this.nodeFile != null) this.loadFromFile(this.nodeFile);
	}

	@SuppressWarnings("deprecation")
	public URL getTemporaryUrl() throws MalformedURLException {
		return (new File("intelliMind3.tmp.imf")).toURL();
	}

	public void loadFromFile(URL fileUrl) throws FileNotFoundException, IOException, DataFormatException, URISyntaxException {
		if (!nodeFileHasBeenLoaded) {
			nodeFileHasBeenLoaded = true;
			if (!Tools.fileIsLocal(fileUrl)) {
				try {
					fileUrl = new URL(fileUrl.toString().replace(" ", "%20"));
				} catch (MalformedURLException e1) {}
			}
			if (!Tools.fileExists(fileUrl)) {
				throw new FileNotFoundException(fileUrl.toString());
			} else {
				
				fileUrl=resolveSymLinks(fileUrl);
				
				MindmapNode n = nodeOpenAndChanged(fileUrl);
				if (n != null) {
					// TODO wenn ein Mindmap geöffnet wird, das schon offen, geändert und noch ncht gespeichert ist:
					// dieses Mindmap in eine temporäre Datei schreiben, und diese öffnen
					URL temp = getTemporaryUrl();
					if (n.saveTo(temp)) {
						n.nodeFile = fileUrl;
						if (Tools.fileIsIntelliMindFile(fileUrl)) loadFromIntellimindFile(temp);
						if (Tools.fileIsFreeMindFile(fileUrl)) loadFromFreemindFile(temp);
						changedMindmaps.remove(n);
						this.nodeFile = fileUrl;
						this.mindmapChanged();
					} else {
						if (Tools.fileIsIntelliMindFile(fileUrl)) loadFromIntellimindFile(fileUrl);
						if (Tools.fileIsFreeMindFile(fileUrl)) loadFromFreemindFile(fileUrl);
					}
				} else {
					if (isFolder(fileUrl)) loadFolder(fileUrl); else
					if (Tools.fileIsKeggUrl(fileUrl)) loadKeggFile(fileUrl); else
					if (Tools.fileIsIntelliMindFile(fileUrl)) loadFromIntellimindFile(fileUrl); else
					if (Tools.fileIsFreeMindFile(fileUrl)) loadFromFreemindFile(fileUrl); else
						throw new DataFormatException(fileUrl.toString());
				}
			}
		}
	}
	
	private static boolean isSymbolicLink(File file) throws IOException {
	  if (file == null)
	    throw new NullPointerException("File must not be null");
	  File canon;
	  if (file.getParent() == null) {
	    canon = file;
	  } else {
	    File canonDir = file.getParentFile().getCanonicalFile();
	    canon = new File(canonDir, file.getName());
	  }
	  return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
	}

	private static URL resolveSymLinks(URL fileUrl) throws URISyntaxException, IOException {
		File path = new File(fileUrl.toString().substring(5));
		if (isSymbolicLink(path))	{
			System.out.println(fileUrl+" refers to symlink");
	    File target = path.getCanonicalFile();	    
//			File target = readSymbolicLink(path);
/*			if (path.isAbsolute()){
				if (!target.isAbsolute())	target=path.getParent().resolve(target);
			} else target=target.toAbsolutePath();*/
			fileUrl = new URL("file:"+target);
		}
		return fileUrl;
	}
	
	private void loadKeggFile(URL fileUrl) throws IOException {
		System.out.println("loading "+fileUrl);
		String url=fileUrl.toString();
		if (url.startsWith("http://www.genome.jp/dbget-bin/www_bget?R") ||url.startsWith("http://www.genome.jp/dbget-bin/www_bget?rn:R")) loadKeggReaction(fileUrl);
		if (url.startsWith("http://www.genome.jp/dbget-bin/www_bget?C") ||url.startsWith("http://www.genome.jp/dbget-bin/www_bget?cpd:C")) loadKeggSubstance(fileUrl);
		MindmapNode child=new MindmapNode("\\=>  browse \\=> ");
		child.link=fileUrl;
		addChild(child);
		nodeFile=fileUrl;
	}

	@SuppressWarnings("deprecation")
	private void loadFolder(URL fileUrl) throws FileNotFoundException, MalformedURLException, IOException {
		nodeFileHasBeenLoaded=true;
		File f=new File(fileUrl.getFile());
		this.setText(f.getName());
		File [] subs=f.listFiles();
		TreeMap<String,File> files=new TreeMap<String,File>(ObjectComparator.get());
		for (int i=0; i<subs.length; i++){
			String name=subs[i].getName();
			if (!name.startsWith(".")) files.put(name, subs[i]);
		}
		for (Iterator<String> it = files.keySet().iterator(); it.hasNext();){
			String name=it.next();			
			MindmapNode child=new MindmapNode(name);
			child.nodeFile=files.get(name).toURL();
			this.addChild(child);
		}
	}

	private boolean isFolder(URL fileUrl) {
		File f=new File(fileUrl.getFile());
		return f.isDirectory();
	}

	public boolean referencesOtherNode() {
		return (referencedNode != null);
	}

	public MindmapNode referencedNode() {
		return referencedNode;
	}

	public static MindmapNode nodeOpenAndChanged(URL url) {
		for (MindmapNode n : changedMindmaps) {
			URL u = n.nodeFile();
			if ((u != null) && (url.equals(u)) && n.nodeFileHasBeenLoaded) return n;
		}
		return null;
	}
	
	private void loadKeggSubstance(URL fileUrl) throws IOException {
		String[] lines=XmlFormatter.loadDocument(fileUrl).split("\n");
		for (int i=0; i<lines.length; i++){
			if (lines[i].contains("<nobr>Formula</nobr>")){
				
				addChild(new MindmapNode(formatFormula(Tools.removeHtml(lines[++i]))));
			}
			if (lines[i].contains("<nobr>Name</nobr>")) {
				formula=new Formula("Substance:\\n "+Tools.removeHtml(lines[++i]).replaceAll(";$", ""));
				MindmapNode otherNames = null;
				while (!lines[++i].contains("</div>")){
					if (otherNames==null) addChild(otherNames=new MindmapNode("other names"));
					otherNames.addChild(new MindmapNode(Tools.removeHtml(lines[i]).replaceAll(";$", "")));
				}
			}
			if (lines[i].contains("<nobr>Reaction</nobr>")) {
				MindmapNode reactions=null;
				while (!lines[++i].contains("</div>")){
					if (reactions==null) addChild(reactions=new MindmapNode("Reactions"));
					String[] ids=Tools.removeHtml(lines[i]).split(" ");
					for (int k=0; k<ids.length; k++){
						MindmapNode reaction = new MindmapNode(ids[k].trim());
						reaction.nodeFile=new URL("http://www.genome.jp/dbget-bin/www_bget?"+ids[k].trim());
						reactions.addChild(reaction);
					}
				}
			}
		}
	}

	private String formatFormula(String s) {
		return s.replaceAll("(\\D)(\\d)", "$1\\\\_{$2").replaceAll("(\\d)(\\D)", "$1}$2").replaceAll("(\\d)$","$1}");
	}

	private void loadKeggReaction(URL fileUrl) throws IOException {
		String[] lines=XmlFormatter.loadDocument(fileUrl).split("\n");
		String name=null;
		for (int i=0; i<lines.length; i++){			
			if (lines[i].contains("<nobr>Name</nobr>")) name=Tools.removeHtml(lines[++i]).replaceAll(";$", "");
			if (lines[i].contains("<nobr>Definition</nobr>") && name==null) name=Tools.removeHtml(lines[++i]);
			if (lines[i].contains("<nobr>Equation</nobr>")) loadKeggEquation(Tools.removeHtml(lines[++i]));
		}
		if (name==null) name="unnamed reaction";
		name=name.replace("<=>", "\\<=> ");
		formula=new Formula("Reaction:\\n "+name);
	}

	private void loadKeggEquation(String equation) {
		String[] sides=equation.split("<=>");
		MindmapNode substrates=new MindmapNode("Substrates");
		addSubstances(substrates,sides[0]);
		addChild(substrates);
		MindmapNode products=new MindmapNode("Products");
		addSubstances(products,sides[1]);
		addChild(products);
	}

	private void addSubstances(MindmapNode substances, String list) {
		String[] parts = list.split(" \\+ ");
		for (int i=0; i<parts.length; i++){
			MindmapNode child = new MindmapNode(parts[i]);
			try{
				child.nodeFile=new URL("http://www.genome.jp/dbget-bin/www_bget?"+parts[i].trim());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			substances.addChild(child);
		}
	}

	private void loadFromIntellimindFile(URL fileUrl) throws FileNotFoundException, IOException {
		fileUrl = Tools.fixUrl(fileUrl);
		FileRecoder.recode(fileUrl);
		try {
			BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileUrl.openStream(), "UTF-8"));
			int waitTime = 1;
			while (!fileReader.ready()) {
				Thread.sleep(waitTime);
				waitTime *= 2;
				System.out.println("Datei ist nicht verfügbar. Versuche es erneut in " + String.valueOf(waitTime) + " ms...");
				if (waitTime > 16000) throw new IOException(fileUrl.toString() + " nicht bereit zum Lesen!");
			}
			MindmapNode root = this;
			root.nodeFile = fileUrl;
			MindmapNode node = root;
			String line;
			while (fileReader.ready()) {
				line = fileReader.readLine();
				if (line.equals("[Root]")) {}
				if (line.equals("[Child]")) {
					node.addChild(new MindmapNode(this.origin));
					node = node.firstChild();
				}
				if (line.equals("[Brother]")) {
					node.parent().addChild(new MindmapNode(this.origin));
					node = node.next();
				}
				if (line.equals("[UP]")) {
					if (node.parent()!=null){
						node = node.parent();
					} else System.out.println("Mindmap corrupt: UP-command found while at root node.");
				}

				if (line.startsWith("text=")) {
					node.formula = new Formula(line.substring(5));
				}
				if (line.startsWith("content=")) {
					String content = line.substring(8).replace("\\", "/");
					if (content.startsWith("Link:")) {
						try {
							node.link = Tools.getURLto(this.getRoot().nodeFile.toString(), content.substring(5));
						} catch (MalformedURLException e) {
							Tools.message("externe Verknüpfung (" + content.substring(5) + ") konnte nicht aufgelöst werden!");
						}
					} else { // eingebundenes Unter-Mindmap
						try {
							URL nodeURL = Tools.getURLto(this.getRoot().nodeFile.toString(), content);
							node.nodeFile = nodeURL;
						} catch (MalformedURLException e) {
							Tools.message("Eingebettetes Mindmap (" + content + ") konnte nicht aufgelöst werden!");
						}

					}
				}
				if (line.startsWith("image=")) {
					String content = line.substring(6).replace("\\", "/");
					try {
						URL imageUrl = Tools.getURLto(this.getRoot().nodeFile.toString(), content);
						node.nodeImage = new NodeImage(imageUrl);
					} catch (MalformedURLException e) {
						Tools.message("Pfad zu Datei (" + content + ") konnte nicht aufgelöst werden!");
					}
				}
				if (line.startsWith("Color1=")) {
					try {
						int r = Integer.decode("0x" + line.substring(14, 16)).intValue();
						int g = Integer.decode("0x" + line.substring(12, 14)).intValue();
						int b = Integer.decode("0x" + line.substring(10, 12)).intValue();
						node.foregroundColor = new Color(r, g, b);
					} catch (Exception e) {
						node.foregroundColor = Tools.lookupColor(line.substring(7));
					}
				}
				if (line.startsWith("Color2=")) {
					try {
						int r = Integer.decode("0x" + line.substring(14, 16)).intValue();
						int g = Integer.decode("0x" + line.substring(12, 14)).intValue();
						int b = Integer.decode("0x" + line.substring(10, 12)).intValue();
						node.backgroundColor = new Color(r, g, b);
					} catch (Exception e) {
						node.backgroundColor = Tools.lookupColor(line.substring(7));
					}
				}
			}
			fileReader.close();
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		}
	}

	public URL nodeFile() {
		return nodeFile;
	}

	public MindmapNode getRoot() {
		MindmapNode result = this;
		while (result.parent != null && result.nodeFile == null)
			result = result.parent;
		return result;
	}

	public MindmapNode getSuperRoot() {
		// TODO Auto-generated method stub
		MindmapNode result = this;
		while (result.parent != null)
			result = result.parent;
		return result;
	}

	public void setForeColor(Color c) {
		this.foregroundColor = c;
		mindmapChanged();

	}

	public void setBGColor(Color c) {
		// System.out.println("Hintergrundfarbe wird gesetzt...");
		if (!c.equals(this.backgroundColor)) {
			this.backgroundColor = c;
			mindmapChanged();
		}
	}

	public Color getBGColor() {
		return this.backgroundColor;
	}

	public Color getColorFromCode(String code) {
		// System.out.println(code);
		if (code.startsWith("#")) code = code.substring(1);
		int r = Integer.decode("0x" + code.substring(0, 2)).intValue();
		int g = Integer.decode("0x" + code.substring(2, 4)).intValue();
		int b = Integer.decode("0x" + code.substring(4, 6)).intValue();
		Color result = new Color(r, g, b);
		return result;
	}

	private boolean readMindmapFile(BufferedReader file) throws IOException {
		while (file.ready() && this.formula == null) {
			String tag = Tools.readNextTag(file);
			if (tag.equals("<icon BUILTIN=\"button_cancel\"/>")) this.parent.formula = new Formula("\\rgb{ff0000,\\nok }" + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"idea\"/>")) this.parent.formula = new Formula("\\info " + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"messagebox_warning\"/>") || tag.equals("<icon BUILTIN=\"clanbomber\"/>")) this.parent.formula = new Formula("\\bomb " + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"forward\"/>")) this.parent.formula = new Formula("\\rgb{0099ff,\\=> }" + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"button_ok\"/>")) this.parent.formula = new Formula("\\rgb{00aa00,\\ok }" + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"pencil\"/>")) this.parent.formula = new Formula("\\rgb{bb0000,\\pen }" + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"back\"/>")) this.parent.formula = new Formula("\\rgb{0099ff,\\<= }" + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"help\"/>")) this.parent.formula = new Formula("\\rgb{000099,\\bold{(?)}} " + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"ksmiletris\"/>")) this.parent.formula = new Formula("\\rgb{008888,\\smile }" + this.parent.formula.toString());
			if (tag.equals("<icon BUILTIN=\"stop\"/>")) this.parent.formula = new Formula("\\rgb{ff0000,\\nokbox }" + this.parent.formula.toString());

			if (tag.startsWith("<node")) {
				String txt = Tools.htmlToUnicode(Tools.getTagProperty(tag, "TEXT"));

				String colString = Tools.getTagProperty(tag, "COLOR");
				if (colString != null) this.foregroundColor = getColorFromCode(colString);

				String colString2 = Tools.getTagProperty(tag, "BACKGROUND_COLOR");
				if (colString2 != null) this.backgroundColor = getColorFromCode(colString2);

				if (txt.contains("<img src=")) txt = extractImageFromTag(txt);
				this.formula = new Formula(txt);
				if (tag.endsWith("/>")) return true;
			}
			if (tag.equals("</node>")) return false;
		}
		while (file.ready()) {
			MindmapNode dummy = new MindmapNode(this.origin);
			dummy.parent = this;
			if (!dummy.readMindmapFile(file)) return true;
			this.addChild(dummy);
		}
		return true;
	}

	private String extractImageFromTag(String txt) throws MalformedURLException {
		// TODO Auto-generated method stub
		String file = Tools.getTagProperty(txt, "src");
		int i = txt.indexOf("<img src=");
		int j = txt.indexOf(">", i);
		txt = txt.substring(0, i) + txt.substring(j + 1);
		URL imageUrl = Tools.getURLto(this.getRoot().nodeFile.toString(), file);
		this.nodeImage = new NodeImage(imageUrl);
		return txt;
	}

	private void loadFromFreemindFile(URL fileUrl) throws IOException {
		// TODO Auto-generated method stub
		// System.out.println("loading intelliMind file " + fileUrl);
		fileUrl = Tools.fixUrl(fileUrl);
		try {
			BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileUrl.openStream(), "UTF-8"));
			int waitTime = 1;
			while (!fileReader.ready()) {
				Thread.sleep(waitTime);
				waitTime *= 2;
				System.out.println("Datei ist nicht verfügbar. Versuche es erneut in " + String.valueOf(waitTime) + " ms...");
				if (waitTime > 16000) throw new IOException(fileUrl.toString() + " nicht bereit zum Lesen!");
			}
			MindmapNode root = this;
			root.nodeFile = fileUrl;
			readMindmapFile(fileReader);

			fileReader.close();
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * replaces teh oldNod by this node
	 * @param oldNode
	 */
	public void replace(MindmapNode oldNode) {
		// TODO Auto-generated method stub
		this.parent = oldNode.parent();
		this.nextBrother = oldNode.next();
		this.previousBrother = oldNode.prev();
		if (this.parent != null) {
			if (this.parent.firstChild == oldNode) this.parent.firstChild = this;
			if (this.parent.lastChild == oldNode) this.parent.lastChild = this;
		}
		if (this.nextBrother != null) this.nextBrother.previousBrother = this;
		if (this.previousBrother != null) this.previousBrother.nextBrother = this;
	}

	public Vector<MindmapNode> linkedNodes() {
		Vector<MindmapNode> result = new Vector<MindmapNode>();
		if (this.parent != null) result.add(this.parent);
		MindmapNode dummy = this.firstChild;
		while (dummy != null) {
			result.add(dummy);
			dummy = dummy.nextBrother;
		}
		return result;
	}

	public void setNodeImage(NodeImage nodeImage) {
		this.nodeImage = nodeImage;
		mindmapChanged();
	}

	public NodeImage getNodeImage() {
		return nodeImage;
	}

	public void setFormula(Formula f) {
		formula = f;
		mindmapChanged();
	}

	public void setText(String tx) {
		formula = new Formula(tx);
		mindmapChanged();
	}

	public void mindmapChanged() {
		MindmapNode dummy = this;
		while (dummy != null && dummy.nodeFile == null) { // sucht nach der Wurzel des aktuellen Teilbaums
			if (dummy.parent() == null) changedMindmaps.add(dummy); // falls die Wurzel selbst noch nicht gespeichert wurde
			dummy = dummy.parent;
		}
		if (dummy != null && dummy.nodeFile != null) {
			changedMindmaps.add(dummy); // fügt die (schon mal gespeicherte) wurzel des aktuellen Teilbaums zur Speicher-Liste hinuzu
		}
	}

	public void changeImageShrinkOption() {
		// TODO Auto-generated method stub
		shrinkLargeImages = !shrinkLargeImages;
	}

	private void backup(String filename) {
		String backupFilename = filename + "." + Tools.getDateTime();
		File f = new File(filename);
		f.renameTo(new File(backupFilename));
		limitOldBackups(f);
	}

	private void limitOldBackups(File f) {
		File[] files = f.getParentFile().listFiles(new Filefilter(f.getName()));
		java.util.Arrays.sort(files);
		int number = files.length;
		number -= maxBackupNumber;
		for (File file : files) {
			if (--number >= 0) file.delete();
		}
	}

	private boolean save() {
		if (nodeFile != null) {
			try {
				String filename = nodeFile.getFile();
				if (Tools.fileIsLocal(nodeFile) && Tools.fileExists(nodeFile) && !filename.contains(".tmp.")) {
					backup(filename);
				}
				OutputStreamWriter outFile = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
				outFile.write("[Encoding]\r\n");
				outFile.write("UTF-8\r\n");
				outFile.write("[Root]\r\n");
				saveNode(outFile, nodeFile);
				if (firstChild != null) {
					outFile.write("[Child]\r\n");
					firstChild.saveTree(outFile, nodeFile);
					outFile.write("[UP]\r\n");
				}
				outFile.close();
				changedMindmaps.remove(this);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private void saveTree(OutputStreamWriter outFile, URL filename) throws IOException {
		// TODO Auto-generated method stub
		saveNode(outFile, filename);
		if (firstChild != null && nodeFile == null) {
			outFile.write("[Child]\r\n");
			firstChild().saveTree(outFile, filename);
			outFile.write("[UP]\r\n");
		}
		if (nextBrother != null) {
			outFile.write("[Brother]\r\n");
			nextBrother.saveTree(outFile, filename);
		}
	}

	private void saveNode(OutputStreamWriter file, URL filename) throws IOException {
		// System.out.println("StarMindmap.saveNode("+filename+" , "+node.getText()+");");
		file.write("text=" + this.getFormulaCode() + "\r\n");
		if (nodeImage != null) {
			Tools.getRelativePath(filename, nodeImage.getUrl());
			file.write("image=" + Tools.getRelativePath(filename, nodeImage.getUrl()) + "\r\n");
		}
		if (link != null) file.write("content=Link:" + Tools.getRelativePath(filename, link) + "\r\n");
		if (nodeFile != null && !nodeFile.equals(filename)) {
			file.write("content=" + Tools.getRelativePath(filename, nodeFile) + "\r\n");
		}
		if (!foregroundColor.equals(Color.BLACK)) file.write("Color1=" + Tools.colorToString(foregroundColor) + "\r\n");
		if (!backgroundColor.equals(Color.WHITE)) file.write("Color2=" + Tools.colorToString(backgroundColor) + "\r\n");
	}

	private static MindmapNode pollFirst(TreeSet<MindmapNode> tree) {
		MindmapNode result = tree.first();
		tree.remove(result);
		return result;
	}

	public static TreeSet<MindmapNode> saveChangedMindmaps() {
		// TODO Auto-generated method stub
		TreeSet<MindmapNode> result = new TreeSet<MindmapNode>(new ObjectComparator());
		while (!changedMindmaps.isEmpty()) {
			MindmapNode dummy = pollFirst(changedMindmaps);
			if (!dummy.save()) result.add(dummy);
			if (nodeOpenAndChanged(dummy.nodeFile) != null) {
				System.out.println(languagePack.WARNING_CONCURRENT_CHANGES().replaceAll("##", dummy.nodeFile.toString()));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		return result;
	}

	public static boolean existUnsavedMindmaps() {
		return changedMindmaps.size() > 0;
	}

	public boolean hasUnsavedChanges() {
		return (changedMindmaps.contains(this.getRoot()));
	}

	public static void flushUnsavedChanges() {
		changedMindmaps.clear();
	}

	public void addBrother(MindmapNode brother) {
		if (brother==null) return;
		if (parent==null) return;
		
		brother.nextBrother = nextBrother;
		if (nextBrother != null) nextBrother.previousBrother = brother;
		brother.previousBrother = this;
		brother.parent = parent;
		nextBrother = brother;
		if (brother.nextBrother == null)	parent.lastChild = brother;
		parent.numChildren++;
		brother.mindmapChanged();
	}

	public MindmapNode clone() {
		try {
			MindmapNode result = new MindmapNode(this.getFormulaCode(),this.getOrigin());
			result.backgroundColor = new Color(backgroundColor.getRGB());
			result.foregroundColor = new Color(foregroundColor.getRGB());
			result.nodeImage = (nodeImage == null) ? null : nodeImage.clone(); // holds the node's image, if given
			result.link = (link == null) ? null : new URL(link.toString());
			return result;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // holds a given link
		return null;
	}

	public Object getFullInfo() {
		String text = getText();
		int i = 0;
		int l = text.length();
		while (i < l) {
			if (i % 50 == 0) {
				text = text.substring(0, i) + "\n" + text.substring(i + 1);
			}
			i++;
		}
		return "Knoten in Datei:\n" + Tools.shorten(getRoot().nodeFile.toString()) + "\n\nText:\n" + Tools.shorten(getText()) + ((nodeImage == null) ? "" : "\n\nBild:\n" + Tools.shorten(nodeImage.toString())) + ((link == null) ? "" : "\nVerweis:\n" + Tools.shorten(link.toString())) + "\n\nZeichenfarbe: " + foregroundColor.toString() + "\nHintergrundfarbe: " + backgroundColor.toString();

	}

	public Color getForeColor() {
		// TODO Auto-generated method stub
		return foregroundColor;
	}

	public boolean hasBeenLoadedFromFile() {
		return this.nodeFileHasBeenLoaded;
	}

	public MindmapNode reload() throws FileNotFoundException, IOException, DataFormatException, URISyntaxException {
		// TODO Auto-generated method stub
		MindmapNode result = new MindmapNode();
		result.nodeFile = this.nodeFile;
		result.loadFromFile();
		return result;
	}

	public Dimension nodeDimension(Graphics g, ImageObserver obs) {
		return paint(g, obs, false);
	}

	public String getTextWithoutPath() {
		String text=getText();
		if (text.startsWith("file:") || text.startsWith("/"))	{
			text=text.substring(text.lastIndexOf('/')+1);
			text=text.substring(0,text.lastIndexOf('.'));
		}
		
		return text;
	}

	public void setFolded(boolean folded) {
		this.folded = folded;
	}

	public boolean isFolded() {
		return (numChildren>0 && folded) || (nodeFile!=null && !nodeFileHasBeenLoaded);
	}

	public void waitForLoading() {
		System.out.println("Waiting for "+this.getText());
		while (this.nodeFile!=null && !hasBeenLoadedFromFile()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}