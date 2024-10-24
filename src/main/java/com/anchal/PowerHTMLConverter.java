package com.anchal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

import com.graphbuilder.struc.LinkedList;
import com.lowagie.text.DocumentException;

public class PowerHTMLConverter {

	private static boolean IS_THUMBNAIL_FORMAT_REQ_FLAG = false;
	// private static boolean IS_PDF_OUTPUT_REQUIRED = false;
	private static int EXPANDED_LINK_COUNTER = 0;
	private static int LINK_TO_EXPAND_LIMIT = 0;
	private static int TOTAL_LINK_COUNT = 0;
	private static String INPUT_FILENAME = null;

	// test paths
	private static final String INPUT_FILE_DIR = ".\\input\\";
	private static final String OUTPUT_FILE_DIR = ".\\output\\";
	private static final String INPUT_OLD_DIR = ".\\input\\old\\";
	private static final String AF_CSS = "../css/aanchalFashionCustomStyles.css";
	private static final String AF_MINIPREVIEW_JS = "../js/jquery.minipreview.js";
	private static final String AF_JS = "../js/aanchalFashionCustomScript.js";
	private static final String AF_MINIPREVIEW_CSS = "../css/jquery.minipreview.css";
	private static final String AF_POST_JS = "../js/aanchalFashionCustomPostScript.js";

	// prod paths
	// private static final String INPUT_FILE_DIR = "..\\input\\";
	// private static final String OUTPUT_FILE_DIR = "..\\output\\";
	// private static String JAR_DIR = "./../PowerHtmlConverter/";
	// private static String AF_CSS = JAR_DIR +
	// "css/aanchalFashionCustomStyles.css";
	// private static String AF_MINIPREVIEW_JS = JAR_DIR +
	// "js/jquery.minipreview.js";
	// private static String AF_JS = JAR_DIR + "js/aanchalFashionCustomScript.js";
	// private static final String AF_MINIPREVIEW_CSS = JAR_DIR +
	// "css/jquery.minipreview.css";
	// private static final String AF_POST_JS = JAR_DIR +
	// "js/aanchalFashionCustomPostScript.js";

	private static final String TEMP_FILE_SUFFIX = "tmp_";
	private static final String HTML_FILE_SUFFIX = "html";
	private static final String DOT_HTML_FILE_SUFFIX = ".html";
	private static final String HEAD_TAG = "head";
	private static final String TITLE_TAG = "title";
	private static final String STYLE_TAG = "style";
	private static final String META_TAG = "meta";
	private static final String SCRIPT_TAG = "script";
	private static final String LINK_TAG = "link";
	private static final String IMAGE_TAG = "img";
	private static final String ANCHOR_TAG = "a";
	private static final String ROW_TAG = "tr";
	private static final String URL_IDENTIFIER = "http";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	private static final String DOT_PDF_FILE_SUFFIX = ".pdf";
	private static final String BOOKMARK_TAG = "bookmarkSelect";
	private static final String BODY_TAG = "body";
	private static final String SELECT_TAG = "select";

	private static final List<String> HTTP_TO_LH3_LIST = new java.util.LinkedList<>();
	private static final ConcurrentHashMap<String, String> LINKS_TO_EXPAND_MAP = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		// InputStream is =
		// PowerHTMLConverter.class.getResourceAsStream("/config.properties" );
		// Properties properties = new Properties();

		try {
			// properties.load(is);
			// JAR_DIR = properties.getProperty("jar_path", JAR_DIR);

			// for some reports to avoid JAX error for more than 64000 entities parse issue
			System.setProperty("jdk.xml.entityExpansionLimit", "0");

			File inputFile = correctHTMLSyntaxErrors();
			if (inputFile == null) {
				System.out.println("App exited.");
				return;
			}

			Document doc = getDocument(inputFile);

			countLinks(doc);

			takeUserInputs();

			expandUrlsUsingThreads();

			addCustomStylesScriptsToHeadTag(doc);

			addBookmarks(doc);

			transformUrls(doc, false);

			export(inputFile, doc);

			System.out.println("App exited.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addBookmarks(Document doc) {
		/*
		 * if(IS_THUMBNAIL_FORMAT_REQ_FLAG) { Element bodyEle =
		 * doc.getElementById(BODY_TAG); Element showBookmarksFlagEle =
		 * doc.createElement("input"); showBookmarksFlagEle.setAttribute("type",
		 * "text"); showBookmarksFlagEle.setAttribute("id", "showBookmarksFlag");
		 * showBookmarksFlagEle.setAttribute("value", "false");// don't show dropdown
		 * showBookmarksFlagEle.setAttribute("hidden", "true");
		 * bodyEle.appendChild(showBookmarksFlagEle);
		 * System.out.println("Don't show bookmarks.."); }
		 */

		NodeList bodyEleNodes = doc.getElementsByTagName(BODY_TAG);
		if (bodyEleNodes != null && bodyEleNodes.getLength() == 1) {
			Node bodyEle = bodyEleNodes.item(0);
			// create bookmark div and dropdown
			Element topNavDiv = doc.createElement("div");
			topNavDiv.setAttribute("class", "topnav");
			Element bookmarkDropdown = doc.createElement(SELECT_TAG);
			bookmarkDropdown.setAttribute("class", "dropdown");
			bookmarkDropdown.setAttribute("id", BOOKMARK_TAG);
			topNavDiv.appendChild(bookmarkDropdown);

			// create back to top btn
			Element topBtn = doc.createElement("button");
			topBtn.setAttribute("class", "topBtn");
			topBtn.setAttribute("title", "Go to top");
			topBtn.setAttribute("id", "topBtn");
			topBtn.setAttribute("onclick", "topFunction()");
			topBtn.setTextContent("Top");

			// add div as the first element
			NodeList bodyChildNodes = bodyEle.getChildNodes();
			if (bodyChildNodes != null && bodyChildNodes.getLength() > 0) {
				// remove all child nodes
				for (int i = 0; i < bodyChildNodes.getLength(); i++) {
					bodyEle.removeChild(bodyChildNodes.item(i));
				}
				// add topnav div and topbtn as the first child
				bodyEle.appendChild(topNavDiv);
				bodyEle.appendChild(topBtn);

				// add back the child nodes
				for (int i = 0; i < bodyChildNodes.getLength(); i++) {
					bodyEle.appendChild(bodyChildNodes.item(i));
				}
			}
			System.out.println("Bookmark dropdown added..");
		}
	}

	private static void countLinks(Document doc) {
		System.out.println("Scanning for links..");
		transformUrls(doc, true);
		System.out.println("Total number of links found - " + TOTAL_LINK_COUNT);
	}

	private static void takeUserInputs() {
		Scanner scanner = new Scanner(System.in); // Create a Scanner object
		// System.out.println("Enter HTML filename with fullpath to convert:");
		// String filename = scanner.nextLine(); // Read user input

		System.out.print("Select report format - Thumbnail or Image hover(default)? (T/Hit Enter): ");
		String reportFormatSelectionStr = scanner.nextLine(); // Read user input
		if (reportFormatSelectionStr.equalsIgnoreCase("T")) {
			IS_THUMBNAIL_FORMAT_REQ_FLAG = true;
		}

		System.out.print("How many links to expand? (Enter any number/Hit Enter for all): ");
		String linkLimitStr = scanner.nextLine(); // Read user input
		if (!linkLimitStr.equalsIgnoreCase("")) {
			try {
				LINK_TO_EXPAND_LIMIT = Integer.parseInt(linkLimitStr);
			} catch (NumberFormatException nfe) {
				System.err.println("Error - Please retry with a valid number");
			}
		} else {
			LINK_TO_EXPAND_LIMIT = TOTAL_LINK_COUNT;
		}

		// System.out.print("Export as HTML or PDF or both? (H/P/B): ");
		// String isPdfStr = scanner.nextLine(); // Read user input
		// if (isPdfStr.equalsIgnoreCase("P") || isPdfStr.equalsIgnoreCase("B")) {
		// IS_PDF_OUTPUT_REQUIRED = true;
		// }

		scanner.close();
	}

	private static void export(File inputFile, Document doc)
			throws IOException, DocumentException, TransformerException {
		// if(IS_PDF_OUTPUT_REQUIRED) {
		File outputFile = new File(OUTPUT_FILE_DIR + INPUT_FILENAME.replace(DOT_HTML_FILE_SUFFIX, "") + " - "
				+ getCurrentDateTime() + DOT_PDF_FILE_SUFFIX);
		transformHtmlToPdf(inputFile, outputFile);

		outputFile = new File(OUTPUT_FILE_DIR + INPUT_FILENAME.replace(DOT_HTML_FILE_SUFFIX, "") + " - "
				+ getCurrentDateTime() + DOT_HTML_FILE_SUFFIX);
		transformHtmlToHTML(doc, outputFile);
		// }
	}

	private static Document getDocument(File inputFile) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		return doc;
	}

	private static void transformHtmlToHTML(Document doc, File outputFile) throws IOException, TransformerException {
		// Save the modified document
		DOMSource source = new DOMSource(doc);
		FileWriter writer = new FileWriter(outputFile);
		StreamResult result = new StreamResult(writer);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, HTML_FILE_SUFFIX);
		transformer.transform(source, result);
		System.out.println("HTML file modified successfully.");
	}

	private static String getCurrentDateTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

	public static String convertTime(long time) {
		Date date = new Date(time);
		Format format = new SimpleDateFormat(DATE_TIME_FORMAT);
		return format.format(date);
	}

	private static void transformUrls(Document doc, boolean countLinksFlag) {
		if (!countLinksFlag) {
			System.out.println("Rendering urls..may take some time");
		}
		// Find the http links to modify
		NodeList trs = doc.getElementsByTagName(ROW_TAG);
		for (int i = 0; i < trs.getLength(); i++) {
			Node tr = trs.item(i);
			if (tr != null && tr.hasChildNodes()) {

				NodeList tds = tr.getChildNodes();
				for (int j = 0; j < tds.getLength(); j++) {
					Node td = tds.item(j);
					if (td != null && td.hasChildNodes()) {
						NodeList nobrNodes = td.getChildNodes();
						if (nobrNodes.getLength() == 1) {
							Node nobrNode = nobrNodes.item(0);
							String urlText = nobrNode.getTextContent();
							// check for url
							if (urlText.toLowerCase().startsWith(URL_IDENTIFIER)) {

								if (countLinksFlag) {
									try {
										HTTP_TO_LH3_LIST.add(urlText);
										TOTAL_LINK_COUNT++;
									} catch (Exception e) {
										e.printStackTrace();
									}
									continue;
								}

								String lh3Url = LINKS_TO_EXPAND_MAP.get(urlText);
								if (IS_THUMBNAIL_FORMAT_REQ_FLAG) {
									// set id
									((Element) td).setAttribute("id", "tinyurl_img");

									// remove text
									nobrNode.setTextContent("");

									if (lh3Url == null || "".equals(lh3Url)) {
										// create anchor tag
										Element anchorEle = doc.createElement(ANCHOR_TAG);
										anchorEle.setAttribute("href", urlText);
										anchorEle.setTextContent(urlText);
										nobrNode.appendChild(anchorEle);
									} else {
										EXPANDED_LINK_COUNTER++;
										// create image tag
										Element imgEle = doc.createElement(IMAGE_TAG);
										// imgEle.setTextContent(urlText);
										imgEle.setAttribute("src", urlText);
										nobrNode.appendChild(imgEle);
									}
								} else {
									// set id
									((Element) td).setAttribute("id", "tinyurl");

									// remove text
									nobrNode.setTextContent("");

									// create anchor tag
									Element anchorEle = doc.createElement(ANCHOR_TAG);
									if (lh3Url == null || "".equals(lh3Url)) {
										anchorEle.setTextContent(urlText);
										anchorEle.setAttribute("href", urlText);
									} else {
										EXPANDED_LINK_COUNTER++;
										anchorEle.setAttribute("href", lh3Url);
										anchorEle.setTextContent("Image Hover");
									}

									nobrNode.appendChild(anchorEle);
								}
							}
						}
					}
				}
			}
		}
		if (!countLinksFlag) {
			// once expansion limit reached stop updating tds just count http links
			System.out.println("Total number of expanded links - " + EXPANDED_LINK_COUNTER + "/" + LINK_TO_EXPAND_LIMIT
					+ "/" + TOTAL_LINK_COUNT);
		}
	}

	private static ExecutorService expandUrlsUsingThreads() {

		Date startDate = new Date();// Set start date

		int threadCnt = 0;

		int coreCnt = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(coreCnt);

		// add first n links in order to the expansion map
		for (String key : HTTP_TO_LH3_LIST) {
			if (threadCnt < LINK_TO_EXPAND_LIMIT) {
				LINKS_TO_EXPAND_MAP.put(key, "");
				threadCnt++;
			} else {
				break;
			}
		}

		// expand only selected first n links
		for (Map.Entry<String, String> entry : LINKS_TO_EXPAND_MAP.entrySet()) {
			String urlText = entry.getKey();

			Runnable expansionTask = new Runnable() {
				@Override
				public void run() {
					// System.out.println("Thread started");
					String lh3Url = expandUrlAndFetchLh3Link(urlText);
					if (lh3Url == null) {
						System.out.println("retrying..");
						// retry connection one more time
						lh3Url = expandUrlAndFetchLh3Link(urlText);
					}
					if(IS_THUMBNAIL_FORMAT_REQ_FLAG) {
						entry.setValue(lh3Url == null ? "" : lh3Url);// for thumbnail use 250 smaller size image
					} else {
						entry.setValue(lh3Url == null ? "" : lh3Url.replace("s250", "s450"));// for image hover - increase image size from 250 to 450
					}
				};
			};
			service.execute(expansionTask);
		}

		System.out.println("###### All tasks are submitted. ###### ");

		shutdownAndAwaitTermination(service);

		System.out.println("###### All tasks are completed. ###### ");

		// calculate time taken to expand links
		Date endDate = new Date();// Set end date
		long duration = endDate.getTime() - startDate.getTime();
		long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		System.out.println("Total time taken - " + diffInSeconds + " seconds");
		System.out.println("Average time taken per link- " + diffInSeconds/LINKS_TO_EXPAND_MAP.size() + " seconds");

		return service;
	}

	private static void shutdownAndAwaitTermination(ExecutorService executorService) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(90, TimeUnit.MINUTES)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException ie) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private static String expandUrlAndFetchLh3Link(String urlText) {
		String finalUrl = "";

		String expandedUrl = expandURL(urlText);
		if (expandedUrl != null) {
			// System.out.println("expandedUrl - " + expandedUrl);

			// Writing the expanded URL in the third column
			String lh3Url = getLh3Link(expandedUrl);
			System.out.println("lh3Url - " + lh3Url);
			return lh3Url;
		}
		return finalUrl;
	}

	private static void addCustomStylesScriptsToHeadTag(Document doc) {
		// Find the body element to modify
		NodeList headNodes = doc.getElementsByTagName(HEAD_TAG);
		if (headNodes.getLength() == 1) {
			Node head = headNodes.item(0);
			if (head != null && head.hasChildNodes()) {
				NodeList childNodes = head.getChildNodes();

				Node title = null;
				Node meta = null;
				Node style = null;

				if (childNodes.getLength() > 0) {
					for (int j = 0; j < childNodes.getLength(); j++) {
						if (childNodes.item(j).getNodeName().equals(TITLE_TAG)) {
							title = childNodes.item(j);
						} else if (childNodes.item(j).getNodeName().equals(META_TAG)) {
							meta = childNodes.item(j);
						} else if (childNodes.item(j).getNodeName().equals(STYLE_TAG)) {
							style = childNodes.item(j);
						}
					}

					if (title == null && meta == null && style == null) {
						return;
					}

					String htmlTitle = INPUT_FILENAME + " - Output";
					if (IS_THUMBNAIL_FORMAT_REQ_FLAG) {
						title.setTextContent(htmlTitle + ": Thumbnail format");
					} else {
						title.setTextContent(htmlTitle + ": Hover format");
					}

					// create script tag
					Element scriptEle = doc.createElement(SCRIPT_TAG);
					scriptEle.setAttribute("src", "https://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js");

					// create link tag
					Element linkEle = doc.createElement(LINK_TAG);
					linkEle.setAttribute("href",
							"https://rawgit.com/shaneapen/Image-Preview-for-Links/master/image_preview_for_links.css");
					linkEle.setAttribute("rel", "stylesheet");

					// create link tag
					Element mpLinkEle = doc.createElement(LINK_TAG);
					mpLinkEle.setAttribute("href", AF_MINIPREVIEW_CSS);
					mpLinkEle.setAttribute("rel", "stylesheet");

					// create link tag
					Element afCssEle = doc.createElement(LINK_TAG);
					afCssEle.setAttribute("href", AF_CSS);
					afCssEle.setAttribute("rel", "stylesheet");

					// create script tag
					Element mpScriptEle = doc.createElement(SCRIPT_TAG);
					mpScriptEle.setAttribute("src", AF_MINIPREVIEW_JS);

					// create script tag
					Element afScriptEle = doc.createElement(SCRIPT_TAG);
					afScriptEle.setAttribute("src", AF_JS);

					head.removeChild(title);
					head.removeChild(meta);
					head.removeChild(style);

					// head.appendChild(meta);
					head.appendChild(title);
					head.appendChild(scriptEle);
					head.appendChild(linkEle);
					head.appendChild(mpScriptEle);
					head.appendChild(afScriptEle);
					head.appendChild(afCssEle);
					head.appendChild(style);

					// if(IS_PDF_OUTPUT_REQUIRED) {
					// create script tag
//					Element pdfStyleEle = doc.createElement(STYLE_TAG);
//					pdfStyleEle.setAttribute("type", "text/css");
//					pdfStyleEle.setTextContent("@page { size: A4 landscape;}");
//					head.appendChild(pdfStyleEle);
					// }
				}
			}

			// create script tag
			Element afPostScriptEle = doc.createElement(SCRIPT_TAG);
			afPostScriptEle.setAttribute("src", AF_POST_JS);
			head.getParentNode().appendChild(afPostScriptEle);
		}

		System.out.println("Adding custom styles scripts...");
	}

	private static String expandURL(String tinyUrl) {
		try {
			Connection connection = Jsoup.connect(tinyUrl).timeout(20 * 1000).followRedirects(true);
			Connection.Response response = connection.execute();
			return response.url().toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getLh3Link(String expandedUrl) {
		try {

			// System.out.println("expandedUrl inside getLh3Link - " + expandedUrl);
			// Fetch the HTML content from the URL
			org.jsoup.nodes.Document doc = Jsoup.connect(expandedUrl).get();

			// Find the <img> tags in the document
			Elements imgTags = doc.select("img");

			// System.out.println("imgTags - " + imgTags);

			// Iterate through each <img> tag to find the image URL
			for (org.jsoup.nodes.Element img : imgTags) {
				String imgUrl = img.attr("src");

				// Check if the img tag src contains a valid image URL
				if (isValidImageUrl(imgUrl)) {
					// System.out.println("Direct Image URL: " + imgUrl);
					return imgUrl;
					// break; // Stop after finding the first valid image URL
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	// Function to check if a given URL ends with a common image extension
	private static boolean isValidImageUrl(String url) {
		return url != null && (url.contains("lh3.googleusercontent.com") || url.endsWith(".jpg")
				|| url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".gif"));
	}

	private static File correctHTMLSyntaxErrors() throws IOException {
		File inputFile = null;
		inputFile = getLatestInputFile();
		if (inputFile == null) {
			System.err.println("Error - No report file found in the input folder. Please add one and retry.");
			return null;
		}

		INPUT_FILENAME = inputFile.getName();

		// to fix error - Open quote is expected for attribute "leftMargin" associated
		// with an element type "body".
		// replace leftMargin=10 topMargin=10 rightMargin=10 bottomMargin=10
		FileReader fileReader = new FileReader(inputFile);
		File tempFile = new File(INPUT_FILE_DIR + TEMP_FILE_SUFFIX + INPUT_FILENAME);
		try (BufferedReader br = new BufferedReader(fileReader)) {
			FileWriter fileWriter = new FileWriter(tempFile);
			try (BufferedWriter bw = new BufferedWriter(fileWriter)) {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.contains("leftMargin=10 topMargin=10 rightMargin=10 bottomMargin=10")) {
						System.out.println("Body style corrected");
						line = line.replace("leftMargin=10 topMargin=10 rightMargin=10 bottomMargin=10",
								"leftMargin='10' topMargin='10' rightMargin='10' bottomMargin='10'");
					}
					bw.write(line);
					bw.newLine();
				}
			}
		}

		// Once everything is complete, delete old file..
		inputFile.delete();

		// And rename tmp file's name to old file name
		tempFile.renameTo(inputFile);

		System.out.println("Corrected file syntax in file - " + inputFile.getAbsolutePath());

		return inputFile;
	}

	private static File getLatestInputFile() {
		// Load the latest report file from input folder
		File inputFileDir = new File(INPUT_FILE_DIR);
		System.out.println("Scanning input folder for html files..." + inputFileDir.getAbsolutePath());
		if (inputFileDir == null || inputFileDir.list() == null) {
			System.err.println("Error - No input folder found. Please create one and retry.");
			return null;
		}

		File[] files = inputFileDir.listFiles(File::isFile);
		long lastModifiedTime = Long.MIN_VALUE;
		File chosenFile = null;

		if (files != null) {
			for (File file : files) {
				try {
					if (file.getCanonicalPath().endsWith(HTML_FILE_SUFFIX) && file.lastModified() > lastModifiedTime) {
						chosenFile = file;
						lastModifiedTime = file.lastModified();
						System.out.println("Converting latest report file - " + chosenFile.getAbsolutePath());
					} else {
						// move other files to old folder
						File oldDir = new File(INPUT_OLD_DIR);
						if (!oldDir.exists()) {
							oldDir.mkdirs();
							System.out.println("old folder created.." + oldDir.getAbsolutePath());
						}

						String newfileNameWithTimeStamp = oldDir.getCanonicalPath() + File.separator
								+ file.getName().replace(".", "_" + convertTime(file.lastModified()) + ".");
						Files.move(Paths.get(file.getCanonicalPath()), Paths.get(newfileNameWithTimeStamp),
								StandardCopyOption.REPLACE_EXISTING);
						System.out.println(
								"old files renamed with timestamp & to avoid override .." + file.getAbsolutePath());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return chosenFile;
	}

	public static void transformHtmlToPdf(File inputFile, File outputFile) throws IOException, DocumentException {
		ITextRenderer renderer = new ITextRenderer();
		/*
		 * This part ist jsoup related. 'doc.toString()' does nothing else than
		 * returning the Html of 'doc' as a string.
		 * 
		 * You can set it like in your code too.
		 */
		renderer.setDocument(inputFile);

		OutputStream os = new FileOutputStream(outputFile);
		renderer.layout();
		renderer.createPDF(os);
		os.flush();
		os.close();
		System.out.println("PDF created successfully.");
	}

}