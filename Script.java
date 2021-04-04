/**
* --------------------------------------------------------------------
* This script automates some parts of the marking process (such as sorting and organising)
* This includes:
* 		- Unzipping all student assignments automatically 
*		- Sorting each student into a separate folder with their files
*		- Copying the feedback sheet into each student's folder
* Date made: 31/05/19
* Last modified: 04/04/21
* University of Newcastle, Australia
* --------------------------------------------------------------------
*/
	
import java.awt.*;
import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
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
       Script unzipAndSort = new Script();
	   unzipAndSort.runScript();
    }
	
	public void runScript()
	{		
		// Select the folder to unzip
		File zip = selectFile("Enter the zip directory that stores assignment submissions.");
		output("Selected zip: " + zip);
		
		// Exits if something is wrong
		selectFileException(zip == null || !zip.getName().endsWith("zip"), "No zip file selected");
		
		// Gets location of the zip file and where the unzipped file should go
		String zipFilePath = zip + "";
        String unzipDestination = zipFilePath.substring(0, zipFilePath.indexOf(".zip"));
		output(unzipDestination);
		
		// Unzip the folder, sorted, per student assignment
		ArrayList<File> studentFolders = unzip(zipFilePath, unzipDestination);
		
		output("---------------------------------------------------------------");
		output("-------------------- UNZIPPING COMPLETE -----------------------");
		output("---------------------------------------------------------------");
		
		// Get the feedback sheet
		File feedback = selectFile("Please select the feedback sheet for this Assignment. "
			+ "\nIt will be copied into each of the student's folders.");
			
		// Gets the source path and the filename to be copied
		String copyFrom = feedback+"";
		String feedbackFileName = copyFrom.substring(copyFrom.lastIndexOf("\\")+1, copyFrom.length());
		
		// Exits if something is wrong
		selectFileException(feedback == null, "No file selected. No feedback sheet was copied for each student. The sorted folder will now open automatically.", unzipDestination);
			
		// If the user does not select a file
		while (feedback.isDirectory() || copyFrom.contains("zip")) {
			
			// Exceptions and outputting messages
			feedback = selectFile("That is not a file. Please select the feedback file.");
			selectFileException(feedback == null, "No file selected. No feedback sheet was copied for each student. The sorted folder will now open automatically.", unzipDestination);
			
			// Gets the source path and the filename to be copied (again)
			copyFrom = feedback+"";
			output("Selected feedback: " + copyFrom);
		}
		
		// Copy the feedback sheet into each student folder
		for (File file : studentFolders) {
			output("Copying " + copyFrom + " to " + file.getAbsolutePath());
			copyFeedback(copyFrom, file.getAbsolutePath() + "\\" + feedbackFileName);
		}
		
		output("---------------------------------------------------------------");
		output("----------------------- COPY COMPLETE -------------------------");
		output("---------------------------------------------------------------");
		
		output("Zip folder has been unzipped and all student files have been sorted into their own folders along with their own feedback sheets. The sorted folder will now open automatically.");
		
		end(unzipDestination);
	}
	
	/**
	* --------------------------------------------------------------------
	* Opens a dialog for the user to find a folder or file.
	* --------------------------------------------------------------------
	*/
	private File selectFile(String message)
	{
		// Opens up a the dialog chooser.
		try {
			JOptionPane.showMessageDialog(null, message);
			JFileChooser chooser = new JFileChooser();
			chooser.showSaveDialog(null);
			return chooser.getSelectedFile(); 
		}
		// Handles exceptions, eg, no file is selected.
		catch(Exception e) {
			return null;
		}
	}
	
	/**
	* --------------------------------------------------------------------
	* Handles all user file selecting exceptions.
	* --------------------------------------------------------------------
	*/
	private void selectFileException(boolean condition, String message) {
		// Overload since the first exception does not require the final unzipped folder to be opened
		selectFileException(condition, message, "");
	}
	private void selectFileException(boolean condition, String message, String unzipDestination) {
		// If the error condition is true
		if (condition) {
			output(message);
			
			// If the folder has been unzipped, open it
			if(!unzipDestination.equals("")) {
				end(unzipDestination);
			}
			
			// Close the program
			System.exit(0);
		}
	}
	
	/**
	* --------------------------------------------------------------------
	* Unzips the initial Gradebook zip folder.
	* --------------------------------------------------------------------
	*/
	private static ArrayList<File> unzip(String zipFilePath, String destDir) {
		// https://www.journaldev.com/960/java-unzip-file-example
		ArrayList<File> listOfStudentFolders = new ArrayList<File>();
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
				
				if(!listOfStudentFolders.contains(studentFolder)) {
					listOfStudentFolders.add(studentFolder);
				}
				
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
		return listOfStudentFolders;
    }
	
	/**
	* --------------------------------------------------------------------
	* Copies the feedback template to all student folders.
	* --------------------------------------------------------------------
	*/
	private void copyFeedback(String sourcePath, String destination)
	{
		// Uses the copy method to just copy the file to the new folder
		try { 	
			Files.copy(Paths.get(sourcePath), Paths.get(destination));
		}
		// In case the folder cannot be copied. For example, if the folder already exists.
		catch(Exception e) {
			System.out.println("Could not copy the specs file in: " + destination + " ..... Check if the folder already exists.");
		}
	}
	
	/**
	* --------------------------------------------------------------------
	* Handles the end of the script.
	* --------------------------------------------------------------------
	*/
	private void end(String unzipDestination)
	{
		// Ensures the user has seen the output and result
		Scanner console = new Scanner(System.in);
		output("Press any button to continue: ");
		String input = console.nextLine(); 
		
		// Opens the unzipped sorted folder for the user
		try {
			Desktop.getDesktop().open(new File(unzipDestination));
		}
		// If the folder cannot be opened for some reason
		catch(Exception e) {
			output("Unable to open file explorer");
		}
		
		System.exit(0);
	}
	
	/**
	* --------------------------------------------------------------------
	* Cleaner way of outputting mesages.
	* --------------------------------------------------------------------
	*/
	private void output(String message)
	{
		System.out.println(message);
	}
}