package com.travelplanner;

import com.travelplanner.servlet.*;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * Main application entry point.
 * Configures and launches an embedded Apache Tomcat web server on port 8080.
 * Mounts REST API Servlets at /api/* and serves the modern frontend from src/main/webapp.
 */
public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) throws LifecycleException {
        System.out.println("==================================================================");
        System.out.println("  Smart Travel Planner & Route Optimizer - DSA Academic Engine");
        System.out.println("  Embedded Apache Tomcat 10.1 Initialization...");
        System.out.println("==================================================================");

        // Verify MySQL database connectivity
        boolean dbOk = com.travelplanner.persistence.DatabaseConnection.testConnection();
        if (dbOk) {
            System.out.println("  [MySQL Status] CONNECTED: Using database 'smart_travel_planner'");
        } else {
            System.err.println("  [MySQL Status] WARNING: Could not connect to MySQL. Verify credentials in db.properties or env vars.");
        }
        System.out.println("==================================================================");

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir("temp/tomcat");

        // Force connector creation
        tomcat.getConnector();

        File docBase = new File("src/main/webapp");
        if (!docBase.exists()) {
            docBase.mkdirs();
        }

        Context ctx = tomcat.addContext("", docBase.getAbsolutePath());

        // Enable default static resource serving
        Tomcat.addServlet(ctx, "defaultServlet", new DefaultServlet());
        ctx.addServletMappingDecoded("*.html", "defaultServlet");
        ctx.addServletMappingDecoded("*.css", "defaultServlet");
        ctx.addServletMappingDecoded("*.js", "defaultServlet");
        ctx.addServletMappingDecoded("*.png", "defaultServlet");
        ctx.addServletMappingDecoded("*.jpg", "defaultServlet");
        ctx.addServletMappingDecoded("*.svg", "defaultServlet");
        ctx.addServletMappingDecoded("*.ico", "defaultServlet");
        ctx.addServletMappingDecoded("/", "defaultServlet");

        // Mount API Servlets
        Tomcat.addServlet(ctx, "dashboardServlet", new DashboardServlet());
        ctx.addServletMappingDecoded("/api/dashboard", "dashboardServlet");

        Tomcat.addServlet(ctx, "destinationServlet", new DestinationServlet());
        ctx.addServletMappingDecoded("/api/destinations/*", "destinationServlet");

        Tomcat.addServlet(ctx, "itineraryServlet", new ItineraryServlet());
        ctx.addServletMappingDecoded("/api/itinerary/*", "itineraryServlet");

        Tomcat.addServlet(ctx, "graphServlet", new GraphServlet());
        ctx.addServletMappingDecoded("/api/graph/*", "graphServlet");

        Tomcat.addServlet(ctx, "searchSortServlet", new SearchSortServlet());
        ctx.addServletMappingDecoded("/api/search-sort/*", "searchSortServlet");

        Tomcat.addServlet(ctx, "optimizationServlet", new OptimizationServlet());
        ctx.addServletMappingDecoded("/api/optimize/*", "optimizationServlet");

        Tomcat.addServlet(ctx, "queueServlet", new QueueServlet());
        ctx.addServletMappingDecoded("/api/queue/*", "queueServlet");

        Tomcat.addServlet(ctx, "dsaLabServlet", new DsaLabServlet());
        ctx.addServletMappingDecoded("/api/dsa-lab/*", "dsaLabServlet");

        Tomcat.addServlet(ctx, "authServlet", new AuthServlet());
        ctx.addServletMappingDecoded("/api/auth/*", "authServlet");

        tomcat.start();
        System.out.println("Server successfully started at: http://localhost:" + PORT);
        System.out.println("Frontend UI is live at: http://localhost:" + PORT + "/index.html");
        System.out.println("REST API is live at:    http://localhost:" + PORT + "/api/dashboard");
        System.out.println("==================================================================");

        tomcat.getServer().await();
    }
}
