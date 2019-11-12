
package com.kar.psoft.directorycleaner;
import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * LogFileFilter provides complex file matching rules.  Age base, Simple file 
 * extension matching, and full regular expression matching.
 * 
 * @author christopher.harvey
 */
public class LogFileFilter implements FileFilter {

	//private String[] okFileExtensions;
	private Pattern okFileRegularExpression;
	//private File[] filesToExlude;
	private long minAgeToRemove;
	


        LogFileFilter() {
		this(null,null,0);
	}
	LogFileFilter(long Age) {
		this(null,null,null,Age);
	}	
	LogFileFilter(String[] searchStrings) {
		this(searchStrings, null,0);
	}
	LogFileFilter(String[] searchStrings, long Age) {
		this(searchStrings, null,Age);
	}
	LogFileFilter(String RawMatchList) {
		this(RawMatchList, 0);
	}	
	LogFileFilter(String RawMatchList,long Age) {
		this(RawMatchList.split(","),Age);
	}	
	LogFileFilter(String[] searchStrings, File[] Filestoexclude, long Age) {
		this(searchStrings,Filestoexclude,null,Age);
	}
	LogFileFilter(Pattern compiledPattern) {
		this(compiledPattern,0);
	}
	LogFileFilter(Pattern compiledPattern,long Age) {
		this(null,null,compiledPattern,Age);
	}

	LogFileFilter(String[] searchStrings, File[] Filestoexclude, Pattern CompiledPattern, long Age) {
		StringBuffer regularExpressionsCombinedText = new StringBuffer();
		
		/*SearchStrings is an array of file ending characters to match on (ie extensions).
		 * These will be moved from an array to a regular expression string.
		 */
		if (searchStrings != null){
			//
			
			if (CompiledPattern != null){
				//Extract the Text Expression for the Compiled Pattern so we can add to it.
				regularExpressionsCombinedText.append(CompiledPattern.toString());
			}

			//Loop the Extensions and turn them into Line ending expressions
			for (String s : searchStrings){
					if (regularExpressionsCombinedText.length()>0){
						regularExpressionsCombinedText.append("|");  //and an OR condition only if this is is not the 1st expression added
					}
					regularExpressionsCombinedText.append(s.toLowerCase());
					regularExpressionsCombinedText.append("$"); //SearchStrings are file extensions only and must exist at the end.
				}
		}
		
		//this.filesToExlude = Filestoexclude;
		this.minAgeToRemove = Age;
		
		
		if (regularExpressionsCombinedText.length()>0){
			this.okFileRegularExpression = Pattern.compile(regularExpressionsCombinedText.toString());
		}else{
			this.okFileRegularExpression = CompiledPattern;
		}
	}


	/**
	 * 
	 */
	@Override
	public boolean accept(File pathname) {
		//Files to exclude
		/*
		if (filesToExlude !=null){
			for (File f : filesToExlude){
				if (pathname.getName().equalsIgnoreCase(f.getAbsolutePath())) {
					return false;
				}
			}
		}
		*/

		// Kick out true for all directories
		if (pathname.isDirectory())
			return true;

		// Kick out false if age fails
		if (pathname.lastModified() >= minAgeToRemove) {
			return false;
		} else {
			// Now check Name
			if (this.okFileRegularExpression==null) {
				//Any file name matches 
				return true;
			} else {
				Matcher matcher = this.okFileRegularExpression.matcher(pathname.getName().toLowerCase());
				return matcher.find();
			}
		}
	}
}
