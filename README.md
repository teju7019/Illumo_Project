Description
The Flow Log Parser is a Java application built using Spring Boot. It reads flow log data from a file, checks each log entry against a lookup table to find a matching tag, and generates two output files:

tag_counts.csv: This file contains the number of log entries for each tag, including "Untagged" for entries that don't match any tag.
port_protocol_counts.csv: This file contains the count of log entries for each port and protocol combination.
The application uses OpenCSV to read and write CSV files. It is designed to process flow logs efficiently and handle exclusions for certain ports.

How to Run
Clone this repository.
Open the project in your preferred IDE.
Ensure the flow_logs.txt and lookup_table.csv files are in the src/main/resources folder.
Run the application using your IDE or by running the following command:
bash
Copy code
mvn spring-boot:run
The output files will be generated in the output folder.
Requirements
Java 8 or higher
Maven
Spring Boot
OpenCSV
