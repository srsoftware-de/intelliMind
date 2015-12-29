package de.keawe.intellimind;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.keawe.tools.Tools;
import de.keawe.tools.translations.Translations;


public class HtmlExportDialog extends JDialog {
	private static final long serialVersionUID = 2513589621443162899L;
	JCheckBox exportOnlyCurrentMindmap;
	JPanel depthPanel,okCancelPanel,folderPanel;
	JCheckBox maximumDepthCheckBox;
	JCheckBox interactiveExport;
	JCheckBox noMultipleFollows;
	JTextField depthEditor,fileNameField;
	ButtonGroup RadioButtons;
	JButton increaseDepth,decreaseDepth,ok,cancel,selectFolder;
	JRadioButton exportToOneFile,exportToMultiplefiles;
	String filename=null;
	private boolean okPressed=false;
	private int maxDepth=10;

  public HtmlExportDialog(JFrame owner, String title, boolean modal) {
    // Dialog-Initialisierung
    super(owner, title, modal);
    init(title,null,modal);
  }
  
  public String _(String text){
		return Translations.get(text);
	}
  
	public String fileName() {
		// TODO Auto-generated method stub
		if (filename.charAt(filename.length()-1) != '/') filename=filename+"/";
		return filename;
	}

	public void init(String title,String text, boolean modal) {
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) { dispose(); }
    });
    int frameWidth = 870;
    int frameHeight = 400;
    setSize(frameWidth, frameHeight);
    
    // Anfang Komponenten
    
    folderPanel=new JPanel();
    folderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    
    filename=(new File(".")).getAbsolutePath();
    filename=filename.substring(0,filename.length()-1);
    fileNameField=new JTextField(filename);
    folderPanel.add(fileNameField);
    
    selectFolder=new JButton(_("Select output folder"));
    selectFolder.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	filename=Tools.selectFolder();
      	fileNameField.setText(filename+"/");
      }
    });
    folderPanel.add(selectFolder);
    add(folderPanel);
    exportOnlyCurrentMindmap=new JCheckBox(_("Export current mindmap only. Do not follow links."));
    exportOnlyCurrentMindmap.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	if (exportOnlyCurrentMindmap.isSelected()){
      	exportToOneFile.setSelected(true);
      	exportToMultiplefiles.setEnabled(false);
    		noMultipleFollows.setSelected(false);
    		noMultipleFollows.setEnabled(false);
    		interactiveExport.setSelected(false);
    		interactiveExport.setEnabled(false);
    		maximumDepthCheckBox.setSelected(false);
    		enableDepthPanel(false);
      	} else {
      		exportToMultiplefiles.setEnabled(true);
      		interactiveExport.setEnabled(true);
      		enableDepthPanel(true);
      		noMultipleFollows.setEnabled(true);
      		noMultipleFollows.setSelected(true);
      		
      	}
      }
    });
    add(exportOnlyCurrentMindmap);
    
    depthPanel=new JPanel();
     
    maximumDepthCheckBox=new JCheckBox(_("maximum depth"));
    maximumDepthCheckBox.setSelected(true);
    depthPanel.add(maximumDepthCheckBox);
    depthEditor=new JTextField("010");
    depthPanel.add(depthEditor);
    increaseDepth=new JButton("+");
    increaseDepth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	setDepth(maxDepth+1);
      }
    });
    depthPanel.add(increaseDepth);
    decreaseDepth=new JButton("-");
    decreaseDepth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	setDepth(maxDepth-1);
      }
    });
    depthPanel.add(decreaseDepth);
    depthPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    add(depthPanel);
    interactiveExport=new JCheckBox(_("interactive export"));
    add(interactiveExport);
    
    exportToOneFile=new JRadioButton(_("export to one single file"));
    exportToOneFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	if (exportToOneFile.isSelected() && !exportOnlyCurrentMindmap.isSelected()) noMultipleFollows.setEnabled(true);
      }
    });
    add(exportToOneFile);
    exportToMultiplefiles=new JRadioButton(_("export into multiple files"));
    exportToMultiplefiles.setSelected(true);
    
    exportToMultiplefiles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	if (exportToMultiplefiles.isSelected()) {
      		noMultipleFollows.setSelected(true);
      		noMultipleFollows.setEnabled(false);
      		enableDepthPanel(true);
      	}
      }
    });
    add(exportToMultiplefiles);
    
    RadioButtons=new ButtonGroup();
    RadioButtons.add(exportToOneFile);
    RadioButtons.add(exportToMultiplefiles);
    noMultipleFollows=new JCheckBox(_("don't follow links to a special file more than one time"));
    noMultipleFollows.setSelected(true);
    noMultipleFollows.setEnabled(false);
    noMultipleFollows.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	if (!noMultipleFollows.isSelected()) {
      		maximumDepthCheckBox.setSelected(true);
      		enableDepthPanel(false);
      	} else {
      		enableDepthPanel(true);
      	}
      }
    });
    add(noMultipleFollows);
    
    okCancelPanel=new JPanel();
    okCancelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    ok=new JButton(_("Ok"));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	okPressed=true;
      	dispose();
      }
    });
    okCancelPanel.add(ok);
    cancel=new JButton(_("Cancel"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
      	dispose();
      }
    });
    okCancelPanel.add(cancel);
    add(okCancelPanel);
    Container cp = getContentPane();
    
    cp.setLayout(new GridLayout(10,1));

    // Ende Komponenten

    setResizable(true);
  }

	public boolean interactive() {
		// TODO Auto-generated method stub
		return interactiveExport.isSelected();
	}

	public int maxDepth() {
		// TODO Auto-generated method stub
		return (maximumDepthCheckBox.isSelected())?maxDepth:0;
	}

	public boolean noMultipleFollow() {
		// TODO Auto-generated method stub
		return noMultipleFollows.isSelected();
	}

	public boolean notCancelled() {
		// TODO Auto-generated method stub
		return okPressed;
	}

	public boolean onlyCurrent() {
		// TODO Auto-generated method stub
		return exportOnlyCurrentMindmap.isSelected();
	}

	public boolean singleFile() {
		// TODO Auto-generated method stub
		return !exportToMultiplefiles.isSelected();
	}

	protected void enableDepthPanel(boolean b) {
		// TODO Auto-generated method stub
		depthPanel.setEnabled(b);
    depthEditor.setEditable(b);
    increaseDepth.setEnabled(b);
    decreaseDepth.setEnabled(b);
    maximumDepthCheckBox.setEnabled(b);
	}

	protected void setDepth(int i) {
		// TODO Auto-generated method stub
		maxDepth=Math.max(i, 1);
		String s=String.valueOf(maxDepth);
		while (s.length()<3) s="0"+s;
		depthEditor.setText(s);
	}

}
