package com.jogamp.jovg.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import static java.util.regex.Pattern.*;

/**
 * Build utility preprocesses header files for GlueGen/Cgram.
 *
 * @author Michael Bien
 * @author John Pritchard
 */
public class Preprocess extends Task {

    final static Pattern PARAMS_PATTERN
            = compile("vg\\w+ \\(   (  \\s* [^;]+  )  \\)", MULTILINE|COMMENTS);

    final static Pattern COMMENT_PATTERN
            = compile("\\s*(const)?\\w+\\s* \\**\\s+ (/\\*) \\s+[^\\*\\[]+ (\\*/)", MULTILINE|COMMENTS);
                                                                     //^ array size in param name causes some problems
    /*
    final static Pattern DEFINITION_PATTERN = compile("\\s*(\\w+)\\s*=\\s*([^,]+),?\\s*$");
    */

    private File src;
    private File dest;


    public Preprocess(){
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
		Rewrite(this.src, this.dest);
	    }
	    catch (IOException ex) {
		throw new BuildException(ex);
	    }
	}
    }

    private final static void Rewrite(File srcFile, File destFile) throws IOException {

        System.out.printf("Preprocess read from %s%n",srcFile.getPath());

        StringBuilder headerSrc = ReadSourceFile(srcFile);

        /*
	 * Drop comments within function parameters
	 */
        Matcher parmsMatcher = PARAMS_PATTERN.matcher(headerSrc);

        while (parmsMatcher.find()) {

            StringBuilder params = new StringBuilder(parmsMatcher.group(1));
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
            headerSrc.replace(parmsMatcher.start(1), parmsMatcher.end(1), params.toString());
        }

        /*
	 * Rewrite constant arithmetic expressions to values

        Matcher defsMatcher = DEFINITION_PATTERN.matcher(headerSrc);

	while (defsMatcher.find()){

            System.out.printf("%s // %s // %s %n",defsMatcher.group(),defsMatcher.group(1),defsMatcher.group(2));
	}
	 */
        if (null != destFile) {
	    if (destFile.isDirectory()){
		destFile = new File(destFile,srcFile.getName());
	    }
	    System.out.printf("Preprocess write to %s%n",destFile.getPath());

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
	    /*
	     * Test case, stdout
	     * (println flushes)
	     */
	    System.out.println(headerSrc);
        }
    }


    private final static StringBuilder ReadSourceFile(File file) throws IOException {
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
	System.err.println("  Preprocess <infile.h>");
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
		    Preprocess.Rewrite(hf,null);

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
