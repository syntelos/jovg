package com.jogamp.jovg.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import static java.util.regex.Pattern.*;

/**
 * Build setup utility. Uncomments header files.
 *
 * @author Michael Bien
 * @author John Pritchard
 */
public class CSourceUncomment extends Task {

    final static Pattern PARAMS_PATTERN
            = compile("cl\\w+ \\(   (  \\s* [^;]+  )  \\)", MULTILINE|COMMENTS);

    final static Pattern COMMENT_PATTERN
            = compile("\\s*(const)?\\w+\\s* \\**\\s+ (/\\*) \\s+[^\\*\\[]+ (\\*/)", MULTILINE|COMMENTS);
                                                                     //^ array size in param name causes some problems


    private File src;
    private File dest;


    public CSourceUncomment(){
	super();
    }


    public void setSrc(String src) {
	if (null != src && 0 < src.length())
	    this.src = new File(src);
    }

    public void setDest(String dest) {
	if (null != dest && 0 < dest.length())
	    this.dest = new File(dest);
    }

    @Override
    public void execute() throws BuildException {
	if (null == this.src)
	    throw new BuildException("Require parameter 'src'.");
	else {
	    try {
		Uncomment(this.src, this.dest);
	    }
	    catch (FileNotFoundException ex) {
		throw new BuildException(ex);
	    }
	    catch (IOException ex) {
		throw new BuildException(ex);
	    }
	}
    }

    private final static void Uncomment(File srcFile, File destFile) throws FileNotFoundException, IOException {

        System.out.printf("CSourceUncomment read from %s%n",srcFile.getPath());

        StringBuilder headerSrc = ReadSourceFile(srcFile);
        Matcher matcher = PARAMS_PATTERN.matcher(headerSrc);

        /*
	 * Iterate through funcions
	 */
        while (matcher.find()) {

            StringBuilder params = new StringBuilder(matcher.group(1));
	    /*
	     * Iterate through params
	     */
            Matcher m = COMMENT_PATTERN.matcher(params);
            while(m.find()) {
                /*
		 * Uncomment param
		 */
                params.replace(m.start(2), m.end(2), "  ");
                params.replace(m.start(3), m.end(3), "  ");
            }
            /*
	     * Replace old params with uncommented params
	     */
            headerSrc.replace(matcher.start(1), matcher.end(1), params.toString());
        }

        if (null != destFile) {
	    if (destFile.isDirectory()){
		destFile = new File(destFile,srcFile.getName());
	    }
	    System.out.printf("CSourceUncomment write to %s%n",destFile.getPath());

            BufferedWriter out = new BufferedWriter(new FileWriter(destFile));
	    try {
		out.write(headerSrc.toString());
		out.flush();
	    }
	    finally {
		out.close();
	    }
        }
	else {
            System.out.println(headerSrc);
        }
    }


    private final static StringBuilder ReadSourceFile(File file) throws FileNotFoundException, IOException {
	final long length = file.length();
	if (length <= Integer.MAX_VALUE){
	    int len = (int)length;
	    char[] buffer = new char[len];
	    FileReader reader = new FileReader(file);
	    try {
		int ofs = 0;
		int read;
		while (0 < (read = reader.read(buffer,ofs,len))){
		    ofs += read;
		    len -= read;
		}
	    }
	    finally {
		reader.close();
	    }

	    StringBuilder sb = new StringBuilder();
	    sb.append(buffer);

	    return sb;
	}
	else
	    throw new IllegalStateException(String.format("File size  %s",file.getPath()));
    }


    private static void usage(){
	System.err.println("Usage");
	System.err.println("  CSourceUncomment <infile.h>");
	System.err.println("Description");
	System.err.println("  Test function, writing to stdout.");
	System.exit(1);
    }
    public static void main(String[] argv){
	if (1 != argv.length)
	    usage();
	else {
	    File hf = new File(argv[0]);
	    if (hf.isFile()){
		try {
		    Uncomment(hf,null);

		    System.exit(0);
		}
		catch (Exception exc){
		    exc.printStackTrace();
		    System.exit(1);
		}
	    }
	    else
		usage();
	}
    }
}
