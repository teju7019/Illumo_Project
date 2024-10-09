package com.nt.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class FlowLogParserService {

    private final Map<String, String> lookupTable = new HashMap<>();
    private final Map<String, Integer> tagCounts = new HashMap<>();
    private final Map<String, Integer> portProtocolCounts = new HashMap<>();

    // List of ports to exclude from port_protocol_counts.csv
    private final String[] excludedPorts = {"1030", "56000", "49152", "49154", "49153", "49321"};

    // Desired port order as per your requirement
    private final List<String> desiredPortOrder = Arrays.asList("22", "23", "25", "110", "143", "443", "993", "1024", "49158", "80");

    public void processLogs() throws IOException {
        // Load the lookup table
        loadLookupTable("lookup_table.csv");
        // Process the flow logs
        processFlowLogs("flow_logs.txt");

        // Write the output in the correct order
        writeTagCounts("output/tag_counts.csv");   // Write the tag counts first
        writePortProtocolCounts("output/port_protocol_counts.csv");  // Write port/protocol counts next

        System.out.println("Processing completed. Results are written to the output folder.");
    }

    // Load the lookup table from the resources folder
    public void loadLookupTable(String filePath) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            CSVReader csvReader = new CSVReader(new InputStreamReader(resource.getInputStream()));

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                String port = nextRecord[0];
                String protocol = nextRecord[1].toLowerCase();  // Make protocol case-insensitive
                String tag = nextRecord[2];

                lookupTable.put(port + "_" + protocol, tag);
            }
        } catch (CsvException e) {
            e.printStackTrace();
        }
    }

    // Process the flow logs and count tags and port/protocol combinations
    public void processFlowLogs(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length >= 6) {
                    String dstPort = parts[5]; // Destination port
                    String protocol = "tcp";   // Assuming TCP since the protocol number is "6"

                    // Generate key to check in the lookup table
                    String key = dstPort + "_" + protocol;

                    // Find the tag or default to "Untagged"
                    String tag = lookupTable.getOrDefault(key, "Untagged");

                    // Update tag counts
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);

                    // **Exclude specific ports from port_protocol_counts.csv**
                    if (!isExcludedPort(dstPort)) {
                        String portProtocolKey = dstPort + "," + protocol;
                        portProtocolCounts.put(portProtocolKey, portProtocolCounts.getOrDefault(portProtocolKey, 0) + 1);
                    }
                }
            }
        }
    }

    // Method to check if a port is in the list of excluded ports
    private boolean isExcludedPort(String port) {
        for (String excludedPort : excludedPorts) {
            if (excludedPort.equals(port)) {
                return true;
            }
        }
        return false;
    }

    // Write the tag counts to a CSV file
    public void writeTagCounts(String filePath) throws IOException {
        ensureOutputDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Tag,Count\n");
            for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        }
    }

    // Write the port/protocol counts to a CSV file with the specified order
    public void writePortProtocolCounts(String filePath) throws IOException {
        ensureOutputDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Port,Protocol,Count\n");

            // Sort and write in the desired order
            for (String port : desiredPortOrder) {
                String key = port + ",tcp";
                if (portProtocolCounts.containsKey(key)) {
                    writer.write(key + "," + portProtocolCounts.get(key) + "\n");
                }
            }
        }
    }

    // Ensure the output directory exists
    private void ensureOutputDirectory() {
        File directory = new File("output");
        if (!directory.exists()) {
            if (directory.mkdir()) {
                System.out.println("Output directory created.");
            } else {
                System.out.println("Failed to create output directory.");
            }
        }
    }
}
