/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kar.psoft.directorycleaner;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/***
 * 
 * @author christopher.harvey
 */
public class AppOptions {
    
	public static final String ARGUMENT_VEBOSE_LOG_FLAG = "v";
	public static final String ARGUMENT_HELP = "help";
	public static final String ARGUMENT_ACTIONLIST = "list";
	public static final String ARGUMENT_ACTIONDELETE = "del";
	public static final String ARGUMENT_ACTIONZIP = "zip";
	public static final String ARGUMENT_MATCH="match";
	public static final String ARGUMENT_MATCHALL="matchall";
	public static final String ARGUMENT_MATCH_REG_EXPRESSION="matchregx";
	public static final String ARGUMENT_AGE = "age";
	public Options options;
	public static AppOptions me= null;
	
	
	
	private AppOptions(){
		options= new Options();
		this.BuildCommandLineRules();
	}
	
	public static AppOptions getInstance(){
		if (me == null){
			me = new AppOptions();
		}
		return me;
		
	}
	
	
	
	
	@SuppressWarnings("static-access")
	private void BuildCommandLineRules() {
	
		// create the Options
		
		options.addOption(ARGUMENT_VEBOSE_LOG_FLAG, false,"Turn on verbose output for debugging.");
		options.addOption(ARGUMENT_HELP, false, "Display this message.");
		
		
		//options.addOption(cmdlineACTIONLIST, false,"Run program, but take no actions.");
		//options.addOption(cmdlineACTIONDELETE, false,"Run with del action. Removes files that match.");


		options.addOption(OptionBuilder.withArgName("dir").hasArg()
				.isRequired(true).withDescription("starting Directory")
				.create("source"));
		options.addOption(OptionBuilder.withArgName("days").hasArg()
				.isRequired(true).withDescription("minimum age of files in days")
				.create(ARGUMENT_AGE));
		
		
		/*Define how name matching will be done.*/
		OptionGroup optGroupMatch = new OptionGroup();
		
		optGroupMatch.addOption(OptionBuilder
				.withArgName("str[,str]")
				.hasArg()
				.withDescription("comma separated list of strings")
				.create(ARGUMENT_MATCH));
		optGroupMatch.addOption(OptionBuilder
				.withDescription("all files names match")
				.create(ARGUMENT_MATCHALL));
		optGroupMatch.addOption(OptionBuilder
				.withArgName("regx")
				.hasArg()
				.withDescription("regular expression string")
				.create(ARGUMENT_MATCH_REG_EXPRESSION));
		
		optGroupMatch.setRequired(true);
		options.addOptionGroup(optGroupMatch);
		

		OptionGroup optGroupAction = new OptionGroup();
		optGroupAction.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("Action: Archive to zip file location")
				.create(ARGUMENT_ACTIONZIP));
		optGroupAction.addOption(OptionBuilder.withDescription(
				"Action: Delete Files that match").create(ARGUMENT_ACTIONDELETE));
		optGroupAction.addOption(OptionBuilder.withDescription(
				"Action: List Files only").create(ARGUMENT_ACTIONLIST));
		optGroupAction.setRequired(true);
		options.addOptionGroup(optGroupAction);
	}
	
	
	public void printCMDUsage() {
		HelpFormatter formatter = new HelpFormatter();
		String Examples = new String();
		Examples +="\nExamples:";
		Examples +="\n\n-Delete *.tmp and *.log files older than 10 days.\nlogfilecleanup -source c:\\temp -del -match .tmp,.log -age 10";
		Examples +="\n\n-Zip Everything\nlogfilecleanup -source c:\\temp -zip c:\\zipall.zip -matchall -age 0"; 
		Examples +="\n\n-List all files that end begin with INS or end in log\nlogfilecleanup -source c:\\temp -list -matchregx ^INS|log$ -age 0";
		formatter.printHelp(
				"LogFileCleanup", "\n\nThis application will reclusively search the given source directory for file that match and are >= given age.  The action will be to deleted, move to zip, or list the files that match.",
				options,Examples, true);
		System.exit(1);
	}
	
	public File setRootFolder(String optionValue) throws FileNotFoundException {
		File rootpath = new File(optionValue);
		if (!rootpath.isDirectory()) {
			throw new FileNotFoundException("Unable to find Directory: "+ optionValue.trim());
		}
		//String rootname= rootpath.getAbsolutePath();
		//rootpathlength = (int) rootname.length();
		
		return rootpath;
	}
}
