package org.example.task_two;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.example.task_two.json_map_converter.Output;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class TaskTwo {

    /**
     Since File objects in Java are not the actual files meaning they do not contain the data but
     only directory info, input is not memory-consuming.
     @param inputFiles list of previously found JSON/XML files
     @return Output XML/JSON file
     */
    public static File overallViolationsStats(List<File> inputFiles) {

        if(inputFiles.stream().anyMatch(f -> f.toString().endsWith(".json")))
            return readFromJSONWriteToXML(inputFiles);

        else return readFromXMLWriteToJSON(inputFiles);
    }

    /**
     * Read data from JSON files, return an output XML file.
     * First, we map Java objects from each input JSON and put them all in a list.
     * Second, we calculate for each {@link ViolationType} total fine sum of each
     * {@link TrafficViolation}. We get a Map of {@link ViolationType}
     * and total fine sum for it. Finally, we write the map into the XML using
     * custom serializer.
     * @param inputFiles list of JSON files
     * @return output XML file
     */
    private static File readFromJSONWriteToXML(List<File> inputFiles) {
        File output = new File("output_2.xml");
        ObjectMapper mapper = new ObjectMapper();
        //mapper.registerModule(new JavaTimeModule());

        List<TrafficViolation> violationList = getListOfTrfViolations(mapper, inputFiles, true);

        Map<ViolationType, BigDecimal> statsMap = getSortedMapOfVTypeAndTotalFineSum(violationList);

        // Set the resulting map
        Output out = new Output();
        out.setEntry(statsMap);

        // Write to the output file
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        try (FileWriter fileWriter = new FileWriter(output);
             PrintWriter printWriter = new PrintWriter(fileWriter)){
            printWriter.print(xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(out));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * The algorithm is the same as {@link #readFromJSONWriteToXML(List) readFromJSONWriteToXML},
     * but with the difference that we read from XML and write the map into the JSON output file.
     * @param inputFiles list of XML files
     * @return output JSON file
     */
    private static File readFromXMLWriteToJSON(List<File> inputFiles) {

        File output = new File("output_2.json");
        List<TrafficViolation> violationList = getListOfTrfViolations(new XmlMapper(), inputFiles, false);

        Map<ViolationType, BigDecimal> statsMap = getSortedMapOfVTypeAndTotalFineSum(violationList);

        // Write Map to the String
        String jsonOutput = null;
        try {
             jsonOutput = new ObjectMapper().writeValueAsString(statsMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Write String to the output file
        try {
            assert jsonOutput != null;
            try (FileWriter fileWriter = new FileWriter(output);
                     PrintWriter printWriter = new PrintWriter(fileWriter)){
                printWriter.print(jsonOutput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * Get the list of TrafficViolation.
     * @param mapper mapper to be used for mapping from either xml of JSON type of input files
     * @param inputFiles list of inputFiles to be mapped
     * @param isJson is input files of JSON format or not
     * @return List of TrafficViolation
     */
    private static List<TrafficViolation> getListOfTrfViolations(
            ObjectMapper mapper, List<File> inputFiles, boolean isJson) {

        List<TrafficViolation> violationList = new ArrayList<>();

        // Input is JSON. Read it using Jackson Streaming API
        if(isJson) {
            for (File inputFile : inputFiles) {
                try (JsonParser jsonParser = mapper.getFactory().createParser(inputFile)) {
                    // Check the first token
                    if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                        throw new IllegalStateException("Expected content to be an array");
                    }
                    // Iterate over the tokens until the end of the array
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        // Read a TrafficViolation instance using ObjectMapper and put into list
                        // Reading by line and not putting the whole JSON to memory at once
                        violationList.add(mapper.readValue(jsonParser, TrafficViolation.class));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Input is XML. Read it using Jackson Streaming API
        else {
            XmlMapper xmlMapper = (XmlMapper) mapper;
            // For each read file add elements to the list one by one
            for(File inputFile : inputFiles) {
                try(FileInputStream fis = new FileInputStream(inputFile)) {
                    XMLStreamReader xr = XMLInputFactory.newInstance().createXMLStreamReader(fis);
                    while (xr.hasNext()) {
                        xr.next();
                        if (xr.getEventType() == START_ELEMENT) {
                            // Check if it is a moving_violation tag (actual mapped object)
                            // We don't read the whole files at once but rather one by one (by tags)
                            if("moving_violation".equals(xr.getLocalName())) {
                                violationList.add(xmlMapper.readValue(xr, TrafficViolation.class));
                            }
                        }
                    }
                    xr.close();
                } catch (XMLStreamException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return violationList;
    }

    /**
     * Get the sorted Map of ViolationType and total fine amount.
     * @param violationList list of TrafficViolation
     * @return Map sorted descending by total sum of fine amount of each ViolationType
     */
    private static Map<ViolationType, BigDecimal> getSortedMapOfVTypeAndTotalFineSum(
            List<TrafficViolation> violationList) {

        // Get the Map of ViolationType and total fine amount
        Map<ViolationType, BigDecimal> statsMap = violationList.stream()
                .collect(Collectors.groupingBy(TrafficViolation::getType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                TrafficViolation::getFine_amount,
                                BigDecimal::add)));

        // Sort by fine amount
        return statsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

    }
}
