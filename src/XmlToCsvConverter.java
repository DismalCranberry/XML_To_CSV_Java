import com.andreyprodromov.csv.CsvMagikk;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

public class XmlToCsvConverter {

    private static final CsvMagikk CSV = new CsvMagikk();

    public static void main(String[] args) {
        // Directory path containing XML files
        String inputDirectoryPath = "./xml-files"; // Change this to your input directory path
        String outputDirectoryPath = "./converted-files"; // Output directory for converted files

        try (Stream<Path> paths = Files.list(Paths.get(inputDirectoryPath))) {
            paths.filter(Files::isRegularFile) // Ensure it's a file
                    .filter(path -> path.toString().endsWith(".xml")) // Filter only .xml files
                    .forEach(xmlFilePath -> {
                        String outputFileName = xmlFilePath.getFileName().toString().replace(".xml", ".csv");
                        String csvFilePath = Paths.get(outputDirectoryPath, outputFileName).toString();
                        processXmlFile(xmlFilePath.toString(), csvFilePath);
                    });
        } catch (IOException e) {
            throw new RuntimeException("Error accessing the directory or files.", e);
        }

    }

    private static void processXmlFile(String xmlFilePath, String csvFilePath) {
        try {
            // Parse the XML file
            File inputFile = new File(xmlFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputFile);

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Get the root element (e.g., <products>)
            NodeList nodeList = document.getElementsByTagName("item");

            // Create the CSV writer
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));

            // Write CSV header
            writer.write("catalog_num,nomenclature,name,manufacturer,ean,weight_kg,length_cm,width_cm,height_cm,available,price");
            writer.newLine();

            // Iterate over each <item> element in the XML
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    // Extract values from XML and handle CDATA and empty elements
                    String catalogNum = CSV.escape(getTextContentByTagName(element, "catalog_num"));
                    String nomenclature = CSV.escape(getTextContentByTagName(element, "nomenclature"));
                    String name = CSV.escape(getTextContentByTagName(element, "name"));
                    String manufacturer = CSV.escape(getTextContentByTagName(element, "manufacturer"));
                    String ean = CSV.escape(getTextContentByTagName(element, "ean"));
                    String weight = CSV.escape(getTextContentByTagName(element, "weight_kg"));
                    String length = CSV.escape(getTextContentByTagName(element, "length_cm"));
                    String width = CSV.escape(getTextContentByTagName(element, "width_cm"));
                    String height = CSV.escape(getTextContentByTagName(element, "height_cm"));
                    String available = CSV.escape(getTextContentByTagName(element, "available"));
                    String price = CSV.escape(getTextContentByTagName(element, "price"));

                    // Write to CSV
                    writer.write(String.join(",",
                            catalogNum,
                            nomenclature,
                            name,
                            manufacturer,
                            ean,
                            weight,
                            length,
                            width,
                            height,
                            available,
                            price
                    ));
                    writer.newLine();
                }
            }

            // Close the writer
            writer.close();
            System.out.println("Converted " + xmlFilePath + " to " + csvFilePath);

        } catch (Exception e) {
            throw new RuntimeException("Error processing file: " + xmlFilePath, e);
        }
    }

    // Helper method to get the text content of an XML element by tag name
    private static String getTextContentByTagName(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }
}