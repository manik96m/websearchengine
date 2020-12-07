package websearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;

public class WebSearchEngine {
	private final String splitting = "[[ ]*|[,]*|[)]*|[(]*|[\"]*|[;]*|[-]*|[:]*|[']*|[Ì]*|[\\.]*|[:]*|[/]*|[!]*|[?]*|[+]*]+";
	private TrieST<HashMap<String, Integer>> webPageWordsTrie;
	List<String> pageNames;
	HashMap<String, Integer> frequency;

	public WebSearchEngine() throws IOException {
		this.webPageWordsTrie = new TrieST<HashMap<String, Integer>>();
		this.pageNames = new ArrayList<String>();
	}

	private void parseWebPages(String webpageDirectoryPath) {
		In in = new In(webpageDirectoryPath);
		String[] allwords = null;
		while (!in.isEmpty()) {
			allwords = in.readAllLines();
		}
		System.out.println("There are total " + allwords.length + " URL's in the file.");
		System.out.println("Parsing the webpages...");
		for (String s : allwords) {
			this.pageNames.add(s);
		}

		// iterate through all the pages and add the words to Trie
		for (int i = 0; i < this.pageNames.size(); i++) {
			String currentPageName = this.pageNames.get(i);
			System.out.println(currentPageName);
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

	// Web Crawler using file name
	private String webCrawler(String currentPageName) throws IOException {
		org.jsoup.nodes.Document doc = null;
		try {
			doc = Jsoup.connect(currentPageName).get();
		} catch (IOException e) {
			System.err.println(e.getMessage() + "\t " + currentPageName);
		}

		return doc == null ? "" : doc.body().text();
	}

	private List<String> cleanText(String text) {
		text = text.replaceAll("[^a-zA-Z0-9]", " ");
		return Arrays.asList(text.toLowerCase().split(splitting));
	}

	// To get the URL's for searched word/sentence
	private Map<String, Integer> webSearch(String searchWord) {
		final Map<String, Integer> urlList = new HashMap<String, Integer>();
		String[] words = searchWord.split(splitting);
		try {
			for (int currentWordIndex = 0; currentWordIndex < words.length; currentWordIndex++) {
				String currentWord = words[currentWordIndex];
				if (currentWordIndex == 0)
					urlList.putAll(this.webPageWordsTrie.get(currentWord));
				else {
					urlList.keySet().retainAll(this.webPageWordsTrie.get(words[currentWordIndex]).keySet());
					this.webPageWordsTrie.get(currentWord).forEach((k, v) -> urlList.merge(k, v, Integer::sum));
				}
			}
		} catch (Exception e) {
			System.out.println("\t No URL found for the search performed.");
		}

		return urlList;
	}

	private Map<String, Integer> sortWebSearch(Map<String, Integer> urlList) {
		return sortByValue((HashMap<String, Integer>) urlList);
	}

	// function to sort hashmap by values
	private static Map<String, Integer> sortByValue(HashMap<String, Integer> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// put data from sorted list to hashmap
		HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}

	public static void main(String arg[]) throws IOException {
		String webPageDirectoryPath = "src/W3C Web Pages/output.txt";
		WebSearchEngine webSearchEngine = new WebSearchEngine();
		webSearchEngine.parseWebPages(webPageDirectoryPath);
		Scanner s = new Scanner(System.in);
		String continueValue = "";

		do {
			System.out.println("Enter the word/words to fetch top URL's/Files");
			String searchWord = s.nextLine();
			System.out.println("-----------------------List of URL's/File Names in sorted order---------------");

			Map<String, Integer> urlList = webSearchEngine
					.sortWebSearch(webSearchEngine.webSearch(searchWord.toLowerCase()));
			if (!urlList.isEmpty()) {
				System.out.println("Frequency\tURL");
			}
			for (Map.Entry<String, Integer> entry : urlList.entrySet()) {
				System.out.println(entry.getValue() + "\t\t" + entry.getKey());
			}
			System.out.println("Do you want to continue yes/no");
			continueValue = s.nextLine().trim();
		} while (continueValue.toLowerCase().equals("yes"));
	}
}
