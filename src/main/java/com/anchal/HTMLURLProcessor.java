package com.anchal;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import org.w3c.dom.NodeList;  

public class HTMLURLProcessor {  
    public static void main(String[] args) {  
        try {  
            // Load the HTML file  
            File inputFile = new File("C:\\Users\\Laksha\\Documents\\_Personal\\Job Related\\Education\\Java Learning\\anchal\\ExcelTinyUrlToImageProject\\project\\src\\main\\resources\\input.html");  
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();  
            Document doc = dBuilder.parse(inputFile);  
            
            // Find the paragraph element to modify  
            NodeList anchors = doc.getElementsByTagName("a");  
            Element anchor = (Element) anchors.item(0);  
            
            String tinyUrl = anchor.getAttribute("href");
            System.out.println("tinyUrl - " + tinyUrl);

            // Writing the expanded URL in the second column
            String expandedUrl = expandURL(tinyUrl);
            System.out.println("expandedUrl - " + expandedUrl);
            
            if(expandedUrl == null) { return; }
            
            // Writing the expanded URL in the third column
            String lh3Url = getLh3Link(expandedUrl);
            System.out.println("lh3Url - " + lh3Url);
            
            // Change the text content of the paragraph element  
            anchor.setAttribute("href", lh3Url);  
            
            // Save the modified document  
            TransformerFactory transformerFactory = TransformerFactory.newInstance();  
            Transformer transformer = transformerFactory.newTransformer();  
            DOMSource source = new DOMSource(doc);  
            StreamResult result = new StreamResult(new File("C:\\Users\\Laksha\\Documents\\_Personal\\Job Related\\Education\\Java Learning\\anchal\\ExcelTinyUrlToImageProject\\project\\src\\main\\resources\\output.html"));  
            transformer.transform(source, result);  
            System.out.println("HTML file modified successfully.");  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    
    private static String expandURL(String tinyUrl) {
        try {
            Map<String , String> headers = new HashMap<>();
            headers.put("username", "raksha.singhania@gmail.com");
            headers.put("password", "mSRxpIQY4oUa68Y");
            
            String username = "raksha.singhania@gmail.com";
            String password = "mSRxpIQY4oUa68Y";
            String login = username + ":" + password;
            String base64login = Base64.getEncoder().encodeToString(login.getBytes());
            Connection connection = Jsoup.connect(tinyUrl).followRedirects(true);
            Connection.Response response = connection.execute().header("Authorization", "Basic " + base64login);
            
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