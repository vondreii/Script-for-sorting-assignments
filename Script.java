/**
* --------------------------------------------------------------------
* This script automates some parts of the marking process (such as sorting and organising)
* This includes:
* 		- Unzipping all student assignments automatically 
*		- Creating a copy of the assignment specs for each student, stored in their own folders
*		- Renaming all feedback files
*		- Checking who has a late submission
* Date made: 31/05/19
* Last modified: 03/04/21
* University of Newcastle, Australia
* --------------------------------------------------------------------
*/
	
import java.awt.*;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Scanner;
import javax.swing.*;

public class Script {

    public static void main(String[] args) {
       Script unzip = new Script();
	   unzip.runScript();
    }
	
	public void runScript()
	{
		// Select the folder to unzip
		File zip = selectFile("Enter the directory that stores assignment submissions.");
		output("Selected zip: " + zip);
		
		// Exits if something is wrong
		if (zip == null || !zip.getName().endsWith("zip")) {
			output("No zip file selected");
			return;
		}
		
		// Gets location of the zip file and where the unzipped file should go
		String zipFilePath = zip + "";
        String destDir = zipFilePath.substring(0, zipFilePath.indexOf(".zip"));
		
		// Unzip the folder, sorted, per assignment
		ArrayList<File> unzippedFiles = unzip(zipFilePath, destDir);
		
		File feedback = selectFile("Please select the feedback sheet for this Assignment. "
			+ "\nIt will be copied into each of the student's folders.");
		
		System.out.println(feedback);
		output("Selected feedback: " + feedback);
		
		System.exit(0);
	}
	
	private File selectFile(String message)
	{
		// Opens up a dialog box where the user can choose which zip to open
		try {
			JOptionPane.showMessageDialog(null, message);
			JFileChooser chooser = new JFileChooser();
			chooser.showSaveDialog(null);
			return chooser.getSelectedFile(); 
		}
		// Handles exceptions, eg, user exits out of the window
		catch(Exception e) {
			return null;
		}
	}
	
	private void copyFeedback() {
		
	}
	
	private static ArrayList<File> unzip(String zipFilePath, String destDir) {
		// https://www.journaldev.com/960/java-unzip-file-example
		ArrayList<File> listOfFiles = new ArrayList<File>();
        File dir = new File(destDir);
		
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
		
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
		
        try {
			// Creates stream for reading the zip entries while unzipping
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
			
			// For each entry
            while(ze != null){
				// Rename the file. eg, 'c3220929_CoverSheet'
                String fileName = ze.getName();
				fileName = fileName.substring(fileName.indexOf("_c")+1, fileName.length());
				
				// removes the 'Assignment1_' etc prefixes
				String removedPrefix = fileName.substring(fileName.indexOf("_c")+1, fileName.length());
				
				// Only gets the student number and creates a folder
				String studentNo = removedPrefix.substring(0, removedPrefix.indexOf("_"));
				File studentFolder = new File(destDir + "\\" + studentNo);
				if(!studentFolder.exists()) studentFolder.mkdirs();
                File newFile = new File(studentFolder + File.separator + fileName);
				System.out.println("Unzipping to "+newFile.getAbsolutePath());
                new File(newFile.getParent()).mkdirs();
				
				listOfFiles.add(newFile);
				
				// Outputstream and buffer
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
                }
				
				// Close zip entry
                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } 
		catch (IOException e) {
            e.printStackTrace();
        }
		return listOfFiles;
    }
	
	private void output(String message)
	{
		// Neater output
		System.out.println(message);
	}

	
}