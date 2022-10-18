package com.us.articulatedesign.db_csv_compare;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit test for comparing the contents of a CSV file with the result of 
 * a database query.
 */
public class CsvFileDatabaseQueryCompareTest 
{
	// FIXME change the connection string to the correct format for your database
	private static final String DB_CONNECTION_STRING = "jdbc:h2:mem:test";
	private static Connection conn;

	@Test
	@DisplayName("compare one database query result to a CSV file's contents")
	public void compareDatabaseQueryResultToCsvFile() throws Exception {
		// Read the contents of the CSV file
        String csvPath = "csv/all_monitors.csv";
		List<Monitor> dataFromCsvFile = parseCsvFile(csvPath);

		// Read the results of a database query
        String query = """
    		select make, model, description, price
    		  from monitor
		""";
        List<Monitor> dataFromDatabase = readQueryResultSet(query);
		
        // Verify they match
        assertEquals(dataFromDatabase.size(), dataFromCsvFile.size());
        for (int i = 0; i < dataFromDatabase.size(); i++) {
            assertEquals(dataFromDatabase.get(i), dataFromCsvFile.get(i));
        }
	}

	@ParameterizedTest
	@DisplayName("compare one database query result to a CSV file's contents")
	@CsvSource(value = {  // pass filepaths and SQL queries, separated by '|'
		"csv/all_monitors.csv | select make, model, description, price from monitor",
		"csv/dell_monitors.csv | select make, model, description, price from monitor where make = 'Dell'"
	}, delimiter = '|')
	public void compareMultipleResults(String csvPath, String query) throws Exception {
		// Read the contents of the CSV file
		List<Monitor> dataFromCsvFile = parseCsvFile(csvPath);

		// Read the results of a database query
        List<Monitor> dataFromDatabase = readQueryResultSet(query);
		
        // Verify they match
        assertEquals(dataFromDatabase.size(), dataFromCsvFile.size());
        for (int i = 0; i < dataFromDatabase.size(); i++) {
            assertEquals(dataFromDatabase.get(i), dataFromCsvFile.get(i));
        }
	}
	
	@Test
	@DisplayName("test the CSV file parser only")
	public void testParseCsvFile() throws Exception {
        String csvPath = "csv/monitor.csv";
        
        List<Monitor> expected = List.of(
    		new Monitor("Dell", "P3421W", "Dell 34, Curved, USB-C Monitor", new BigDecimal("2499.00")),
    		new Monitor("Dell", "", "Alienware 38 Curved \"Gaming Monitor\"", new BigDecimal("6699.00")),
    		new Monitor("Samsung", "", "49\" Dual QHD, QLED, HDR1000", new BigDecimal("6199.00")),
    		new Monitor("Samsung", "", "Promotion! Special Price 49\" Dual QHD, QLED, HDR1000", new BigDecimal("4999.00"))
		);
        
		List<Monitor> actual = parseCsvFile(csvPath);
        
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
	}

	@Test
	@DisplayName("test the database query result reader only")
	public void testReadQueryResultSet() throws Exception {
        List<Monitor> expected = List.of(
    		new Monitor("Dell", "P3421W", "Dell 34, Curved, USB-C Monitor", new BigDecimal("2499.00")),
    		new Monitor("Dell", "", "Alienware 38 Curved \"Gaming Monitor\"", new BigDecimal("6699.00")),
    		new Monitor("Samsung", "", "49\" Dual QHD, QLED, HDR1000", new BigDecimal("6199.00")),
    		new Monitor("Samsung", "", "Promotion! Special Price 49\" Dual QHD, QLED, HDR1000", new BigDecimal("4999.00"))
		);
        
        String query = """
    		select make, model, description, price
    		  from monitor
		""";
        List<Monitor> actual = readQueryResultSet(query);
        
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
	}
	
	private List<Monitor> parseCsvFile(String csvPath) throws Exception {
        // loads CSV file from the resource folder.
        URL resource = CsvParserSimple.class.getClassLoader().getResource(csvPath);
        File file = Paths.get(resource.toURI()).toFile();

        // parse the file into a list of model instances
        return new CsvParserSimple()
        			.readFile(file, 1)
        			.stream()
        		    .map(values -> new Monitor(values))
        		    .toList();
	}
	
	private List<Monitor> readQueryResultSet(String query) throws Exception {
		List<Monitor> monitors = new ArrayList<>();
		PreparedStatement stmt = conn.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			monitors.add(new Monitor(rs.getString("make"), rs.getString("model"), 
					                 rs.getString("description"), rs.getBigDecimal("price")));
		}
		rs.close();
		stmt.close();
		// don't close the Connection yet
		return monitors;
	}

	@BeforeAll
	public static void setUpAll() throws Exception {
		conn = DriverManager.getConnection(DB_CONNECTION_STRING);
		// Create some test data
		conn.setAutoCommit(true);
		Statement stmt = conn.createStatement();
		stmt.execute(
			"""
				CREATE TABLE monitor (
					make varchar(255),
					model varchar(255),
					description varchar(255),
					price numeric(10, 2)
				)
			""");
        stmt.execute("INSERT INTO monitor VALUES ('Dell', 'P3421W', 'Dell 34, Curved, USB-C Monitor', 2499.00)");
        stmt.execute("INSERT INTO monitor VALUES ('Dell', '', 'Alienware 38 Curved \"Gaming Monitor\"', 6699.00)");
        stmt.execute("INSERT INTO monitor VALUES ('Samsung', '', '49\" Dual QHD, QLED, HDR1000', 6199.00)");
        stmt.execute("INSERT INTO monitor VALUES ('Samsung', '', 'Promotion! Special Price 49\" Dual QHD, QLED, HDR1000', 4999.00)");
		// don't close the Connection yet: when you close a connection to
        // an in-memory H2 database, the database contents are discarded
	}
	
	@AfterAll
	public static void tearDownAll() throws Exception {
		conn.close();
	}
}
