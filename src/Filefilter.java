import java.io.File;
import java.io.FileFilter;


public class Filefilter implements FileFilter {
	private String name=null;
	public  Filefilter(String filename){
		name=filename;
	}

	public boolean accept(File arg0) {
		// TODO Auto-generated method stub
		return arg0.getName().startsWith(name);
	}

}
