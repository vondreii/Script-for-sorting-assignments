/**
* --------------------------------------------------------------------
* This script automates some parts of the marking process (such as sorting and organising)
* This includes:
* 		- Unzipping all student assignments automatically 
*		- Creating a copy of the assignment specs for each student, stored in their own folders
*		- Renaming all feedback files
*		- Checking who has a late submission 
* Auther: Sharlene Von Drehnen 
* Date made: 31/05/19
* University of Newcastle, Australia
* --------------------------------------------------------------------
*/
	
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
		// Gets the parent folder that stores the Assignment submissions
		// Returns a list of all subfolders (folders named with their student Numbers).
		/*
		Scanner console = new Scanner(System.in);
		System.out.println("Enter the folder");
		File[] directories = new File(console.nextLine()).listFiles(File::isDirectory);
		*/
		File[] directories = userSelect("folder");
		
		// Unzips each assignment inside each folder
		manageSubmissions(directories, "Unzip");

		// Returns the file of where the specs are.
		File[] markingGuide = userSelect("file");
		
		// Copies the specs into all the folders
		manageSubmissions(directories, "CopySpecs", markingGuide[0].toString());
		
		// Copies the specs into all the folders
		manageSubmissions(directories, "RenameFiles");
		
		System.exit(0);
	}

	/**
	* --------------------------------------------------------------------
	* User select - based on the folder provided, this asks the user to select the file/folder (Depending on the 
	* scenario, they could either be asked for the parent folder storing all assignments, or the marking guide)
	*
	* type: This can either be 'file' or 'folder'. 
	* --------------------------------------------------------------------
	*/
	private File[] userSelect(String type)
	{
		File[] directories = null;
		
		// Messages are set up, depending on what the user is selecting (file/folder)
		String message = "Enter the directory that stores assignment submissions";
		if(type.equals("file")) message = "Please select the marking guide for this Assignment. "
			+ "\nIt will be copied into each of the student's folders.";	
		
		JOptionPane.showMessageDialog(null, message); // Shows message - "Please enter.. "
		
		if(type.equals("file")) // Because it is only one file, it is stored at index 0
		{
			directories = new File[1];
			directories[0] = getFile();
		}
		// Gets a list of all student submission folders in that directory
		else directories = getFolder().listFiles(File::isDirectory);

		return directories;
	}
	
	/**
	* --------------------------------------------------------------------
	* getFile and fetFolder are methods that display a dialog box where the user can select their file or folder.
	*
	* https://stackoverflow.com/questions/10083447/selecting-folder-destination-in-java
	* --------------------------------------------------------------------
	*/	
	private File getFile()
	{
		// If the user is meant to select a file, they will be given the 'file Selector' dialog box
		FileDialog dialog = new FileDialog((Frame)null, "Select File to Open");
		dialog.setMode(FileDialog.LOAD);
		dialog.setVisible(true);

		return new File(dialog.getFile()); // Their selected file is returned
	}
	
	private File getFolder()
	{
		// If the user is meant to select a file, they will be given the 'folder Selector' dialog box
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
		chooser.showSaveDialog(null);
		
		return chooser.getSelectedFile(); // Their selected folder is returned
	}
	
	/**
	* --------------------------------------------------------------------
	* Viewing directories - used to view/debug and to make sure the program is getting the correct directories
	*
	* directories: the list of files (of all student submissions - each submission is in each file)
	* --------------------------------------------------------------------
	*/
	private void viewDirectories(File[] directories)
	{
		for (int i=0; i<directories.length; i++)
			System.out.println(directories[i]);
	}
	
	/**
	* --------------------------------------------------------------------
	* Manage Submissions - This is the main loop that goes inside each student's folder to unzip their
	* submissions, to copy the specs into their folder, and to read their attempt files (which are generated
	* after downloading them from the gradecenter)
	*
	* directories: the list of files (of all student submissions - each submission is in each file)
	* action: could be "Unzip", "CopySpecs", etc. depending on what needs to be done inside the loop for each student
	* markingGuide: The path to the actual marking guide that will be copied for each student.
	* --------------------------------------------------------------------
	*/
	private void manageSubmissions(File[] directories, String action)
	{
		manageSubmissions(directories, action, ""); // Overloading
	}
	
	private void manageSubmissions(File[] directories, String action, String markingGuide)
	{
		String unsuccesfulUnzips = "";
		File[] listOfFiles = null;
		
		// For each assignment folder
		for(int folder = 0; folder < directories.length; folder++)
		{
			// Gets the list of files inside the submission folder (this might include their zip file, cover sheet, and the txt file)		
			listOfFiles = directories[folder].listFiles();
			
			// Copies the marking guide into their folder
			if(action.equals("CopySpecs")) copyAssignmentSpecs(markingGuide, directories[folder].toString() + "\\" + markingGuide);
			else
			{
				for (int i = 0; i < listOfFiles.length; i++) // For each file inside the folder
				{	
					// Unzips their submission and gets a list of the unsuccessful zips
					if(action.equals("Unzip")) unsuccesfulUnzips = unzip(directories, folder, listOfFiles[i]);

					// Copies the marking guide into their folder
					if(action.equals("RenameFiles")) renameFile(directories, folder, listOfFiles[i]);
				}
			}
			
		}
		if(action.equals("Unzip")) statusOfZips(unsuccesfulUnzips);
	}

	private void statusOfZips(String unsuccesfulUnzips)
	{
		// Used as part of the manageSubmissions method - the user is told about the success of the unzips
		String message = "All files successfully unzipped";
		if(!unsuccesfulUnzips.equals("")) message = "Files unzipped. Files that could not be zipped: " + unsuccesfulUnzips;
				
		JOptionPane.showMessageDialog(null, message); // Shows the appropriate message
	}
	
	/**
	* --------------------------------------------------------------------
	* Unzip methods - unzips all files and folders inside a zipped directory, given a list of files 
	* 
	* directories: the list of files (of all student submissions - each submission is in each file)
	* folder: The student's submission folder we are currently looking at
	* listOfFiles: The list of files inside their submission folder
	*
	* zipFilePath: Takes in the path to the zip file
	* destDir: The path to the new folder it should be unzipped to
	*
	* SOOO much help from https://www.journaldev.com/960/java-unzip-file-example
	* --------------------------------------------------------------------
	*/
	private String unzip(File[] directories, int folder, File thisFile)
	{
		String zipFilePath, destDir, couldNotBeUnzipped = "";
		
		// If the file is a zip folder, then unzip it.
		if (thisFile.getName().endsWith("zip")) 
		{
			// path of the zip folder and the destination of the outputted files
			zipFilePath = directories[folder] + "\\" + thisFile.getName();				
			destDir = directories[folder] + "\\attempt";
				
			couldNotBeUnzipped += unzip(zipFilePath, destDir) + "\n"; // Unzips individual zip file
		}
		return couldNotBeUnzipped;
	}
	
    private String unzip(String zipFilePath, String destDir) 
	{
		// Creates a new file object for the directory of the output
        File newEmptyFolder = new File(destDir), newFile = null;
		ZipEntry entry = null;
		String couldNotBeUnzipped = "";
		
		// create output directory if it doesn't exist
        if(!newEmptyFolder.exists()) newEmptyFolder.mkdirs();

        byte[] buffer = new byte[1024]; // buffer for read and write data to file
		
        try 
		{	// Input stream for reading the zip file
			FileInputStream in = new FileInputStream(zipFilePath);
			ZipInputStream zipInput = new ZipInputStream(in); 
			entry = zipInput.getNextEntry();
		
            while(entry != null) // While the next entry is not null	
			{   
				// Creates the new files with the filename, makes a new directory, and an outputstream
                newFile = new File(destDir + File.separator + entry.getName());
                new File(newFile.getParent()).mkdirs();
                FileOutputStream out = new FileOutputStream(newFile);
                
				// Writes out the files
				int len;
                while ((len = zipInput.read(buffer)) > 0) out.write(buffer, 0, len);

				// close output stream and this ZipEntry, and gets the next zip file entry
                out.close(); zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }
			
            //close last ZipEntry
            zipInput.closeEntry(); zipInput.close(); in.close();
			
			System.out.println(newFile.getParent() + " was successfully unzipped");
        } 
		catch (Exception e) 
		{
			// Sometimes this ends up happening, not sure why
			if(newFile != null) System.out.println(newFile.getParent() + " could not be unzipped");
			
			// Compiles a list of the ones that didn't work
			couldNotBeUnzipped += entry.getName();
        } 
		return couldNotBeUnzipped; // Returns the list of the ones that couldn't be unzipped
    }

	/**
	* --------------------------------------------------------------------
	* Copy Assignment Specs - This copies a file from one destination to another
	*
	* sourcePath: The path to the original file (that needs to be copied)
	* destPath: The path to the new location the file should be copied to 
	* --------------------------------------------------------------------
	*/
	private void copyAssignmentSpecs(String sourcePath, String destPath)
	{
		try 
		{ 	// Uses the copy method to just copy the file to the new folder
			Files.copy(new File(sourcePath).toPath(), new File(destPath).toPath());
		}
		catch(Exception e)
		{
			System.out.println("Could not copy the specs file in: " + destPath + " ..... " + e.getMessage());
		}
	}
	
	/**
	* --------------------------------------------------------------------
	* Rename File - Renames all the feedback sheets so it has the student's name in it
	*
	* sourcePath: The path to the original file (that needs to be copied)
	* destPath: The path to the new location the file should be copied to 
	* --------------------------------------------------------------------
	*/
	private void renameFile(File[] directories, int folder, File thisFile)
	{
		String contentsFromTxt = "";
		
		if (thisFile.getName().endsWith("txt")) // If the file is a text file, read from it.
		{ 
			contentsFromTxt = read(thisFile.getParent() + "\\" + thisFile.getName());

			// WORKING PROGRESS
			int indexOfName = contentsFromTxt.indexOf("Name: ");
			int indexOfBracket = contentsFromTxt.indexOf("(");
			
			System.out.println(contentsFromTxt.substring(indexOfName, indexOfBracket));
		}
	}
	
	/**
	* --------------------------------------------------------------------
	* read file - Reads all the contents of a file
	* --------------------------------------------------------------------
	*/
	private String read(String fullFilePath)
	{
		String txtFileContents = "";
		try { 
			// Gets the full String file path of the text file and opens an input stream to it
			Scanner inputStream = new Scanner (new File(fullFilePath));
				
			// Concats each line onto a String
			while (inputStream.hasNextLine ()) txtFileContents += inputStream.nextLine () + "\n";

			inputStream.close (); // Closes input stream
		}
		catch (Exception e)
		{
			System.out.println("Error reading from " + fullFilePath + " - " + e.getMessage());
		}
		
		return txtFileContents;
	}	
}