import com.opencsv.CSVWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XMLToCSVConverter {
    public static void main(String[] args) {
        File resFolder = new File("res");
        File[] xmlFiles = resFolder.listFiles((_, name) -> name.toLowerCase().endsWith(".xml"));

        if (xmlFiles == null || xmlFiles.length == 0) {
            System.out.println("No XML files found in the 'res' folder.");
            return;
        }

        for (File xmlFile : xmlFiles) {
            convertXMLToCSV(xmlFile);
        }
    }

    public static void convertXMLToCSV(File xmlFile) {
        try {
            // Parse the XML file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // Normalize XML structure
            doc.getDocumentElement().normalize();

            // Prepare a list to hold the data
            List<String[]> rows = new ArrayList<>();

            // Get the list of 'item' elements
            NodeList nodeList = doc.getElementsByTagName("item");

            // Use a Set to gather unique tags for the header
            Set<String> headerSet = new HashSet<>();

            // Loop through each 'item' element in the XML
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    // For each item, loop through its child nodes and collect the tag names
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            headerSet.add(childNode.getNodeName());
                        }
                    }

                    // Add row to the list (order of tags will be determined dynamically later)
                    List<String> row = new ArrayList<>();
                    for (String tag : headerSet) {
                        row.add(getTagValue(tag, element));
                    }
                    rows.add(row.toArray(String[]::new));
                }
            }

            // Convert Set to List and use it as header
            String[] header = headerSet.toArray(new String[0]);
            rows.addFirst(header); // Add the header row at the beginning

            // Write the data to a CSV file
            String csvFileName = "csv/" + xmlFile.getName().replace(".xml", ".csv");
            CSVWriter writer = new CSVWriter(new FileWriter(csvFileName));
            writer.writeAll(rows);
            writer.close();
            System.out.println("CSV file created successfully: " + csvFileName);
        } catch (Exception e) {
            System.err.println("Error processing file: " + xmlFile.getName());
            e.printStackTrace();
        }
    }

    // Helper method to get the value of a tag
    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return ""; // Return empty string if tag is missing
    }
}
