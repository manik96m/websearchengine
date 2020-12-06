package websearch;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jsoup.Jsoup;  
import org.jsoup.nodes.Document;  
import org.jsoup.nodes.Element;  
import org.jsoup.select.Elements;  
public class WebCrawler {  
	private void webCrawlerwithURL(String link) throws IOException {
		Document doc = Jsoup.connect(link).userAgent("Mozilla").get();
		Elements links = doc.select("a[href]");  
        System.out.println("SIZE "+links.size());
        PrintWriter f0 = new PrintWriter(new FileWriter("output.txt",true));
        String newLine = System.getProperty("line.separator");
        for (Element linkin : links) {  
            System.out.println("\nlink in fn : " + linkin.attr("abs:href"));  
            f0.write(linkin.attr("abs:href") + newLine);
            System.out.println("text in fn : " + linkin.text());  
        }  
        f0.close();
	}
     public static void main( String[] args ) throws IOException{  
    	 WebCrawler wc=new WebCrawler();
            Document doc = Jsoup.connect("http://www.google.com").get();  
            Elements links = doc.select("a[href]");  
            System.out.println("SIZE "+links.size());
            PrintWriter writer = new PrintWriter("src/W3C Web Pages/output.txt");
            writer.print("");
            writer.close();
            for (Element link : links) {  
                System.out.println("\nlink : " + link.attr("abs:href"));  
                wc.webCrawlerwithURL(link.attr("abs:href"));
                System.out.println("text : " + link.text());  
            }  
}  
}  