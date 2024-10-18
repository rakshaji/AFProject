package com.anchal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.log4j.BasicConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExcelURLProcessor {

    private static final Logger logger = Logger.getLogger(ExcelURLProcessor.class.getName());

    static {
        try {
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logger.info("doesnot exist");
                logsDir.mkdir();
            }
            LogManager.getLogManager().readConfiguration(ExcelURLProcessor.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not load logging properties", e);
        }
    }

    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\Laksha\\Documents\\_Personal\\Job Related\\Education\\Java Learning\\anchal\\ExcelTinyUrlToImageProject\\project\\src\\main\\resources\\input.xlsx";
        String outputFilePath = "../../resources/output.xlsx";

        try (FileInputStream fis = new FileInputStream(new File(inputFilePath));
        Workbook workbook = new XSSFWorkbook(fis)) {
            BasicConfigurator.configure();  
            logger.info("Hello world!.............");  

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell urlCell = row.getCell(0);

                if (urlCell != null) {
                    String tinyUrl = urlCell.getStringCellValue();
                    logger.info("tinyUrl - " + tinyUrl);

                    // Writing the expanded URL in the second column
                    String expandedUrl = expandURL(tinyUrl);
                    if(expandedUrl == null) {
                        break;
                    }
                    
                    Cell expandedUrlCell = row.createCell(1);
                    expandedUrlCell.setCellValue(expandedUrl);
                    logger.info("expandedUrl - " + expandedUrl);

                    // Writing the expanded URL in the third column
                    String lh3Url = getLh3Link(expandedUrl);
                    Cell lh3UrlCell = row.createCell(2);
                    lh3UrlCell.setCellValue(lh3Url);
                    logger.info("lh3Url - " + lh3Url);
                    
                    // if(lh3Url != null && !lh3Url.equals("")){
                    //     // Creating a thumbnail and writing it in the third column
                    //     BufferedImage thumbnail = createThumbnail(lh3Url);
                    //     if (thumbnail != null) {
                    //         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    //         ImageIO.write(thumbnail, "jpg", baos);
                    //         byte[] imageBytes = baos.toByteArray();

                    //         int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);
                    //         CreationHelper helper = workbook.getCreationHelper();
                    //         Drawing drawing = sheet.createDrawingPatriarch();

                    //         ClientAnchor anchor = helper.createClientAnchor();
                    //         anchor.setCol1(2);
                    //         anchor.setRow1(row.getRowNum());

                    //         Picture pict = drawing.createPicture(anchor, pictureIdx);
                    //         pict.resize();
                    //     }
                    // } else {
                    //     break;
                    // }
                    
                }
            }

            try (FileOutputStream fos = new FileOutputStream(new File(inputFilePath))) {
                workbook.write(fos);
            }

            if(workbook != null) {
                workbook.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info(inputFilePath);
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

    // private static BufferedImage createThumbnail(String url) {
    //     try {
    //         URL imageUrl = new URL(url);
    //         logger.info(imageUrl);
    //         BufferedImage image = ImageIO.read(imageUrl);
    //         return Thumbnails.of(image).size(100, 100).asBufferedImage();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         return null;
    //     }
    // }

    public static String getLh3Link(String expandedUrl) {       
        try {

            logger.info("expandedUrl inside getLh3Link - " + expandedUrl);
            // Fetch the HTML content from the URL
            Document doc = Jsoup.connect(expandedUrl).get();

            // Find the <img> tags in the document
            Elements imgTags = doc.select("img");

            logger.info("imgTags - " + imgTags);

            // Iterate through each <img> tag to find the image URL
            for (Element img : imgTags) {
                String imgUrl = img.attr("src");

                // Check if the img tag src contains a valid image URL
                if (isValidImageUrl(imgUrl)) {
                    logger.info("Direct Image URL: " + imgUrl);
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
