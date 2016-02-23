import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextBuddyTest {

	@Before
    public void setUp() {
		TextBuddy.FILE_NAME = "test.txt";
		try {
			TextBuddy.openFile(TextBuddy.FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @After
    public void tearDown() {
        TextBuddy.clear("");
    }

	@Test
	public void testInvalidCommand() {
		String invalid = "invalid command format: ";
		if (!invalid.equals(TextBuddy.executeCommand(""))) {
			fail("TextBuddy unable to recognize invalid commands.");
		}
	}
	
	@Test
	public void testAdd() {
		TextBuddy.executeCommand("add test");
		try {
			List<String> fileLines = Files.readAllLines(TextBuddy.FILE_PATH, Charset.forName("UTF-8"));
			if (!fileLines.get(0).equals("test")) {
				fail("TextBuddy unable to add lines correctly.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDelete() {
		TextBuddy.executeCommand("delete 1");
		try {
			List<String> fileLines = Files.readAllLines(TextBuddy.FILE_PATH, Charset.forName("UTF-8"));
			if (fileLines.size() != 0) {
				fail("TextBuddy unable to delete lines correctly.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSearch() {
		TextBuddy.executeCommand("add c");
		TextBuddy.executeCommand("add b");
		TextBuddy.executeCommand("add a");
		String searchResults = TextBuddy.executeCommand("search b");
		if (!searchResults.equals("b found in line number(s): 2")) {
			fail("TextBuddy unable to search lines correctly.");
		}
	}
	
	@Test
	public void testSort() {
		TextBuddy.executeCommand("add c");
		TextBuddy.executeCommand("add b");
		TextBuddy.executeCommand("add a");
		TextBuddy.executeCommand("sort");
		try {
			List<String> fileLines = Files.readAllLines(TextBuddy.FILE_PATH, Charset.forName("UTF-8"));
			if (!fileLines.get(0).equals("a") || !fileLines.get(1).equals("b") || !fileLines.get(2).equals("c")) {
				fail("TextBuddy unable to sort lines correctly.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testClear() {
		TextBuddy.executeCommand("clear");
		try {
			List<String> fileLines = Files.readAllLines(TextBuddy.FILE_PATH, Charset.forName("UTF-8"));
			if (fileLines.size() != 0) {
				fail("TextBuddy unable to clear lines correctly.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
