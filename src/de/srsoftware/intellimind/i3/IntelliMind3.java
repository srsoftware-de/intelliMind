package de.srsoftware.intellimind.i3;

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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.DataFormatException;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import de.srsoftware.formula.FormulaInputDialog;
import de.srsoftware.tools.GenericFileFilter;
import de.srsoftware.tools.SuggestField;
import de.srsoftware.tools.Tools;

public class IntelliMind3 extends JFrame implements ActionListener, WindowListener, KeyListener, ComponentListener {

	private static final long serialVersionUID = -6738627138627936663L;
	private String version = "0.5.1";
	private String date = "August 2013";
	private static String helpFile="http://mindmaps.srsoftware.de/Hilfe zu IntelliMind/hilfe.imf";
	private TreePanel mindmapPanel;
	private KeyStroke CtrlW=KeyStroke.getKeyStroke(KeyEvent.VK_W,2);
	private KeyStroke CtrlMinus=KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,2);
	private KeyStroke CtrlPlus=KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,2);
	private JMenuBar MainMenu;
	private JMenu MindmapMenu;
	private JMenuItem IOpen, INew, IClose, ISaveAs, ISave, ISaveTreeAs, IExport, ISetBGColor, IExit;
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
	private JMenuItem IMindmapForChild2, IInsertImage2, IDeleteImage2, IInsertLink2, IDeleteLink2, ICut2, ICopy2, IPaste2, IDelete2, IBGColor2, IForeColor2;
	private static String trace;
	private static URL mindmapToOpenAtStart;
	private static MindmapLanguagePack languagePack=null;
	//private URL lastOpenedFile = null;

	
	public IntelliMind3() {
		this("");

		this.addComponentListener(this);
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

	private void createInfoMenu() {
		InfoMenu = new JMenu("Info");
		InfoMenu.setMnemonic(KeyEvent.VK_I);

		InfoMenu.add(IHelp = new JMenuItem(languagePack.HELP(), KeyEvent.VK_H));
		IHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		IHelp.setActionCommand("LoadHelp");
		IHelp.addActionListener(this);

		InfoMenu.add(IInfo = new JMenuItem(languagePack.INFO(), KeyEvent.VK_I));
		IInfo.setActionCommand("InfoWindow");
		IInfo.addActionListener(this);

		InfoMenu.add(IPreferences = new JMenuItem(languagePack.PREFERENCES(), KeyEvent.VK_E));
		IPreferences.addActionListener(this);

		InfoMenu.add(INodeDetails = new JMenuItem(languagePack.NODE_DETAILS(), KeyEvent.VK_D));
		INodeDetails.setActionCommand("NodeDetails");
		INodeDetails.addActionListener(this);
		
		MainMenu.add(InfoMenu);
	}

	private void createNavigationMenu() {
		NavigationMenu = new JMenu(languagePack.NAVIGATION());
		NavigationMenu.setMnemonic(KeyEvent.VK_N);

		NavigationMenu.add(IToRoot = new JMenuItem(languagePack.TO_ROOT(), KeyEvent.VK_W));
		IToRoot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		IToRoot.setActionCommand("navigateToRoot");
		IToRoot.addActionListener(this);

		NavigationMenu.add(IToParent = new JMenuItem(languagePack.TO_PARENT(), KeyEvent.VK_U));
		IToParent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		IToParent.setActionCommand("navigateLeft");
		IToParent.addActionListener(this);

		NavigationMenu.addSeparator();

		NavigationMenu.add(IToFirstChild = new JMenuItem(languagePack.TO_FIRST_CHILD(), KeyEvent.VK_U));
		IToFirstChild.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		IToFirstChild.setActionCommand("navigateRight");
		IToFirstChild.addActionListener(this);

		NavigationMenu.add(IToLastChild = new JMenuItem(languagePack.TO_LAST_CHILD(), KeyEvent.VK_L));
		IToLastChild.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
		IToLastChild.setActionCommand("navigateToLastChild");
		IToLastChild.addActionListener(this);

		NavigationMenu.addSeparator();

		NavigationMenu.add(IToNext = new JMenuItem(languagePack.NEXT(), KeyEvent.VK_N));
		IToNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		IToNext.setActionCommand("navigateDown");
		IToNext.addActionListener(this);

		NavigationMenu.add(IToPrev = new JMenuItem(languagePack.PREVIOUS(), KeyEvent.VK_V));
		IToPrev.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
		IToPrev.setActionCommand("navigateUp");
		IToPrev.addActionListener(this);

		NavigationMenu.addSeparator();

		NavigationMenu.add(ILoadToRoot = new JMenuItem(languagePack.CURRENT_SUBTREE_TO_ROOT(), KeyEvent.VK_A));
		ILoadToRoot.setAccelerator(KeyStroke.getKeyStroke(36, 0));
		ILoadToRoot.setActionCommand("LoadToRoot");
		ILoadToRoot.addActionListener(this);
		
		MainMenu.add(NavigationMenu);
	}

	private void createSuchenMenu() {
		SuchenMenu = new JMenu(languagePack.SEARCH());
		SuchenMenu.setMnemonic(KeyEvent.VK_S);

		SuchenMenu.add(IWikiSearch = new JMenuItem(languagePack.WIKI_SEARCH(), KeyEvent.VK_W));
		IWikiSearch.setActionCommand("wikiSearch");
		IWikiSearch.addActionListener(this);

		SuchenMenu.add(IGoogleSearch = new JMenuItem(languagePack.GOOGLE_SEARCH(), KeyEvent.VK_G));
		IGoogleSearch.setActionCommand("googleSearch");
		IGoogleSearch.addActionListener(this);

		SuchenMenu.add(IImageSearch = new JMenuItem(languagePack.GOOGLE_IMAGE_SEARCH(), KeyEvent.VK_B));
		IImageSearch.setActionCommand("imageSearch");
		IImageSearch.addActionListener(this);

		SuchenMenu.add(IEbaySearch = new JMenuItem(languagePack.EBAY_SEARCH(), KeyEvent.VK_E));
		IEbaySearch.setActionCommand("ebaySearch");
		IEbaySearch.addActionListener(this);
		
		MainMenu.add(SuchenMenu);
	}

	private void createAnsichtMenu() {
		AnsichtMenu = new JMenu(languagePack.VIEW());
		AnsichtMenu.setMnemonic(KeyEvent.VK_A);

		AnsichtMenu.add(IFold = new JMenuItem(languagePack.FOLD(), KeyEvent.VK_Z));
		IFold.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		IFold.addActionListener(this);
		IFold.setActionCommand("fold");

		AnsichtMenu.add(IFoldAll = new JMenuItem(languagePack.UNFOLD_RECURSIVE(), KeyEvent.VK_L));
		IFoldAll.addActionListener(this);
		IFoldAll.setActionCommand("foldall");

		AnsichtMenu.addSeparator();

		AnsichtMenu.add(ILarger = new JMenuItem(languagePack.LARGER(), KeyEvent.VK_G));
		ILarger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
		ILarger.setActionCommand("larger");
		ILarger.addActionListener(this);

		AnsichtMenu.add(ISmaller = new JMenuItem(languagePack.SMALLER(), KeyEvent.VK_K));
		ISmaller.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
		ISmaller.setActionCommand("smaller");
		ISmaller.addActionListener(this);

		AnsichtMenu.add(IIncVertDist = new JMenuItem(languagePack.INCREASE_DISTANCE(), KeyEvent.VK_V));
		IIncVertDist.setActionCommand("incVertDist");
		IIncVertDist.addActionListener(this);

		AnsichtMenu.add(IDecVertDist = new JMenuItem(languagePack.DECREASE_DISTANCE(), KeyEvent.VK_N));
		IDecVertDist.setActionCommand("decVertDist");
		IDecVertDist.addActionListener(this);

		AnsichtMenu.addSeparator();

		AnsichtMenu.add(IResetView = new JMenuItem(languagePack.RESTORE_DEFAULTS(), KeyEvent.VK_S));
		IResetView.setActionCommand("setDefaultView");
		IResetView.addActionListener(this);

		AnsichtMenu.add(IRefresh = new JMenuItem(languagePack.REFRESH(), KeyEvent.VK_F5));
		IRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		IRefresh.setActionCommand("refreshView");
		IRefresh.addActionListener(this);

		AnsichtMenu.addSeparator();

		AnsichtMenu.add(IStarTree = new JMenuItem(languagePack.STARTREE(), KeyEvent.VK_F6));
		IStarTree.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		IStarTree.setActionCommand("switchToStarTree");
		IStarTree.addActionListener(this);
		
		AnsichtMenu.add(IRootedTree = new JMenuItem(languagePack.ROOTEDTREE(), KeyEvent.VK_F7));
		IRootedTree.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		IRootedTree.setActionCommand("switchToRootedTree");
		IRootedTree.addActionListener(this);
		
		MainMenu.add(AnsichtMenu);
	}

	private void createBearbeitenMenu() {
		BearbeitenMenu = new JMenu(languagePack.EDIT()); // Zweites...
		BearbeitenMenu.setMnemonic(KeyEvent.VK_B);

		BearbeitenMenu.add(INewBrother = new JMenuItem(languagePack.NEW_BROTHER(), KeyEvent.VK_B));
		INewBrother.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		INewBrother.setActionCommand("newBrother");
		INewBrother.addActionListener(this);

		BearbeitenMenu.add(INewChild = new JMenuItem(languagePack.NEW_CHILD(), KeyEvent.VK_K));
		INewChild.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		INewChild.setActionCommand("newChild");
		INewChild.addActionListener(this);

		BearbeitenMenu.add(IMindmapForChild = new JMenuItem(languagePack.MINDMAP_FOR_SUBTREE(), KeyEvent.VK_M));
		IMindmapForChild.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 8));
		IMindmapForChild.setActionCommand("mindmapForChild");
		IMindmapForChild.addActionListener(this);

		IMindmapForChild2 = new JMenuItem(languagePack.MINDMAP_FOR_SUBTREE(), KeyEvent.VK_M);
		IMindmapForChild2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 8));
		IMindmapForChild2.setActionCommand("mindmapForChild");
		IMindmapForChild2.addActionListener(this);

		BearbeitenMenu.addSeparator();

		BearbeitenMenu.add(IChangeText = new JMenuItem(languagePack.EDIT_TEXT(), KeyEvent.VK_T));
		IChangeText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		IChangeText.setActionCommand("changeText");
		IChangeText.addActionListener(this);

		BearbeitenMenu.add(IInsertImage = new JMenuItem(languagePack.INSERT_IMAGE(), KeyEvent.VK_I));
		IInsertImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
		IInsertImage.setActionCommand("insertImage");
		IInsertImage.addActionListener(this);

		IInsertImage2 = new JMenuItem(languagePack.INSERT_IMAGE(), KeyEvent.VK_I);
		IInsertImage2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
		IInsertImage2.setActionCommand("insertImage");
		IInsertImage2.addActionListener(this);

		BearbeitenMenu.add(IDeleteImage = new JMenuItem(languagePack.DELETE_IMAGE(), KeyEvent.VK_D));
		IDeleteImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
		IDeleteImage.setActionCommand("deleteImage");
		IDeleteImage.addActionListener(this);

		IDeleteImage2 = new JMenuItem(languagePack.DELETE_IMAGE(), KeyEvent.VK_D);
		IDeleteImage2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
		IDeleteImage2.setActionCommand("deleteImage");
		IDeleteImage2.addActionListener(this);

		BearbeitenMenu.add(IInsertLink = new JMenuItem(languagePack.INSERT_LINK(), KeyEvent.VK_N));
		IInsertLink.setActionCommand("insertLink");
		IInsertLink.addActionListener(this);

		IInsertLink2 = new JMenuItem(languagePack.INSERT_LINK(), KeyEvent.VK_N);
		IInsertLink2.setActionCommand("insertLink");
		IInsertLink2.addActionListener(this);

		BearbeitenMenu.add(IDeleteLink = new JMenuItem(languagePack.DELETE_LINK(), KeyEvent.VK_E));
		IDeleteLink.setActionCommand("deleteLink");
		IDeleteLink.addActionListener(this);

		IDeleteLink2 = new JMenuItem(languagePack.DELETE_LINK(), KeyEvent.VK_E);
		IDeleteLink2.setActionCommand("deleteLink");
		IDeleteLink2.addActionListener(this);

		BearbeitenMenu.addSeparator();

		BearbeitenMenu.add(ICut = new JMenuItem(languagePack.CUT(), KeyEvent.VK_A));
		ICut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 2));
		ICut.setActionCommand("cut");
		ICut.addActionListener(this);

		ICut2 = new JMenuItem(languagePack.CUT(), KeyEvent.VK_A);
		ICut2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 2));
		ICut2.setActionCommand("cut");
		ICut2.addActionListener(this);

		BearbeitenMenu.add(ICopy = new JMenuItem(languagePack.COPY(), KeyEvent.VK_O));
		ICopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 2));
		ICopy.setActionCommand("copy");
		ICopy.addActionListener(this);

		ICopy2 = new JMenuItem(languagePack.COPY(), KeyEvent.VK_O);
		ICopy2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 2));
		ICopy2.setActionCommand("copy");
		ICopy2.addActionListener(this);

		BearbeitenMenu.add(IPaste = new JMenuItem(languagePack.INSERT(), KeyEvent.VK_F));
		IPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 2));
		IPaste.setActionCommand("paste");
		IPaste.addActionListener(this);
		IPaste2 = new JMenuItem(languagePack.INSERT(), KeyEvent.VK_F);
		IPaste2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 2));
		IPaste2.setActionCommand("paste");
		IPaste2.addActionListener(this);

		BearbeitenMenu.add(IDelete = new JMenuItem(languagePack.DELETE(), KeyEvent.VK_L));
		IDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		IDelete.setActionCommand("delete");
		IDelete.addActionListener(this);
		IDelete2 = new JMenuItem(languagePack.DELETE(), KeyEvent.VK_L);
		IDelete2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		IDelete2.setActionCommand("delete");
		IDelete2.addActionListener(this);

		BearbeitenMenu.addSeparator();

		BearbeitenMenu.add(IBGColor = new JMenuItem(languagePack.BACKGROUND_COLOR(), KeyEvent.VK_H));
		IBGColor.setActionCommand("bgColor");
		IBGColor.addActionListener(this);

		BearbeitenMenu.add(IBGCTrace = new JMenuItem(languagePack.BACKGROUND_COLOR_FOLLOW(), KeyEvent.VK_V));
		IBGCTrace.setActionCommand("bgColorTrace");
		IBGCTrace.addActionListener(this);

		IBGColor2 = new JMenuItem(languagePack.BACKGROUND_COLOR(), KeyEvent.VK_H);
		IBGColor2.setActionCommand("bgColor");
		IBGColor2.addActionListener(this);

		BearbeitenMenu.add(IForeColor = new JMenuItem(languagePack.TEXT_COLOR(), KeyEvent.VK_X));
		IForeColor.setActionCommand("foreColor");
		IForeColor.addActionListener(this);

		IForeColor2 = new JMenuItem(languagePack.TEXT_COLOR(), KeyEvent.VK_X);
		IForeColor2.setActionCommand("foreColor");
		IForeColor2.addActionListener(this);

		BearbeitenMenu.add(IFGCTrace = new JMenuItem(languagePack.TEXT_COLOR_FOLLOW(), KeyEvent.VK_P));
		IFGCTrace.setActionCommand("foreColorTrace");
		IFGCTrace.addActionListener(this);
		
		MainMenu.add(BearbeitenMenu);
	}

	private void createMindmapMenu() {
		MindmapMenu = new JMenu(languagePack.MINDMAP()); // Erstes Menü
		MindmapMenu.setMnemonic(KeyEvent.VK_D);

		MindmapMenu.add(INew = new JMenuItem(languagePack.NEW_MINDMAP(), KeyEvent.VK_N));
		INew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 2));
		INew.setActionCommand("new");
		INew.addActionListener(this);

		MindmapMenu.add(IOpen = new JMenuItem(languagePack.OPEN(), KeyEvent.VK_F));
		IOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 2));
		IOpen.setActionCommand("open");
		IOpen.addActionListener(this);

		MindmapMenu.add(IClose = new JMenuItem(languagePack.CLOSE(), KeyEvent.VK_C));
		IClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 2));
		IClose.setActionCommand("closeMindmap");
		IClose.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(ISave = new JMenuItem(languagePack.SAVE(), KeyEvent.VK_S));
		ISave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 2));
		ISave.setActionCommand("save");
		ISave.addActionListener(this);

		MindmapMenu.add(ISaveAs = new JMenuItem(languagePack.SAVE_AS(), KeyEvent.VK_U));
		ISaveAs.setActionCommand("saveAs");
		ISaveAs.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(ISaveTreeAs = new JMenuItem(languagePack.SAVE_SUBTREE(), KeyEvent.VK_T));
		ISaveTreeAs.setActionCommand("saveTreeAs");
		ISaveTreeAs.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(IExport = new JMenuItem(languagePack.EXPORT_TO_HTML(), KeyEvent.VK_E));
		IExport.setActionCommand("export");
		IExport.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(ISetBGColor = new JMenuItem(languagePack.CHANGE_BACKGROUND(), KeyEvent.VK_S));
		ISetBGColor.setActionCommand("changeBGColor");
		ISetBGColor.addActionListener(this);

		MindmapMenu.addSeparator();

		MindmapMenu.add(IExit = new JMenuItem(languagePack.EXIT(), KeyEvent.VK_B));
		IExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 8));
		IExit.setActionCommand("close");
		IExit.addActionListener(this);
		
		MainMenu.add(MindmapMenu);
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

	private void enableMindmapOptions() {
		IFold.setEnabled(false);
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

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		boolean commandKnown = false;
		if (command.equals("bgColor") && (commandKnown = true)) mindmapPanel.setCurrentBackgroundColor(JColorChooser.showDialog(this, languagePack.SELECT_BACKGROUND_COLOR(), mindmapPanel.getBackground()));
		if (command.equals("bgColorTrace") && (commandKnown = true)) startStopBackgroundtrace();
		if (command.equals("changeText") && (commandKnown = true)) mindmapPanel.editMindmap();
		if (command.equals("changeBGColor") && (commandKnown = true)) {
			mindmapPanel.setBackground(JColorChooser.showDialog(this, languagePack.SELECT_BACKGROUND_COLOR(), mindmapPanel.getBackground()));
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
		if (command.equals("ebaySearch") && (commandKnown = true)) Tools.execute("\"http://search.ebay.de/search/search.dll?satitle=" + mindmapPanel.currentMindmap().getTextWithoutPath() + "\"");
		if (command.equals("export") && (commandKnown = true)) doHtmlExport();

		if (command.equals("foreColor") && (commandKnown = true)) mindmapPanel.setCurrentForegroundColor(JColorChooser.showDialog(this, languagePack.SELECT_FOREGROUND_COLOR(), mindmapPanel.getForeground()));
		if (command.equals("foreColorTrace") && (commandKnown = true)) startStopForegroundtrace();
		if (command.equals("googleSearch") && (commandKnown = true)) Tools.execute("\"http://www.google.de/search?q=" + mindmapPanel.currentMindmap().getTextWithoutPath() + "\"");
		if (command.equals("imageSearch") && (commandKnown = true)) Tools.execute("\"http://images.google.de/images?q=" + mindmapPanel.currentMindmap().getTextWithoutPath() + "\"");
		if (command.equals("incVertDist") && (commandKnown = true)) {
				mindmapPanel.increaseDistance();
				changeConfigurationFile();
		}
		if (command.equals("InfoWindow") && (commandKnown = true)) JOptionPane.showMessageDialog(this, "IntelliMind3\nversion " + version + "\nvon SRSoftware - www.srsoftware.de\nauthor:\n Stephan Richter (srichter@srsoftware.de)\nall rights reserved\n" + date, "Information", JOptionPane.INFORMATION_MESSAGE);
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
		if (command.equals("newChild") && (commandKnown = true)) mindmapPanel.appendNewChild(createNewNode(mindmapPanel.mindmap.getOrigin()));
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
		if (command.equals("save") && (commandKnown = true)) mindmapPanel.saveMindmaps();
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
		if (command.equals("wikiSearch")) Tools.execute("\"http://de.wikipedia.org/wiki/Spezial:Search?search=" + mindmapPanel.currentMindmap().getTextWithoutPath() + "\"");
		if (command.startsWith("SetTitle:") && (commandKnown = true)) setTitle(command.substring(9));
		if (command.equals("NodeDetails") && (commandKnown = true)) mindmapPanel.showNodeDetails();
		if (mindmapPanel.currentMindmap() != null) {
			IInsertLink.setEnabled(mindmapPanel.currentMindmap().firstChild() == null);
			IInsertLink2.setEnabled(mindmapPanel.currentMindmap().firstChild() == null);
			IDeleteLink.setEnabled(mindmapPanel.currentMindmap().getLink() != null);
			IDeleteLink2.setEnabled(mindmapPanel.currentMindmap().getLink() != null);
		}
		if (!commandKnown) System.out.println("actionPerformed recieved the following, unimplemented command: " + command);

	}

	private void moveCurrentNodeToRoot() {
		try {
			setMindmap(openMindmap(mindmapPanel.currentMindmap().getRoot().nodeFile()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void doHtmlExport() {
		HtmlExportDialog exportDialog = new HtmlExportDialog(this, languagePack.EXPORT_TO_HTML(), true, languagePack);
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

	private void loadHelp() {
		boolean online = true;
		if (online) {
			MindmapNode dummy = mindmapPanel.currentMindmap();
			URL url = null;
			try {
				url = new URL(helpFile);
			} catch (MalformedURLException e) {
				JOptionPane.showMessageDialog(this, languagePack.NO_HELP(), "Warning", JOptionPane.OK_OPTION);
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

	private void fileNotFound(FileNotFoundException e) {
		JOptionPane.showMessageDialog(this, languagePack.FILE_NOT_FOUND().replace("##", e.toString().split(" ")[1]), "Warning", JOptionPane.OK_OPTION);
	}

	private void fileNotSupported(DataFormatException e) {
		JOptionPane.showMessageDialog(this, languagePack.FILE_NOT_SUPPORTED().replace("##", e.toString().split(" ")[1]), "Warning", JOptionPane.OK_OPTION);
	}
	
	private void startStopBackgroundtrace() {
		// TODO Auto-generated method stub
		if (mindmapPanel.traceBGColor()) {
			IBGCTrace.setText("x " + languagePack.BACKGROUND_COLOR_FOLLOW());
		} else {
			IBGCTrace.setText(languagePack.BACKGROUND_COLOR_FOLLOW());
		}
	}

	private void startStopForegroundtrace() {
		// TODO Auto-generated method stub
		if (mindmapPanel.traceForeColor()) {
			IFGCTrace.setText("x " + languagePack.TEXT_COLOR_FOLLOW());
		} else {
			IFGCTrace.setText(languagePack.TEXT_COLOR_FOLLOW());
		}
	}

	private void insertNewBrother() {
		// TODO Auto-generated method stub
		MindmapNode dummy = mindmapPanel.currentMindmap();
		if (dummy.parent() != null) {
			mindmapPanel.appendNewBrother(createNewNode(mindmapPanel.mindmap.getOrigin()));
		} else {
			JOptionPane.showMessageDialog(this, languagePack.NO_BROTHER_FOR_ROOT(), "Warning", JOptionPane.OK_OPTION);
		}
	}

	private NodeImage selectImage() {
		URL u = Tools.showSelectFileDialog(languagePack.OPEN_IMAGE(), null, new GenericFileFilter(languagePack.IMAGE_FILE(), "*.jpg;*.jpeg;*.gif;*.png;*.bmp"), this);
		this.requestFocus();
		return (u == null) ? null : new NodeImage(u);
	}

	private MindmapNode openMindmap(URL fileUrl) throws FileNotFoundException, DataFormatException, URISyntaxException {
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
				System.out.print("searching for " + name);
				URL searchedFile = Tools.searchFiles(names, path);
				if (searchedFile != null) {
					fileUrl = searchedFile;
				} else {
					if (!fileUrl.toString().toLowerCase().endsWith(".mm") && !fileUrl.toString().toLowerCase().endsWith(".imf")) fileUrl = urlPlusExtension;
					if (!requestFileCreation(fileUrl)) return null;
				}
			}
		}

		MindmapNode result = new MindmapNode();
		try {
			result.loadFromFile(fileUrl);
			this.setTitle(fileUrl.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}



	private int aksForSavingMindmaps() {
		// TODO Auto-generated method stub
		return JOptionPane.showConfirmDialog(this, languagePack.UNSAVED_CHANGES(), languagePack.SAVE_ERROR(), JOptionPane.YES_NO_CANCEL_OPTION);
	}

	private MindmapNode openMindmap() throws FileNotFoundException, DataFormatException, URISyntaxException {
		//String filename = (lastOpenedFile == null) ? null : lastOpenedFile.toString();
		URL fileUrl = Tools.showSelectFileDialog(languagePack.OPEN_MINDMAP(), null, new GenericFileFilter(languagePack.MINDMAP_FILE(), ".imf;.mm"), this);
		if (fileUrl == null) fileUrl = Tools.showUrlInputDialog(this, languagePack.SELECT_TARGET_MANUALLY());
		//lastOpenedFile = fileUrl;
		return openMindmap(fileUrl);
	}
	
	private URL openFile() throws FileNotFoundException {
		String filename=null;
		URL fileUrl = Tools.showSelectFileDialog(languagePack.OPEN_MINDMAP(), filename, null, this);
		if (fileUrl == null) fileUrl = Tools.showUrlInputDialog(this, languagePack.SELECT_TARGET_MANUALLY());
		//lastOpenedFile = fileUrl;
		return fileUrl;
	}	

	private boolean requestFileCreation(URL fileUrl) {
		if (JOptionPane.showConfirmDialog(this, languagePack.FILE_NOT_FOUND().replace("##", fileUrl.toString()) + ' ' + languagePack.ASK_FOR_CREATION()) == 0) {
			StringBuffer formula=new StringBuffer("\\small{"+fileUrl.getFile()+"}");
			formula.insert(formula.lastIndexOf("/")+1, "}\\bold{");
			formula.insert(formula.lastIndexOf("."), "}\\small{");
			MindmapNode newMindmap = new MindmapNode(formula.toString());
			newMindmap.saveTo(fileUrl);
			return true;
		}
		return false;
	}

	private void createNewMindmap() {
		MindmapNode newRoot = createNewNode(null);
		if (newRoot != null && closeMindmap()) {
			setMindmap(newRoot);
			setTitle(languagePack.NEW_MINDMAP() + "*");
			newRoot.mindmapChanged();
			enableMindmapOptions();
		}
	}

	private MindmapNode createNewNode(Point point) {
		// TODO Auto-generated method stub
		// System.out.println("createNewNode");
		String text = FormulaInputDialog.readInput(this, languagePack.NEW_NODE(), null);
		this.requestFocus();
		if (text == null) return null;
		return new MindmapNode(text,point);
	}

	private boolean closeMindmap() {
		if (mindmapPanel.hasUnsavedMindmap()) {
			int choice = aksForSavingMindmaps();
			switch (choice) {
			case JOptionPane.YES_OPTION:
				mindmapPanel.saveMindmaps();
				break;
			case JOptionPane.NO_OPTION:
				mindmapPanel.flushMindmapChanges();
				break;
			case JOptionPane.CANCEL_OPTION:
				return false;
			}
		}
		changeConfigurationFile();
		mindmapPanel.setMindmap(null);
		disableMindmapOptions();
		return true;
	}

	public IntelliMind3(String title) {
		super(title);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (Tools.language.equals("English")){
			languagePack=new MindmapLanguagePack_English(); // auch verwendet in StarTreePanel und MindmapNode; FormulaInputDialog verwendet FormulaLanguagePack 
		} else languagePack = new MindmapLanguagePack_German(); // auch verwendet in StarTreePanel und MindmapNode; FormulaInputDialog verwendet FormulaLanguagePack

		addWindowListener(this);
		addKeyListener(this);
		createComponents();
		loadPreferences();
	}

	private void loadPreferences() {
		try {
			BufferedReader configurationFile = new BufferedReader(new InputStreamReader(new FileInputStream(".intelliMind.config")));
			while (configurationFile.ready()) {
				String configurationLine = configurationFile.readLine();
				System.out.println();
				if (configurationLine.startsWith("Mindmap=")) {
					mindmapToOpenAtStart = new URL(configurationLine.substring(8));
				}
				if (configurationLine.startsWith("Backgroundcolor=")) {
					mindmapPanel.setBackground(new Color(Integer.parseInt(configurationLine.substring(16))));
				}
				if (configurationLine.startsWith("NodeDistance=") && mindmapPanel instanceof StarTreePanel) {
					((StarTreePanel) mindmapPanel).setDistance(Integer.parseInt(configurationLine.substring(13)));
				}
				if (configurationLine.startsWith("TextSize=")) {
					mindmapPanel.setTextSize(Float.parseFloat(configurationLine.substring(9)));
				}
				if (configurationLine.startsWith("Trace=")) {
					trace=configurationLine.substring(6);
				}
				if (configurationLine.startsWith("WindowSize=")) {
					String size=configurationLine.substring(11).trim();
					int h=size.indexOf(' ');
					int w=Integer.parseInt(size.substring(0,h));
					h=Integer.parseInt(size.substring(h+1));
					setSize(new Dimension(w,h));
				}
				if (configurationLine.startsWith("WindowLocation=")) {
					String size=configurationLine.substring(15).trim();
					int y=size.indexOf(' ');
					int x=Integer.parseInt(size.substring(0,y));
					y=Integer.parseInt(size.substring(y+1));
					setLocation(x, y);
				}
				if (configurationLine.startsWith("Display=")) {
					String display=configurationLine.substring(8).trim();
					if (display.equals("StarTreePanel")) enableStarTree(true);
				}
			}
			configurationFile.close();
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

	private void setMindmap(MindmapNode mindmapNode) {
		if (mindmapNode != null && closeMindmap()) {
			mindmapPanel.setMindmap(mindmapNode);
			changeConfigurationFile();
			enableMindmapOptions();
		}
	}

	private void changeConfigurationFile() {
		try {
			BufferedWriter configFile = new BufferedWriter(new FileWriter(".intelliMind.config"));
			try {
				
				configFile.write("Mindmap=" + getTrace() + "\n");
			} catch (NullPointerException e) {

			}
			configFile.write("Backgroundcolor=" + mindmapPanel.getBackground().getRGB() + "\n");
			configFile.write("NodeDistance=" + mindmapPanel.getDistance() + "\n"); // die Soll-Distanz zwischen den Knoten speichern
			configFile.write("TextSize=" + mindmapPanel.getTextSize() + "\n"); // die Schriftgröße der Knoten speichern
			configFile.write("WindowSize="+getSize().width+" "+getSize().height + "\n");
			configFile.write("WindowLocation="+getLocation().x+" "+getLocation().y + "\n");
			configFile.write("Display="+(mindmapPanel.getClass().getCanonicalName()) + "\n");
			configFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
 
	private String getTrace() {
		MindmapNode node = mindmapPanel.currentMindmap();
		StringBuffer trace=new StringBuffer();
		while (node.parent()!=null){
			while (node.prev()!=null){
				node=node.prev();
				trace.insert(0, 'D');
			}
			node=node.parent();
			trace.insert(0, "R");
		}
		return node.getRoot().nodeFile().toString()+"\nTrace="+trace;
	}

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

	public void keyPressed(KeyEvent k) {
		KeyStroke ks = KeyStroke.getKeyStrokeForEvent(k);
		if (ks.equals(CtrlW)) this.actionPerformed(new ActionEvent(this, 0, "closeMindmap"));
		if (ks.equals(CtrlPlus)) this.actionPerformed(new ActionEvent(this, 0, "incVertDist"));
		if (ks.equals(CtrlMinus)) this.actionPerformed(new ActionEvent(this, 0, "decVertDist"));
	}

	public void keyReleased(KeyEvent e) {}
	

	public void keyTyped(KeyEvent k) {}

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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


}
