/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kar.psoft.directorycleaner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author christopher.harvey
 */
public class LogFileCleanup {
	private long minAgeToRemove;
	private LogFileFilter filter;
	private File rootpath;
	private boolean keepFiles;
	private boolean makeZip;
	private boolean listFiles;
	private File zipFile;
	private ZipOutputStream zipOut; 
	private boolean VerboseLog = false;
	private ArrayList<File> FilesToDelete;
	private static CommandLine cmd;
	
	public static void main(String[] args) throws Exception {
		LogFileCleanup app = new LogFileCleanup();

		try {
			app.beginsetup(args);
			app.beginwork();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void beginwork() throws Exception {
		FilesToDelete = new ArrayList<>();
		
		/**
		 * Setup the Zip File and create a small README.TXT
		 */
		if (makeZip) {
			zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
			zipOut.putNextEntry(new ZipEntry("Readme.txt"));
			String tmp = "Archive Created on " + Calendar.getInstance().getTime().toString();
			zipOut.write(tmp.getBytes());
			zipOut.closeEntry();
		}

		/**
		 * Begin File Search
		 */
		processDirectory(rootpath, zipOut);
		

		/**
		 * Close up the zip, and delete the files
		 */
		if (zipOut != null) {

			//Write the Manifest File
			zipOut.putNextEntry(new ZipEntry("manifest.txt"));
				for (File f: FilesToDelete){
					f.delete();
					zipOut.write(f.getAbsolutePath().getBytes());
					zipOut.write("\n".getBytes());
				}
			zipOut.closeEntry();
			
			zipOut.close();
			//Now actually delete the files
		}
		
		
		for (File f: FilesToDelete){
			f.delete();
		}

	}

	void processDirectory(File dirObj, ZipOutputStream out) throws IOException {
		File[] files = dirObj.listFiles(filter);
		byte[] tmpBuf = new byte[1024];

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				processDirectory(files[i], out);
				continue;
			}
			String abspath = files[i].getAbsolutePath();
			String relativePath = abspath.replace(":", "");
			
			if (VerboseLog || this.listFiles ){
				System.out.println(" File   :" + relativePath);
			}

			if (this.makeZip) {
				FileInputStream in = new FileInputStream(abspath);
				
				ZipEntry zipEntry = new ZipEntry(relativePath);
				zipEntry.setTime(files[i].lastModified()); // retain os updated time

				out.putNextEntry(zipEntry);
				int len;
				while ((len = in.read(tmpBuf)) > 0) {
					out.write(tmpBuf, 0, len);
				}
				out.closeEntry();
				in.close();
			}

			if (!this.keepFiles) {
				//Store the list of files to delete.  The delete will occur when the zip file is closed.
				FilesToDelete.add(files[i]);
			}
		}
	}



	private void beginsetup(String[] args) {
		
		com.kar.psoft.directorycleaner.AppOptions CL = com.kar.psoft.directorycleaner.AppOptions.getInstance();

		try {
			CommandLineParser parser = new PosixParser();
			cmd = parser.parse(CL.options, args);

			if (cmd.hasOption(com.kar.psoft.directorycleaner.AppOptions.ARGUMENT_HELP)) {
				CL.printCMDUsage();
			}

		} catch (ParseException exp) {
			System.out.println(exp.getMessage() + "\n\n");
			CL.printCMDUsage();
		}
		
		//Set the starting point
		try {
			this.rootpath = CL.setRootFolder(cmd.getOptionValue("source"));
		} catch (FileNotFoundException e1) {
			CL.printCMDUsage();
		}
		

		minAgeToRemove = LogFileCleanup.setArchiveDate(cmd.getOptionValue(com.kar.psoft.directorycleaner.AppOptions.ARGUMENT_AGE));
		
		/*Detect the method for file name matching*/
		if (cmd.hasOption(AppOptions.ARGUMENT_MATCHALL)){
			//All File Names
			filter = new LogFileFilter(minAgeToRemove);
		}else{
			
			if (cmd.hasOption(AppOptions.ARGUMENT_MATCH_REG_EXPRESSION)){
				//
				filter = new LogFileFilter(Pattern.compile(cmd.getOptionValue(AppOptions.ARGUMENT_MATCH_REG_EXPRESSION)),minAgeToRemove);
			}else{
				filter = new LogFileFilter(cmd.getOptionValue(AppOptions.ARGUMENT_MATCH),minAgeToRemove);
			}
		}
		
		this.VerboseLog = cmd.hasOption(com.kar.psoft.directorycleaner.AppOptions.ARGUMENT_VEBOSE_LOG_FLAG);

		
		/*
		 * Determine which action chosen and set list, zip or delete flags.
		 */
		if (cmd.hasOption(com.kar.psoft.directorycleaner.AppOptions.ARGUMENT_ACTIONLIST)) {
			System.out.println("List Matches:");
			this.keepFiles = true;
			this.makeZip = false;
			this.listFiles=true;

		}
		if (cmd.hasOption(com.kar.psoft.directorycleaner.AppOptions.ARGUMENT_ACTIONDELETE)) {
			System.out.println("Delete Matches:");
			this.keepFiles = false;
			this.makeZip = false;
			this.listFiles=true;

		}
		if (cmd.hasOption(com.kar.psoft.directorycleaner.AppOptions.ARGUMENT_ACTIONZIP)) {
			System.out.println("Zip Matches:");
			this.zipFile = new File(cmd.getOptionValue(com.kar.psoft.directorycleaner.AppOptions.ARGUMENT_ACTIONZIP));
			this.keepFiles = false;
			this.makeZip = true;

		}

		

	}



	
	/**
	 * 
	 * @param string 
	 * @return date in milliseconds representing the 
	 */
	static private long setArchiveDate(String string) {
		int days = Integer.parseInt(string);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, (days * -1));
		return cal.getTimeInMillis();
	}
	
}