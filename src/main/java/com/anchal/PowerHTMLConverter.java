package com.anchal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;

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

import com.lowagie.text.DocumentException;

public class PowerHTMLConverter {

	private static boolean IS_THUMBNAIL_FORMAT_REQ_FLAG = false;
	private static boolean IS_PDF_OUTPUT_REQUIRED = false;
	private static int EXPANDED_LINK_COUNTER = 0;
	private static int LINK_TO_EXPAND_LIMIT = 0;
	private static int TOTAL_LINK_COUNT = 0;
	private static String INPUT_FILENAME = null;

	private static final String INPUT_FILE_DIR = "..\\input\\";
	private static final String OUTPUT_FILE_DIR = "..\\output\\";
	private static final String PATH_SEPARATOR = "\\";
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
	private static String JAR_DIR = "./../PowerHtmlConverter/";
	

	public static void main(String[] args) {
		//InputStream is = PowerHTMLConverter.class.getResourceAsStream("/config.properties" );
		//Properties properties = new Properties();

		try {
			//properties.load(is);
			//JAR_DIR = properties.getProperty("jar_path", JAR_DIR);
			
			File inputFile = correctHTMLSyntaxErrors();
			if(inputFile == null) {
				System.out.println("App exited.");
				return;
			}
			
			Document doc = getDocument(inputFile);
			
			countLinks(doc);

			takeUserInputs();

			addCustomStylesScriptsToHeadTag(doc);

			findAndExpandUrl(doc, false);
			
			export(inputFile, doc);
			
			System.out.println("App exited.");
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		/*
		 * finally { if (is != null) { try { is.close(); } catch (IOException e) { //
		 * catch block e.printStackTrace(); } } }
		 */
	}
	
	private static void countLinks(Document doc) {
		System.out.println("Scanning for links..");
		findAndExpandUrl(doc, true);
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

		//		System.out.print("Export as HTML or PDF or both? (H/P/B): ");
		//		String isPdfStr = scanner.nextLine(); // Read user input
		//		if (isPdfStr.equalsIgnoreCase("P") || isPdfStr.equalsIgnoreCase("B")) {
		//			IS_PDF_OUTPUT_REQUIRED = true;
		//		}

		scanner.close();
	}

	private static void export(File inputFile, Document doc) throws IOException, DocumentException, TransformerException {
		// if(IS_PDF_OUTPUT_REQUIRED) {
		File outputFile = new File(
				OUTPUT_FILE_DIR + INPUT_FILENAME.replace(DOT_HTML_FILE_SUFFIX, "")
						+ " - " + getCurrentDateTime() + DOT_PDF_FILE_SUFFIX);
		transformHtmlToPdf(inputFile, outputFile);
		
		outputFile = new File(
				OUTPUT_FILE_DIR + INPUT_FILENAME.replace(DOT_HTML_FILE_SUFFIX, "")
						+ " - " + getCurrentDateTime() + DOT_HTML_FILE_SUFFIX);
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

	private static void findAndExpandUrl(Document doc, boolean countLinksFlag) {
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
								if(countLinksFlag) {
									TOTAL_LINK_COUNT++;
									continue;
								}
								// System.out.println("Found link number - " + TOTAL_LINK_COUNT);

								// once expansion limit reached stop updating tds just count http links
								if (EXPANDED_LINK_COUNTER == LINK_TO_EXPAND_LIMIT) {
									System.out.println("Total number of expanded links - " + EXPANDED_LINK_COUNTER + "/" + TOTAL_LINK_COUNT);
									return;
								}

								// Writing the expanded URL in the second column
								String expandedUrl = expandURL(urlText);
								if (expandedUrl == null) {
									break;
								} else {
									EXPANDED_LINK_COUNTER++;
									System.out.println("expandedUrl - " + expandedUrl);

									// Writing the expanded URL in the third column
									String lh3Url = getLh3Link(expandedUrl);
									System.out.println("lh3Url - " + lh3Url);

									if (IS_THUMBNAIL_FORMAT_REQ_FLAG) {
										// set id
										((Element) td).setAttribute("id", "tinyurl_img");
										
										// remove text
										nobrNode.setTextContent("");

										// create image tag
										Element anchorEle = doc.createElement(IMAGE_TAG);
										anchorEle.setAttribute("src", lh3Url);

										nobrNode.appendChild(anchorEle);
									} else {
										// set id
										((Element) td).setAttribute("id", "tinyurl");

										// remove text
										nobrNode.setTextContent("");

										// create anchor tag
										Element anchorEle = doc.createElement(ANCHOR_TAG);
										if(lh3Url == null) {
											((Node) anchorEle).setTextContent(urlText);
										} else {
											anchorEle.setAttribute("href", lh3Url);	
											((Node) anchorEle).setTextContent("Image Hover");
										}
										
										

										nobrNode.appendChild(anchorEle);
									}
								}
							}
						}
					}
				}
			}
		}
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
					mpLinkEle.setAttribute("href", JAR_DIR + "css/jquery.minipreview.css");
					mpLinkEle.setAttribute("rel", "stylesheet");

					// create script tag
					Element mpScriptEle = doc.createElement(SCRIPT_TAG);
					mpScriptEle.setAttribute("src", JAR_DIR + "js/jquery.minipreview.js");

					// create script tag
					Element afScriptEle = doc.createElement(SCRIPT_TAG);
					afScriptEle.setAttribute("src", JAR_DIR + "js/aanchalFashionCustomScript.js");

					head.removeChild(title);
					head.removeChild(meta);
					head.removeChild(style);

					// head.appendChild(meta);
					head.appendChild(title);
					head.appendChild(scriptEle);
					head.appendChild(linkEle);
					head.appendChild(mpScriptEle);
					head.appendChild(afScriptEle);
					head.appendChild(style);

					// if(IS_PDF_OUTPUT_REQUIRED) {
					// create script tag
					Element pdfStyleEle = doc.createElement(STYLE_TAG);
					pdfStyleEle.setAttribute("type", "text/css");
					pdfStyleEle.setTextContent("@page { size: A4 landscape;}");
					head.appendChild(pdfStyleEle);
					// }
				}
			}
		}
		System.out.println("Adding custom styles scripts...");
	}

	private static String expandURL(String tinyUrl) {
		try {
			Connection connection = Jsoup.connect(tinyUrl).followRedirects(true);
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
		String inputFileName = null;

		// Load the first HTML file from input folder
		File inputFileDir = new File(INPUT_FILE_DIR);
		System.out.println("Scanning input folder for html files..." + inputFileDir.getAbsolutePath());
		if (inputFileDir == null || inputFileDir.list() == null) {
			System.err.println("Error - No input folder found. Please create one and retry.");
			return null;
		}

		for (String file : inputFileDir.list()) {
			if (file.endsWith(HTML_FILE_SUFFIX)) {
				inputFileName = file;
				inputFile = new File(INPUT_FILE_DIR + PATH_SEPARATOR + file);
				System.out.println("Converting html file - " + inputFile.getAbsolutePath());
				break;
			}
		}
		
		if(inputFile == null) {
			System.err.println("Error - No report file found in the input folder. Please add one and retry.");
			return null;
		}

		// to fix error - Open quote is expected for attribute "leftMargin" associated
		// with an element type "body".
		// replace leftMargin=10 topMargin=10 rightMargin=10 bottomMargin=10
		FileReader fileReader = new FileReader(inputFile);
		File tempFile = new File(INPUT_FILE_DIR + TEMP_FILE_SUFFIX + inputFileName);
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

		INPUT_FILENAME = inputFile.getName();
		
		System.out.println("Corrected file syntax in file - " + inputFile.getAbsolutePath());

		return inputFile;
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