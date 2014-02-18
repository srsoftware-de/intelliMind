package de.srsoftware.intellimind;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.DataFormatException;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import de.srsoftware.formula.FormulaInputDialog;
import de.srsoftware.gui.treepanel.NodeImage;
import de.srsoftware.gui.treepanel.RootTreePanel;
import de.srsoftware.gui.treepanel.StarTreePanel;
import de.srsoftware.gui.treepanel.TreeNode;
import de.srsoftware.gui.treepanel.TreePanel;
import de.srsoftware.tools.Configuration;
import de.srsoftware.tools.GenericFileFilter;
import de.srsoftware.tools.HorizontalPanel;
import de.srsoftware.tools.SuggestField;
import de.srsoftware.tools.Tools;
import de.srsoftware.tools.translations.Translations;

public class IntelliMind3 extends JFrame implements ActionListener, WindowListener, KeyListener, ComponentListener {

	private static final long serialVersionUID = -6738627138627936663L;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Translations.getFor(IntelliMind3.class);
		IntelliMind3 intelliMind = new IntelliMind3();
		try {
			if (args!=null && args.length>0){
				if (!args[0].contains(":")) args[0]="file://"+args[0];
				mindmapToOpenAtStart=new URL(args[0]);
			}
			intelliMind.setMindmap(intelliMind.openMindmap(mindmapToOpenAtStart));			
			if (trace!=null) {
				(new Tracer(intelliMind.mindmapPanel,trace)).start();
			}
			/*
			 * / intelliMind.setMindmap(intelliMind.openMindmap(new URL("http://srsoftware.dyndns.info/mindmaps/I/intelliMind3.imf"))); //
			 */
		} catch (FileNotFoundException e) {
			intelliMind.fileNotFound(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	private static String _(String text) { 
		return Translations.get(text);
	}
	private static String _(String key, Object insert) {
		return Translations.get(key, insert);
	}
	private static String version = /* Beim Updaten Versionshistory aktualisieren! */ "0.5.5";
	private String date = "Januar 2014";
	private static String helpFile="http://mindmaps.srsoftware.de/Hilfe zu IntelliMind/hilfe.imf";
	private TreePanel mindmapPanel;
	private KeyStroke CtrlW=KeyStroke.getKeyStroke(KeyEvent.VK_W,2);
	private KeyStroke CtrlMinus=KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,2);
	private KeyStroke CtrlPlus=KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,2);
	private JMenuBar MainMenu;
	private JMenu MindmapMenu;
	private JMenuItem IOpen, INew, IClose, ISaveAs, ISave, ISaveTreeAs, IExport, ISetBGColor, ISetLanguage, IExit;
	private JMenu BearbeitenMenu;
	private JMenuItem INewBrother, INewChild, IMindmapForChild, IChangeText, IInsertImage, IDeleteImage, IInsertLink, IDeleteLink, ICut, ICopy, IPaste, IDelete, IBGColor, IForeColor, IBGCTrace, IFGCTrace;
	private JMenu AnsichtMenu;
	private JMenuItem IFold, ILarger, ISmaller, IIncVertDist, IDecVertDist, IResetView, IFoldAll, IRefresh,IStarTree,IRootedTree;
	private JMenu SuchenMenu;
	private JMenuItem IWikiSearch, IGoogleSearch, IImageSearch, IEbaySearch;
	private JMenu NavigationMenu;
	private JMenuItem IToRoot, IToParent, IToFirstChild, IToLastChild, IToNext, IToPrev, ILoadToRoot;
	private JMenu InfoMenu;
	private JMenuItem IHelp, IInfo, IPreferences, INodeDetails;

	
	private JMenuItem IMindmapForChild2, IInsertImage2, IInsertLink2, IDeleteLink2, ICut2, ICopy2, IPaste2, IDelete2, IBGColor2, IForeColor2;
	private String langConf;
	private Configuration config;

	private static String trace;

	private static URL mindmapToOpenAtStart;
	//private URL lastOpenedFile = null;

	public IntelliMind3() throws IOException {
		super(_("Welcome to IntelliMind #",version));
		config=new Configuration("intelliMind3");
		langConf=config.get("languages");
		setLang(langConf);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		addKeyListener(this);
		createComponents();
		readConfig();
		this.addComponentListener(this);
	}

	private void setLang(String langConf) {	
		if (langConf!=null && !langConf.isEmpty()){
		String[] langs = langConf.split(",");
		for (String lang:langs){
			if (Translations.getFor(IntelliMind3.class,lang)) break;
		}
		super.setTitle(_("Welcome to IntelliMind #",version));
	}
}
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		boolean commandKnown = false;
		if (command.equals("bgColor") && (commandKnown = true)) mindmapPanel.setCurrentBackgroundColor(JColorChooser.showDialog(this, _("select background color"), mindmapPanel.getBackground()));
		if (command.equals("bgColorTrace") && (commandKnown = true)) startStopBackgroundtrace();
		if (command.equals("changeLanguage") && (commandKnown = true)) selectLang();
		if (command.equals("changeText") && (commandKnown = true)) mindmapPanel.editNode();
		if (command.equals("changeBGColor") && (commandKnown = true)) {
			mindmapPanel.setBackground(JColorChooser.showDialog(this, _("select background color"), mindmapPanel.getBackground()));
			changeConfigurationFile();
		}
		if (command.equals("close") && (commandKnown = true) && closeMindmap()) System.exit(0);
		if (command.equals("closeMindmap") && (commandKnown = true)) closeMindmap();
		if (command.equals("copy") && (commandKnown = true)) mindmapPanel.copy();
		if (command.equals("cut") && (commandKnown = true)) mindmapPanel.cut();
		if (command.equals("decVertDist") && (commandKnown = true)) {
				mindmapPanel.decreaseDistance();
				changeConfigurationFile();
		}
		if (command.equals("delete") && (commandKnown = true)) mindmapPanel.deleteActive();
		if (command.equals("deleteImage") && (commandKnown = true)) mindmapPanel.deleteActiveImage();
		if (command.equals("deleteLink") && (commandKnown = true)) mindmapPanel.deleteActiveLink();
		if (command.equals("ebaySearch") && (commandKnown = true)) Tools.execute("\"http://search.ebay.de/search/search.dll?satitle=" + mindmapPanel.currentNode().getTextWithoutPath() + "\"");
		if (command.equals("export") && (commandKnown = true)) doHtmlExport();
		
		if (command.equals("fold") && (commandKnown=true)) mindmapPanel.toogleFold();

		if (command.equals("foreColor") && (commandKnown = true)) mindmapPanel.setCurrentForegroundColor(JColorChooser.showDialog(this, _("select foreground color"), mindmapPanel.getForeground()));
		if (command.equals("foreColorTrace") && (commandKnown = true)) startStopForegroundtrace();
		if (command.equals("googleSearch") && (commandKnown = true)) Tools.execute("\"http://www.google.de/search?q=" + mindmapPanel.currentNode().getTextWithoutPath() + "\"");
		if (command.equals("imageSearch") && (commandKnown = true)) Tools.execute("\"http://images.google.de/images?q=" + mindmapPanel.currentNode().getTextWithoutPath() + "\"");
		if (command.equals("incVertDist") && (commandKnown = true)) {
				mindmapPanel.increaseDistance();
				changeConfigurationFile();
		}
		if (command.equals("InfoWindow") && (commandKnown = true)) JOptionPane.showMessageDialog(this, _("IntelliMind3\nversion #\nby SRSoftware - www.srsoftware.de\nauthor:\nStephan Richter (s.richter@srsoftware.de)\nall rights reserved\n#",new Object[]{version,date}), _("Information"), JOptionPane.INFORMATION_MESSAGE);
		if (command.equals("insertImage") && (commandKnown = true)) mindmapPanel.setImageOfCurrentNode(selectImage());
		if (command.equals("insertLink") && (commandKnown = true)) try {
			mindmapPanel.setLinkOfCurrentNode(openFile());
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (command.equals("LoadHelp") && (commandKnown = true)) loadHelp();
		if (command.equals("LoadToRoot") && (commandKnown = true)) moveCurrentNodeToRoot();
		if (command.equals("mindmapForChild") && (commandKnown = true)) try {
			mindmapPanel.appendNewChild(openMindmap());
			this.requestFocus();
		} catch (FileNotFoundException e1) {
			fileNotFound(e1);
		} catch (DataFormatException e1) {
			fileNotSupported(e1);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		if (command.equals("navigateToRoot") && (commandKnown = true)) mindmapPanel.navigateToRoot();
		if (command.equals("new") && (commandKnown = true)) createNewMindmap();
		if (command.equals("newBrother") && (commandKnown = true)) insertNewBrother();
		if (command.equals("newChild") && (commandKnown = true)) mindmapPanel.appendNewChild(createNewNode(mindmapPanel.tree.getOrigin()));
		if (command.equals("navigateRight") && (commandKnown = true)) mindmapPanel.navigateRight();
		if (command.equals("navigateLeft") && (commandKnown = true)) mindmapPanel.navigateLeft();
		if (command.equals("navigateDown") && (commandKnown = true)) mindmapPanel.navigateDown();
		if (command.equals("navigateToLastChild") && (commandKnown = true)) mindmapPanel.navigateToEnd();
		if (command.equals("navigateUp") && (commandKnown = true)) mindmapPanel.navigateUp();
		if (command.equals("switchToStarTree") && (commandKnown = true)) enableStarTree(true);		
		if (command.equals("switchToRootedTree") && (commandKnown = true)) enableStarTree(false);		
		if (command.equals("paste") && (commandKnown = true)) mindmapPanel.paste();
		if (command.equals("open") && (commandKnown = true)) try {
			setMindmap(openMindmap());
		} catch (FileNotFoundException e1) {
			fileNotFound(e1);
		} catch (DataFormatException e1) {
			fileNotSupported(e1);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		if (command.equals("refreshView") && (commandKnown = true)) mindmapPanel.refreshView();
		if (command.equals("smaller") && (commandKnown = true)) {
			mindmapPanel.setTextSmaller();
			changeConfigurationFile();
		}
		if (command.equals("larger") && (commandKnown = true)) {
			mindmapPanel.setTextLarger();
			changeConfigurationFile();
		}
		if (command.equals("save") && (commandKnown = true)) mindmapPanel.saveNodes();
		if (command.equals("saveAs") && (commandKnown = true)) {
			mindmapPanel.saveRoot();
			changeConfigurationFile();
		}

		if (command.equals("saveTreeAs") && (commandKnown = true)) {
			mindmapPanel.saveCurrentFork();
			this.requestFocus();
		}
		if (command.equals("setDefaultView")) {
			mindmapPanel.setTextSize(18f);
			mindmapPanel.setBackground(new Color(0, 155, 255));
			mindmapPanel.setSize(mindmapPanel.getSize()); // forces distances to be newly calculated
			changeConfigurationFile();
		}
		if (command.equals("wikiSearch")) Tools.execute("\"http://de.wikipedia.org/wiki/Spezial:Search?search=" + mindmapPanel.currentNode().getTextWithoutPath() + "\"");
		if (command.startsWith("SetTitle:") && (commandKnown = true)) setTitle(command.substring(9));
		if (command.equals("NodeDetails") && (commandKnown = true)) mindmapPanel.showNodeDetails();
		if (mindmapPanel.currentNode() != null) {
			IInsertLink.setEnabled(mindmapPanel.currentNode().firstChild() == null);
			IInsertLink2.setEnabled(mindmapPanel.currentNode().firstChild() == null);
			IDeleteLink.setEnabled(mindmapPanel.currentNode().getLink() != null);
			IDeleteLink2.setEnabled(mindmapPanel.currentNode().getLink() != null);
		}
		if (!commandKnown) System.out.println("actionPerformed recieved the following, unimplemented command: " + command);

	}

	private void selectLang() {
		if (langConf==null)	langConf="de,en";
		HorizontalPanel message=new HorizontalPanel();
		JLabel text=new JLabel(_("<html><br>Select the languages you prefer to<br>use (high priority first) separated by commas:"));
		JButton codeButton=new JButton(_("Show me allowed two-letter codes!"));
		codeButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Tools.openWebpage("http://www.mathguide.de/info/tools/languagecode.html");
			}
		});
		message.add(text);
		message.add(codeButton);
		message.skalieren();
		langConf=JOptionPane.showInputDialog(this,message,langConf);
		String oldLang=_("You need to restart the program to apply these settings!");
		setLang(langConf);
		String newLang=_("You need to restart the program to apply these settings!");
		JOptionPane.showMessageDialog(this,"<html>"+oldLang+"<br>"+newLang);
	}
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void componentMoved(ComponentEvent e) {
		changeConfigurationFile();
	}

	public void componentResized(ComponentEvent e) {
		changeConfigurationFile();
	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}	

	public void keyPressed(KeyEvent k) {
		KeyStroke ks = KeyStroke.getKeyStrokeForEvent(k);
		if (ks.equals(CtrlW)) this.actionPerformed(new ActionEvent(this, 0, "closeMindmap"));
		if (ks.equals(CtrlPlus)) this.actionPerformed(new ActionEvent(this, 0, "incVertDist"));
		if (ks.equals(CtrlMinus)) this.actionPerformed(new ActionEvent(this, 0, "decVertDist"));
	}

	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent k) {}

	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent arg0) {}

	public void windowClosing(WindowEvent arg0) {
		try {
			SuggestField.save();
		} catch (IOException e) {
		}
		if (closeMindmap()) System.exit(0);
	}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}
	
	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}

	private int aksForSavingMindmaps() {
		// TODO Auto-generated method stub
		return JOptionPane.showConfirmDialog(this, _("You have unsaved changes in your current mindmap. Shall those be saved?"), _("Error while trying to save"), JOptionPane.YES_NO_CANCEL_OPTION);
	}

	private void changeConfigurationFile() {
		try {
			config.set("mindmap", mindmapPanel.tree.getSuperRoot().nodeFile());
			config.set("trace",getTrace());
		} catch (NullPointerException npe){}
		config.set("languages",langConf);
		config.set("backgroundColor",mindmapPanel.getBackground().getRGB());
		config.set("nodeDistance",mindmapPanel.getDistance()); // die Soll-Distanz zwischen den Knoten speichern
		config.set("textSize",mindmapPanel.getTextSize()); // die Schriftgröße der Knoten speichern
		config.set("windowSize",getSize().width+" "+getSize().height);
		config.set("windowLocation",getLocation().x+" "+getLocation().y);
		config.set("display",(mindmapPanel.getClass().getSimpleName()));
		try {
	    config.save();
    } catch (IOException e) {
	    e.printStackTrace();
    }
	}

	private boolean closeMindmap() {
		if (mindmapPanel.hasUnsavedNodes()) {
			int choice = aksForSavingMindmaps();
			switch (choice) {
			case JOptionPane.YES_OPTION:
				mindmapPanel.saveNodes();
				break;
			case JOptionPane.NO_OPTION:
				mindmapPanel.flushTreeChanges();
				break;
			case JOptionPane.CANCEL_OPTION:
				return false;
			}
		}
		changeConfigurationFile();
		mindmapPanel.setTree(null);
		disableMindmapOptions();
		return true;
	}

	private void createAnsichtMenu() {
		AnsichtMenu = new JMenu(_("View"));
		AnsichtMenu.setMnemonic(KeyEvent.VK_A);

		AnsichtMenu.add(IFold = new JMenuItem(_("collapse/expand"), KeyEvent.VK_Z));
		IFold.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		IFold.addActionListener(this);
		IFold.setActionCommand("fold");

		AnsichtMenu.add(IFoldAll = new JMenuItem(_("expand all subtrees"), KeyEvent.VK_L));
		IFoldAll.addActionListener(this);
		IFoldAll.setActionCommand("foldall");

		AnsichtMenu.addSeparator();

		AnsichtMenu.add(ILarger = new JMenuItem(_("larger"), KeyEvent.VK_G));
		ILarger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
		ILarger.setActionCommand("larger");
		ILarger.addActionListener(this);

		AnsichtMenu.add(ISmaller = new JMenuItem(_("smaller"), KeyEvent.VK_K));
		ISmaller.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
		ISmaller.setActionCommand("smaller");
		ISmaller.addActionListener(this);

		AnsichtMenu.add(IIncVertDist = new JMenuItem(_("increase node distance"), KeyEvent.VK_V));
		IIncVertDist.setActionCommand("incVertDist");
		IIncVertDist.addActionListener(this);

		AnsichtMenu.add(IDecVertDist = new JMenuItem(_("decrease node distance"), KeyEvent.VK_N));
		IDecVertDist.setActionCommand("decVertDist");
		IDecVertDist.addActionListener(this);

		AnsichtMenu.addSeparator();

		AnsichtMenu.add(IResetView = new JMenuItem(_("restore default settings"), KeyEvent.VK_S));
		IResetView.setActionCommand("setDefaultView");
		IResetView.addActionListener(this);

		AnsichtMenu.add(IRefresh = new JMenuItem(_("refresh (F5)"), KeyEvent.VK_F5));
		IRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		IRefresh.setActionCommand("refreshView");
		IRefresh.addActionListener(this);

		AnsichtMenu.addSeparator();

		AnsichtMenu.add(IStarTree = new JMenuItem(_("STAR TREE layout"), KeyEvent.VK_F6));
		IStarTree.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		IStarTree.setActionCommand("switchToStarTree");
		IStarTree.addActionListener(this);
		
		AnsichtMenu.add(IRootedTree = new JMenuItem(_("LYING TREE layout"), KeyEvent.VK_F7));
		IRootedTree.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		IRootedTree.setActionCommand("switchToRootedTree");
		IRootedTree.addActionListener(this);
		
		MainMenu.add(AnsichtMenu);
	}
	private void createBearbeitenMenu() {
		BearbeitenMenu = new JMenu(_("Edit")); // Zweites...
		BearbeitenMenu.setMnemonic(KeyEvent.VK_B);

		BearbeitenMenu.add(INewBrother = new JMenuItem(_("new brother node"), KeyEvent.VK_B));
		INewBrother.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		INewBrother.setActionCommand("newBrother");
		INewBrother.addActionListener(this);

		BearbeitenMenu.add(INewChild = new JMenuItem(_("new child node"), KeyEvent.VK_K));
		INewChild.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		INewChild.setActionCommand("newChild");
		INewChild.addActionListener(this);

		BearbeitenMenu.add(IMindmapForChild = new JMenuItem(_("mindmap for subtree"), KeyEvent.VK_M));
		IMindmapForChild.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 8));
		IMindmapForChild.setActionCommand("mindmapForChild");
		IMindmapForChild.addActionListener(this);

		IMindmapForChild2 = new JMenuItem(_("mindmap for subtree"), KeyEvent.VK_M);
		IMindmapForChild2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 8));
		IMindmapForChild2.setActionCommand("mindmapForChild");
		IMindmapForChild2.addActionListener(this);

		BearbeitenMenu.addSeparator();

		BearbeitenMenu.add(IChangeText = new JMenuItem(_("edit text"), KeyEvent.VK_T));
		IChangeText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		IChangeText.setActionCommand("changeText");
		IChangeText.addActionListener(this);

		BearbeitenMenu.add(IInsertImage = new JMenuItem(_("insert image"), KeyEvent.VK_I));
		IInsertImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
		IInsertImage.setActionCommand("insertImage");
		IInsertImage.addActionListener(this);

		IInsertImage2 = new JMenuItem(_("insert Image"), KeyEvent.VK_I);
		IInsertImage2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
		IInsertImage2.setActionCommand("insertImage");
		IInsertImage2.addActionListener(this);

		BearbeitenMenu.add(IDeleteImage = new JMenuItem(_("delete image"), KeyEvent.VK_D));
		IDeleteImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
		IDeleteImage.setActionCommand("deleteImage");
		IDeleteImage.addActionListener(this);

		BearbeitenMenu.add(IInsertLink = new JMenuItem(_("insert/edit link"), KeyEvent.VK_N));
		IInsertLink.setActionCommand("insertLink");
		IInsertLink.addActionListener(this);

		IInsertLink2 = new JMenuItem(_("insert/edit link"), KeyEvent.VK_N);
		IInsertLink2.setActionCommand("insertLink");
		IInsertLink2.addActionListener(this);

		BearbeitenMenu.add(IDeleteLink = new JMenuItem(_("delete link"), KeyEvent.VK_E));
		IDeleteLink.setActionCommand("deleteLink");
		IDeleteLink.addActionListener(this);

		IDeleteLink2 = new JMenuItem(_("delete link"), KeyEvent.VK_E);
		IDeleteLink2.setActionCommand("deleteLink");
		IDeleteLink2.addActionListener(this);

		BearbeitenMenu.addSeparator();

		BearbeitenMenu.add(ICut = new JMenuItem(_("cut"), KeyEvent.VK_A));
		ICut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 2));
		ICut.setActionCommand("cut");
		ICut.addActionListener(this);

		ICut2 = new JMenuItem(_("cut"), KeyEvent.VK_A);
		ICut2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 2));
		ICut2.setActionCommand("cut");
		ICut2.addActionListener(this);

		BearbeitenMenu.add(ICopy = new JMenuItem(_("copy"), KeyEvent.VK_O));
		ICopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 2));
		ICopy.setActionCommand("copy");
		ICopy.addActionListener(this);

		ICopy2 = new JMenuItem(_("copy"), KeyEvent.VK_O);
		ICopy2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 2));
		ICopy2.setActionCommand("copy");
		ICopy2.addActionListener(this);

		BearbeitenMenu.add(IPaste = new JMenuItem(_("paste"), KeyEvent.VK_F));
		IPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 2));
		IPaste.setActionCommand("paste");
		IPaste.addActionListener(this);
		IPaste2 = new JMenuItem(_("paste"), KeyEvent.VK_F);
		IPaste2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 2));
		IPaste2.setActionCommand("paste");
		IPaste2.addActionListener(this);

		BearbeitenMenu.add(IDelete = new JMenuItem(_("delete"), KeyEvent.VK_L));
		IDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		IDelete.setActionCommand("delete");
		IDelete.addActionListener(this);
		IDelete2 = new JMenuItem(_("delete"), KeyEvent.VK_L);
		IDelete2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		IDelete2.setActionCommand("delete");
		IDelete2.addActionListener(this);

		BearbeitenMenu.addSeparator();

		BearbeitenMenu.add(IBGColor = new JMenuItem(_("node background color"), KeyEvent.VK_H));
		IBGColor.setActionCommand("bgColor");
		IBGColor.addActionListener(this);

		BearbeitenMenu.add(IBGCTrace = new JMenuItem(_("propagate background color"), KeyEvent.VK_V));
		IBGCTrace.setActionCommand("bgColorTrace");
		IBGCTrace.addActionListener(this);

		IBGColor2 = new JMenuItem(_("node background color"), KeyEvent.VK_H);
		IBGColor2.setActionCommand("bgColor");
		IBGColor2.addActionListener(this);

		BearbeitenMenu.add(IForeColor = new JMenuItem(_("text color"), KeyEvent.VK_X));
		IForeColor.setActionCommand("foreColor");
		IForeColor.addActionListener(this);

		IForeColor2 = new JMenuItem(_("text color"), KeyEvent.VK_X);
		IForeColor2.setActionCommand("foreColor");
		IForeColor2.addActionListener(this);

		BearbeitenMenu.add(IFGCTrace = new JMenuItem(_("propagate text color"), KeyEvent.VK_P));
		IFGCTrace.setActionCommand("foreColorTrace");
		IFGCTrace.addActionListener(this);
		
		MainMenu.add(BearbeitenMenu);
	}

	private void createComponents() {
		setPreferredSize(new Dimension(640, 480));
		setSize(getPreferredSize());
		this.getContentPane().setLayout(new BorderLayout());

		createMainMenu();

		/*mindmapPanel = new StarTreePanel(); /*/
		mindmapPanel = new RootTreePanel(); //*/
		mindmapPanel.addActionListener(this);
		add(mindmapPanel);
		this.setVisible(true);
		disableMindmapOptions();
	}

	private void createInfoMenu() {
		InfoMenu = new JMenu("Info");
		InfoMenu.setMnemonic(KeyEvent.VK_I);

		InfoMenu.add(IHelp = new JMenuItem(_("Help"), KeyEvent.VK_H));
		IHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		IHelp.setActionCommand("LoadHelp");
		IHelp.addActionListener(this);

		InfoMenu.add(IInfo = new JMenuItem(_("Info"), KeyEvent.VK_I));
		IInfo.setActionCommand("InfoWindow");
		IInfo.addActionListener(this);

		InfoMenu.add(IPreferences = new JMenuItem(_("Preferences"), KeyEvent.VK_E));
		IPreferences.setEnabled(false);
		IPreferences.addActionListener(this);

		InfoMenu.add(INodeDetails = new JMenuItem(_("Node Details"), KeyEvent.VK_D));
		INodeDetails.setActionCommand("NodeDetails");
		INodeDetails.addActionListener(this);
		
		MainMenu.add(InfoMenu);
	}
	
	private void createMainMenu() {
		MainMenu = new JMenuBar(); // Hauptmenü-Leiste anlegen
		createMindmapMenu();
		createBearbeitenMenu();
		createAnsichtMenu();
		createSuchenMenu();
		createNavigationMenu();
		createInfoMenu();		
		
		setJMenuBar(MainMenu);
	}	

	private void createMindmapMenu() {
		MindmapMenu = new JMenu(_("Mindmap")); // Erstes Menü
		MindmapMenu.setMnemonic(KeyEvent.VK_D);

		MindmapMenu.add(INew = new JMenuItem(_("new Mindmap"), KeyEvent.VK_N));
		INew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 2));
		INew.setActionCommand("new");
		INew.addActionListener(this);

		MindmapMenu.add(IOpen = new JMenuItem(_("open"), KeyEvent.VK_F));
		IOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 2));
		IOpen.setActionCommand("open");
		IOpen.addActionListener(this);

		MindmapMenu.add(IClose = new JMenuItem(_("close"), KeyEvent.VK_C));
		IClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 2));
		IClose.setActionCommand("closeMindmap");
		IClose.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(ISave = new JMenuItem(_("save"), KeyEvent.VK_S));
		ISave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 2));
		ISave.setActionCommand("save");
		ISave.addActionListener(this);

		MindmapMenu.add(ISaveAs = new JMenuItem(_("save as"), KeyEvent.VK_U));
		ISaveAs.setActionCommand("saveAs");
		ISaveAs.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(ISaveTreeAs = new JMenuItem(_("save subtree as..."), KeyEvent.VK_T));
		ISaveTreeAs.setActionCommand("saveTreeAs");
		ISaveTreeAs.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(IExport = new JMenuItem(_("export to HTML"), KeyEvent.VK_E));
		IExport.setActionCommand("export");
		IExport.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(ISetBGColor = new JMenuItem(_("change background color"), KeyEvent.VK_S));
		ISetBGColor.setActionCommand("changeBGColor");
		ISetBGColor.addActionListener(this);

		MindmapMenu.add(ISetLanguage = new JMenuItem(_("change language"), KeyEvent.VK_S));
		ISetLanguage.setActionCommand("changeLanguage");
		ISetLanguage.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(IExit = new JMenuItem(_("exit"), KeyEvent.VK_B));
		IExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 8));
		IExit.setActionCommand("close");
		IExit.addActionListener(this);
		
		MainMenu.add(MindmapMenu);
	}

	private void createNavigationMenu() {
		NavigationMenu = new JMenu(_("Navigation"));
		NavigationMenu.setMnemonic(KeyEvent.VK_N);

		NavigationMenu.add(IToRoot = new JMenuItem(_("to root"), KeyEvent.VK_W));
		IToRoot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		IToRoot.setActionCommand("navigateToRoot");
		IToRoot.addActionListener(this);

		NavigationMenu.add(IToParent = new JMenuItem(_("to parent node"), KeyEvent.VK_U));
		IToParent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		IToParent.setActionCommand("navigateLeft");
		IToParent.addActionListener(this);

		NavigationMenu.addSeparator();

		NavigationMenu.add(IToFirstChild = new JMenuItem(_("to first child node"), KeyEvent.VK_U));
		IToFirstChild.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		IToFirstChild.setActionCommand("navigateRight");
		IToFirstChild.addActionListener(this);

		NavigationMenu.add(IToLastChild = new JMenuItem(_("to last child node"), KeyEvent.VK_L));
		IToLastChild.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
		IToLastChild.setActionCommand("navigateToLastChild");
		IToLastChild.addActionListener(this);

		NavigationMenu.addSeparator();

		NavigationMenu.add(IToNext = new JMenuItem(_("to next node"), KeyEvent.VK_N));
		IToNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		IToNext.setActionCommand("navigateDown");
		IToNext.addActionListener(this);

		NavigationMenu.add(IToPrev = new JMenuItem(_("to previous node"), KeyEvent.VK_V));
		IToPrev.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
		IToPrev.setActionCommand("navigateUp");
		IToPrev.addActionListener(this);

		NavigationMenu.addSeparator();

		NavigationMenu.add(ILoadToRoot = new JMenuItem(_("set current subtree to root"), KeyEvent.VK_A));
		ILoadToRoot.setAccelerator(KeyStroke.getKeyStroke(36, 0));
		ILoadToRoot.setActionCommand("LoadToRoot");
		ILoadToRoot.addActionListener(this);
		
		MainMenu.add(NavigationMenu);
	}

	private void createNewMindmap() {
		TreeNode newRoot = createNewNode(null);
		if (newRoot != null && closeMindmap()) {
			setMindmap(newRoot);
			setTitle(_("new Mindmap") + "*");
			newRoot.treeChanged();
			enableMindmapOptions();
		}
	}

	private TreeNode createNewNode(Point point) {
		// TODO Auto-generated method stub
		// System.out.println("createNewNode");
		String text = FormulaInputDialog.readInput(this, _("new mindmap node"), null);
		this.requestFocus();
		if (text == null) return null;
		return new TreeNode(text,point);
	}

	private void createSuchenMenu() {
		SuchenMenu = new JMenu(_("Search"));
		SuchenMenu.setMnemonic(KeyEvent.VK_S);

		SuchenMenu.add(IWikiSearch = new JMenuItem(_("on Wikipedia"), KeyEvent.VK_W));
		IWikiSearch.setActionCommand("wikiSearch");
		IWikiSearch.addActionListener(this);

		SuchenMenu.add(IGoogleSearch = new JMenuItem(_("on Google"), KeyEvent.VK_G));
		IGoogleSearch.setActionCommand("googleSearch");
		IGoogleSearch.addActionListener(this);

		SuchenMenu.add(IImageSearch = new JMenuItem(_("on Google Images"), KeyEvent.VK_B));
		IImageSearch.setActionCommand("imageSearch");
		IImageSearch.addActionListener(this);

		SuchenMenu.add(IEbaySearch = new JMenuItem(_("on eBay"), KeyEvent.VK_E));
		IEbaySearch.setActionCommand("ebaySearch");
		IEbaySearch.addActionListener(this);
		
		MainMenu.add(SuchenMenu);
	}

	private void disableMindmapOptions() {
		ISave.setEnabled(false);
		IClose.setEnabled(false);
		ISaveAs.setEnabled(false);
		IExport.setEnabled(false);
		IDeleteLink.setEnabled(false);
		IDeleteLink2.setEnabled(false);
		ISaveTreeAs.setEnabled(false);
		BearbeitenMenu.setEnabled(false);
		AnsichtMenu.setEnabled(false);
		SuchenMenu.setEnabled(false);
		NavigationMenu.setEnabled(false);
	}

	private void doHtmlExport() {
		HtmlExportDialog exportDialog = new HtmlExportDialog(this, _("export to HTML"), true);
		exportDialog.setVisible(true);
		if (exportDialog.notCancelled()) {
			try {
				mindmapPanel.startHtmlExport(exportDialog.fileName(), exportDialog.onlyCurrent(), exportDialog.maxDepth(), exportDialog.interactive(), exportDialog.singleFile(), exportDialog.noMultipleFollow());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DataFormatException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	private void enableMindmapOptions() {
		//IFold.setEnabled(false);
		IFoldAll.setEnabled(false);
		
		ISave.setEnabled(true);
		IClose.setEnabled(true);
		ISaveAs.setEnabled(true);
		IExport.setEnabled(true);
		// IInsertLink.setEnabled(true);
		// IInsertLink2.setEnabled(true);
		// IDeleteLink.setEnabled(true);
		// IDeleteLink2.setEnabled(true);
		ISaveTreeAs.setEnabled(true);
		BearbeitenMenu.setEnabled(true);
		AnsichtMenu.setEnabled(true);
		SuchenMenu.setEnabled(true);
		NavigationMenu.setEnabled(true);
	}
	
 
	private void enableStarTree(boolean enable) {
		remove(mindmapPanel);
		mindmapPanel.stopOrganizing();
		Dimension size=mindmapPanel.getSize();		
		if (enable)	mindmapPanel = new StarTreePanel(mindmapPanel);
		else mindmapPanel = new RootTreePanel(mindmapPanel);
		mindmapPanel.setSize(size);
		mindmapPanel.addActionListener(this);
		add(mindmapPanel);
		//pack();
	}

	private void fileNotFound(FileNotFoundException e) {
		JOptionPane.showMessageDialog(this, _("file (#) could not be found.",e.toString().split(" ")[1]), _("Warning"), JOptionPane.OK_OPTION);
	}

	private void fileNotSupported(DataFormatException e) {
		JOptionPane.showMessageDialog(this, _("files of this type (#) can currently not be opened.",e.toString().split(" ")[1]), _("Warning"), JOptionPane.OK_OPTION);
	}

	private String getTrace() {
		TreeNode node = mindmapPanel.currentNode();
		StringBuffer trace=new StringBuffer();
		while (node.parent()!=null){
			while (node.prev()!=null){
				node=node.prev();
				trace.insert(0, 'D');
			}
			node=node.parent();
			trace.insert(0, "R");
		}
		return trace.toString();
	}

	private void insertNewBrother() {
		// TODO Auto-generated method stub
		TreeNode dummy = mindmapPanel.currentNode();
		if (dummy.parent() != null) {
			mindmapPanel.appendNewBrother(createNewNode(mindmapPanel.tree.getOrigin()));
		} else {
			JOptionPane.showMessageDialog(this, _("a mindmap's root must not have a brother"), _("Warning"), JOptionPane.OK_OPTION);
		}
	}

	private void loadHelp() {
		boolean online = true;
		if (online) {
			TreeNode dummy = mindmapPanel.currentNode();
			URL url = null;
			try {
				url = new URL(helpFile);
			} catch (MalformedURLException e) {
				JOptionPane.showMessageDialog(this, _("Sorry, currently no help is available here."), "Warning", JOptionPane.OK_OPTION);
			}
			try {
				if (dummy != null) {
					mindmapPanel.appendNewChild(openMindmap(url));
					mindmapPanel.navigateToEnd();
				} else {
					setMindmap(openMindmap(url));
				}
			} catch (FileNotFoundException e) {
				fileNotFound(e);
			} catch (DataFormatException e) {
				fileNotSupported(e);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	private void readConfig() {
		try {
			String mindmap=config.get("mindmap");
			if (mindmap!=null) mindmapToOpenAtStart=new URL(mindmap);
			String bgColor = config.get("backgroundColor");
			if (bgColor != null) mindmapPanel.setBackground(new Color(Integer.parseInt(bgColor)));
			String nodeDistance=config.get("nodeDistance");
			if (nodeDistance!=null && mindmapPanel instanceof StarTreePanel) ((StarTreePanel) mindmapPanel).setDistance(Integer.parseInt(nodeDistance));
			String textSize=config.get("textSize");
			if (textSize!=null) mindmapPanel.setTextSize(Float.parseFloat(textSize));
			trace=config.get("trace");
			String windowSize = config.get("windowSize");
			if (windowSize != null) {
				int h=windowSize.indexOf(' ');
				int w=Integer.parseInt(windowSize.substring(0,h));
				h=Integer.parseInt(windowSize.substring(h+1));
				setSize(new Dimension(w,h));
			}
			String location=config.get("windowLocation");
			if (location!=null){
				int y=location.indexOf(' ');
				int x=Integer.parseInt(location.substring(0,y));
				y=Integer.parseInt(location.substring(y+1));
				setLocation(x, y);
			}
			String display=config.get("display");
			if (display!=null) {
				if (display.equals("StarTreePanel")) enableStarTree(true);
			}
		} catch (FileNotFoundException e) {
			try {
				mindmapToOpenAtStart = new URL(helpFile);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			mindmapPanel.setBackground(new Color(0, 155, 255));
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void moveCurrentNodeToRoot() {
		try {
			setMindmap(openMindmap(mindmapPanel.currentNode().getRoot().nodeFile()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private URL openFile() throws FileNotFoundException {
		String filename=null;
		URL fileUrl = Tools.showSelectFileDialog(_("open mindmap"), filename, null, this);
		if (fileUrl == null) fileUrl = Tools.showUrlInputDialog(this, _("Choose link target manually:"));
		//lastOpenedFile = fileUrl;
		return fileUrl;
	}

	private TreeNode openMindmap() throws FileNotFoundException, DataFormatException, URISyntaxException {
		//String filename = (lastOpenedFile == null) ? null : lastOpenedFile.toString();
		URL fileUrl = Tools.showSelectFileDialog(_("open mindmap..."), null, new GenericFileFilter(_("mindmap file"), ".imf;.mm"), this);
		if (fileUrl == null) fileUrl = Tools.showUrlInputDialog(this, _("Choose link target manually:"));
		//lastOpenedFile = fileUrl;
		return openMindmap(fileUrl);
	}
	

	private TreeNode openMindmap(URL fileUrl) throws FileNotFoundException, DataFormatException, URISyntaxException {
		if (fileUrl == null) return null; // wenn keine URl angegeben ist: abbrechen
		URL urlPlusExtension = null;
		try {
			urlPlusExtension = new URL(fileUrl.toString() + ".imf"); // alternative Url mit Standardendung erzeugen
		} catch (MalformedURLException e) {	} // muss nicht gefangen werden: aus einer validen URL kann durch anhängen von ".imf" keine ungültige werden

		if (Tools.fileIsLocal(fileUrl) && !Tools.fileExists(fileUrl)) { // wenn der Pfad ein lokaler ist und auf eine nicht existierde Datei zeigt:
			if (Tools.fileExists(urlPlusExtension)) { // testen, ob die Datei mit zusätzlicher Endung .imf existiert
				fileUrl = urlPlusExtension; // wenn ja: diese nutzen
			} else { // wenn keine der beiden Dateinamen eine existierende datei bezeichnet: Fragen, ob Datei erzeugt werden soll
				File f = new File(fileUrl.getPath());
				String name = f.getName();
				String path = f.getParent();
				String[] names = { name, name + ".imf", name + ".mm" };
				System.out.print(_("searching for #",name));
				URL searchedFile = Tools.searchFiles(names, path);
				if (searchedFile != null) {
					fileUrl = searchedFile;
				} else {
					if (!fileUrl.toString().toLowerCase().endsWith(".mm") && !fileUrl.toString().toLowerCase().endsWith(".imf")) fileUrl = urlPlusExtension;
					if (!requestFileCreation(fileUrl)) return null;
				}
			}
		}

		TreeNode result = new TreeNode();
		try {
			result.loadFromFile(fileUrl);
			this.setTitle(fileUrl.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private boolean requestFileCreation(URL fileUrl) {
		if (JOptionPane.showConfirmDialog(this, _("File (#) could not be found. Shall this file be created?",fileUrl)) == 0) {
			StringBuffer formula=new StringBuffer("\\small{"+fileUrl.getFile()+"}");
			formula.insert(formula.lastIndexOf("/")+1, "}\\bold{");
			formula.insert(formula.lastIndexOf("."), "}\\small{");
			TreeNode newMindmap = new TreeNode(formula.toString());
			newMindmap.saveTo(fileUrl);
			return true;
		}
		return false;
	}

	private NodeImage selectImage() {
		URL u = Tools.showSelectFileDialog(_("open image..."), null, new GenericFileFilter(_("image file"), "*.jpg;*.jpeg;*.gif;*.png"), this);
		this.requestFocus();
		return (u == null) ? null : new NodeImage(u);
	}

	private void setMindmap(TreeNode mindmapNode) {
		if (mindmapNode != null && closeMindmap()) {
			mindmapPanel.setTree(mindmapNode);
			changeConfigurationFile();
			enableMindmapOptions();
		}
	}

	private void startStopBackgroundtrace() {
		// TODO Auto-generated method stub
		if (mindmapPanel.traceBGColor()) {
			IBGCTrace.setText("x " + _("propagate background color"));
		} else {
			IBGCTrace.setText(_("propagate background color"));
		}
	}

	private void startStopForegroundtrace() {
		// TODO Auto-generated method stub
		if (mindmapPanel.traceForeColor()) {
			IFGCTrace.setText("x " + _("propagate text color"));
		} else {
			IFGCTrace.setText(_("propagate text color"));
		}
	}


}
