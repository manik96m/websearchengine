package websearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;

public class WebSearchEngine {
	private final String splitting = "[[ ]*|[,]*|[)]*|[(]*|[\"]*|[;]*|[-]*|[:]*|[']*|[’]*|[\\.]*|[:]*|[/]*|[!]*|[?]*|[+]*]+";
	private TrieST<HashMap<String, Integer>> webPageWordsTrie;
	List<String> pageNames;
	HashMap<String, Integer> frequency;

	public WebSearchEngine() throws IOException {
		this.webPageWordsTrie = new TrieST<HashMap<String, Integer>>();
		this.pageNames = new ArrayList<String>();
	}

	private void parseWebPages(String webpageDirectoryPath) {
		// ToDo: can give null pointer
		File webpageDirectory = new File(webpageDirectoryPath);
		File[] htmlFilesList = webpageDirectory.listFiles();

		// adding HTML file names to the list
		for (File s : htmlFilesList) {
			if (s.isFile())
				this.pageNames.add(s.getAbsolutePath());
		}

		// iterate through all the pages and add the words to Trie
		for (int i = 0; i < this.pageNames.size(); i++) {
			String currentPageName = this.pageNames.get(i);
			String currentPageText;
			List<String> urlWords;
			
			// extract the text and remove unexpected characters
			try {
				currentPageText = this.webCrawler(currentPageName);
				urlWords = this.cleanText(currentPageText);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			// iterate the words and put it to the Trie
			for (String s : urlWords) {
				HashMap<String, Integer> occurenceList = this.webPageWordsTrie.get(s);

				// check if word is present in the Trie
				if (occurenceList == null) {
					// insert a new word referencing to a new Hashmap
					HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
					// insert current URL in Hashmap and set the occurrence count to 1
					hashMap.put(this.pageNames.get(i), 1);
					this.webPageWordsTrie.put(s, hashMap);
				} else {
					// check if the current URL exists in the Hashmap
					if (occurenceList.containsKey(this.pageNames.get(i))) {
						// for current URL increase the occurrence count
						occurenceList.replace(this.pageNames.get(i), occurenceList.get(this.pageNames.get(i)) + 1);
					} else {
						// insert current URL in Hashmap and set the occurrence count to 1
						occurenceList.put(this.pageNames.get(i), 1);
					}
				}
			}
		}
		System.out.println("The trie has " + this.webPageWordsTrie.size() + " entries.");
	}

	private String webCrawler(String currentPageName) throws IOException {
		File currentFile = new File(currentPageName);
		org.jsoup.nodes.Document doc = Jsoup.parse(currentFile, "UTF-8");
		return doc.body().text();
	}
	
	private List<String> cleanText(String text) {
		text = text.replaceAll("[^a-zA-Z0-9]", " ");
		return Arrays.asList(text.toLowerCase().split(splitting));
	}

	public static void main(String arg[]) throws IOException {
		String webPageDirectoryPath = "src/W3C Web Pages";
		// ToDo: different path for MAC and WINDOWS
		WebSearchEngine webSearchEngine = new WebSearchEngine();
		webSearchEngine.parseWebPages(webPageDirectoryPath);
	}
}
