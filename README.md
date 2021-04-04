# sorting-script

## About 

This script automates some parts of the university marking process (such as sorting and organising).

This includes:
* Unzipping all student assignments automatically 
* Creating a copy of the assignment specs for each student, stored in their own folders
* Renaming all feedback files

## To Run 

This only works with the zip files generated from UONline (Blackboard) - University of Newcastle, and certain assignment submission file formats.

There is an example zip folder under **gradebook-example/Gradebook.zip** that includes 'dummy' files zipped in the same way as it would in the real use case.

* Open the command prompt by typing 'cmd' into the windows search bar.
* Navigate to location of the script:

``` bash
cd C:\Users\User\Documents\...\sorting-script
```

* Run:

``` bash
Java Script
```

A folder explorer interface will appear asking you to navigate to the zip folder that you want to sort.

Another folder explorer will open asking you to navigate to the feedback sheet you want to copy for each student.
	
## To build:

If you want to make changes and re-compile the program, run the command:

``` bash
Javac Script.java
```
