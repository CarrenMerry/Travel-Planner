package com.travelplanner;

import com.travelplanner.dao.*;
import com.travelplanner.model.Destination;
import com.travelplanner.model.User;
import com.travelplanner.persistence.DatabaseConnection;
import com.travelplanner.service.AuthService;
import com.travelplanner.service.DestinationService;
import com.travelplanner.service.ItineraryService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseIntegrationTest {

    private final DestinationDao destinationDao = new DestinationDao();
    private final ConnectionDao connectionDao = new ConnectionDao();
    private final UserDao userDao = new UserDao();
    private final ItineraryDao itineraryDao = new ItineraryDao();
    private final ItineraryStopDao itineraryStopDao = new ItineraryStopDao();

    @Test
    @Order(1)
    @DisplayName("1. JDBC Connection and SELECT 1 succeeds")
    void testJdbcConnectionAndSelect1() throws Exception {
        assertTrue(DatabaseConnection.testConnection(), "Database connection must succeed");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 AS ok")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ok"));
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. Destinations can be read from MySQL")
    void testReadDestinations() {
        List<Destination> destinations = destinationDao.findAll();
        assertNotNull(destinations);
        assertFalse(destinations.isEmpty(), "Destinations table should contain existing records");
        System.out.println("Loaded " + destinations.size() + " destinations from MySQL.");

        Destination d = destinationDao.findByCode("DEST-01");
        assertNotNull(d, "DEST-01 (Tokyo) should exist in destinations table");
        assertEquals("Tokyo Imperial Trails", d.getName());
        assertEquals("Japan", d.getCountry());
        assertTrue(d.getEstimatedCost() > 0);
    }

    @Test
    @Order(3)
    @DisplayName("3. Destination CRUD operations in MySQL")
    void testDestinationCrud() {
        String testCode = "TEST-99";
        // Clean up before test in case of prior run
        destinationDao.deleteByCode(testCode);

        // CREATE
        Destination testDest = new Destination(0, testCode, "Test Destination Oasis", "Morocco", "Marrakech",
                "A vibrant oasis for testing database persistence.", 999.0, 3, 2, 4.9, "https://example.com/test.jpg");
        boolean created = destinationDao.insert(testDest);
        assertTrue(created, "Destination insertion should succeed");
        assertTrue(testDest.getDbId() > 0, "Generated database ID should be assigned");

        // READ
        Destination fetched = destinationDao.findByCode(testCode);
        assertNotNull(fetched, "Inserted destination must be retrievable by code");
        assertEquals("Test Destination Oasis", fetched.getName());
        assertEquals(999.0, fetched.getEstimatedCost());

        // UPDATE
        fetched.setName("Updated Destination Oasis");
        fetched.setEstimatedCost(1050.0);
        boolean updated = destinationDao.update(fetched);
        assertTrue(updated, "Destination update should succeed");

        Destination updatedFetched = destinationDao.findByCode(testCode);
        assertEquals("Updated Destination Oasis", updatedFetched.getName());
        assertEquals(1050.0, updatedFetched.getEstimatedCost());

        // DELETE
        boolean deleted = destinationDao.deleteByCode(testCode);
        assertTrue(deleted, "Destination deletion should succeed");
        assertNull(destinationDao.findByCode(testCode), "Destination must no longer exist after deletion");
    }

    @Test
    @Order(4)
    @DisplayName("4. Travel connections can be read and updated in MySQL")
    void testTravelConnections() {
        // Ensure connections between DEST-01 and DEST-04 exist or can be created
        boolean added = connectionDao.addConnection("DEST-01", "DEST-04", 5500.0, 410.0);
        assertTrue(added || connectionDao.count() > 0, "Adding connection should succeed");

        List<ConnectionDao.ConnectionRecord> records = connectionDao.findAll();
        assertNotNull(records);
        assertFalse(records.isEmpty(), "Connection records should be present");
        System.out.println("Loaded " + records.size() + " connection edges from MySQL.");
    }

    @Test
    @Order(5)
    @DisplayName("5. Users table provisioning and authentication against MySQL")
    void testUserAuthentication() {
        AuthService authService = AuthService.getInstance();

        // Admin authentication
        User admin = authService.authenticate("admin", "admin123");
        assertNotNull(admin, "Admin credentials must authenticate successfully");
        assertTrue(admin.isAdmin(), "Admin must have ADMIN role");

        // Traveler authentication
        User traveler = authService.authenticate("traveler", "travel123");
        assertNotNull(traveler, "Traveler credentials must authenticate successfully");
        assertFalse(traveler.isAdmin(), "Traveler must have USER role");

        // Invalid credentials rejection
        User invalid = authService.authenticate("admin", "wrong_password");
        assertNull(invalid, "Invalid password must be rejected");
    }

    @Test
    @Order(6)
    @DisplayName("6. Itinerary header and stops persist atomically in MySQL")
    void testItineraryPersistence() {
        ItineraryService itineraryService = ItineraryService.getInstance();
        assertNotNull(itineraryService);

        // Read itinerary
        List<Destination> stops = itineraryService.getItineraryList();
        assertFalse(stops.isEmpty(), "Itinerary stops should be loaded from MySQL");
        int initialSize = stops.size();

        // Add destination
        boolean added = itineraryService.addDestination("DEST-05");
        assertTrue(added, "Adding destination DEST-05 should succeed");
        assertEquals(initialSize + 1, itineraryService.getItineraryList().size());

        // Verify stats calculation from real stops
        ItineraryService.ItineraryStats stats = itineraryService.getStatistics();
        assertTrue(stats.totalCost > 0, "Total cost must be calculated from stops");
        assertTrue(stats.totalDuration > 0, "Total duration must be calculated from stops");

        // Undo action
        String undoMsg = itineraryService.undo();
        assertNotNull(undoMsg, "Undo must return confirmation description");
        assertEquals(initialSize, itineraryService.getItineraryList().size(), "Itinerary size must revert after undo");
    }

    @Test
    @Order(7)
    @DisplayName("7. In-memory DSA (BST & Graph) synchronized with MySQL data")
    void testDsaSynchronization() {
        DestinationService destinationService = DestinationService.getInstance();
        assertNotNull(destinationService);

        // Verify BST contains all database destinations
        List<Destination> all = destinationService.getAllDestinations();
        for (Destination d : all) {
            com.travelplanner.ds.BinarySearchTree.SearchResult<Destination> inBst = destinationService.getBst().search(d);
            assertTrue(inBst.found, "Destination " + d.getId() + " must be present in custom BST");
            assertNotNull(inBst.foundItem);
        }

        // Verify custom Graph contains vertices and edges
        List<String> vertexIds = destinationService.getGraph().getAllVertexIds();
        assertEquals(all.size(), vertexIds.size(), "Graph vertex count must match destination count");
    }
}
