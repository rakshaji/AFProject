package com.anchal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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

public class HTMLDOMManipulator {
	
	private static int LINK_COUNT = 0;
	
    public static void main(String[] args) {  
        try {  
            // Load the HTML file  
            File inputFile = new File("C:\\Users\\Laksha\\Documents\\_Personal\\Job Related\\Education\\Java Learning\\anchal\\Purchase Register.html");  
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();  
            Document doc = dBuilder.parse(inputFile);  
            
            // Find the paragraph element to modify  
            NodeList head = doc.getElementsByTagName("head");
            if(head.getLength() == 3) {
            	Node title = head.item(0);
            	Node meta =  head.item(1);
            	Node style = head.item(2);
            	
            	// create anchor tag 
				/*
				 * Element anchorEle = doc.createElement("a"); anchorEle.setAttribute("href",
				 * lh3Url); nobrNode.appendChild(anchorEle);
				 * 
				 * meta.insertBefore(newChild, refChild);
				 */
            }
            
            // Find the paragraph element to modify  
            NodeList trs = doc.getElementsByTagName("tr");  
            for(int i = 0; i < trs.getLength(); i++) {
            	Node tr = trs.item(i);
            	if(tr != null && tr.hasChildNodes()) {
            		
            		NodeList tds = tr.getChildNodes();
            		for(int j = 0; j < tds.getLength(); j++) {
            			Node td = tds.item(j);
            			if(td != null && td.hasChildNodes()) {
            				NodeList nobrNodes = td.getChildNodes();
                			if(nobrNodes.getLength() == 1) {
                				Node nobrNode = nobrNodes.item(0);
                				String urlText = nobrNode.getTextContent();
                				// check for url
                				if(urlText.toLowerCase().startsWith("http")) {
                					
                					if(LINK_COUNT == 10) {
                                		break;
                                	}
                					
                					// Writing the expanded URL in the second column
                                    String expandedUrl = expandURL(urlText);
                                    if(expandedUrl == null) {
                                    	break;
                                    } else {
                                    	LINK_COUNT++;
                                    	
                                    	// set id
                    					((Element) td).setAttribute("id", "tinyurl");
                    					
                                    	// remove text
                    					nobrNode.setTextContent("");
                                    	System.out.println("expandedUrl - " + expandedUrl);

                                        // Writing the expanded URL in the third column
                                        String lh3Url = getLh3Link(expandedUrl);
                                        System.out.println("lh3Url - " + lh3Url);
                                        
                    					// create anchor tag 
                    					Element anchorEle = doc.createElement("a");
                    					anchorEle.setAttribute("href", lh3Url);
                    					((Node)anchorEle).setTextContent("Image Hover");
                    					
                        				nobrNode.appendChild(anchorEle);
                                    }
                                    
                				}
                			}
            			}
            		}
            	}
            }
            
            // Save the modified document  
            DOMSource source = new DOMSource(doc);
            FileWriter writer = new FileWriter(new File("C:\\Users\\Laksha\\Documents\\_Personal\\Job Related\\Education\\Java Learning\\anchal\\Purchase Register Output.html"));
            StreamResult result = new StreamResult(writer);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();  
            Transformer transformer = transformerFactory.newTransformer();  
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.transform(source, result);  
            
            System.out.println("HTML file modified successfully.");  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
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

            System.out.println("expandedUrl inside getLh3Link - " + expandedUrl);
            // Fetch the HTML content from the URL
            org.jsoup.nodes.Document doc = Jsoup.connect(expandedUrl).get();

            // Find the <img> tags in the document
            Elements imgTags = doc.select("img");

            System.out.println("imgTags - " + imgTags);

            // Iterate through each <img> tag to find the image URL
            for (org.jsoup.nodes.Element img : imgTags) {
                String imgUrl = img.attr("src");

                // Check if the img tag src contains a valid image URL
                if (isValidImageUrl(imgUrl)) {
                    System.out.println("Direct Image URL: " + imgUrl);
                    return imgUrl;
                    //break;  // Stop after finding the first valid image URL
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Function to check if a given URL ends with a common image extension
    private static boolean isValidImageUrl(String url) {
        return url != null 
            && (url.contains("lh3.googleusercontent.com") || url.endsWith(".jpg") || url.endsWith(".jpeg") 
            || url.endsWith(".png") || url.endsWith(".gif"));
    }
    
}  