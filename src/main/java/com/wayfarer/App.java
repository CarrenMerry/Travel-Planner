package com.wayfarer;

import com.wayfarer.persistence.DatabaseConnection;
import com.wayfarer.servlet.*;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * Wayfarer — Smart Travel Planner & Route Optimizer
 * Main Server Application Entry Point.
 * Launches embedded Apache Tomcat 10.1 on port 8080 and mounts REST Servlets and modern UI.
 */
public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) throws LifecycleException {
        System.out.println("==================================================================");
        System.out.println("  Wayfarer — Smart Travel Planner & Route Optimizer");
        System.out.println("  Embedded Apache Tomcat 10.1 Initialization...");
        System.out.println("==================================================================");

        // Verify MySQL database connectivity
        boolean dbOk = DatabaseConnection.testConnection();
        if (dbOk) {
            System.out.println("  [MySQL Status] CONNECTED: Using database 'smart_travel_planner'");
            DatabaseConnection.ensureCategoryColumn();
        } else {
            System.err.println("  [MySQL Status] WARNING: Could not connect to MySQL. Verify credentials in db.properties or env vars.");
        }
        System.out.println("==================================================================");

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir("temp/tomcat");

        tomcat.getConnector();

        File docBase = new File("src/main/webapp");
        if (!docBase.exists()) {
            docBase.mkdirs();
        }

        Context ctx = tomcat.addContext("", docBase.getAbsolutePath());
        ctx.addWelcomeFile("index.html");

        // Default static resource serving
        Tomcat.addServlet(ctx, "defaultServlet", new DefaultServlet());
        ctx.addServletMappingDecoded("*.html", "defaultServlet");
        ctx.addServletMappingDecoded("*.css", "defaultServlet");
        ctx.addServletMappingDecoded("*.js", "defaultServlet");
        ctx.addServletMappingDecoded("*.png", "defaultServlet");
        ctx.addServletMappingDecoded("*.jpg", "defaultServlet");
        ctx.addServletMappingDecoded("*.jpeg", "defaultServlet");
        ctx.addServletMappingDecoded("*.webp", "defaultServlet");
        ctx.addServletMappingDecoded("*.svg", "defaultServlet");
        ctx.addServletMappingDecoded("*.ico", "defaultServlet");
        ctx.addServletMappingDecoded("/", "defaultServlet");

        // REST API Servlets
        Tomcat.addServlet(ctx, "wayfarerAuthServlet", new AuthServlet());
        ctx.addServletMappingDecoded("/api/auth/*", "wayfarerAuthServlet");

        Tomcat.addServlet(ctx, "wayfarerDestinationServlet", new DestinationServlet());
        ctx.addServletMappingDecoded("/api/destinations/*", "wayfarerDestinationServlet");

        Tomcat.addServlet(ctx, "wayfarerConnectionServlet", new ConnectionServlet());
        ctx.addServletMappingDecoded("/api/connections/*", "wayfarerConnectionServlet");

        Tomcat.addServlet(ctx, "wayfarerItineraryServlet", new ItineraryServlet());
        ctx.addServletMappingDecoded("/api/itineraries/*", "wayfarerItineraryServlet");

        Tomcat.addServlet(ctx, "wayfarerRouteServlet", new RouteServlet());
        ctx.addServletMappingDecoded("/api/routes/*", "wayfarerRouteServlet");

        Tomcat.addServlet(ctx, "wayfarerRecommendationServlet", new RecommendationServlet());
        ctx.addServletMappingDecoded("/api/recommendations/*", "wayfarerRecommendationServlet");

        Tomcat.addServlet(ctx, "wayfarerAdminServlet", new AdminServlet());
        ctx.addServletMappingDecoded("/api/admin/*", "wayfarerAdminServlet");

        tomcat.start();
        System.out.println("Server successfully started at: http://localhost:" + PORT);
        System.out.println("Landing Page:        http://localhost:" + PORT + "/index.html");
        System.out.println("User Dashboard:      http://localhost:" + PORT + "/user/dashboard.html");
        System.out.println("Admin Dashboard:     http://localhost:" + PORT + "/admin/dashboard.html");
        System.out.println("==================================================================");

        tomcat.getServer().await();
    }
}
