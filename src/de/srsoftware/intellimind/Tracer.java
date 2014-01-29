package de.srsoftware.intellimind;

import de.srsoftware.gui.treepanel.TreePanel;

	public class Tracer extends Thread{
		private TreePanel mindmap;
		private String trace;

		public Tracer(TreePanel mindmapPanel,String trace) {
			mindmap=mindmapPanel;
			this.trace=trace;
		}
		
		public void run() {
			super.run();
			try {
				sleep(1000);
			for (int i=0; i<trace.length(); i++){
				switch (trace.charAt(i)){
				case 'R':
					sleep(400);
					mindmap.navigateRight();
					break;
				
				case 'D': mindmap.navigateDown(); 
					break;
				}
				mindmap.currentNode().waitForLoading();
			}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}