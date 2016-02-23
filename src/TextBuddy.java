import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TextBuddy {

	/*
	 * The following strings are used through the program in order to alert the user of the activity that is
	 * taking place. They are adjusted for formatting to allow for the content to be customized to what they 
	 * are adding specifically.
	 */
	private static final String WELCOME_MESSAGE = "Welcome to TextBuddy. %1$s is ready for use.";
	private static final String ADD_MESSAGE = "added to %1$s: \"%2$s\"";
	private static final String DELETE_MESSAGE = "deleted from %1$s: \"%2$s\"";
	private static final String CLEAR_MESSAGE = "all content deleted from %1$s";
	private static final String INVALID_MESSAGE = "invalid command format: %1$s";
	private static final String SORT_MESSAGE = "sorted content in %1$s";
	private static final String SEARCH_MESSAGE = "%1$s found in line number(s): %2$s";

	/*
	 * A command type enumeration has been utilized in order to distinguish between the different commands that are
	 * possible. 
	 */
	private enum COMMAND_TYPE {
		ADD, DISPLAY, CLEAR, EXIT, DELETE, INVALID, SORT, SEARCH
	}

	private static Scanner SCANNER = new Scanner(System.in);
	private static PrintWriter PRINT_WRITER;
	public static Path FILE_PATH;
	public static String FILE_NAME;

	/*
	 * In the main method, the first argument is set to the file name, and the file name will then be opened. 
	 * While the user has not chosen to exit the program, the program will run continuously, prompting the
	 * user to enter a command and then executing the command before restarting this process. 
	 */
	public static void main(String[] args) {
		FILE_NAME = args[0];
		try {
			openFile(FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
		showToUser(String.format(WELCOME_MESSAGE, FILE_NAME));
		while (true) {
			processUserCommands();
		}
	}
	
	/*
	 * This function processes the commands from the user by prompting the user to first enter a new command,
	 * and then executes the command and prints the feedback of the command to the user. 
	 */
	private static void processUserCommands() {
		System.out.print("command: ");
		String command = SCANNER.nextLine();
		String feedback = executeCommand(command);
		showToUser(feedback);
	}

	/*
	 * This method opens the file that the user has entered as the first argument when opening TextBuddy. It 
	 * remains the same file throughout. 
	 */
	public static void openFile(String fileName) throws IOException {
		FILE_PATH = Paths.get(fileName);
		File file = new File(FILE_PATH.toString());
		if (!file.exists()) { 
			file.createNewFile();
		}
	}

	/*
	 * This method shows the message to the user based off of the feedback that was yielded from executing the
	 * command. 
	 */
	private static void showToUser(String text) {
		System.out.println(text);
	}

	/*
	 * This command will execute the command according to what command type is returned from the enumeration. 
	 * It will then perform another method specific to what command type it is, and will return a formatted message
	 * which will then be shown to the user as feedback. 
	 */
	public static String executeCommand(String userCommand) {
		if (userCommand.trim().equals("")) {
			return String.format(INVALID_MESSAGE, userCommand);
		}
		String commandTypeString = getFirstWord(userCommand);
		COMMAND_TYPE commandType = determineCommandType(commandTypeString);
		switch (commandType) {
		case ADD:
			return add(userCommand);
		case DISPLAY:
			return display(userCommand);
		case CLEAR:
			return clear(userCommand);
		case DELETE:
			return delete(userCommand);
		case INVALID:
			return String.format(INVALID_MESSAGE, userCommand);
		case EXIT:
			PRINT_WRITER.close();
			System.exit(0);
		case SORT:
			return sortFile(userCommand);
		case SEARCH:
			return search(userCommand);
		default:
			//throw an error if the command is not recognized
			throw new Error("Unrecognized command type");
		}
	}

	/*
	 * This method will delete the corresponding contents from the file, and will return the delete contents
	 * from that file.
	 */
	private static String delete(String userCommand) {
		try {
			int lineNumber = Integer.parseInt(removeFirstWord(userCommand)); //Getting the line number that must be deleted
			List<String> fileLines = Files.readAllLines(FILE_PATH, Charset.forName("UTF-8")); 
			if (lineNumber < fileLines.size() && lineNumber >= 0) { //Checking that the lineNumber is actually valid before proceeding
				String deletedLine = fileLines.remove(lineNumber - 1);
				PRINT_WRITER = new PrintWriter(FILE_NAME);
				PRINT_WRITER.print("");
				PRINT_WRITER.close();
				for (String parameter : fileLines) {
					Files.write(FILE_PATH, parameter.getBytes(), StandardOpenOption.APPEND);
					Files.write(FILE_PATH, "\n".getBytes(), StandardOpenOption.APPEND);
				}
				return String.format(DELETE_MESSAGE, FILE_NAME, deletedLine);
			} else  if (lineNumber >= fileLines.size()){
				return ("The given index is greater than the number of lines, try again. \n");
			} else {
				return ("The given index is negative, try again. \n");
			}
		} catch (IOException e) {
			//If the line number is found to not contain an integer, then the command is invalid, and the invalid message will be shown.
			return String.format(INVALID_MESSAGE, userCommand);
		}
	}

	/*
	 * This method displays the contents of the file to the user. 
	 */
	private static String display(String userCommand) {
		try {
			List<String> fileLines = Files.readAllLines(FILE_PATH, Charset.forName("UTF-8"));
			if (fileLines.size() > 0) {
				for (int i = 0; i < fileLines.size(); i++) {
					System.out.println(i + 1 + ". " + fileLines.get(i));
				}
				return ""; //If the file is not empty, we do not need to return anything because we have already printed all lines in the file, and that should suffice. 
			} else {
				return FILE_NAME + " is empty.";
			}
		} catch (IOException e) {
			//If the file is unable to be read, the invalid message will be displayed. 
			return String.format(INVALID_MESSAGE, userCommand);
		}
	}

	/*
	 * This method will clear all content in the file. 
	 */
	public static String clear(String userCommand) {
		try {
			PRINT_WRITER = new PrintWriter(FILE_NAME);
			PRINT_WRITER.print("");
			PRINT_WRITER.close();
			return String.format(CLEAR_MESSAGE, FILE_NAME);
		} catch (FileNotFoundException e) {
			//If the file is unable to be found, the invalid message will be displayed. 
			return String.format(INVALID_MESSAGE, userCommand);
		}
	}

	/*
	 * This method will add a string, specified in userCommand, to the file. It returns a formatted 
	 * message specifying either if the add line was successful, or if the command was invalid. 
	 */
	private static String add(String userCommand) { 
		try {
			Files.write(FILE_PATH, removeFirstWord(userCommand).getBytes(), StandardOpenOption.APPEND);
			Files.write(FILE_PATH, "\n".getBytes(), StandardOpenOption.APPEND);
			return String.format(ADD_MESSAGE, FILE_NAME, removeFirstWord(userCommand));
		} catch (IOException e) {
			return String.format(INVALID_MESSAGE, userCommand);
		}
	}

	/*
	 * This function simply sorts the file lines by means of an in place quicksort. It then alerts the user if
	 * the file was successfully able to be sorted. If not, it prompts the user as to the invalid action.
	 */
	private static String sortFile(String userCommand) {
		try {
			List<String> fileLines = Files.readAllLines(FILE_PATH, Charset.forName("UTF-8"));
			String[] fileLinesArray = new String[fileLines.size()];
			int index = 0;
			for (String line : fileLines) {
				fileLinesArray[index] = line;
				index++;
			}
			sortLines(fileLinesArray, 0, fileLines.size() - 1);
			clear("");
			for (String line : fileLinesArray) {
				Files.write(FILE_PATH, line.getBytes(), StandardOpenOption.APPEND);
				Files.write(FILE_PATH, "\n".getBytes(), StandardOpenOption.APPEND);
			}
			return String.format(SORT_MESSAGE, FILE_NAME);
		} catch (IOException e) {
			return String.format(INVALID_MESSAGE, userCommand);
		}
	}

	/*
	 * This function is the outer function for the quicksort. It will recurse until the quicksort has finished
	 * and the file is completely sorted.
	 */
	private static void sortLines(String[] lines, int begin, int end) {
		if (begin < end) {
			int partition = partition(lines, begin, end);
			sortLines(lines, begin, partition - 1);
			sortLines(lines, partition + 1, end);
		}
	}

	/*
	 * This function is the inner function for the quicksort. It acts as the partition function and is called 
	 * multiple times until the lines are completely sorted. 
	 */
	private static int partition(String[] lines, int begin, int end) {
		String pivot = lines[end];
		int swap = begin;
		for (int i = begin; i < end; i++) {
			if (lines[i].compareTo(pivot) < 0) {
				String temporary = lines[i];
				lines[i] = lines[swap];
				lines[swap] = temporary;
				swap++;
			}
		}
		lines[end] = lines[swap];
		lines[swap] = pivot;
		return swap;
	}
	
	/*
	 * This function returns a formatted string corresponding to the line numbers that have matches for a given search
	 * string. If no matches are found, the file will be shown to have no matches. If the file is unable to be opened,
	 * a message declaring the action as invalid will appear to the user. 
	 * 
	 */
	private static String search(String userCommand) {
		try {
			List<String> fileLines = Files.readAllLines(FILE_PATH, Charset.forName("UTF-8"));
			String searchWord = removeFirstWord(userCommand);
			List<Integer> matchedLines = findLineMatches(searchWord, fileLines);
			if (matchedLines.size() > 0) {
				return String.format(SEARCH_MESSAGE, searchWord, lineNumbersToString(matchedLines));
			} else {
				return "this file has no found matches.";
			}
		} catch (IOException e) {
			return String.format(INVALID_MESSAGE, userCommand);
		}
	}
	
	/*
	 * This function, given a search word and a list of Strings that correspond to lines in a file, will
	 * return the line numbers that have a match of the string anywhere in that line. It aids in the search function.
	 */
	private static List<Integer> findLineMatches(String searchWord, List<String> fileLines) {
		List<Integer> matchedLines = new ArrayList<Integer>();
		int currentLineNumber = 1;
		for (String line : fileLines) {
			if (line.contains(searchWord.subSequence(0, searchWord.length()))) {
				matchedLines.add(currentLineNumber);
			}
			currentLineNumber++;
		}
		return matchedLines;
	}
	
	/*
	 * This function converts a list of line Numbers to a correctly formatted list of integers separated
	 * by commas. This aids the programming when searching for string matches, so that the user is able
	 * to correct deduce which line numbers have matches easily.
	 */
	private static String lineNumbersToString(List<Integer> lineNumbers) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < lineNumbers.size(); i++) {
			if (i != lineNumbers.size() - 1) {
				stringBuilder.append(lineNumbers.get(i) + ", ");
			} else {
				stringBuilder.append(lineNumbers.get(i));
			}
		}
		return stringBuilder.toString();
	}

	/*
	 * This function, give a string userCommand, will return the first word in userCommand, as separated by spaces.
	 */
	private static String getFirstWord(String userCommand) {
		String commandTypeString = userCommand.trim().split("\\s+")[0];
		return commandTypeString;
	}

	/*
	 * This function, given a string userCommand, will remove the first word from a string (separated by spaces)
	 * and will return the string with the first word and the space removed.
	 */
	private static String removeFirstWord(String userCommand) {
		return userCommand.replace(getFirstWord(userCommand), "").trim();
	}

	/*
	 * This function will determine the command type enumeration that a command is based
	 * off of the commandTypeString that is supplied as a method argument and return this as an enumeration. 
	 */
	private static COMMAND_TYPE determineCommandType(String commandTypeString) {
		if (commandTypeString == null) {
			throw new Error("command type string cannot be null!");
		} else if (commandTypeString.equalsIgnoreCase("add")) {
			return COMMAND_TYPE.ADD;
		} else if (commandTypeString.equalsIgnoreCase("display")) {
			return COMMAND_TYPE.DISPLAY;
		} else if (commandTypeString.equalsIgnoreCase("exit")) {
			return COMMAND_TYPE.EXIT;
		} else if (commandTypeString.equalsIgnoreCase("clear")) {
			return COMMAND_TYPE.CLEAR;
		} else if (commandTypeString.equalsIgnoreCase("delete")) {
			return COMMAND_TYPE.DELETE;
		} else if (commandTypeString.equalsIgnoreCase("sort")) {
			return COMMAND_TYPE.SORT;
		} else if (commandTypeString.equalsIgnoreCase("search")) {
			return COMMAND_TYPE.SEARCH;
		} else {
			return COMMAND_TYPE.INVALID;
		}
	}
}
