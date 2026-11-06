package com.travelplanner.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.travelplanner.model.Activity;
import com.travelplanner.model.Connection;
import com.travelplanner.model.Destination;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles JSON file-based persistence for destinations and itinerary data.
 * Keeps persisted storage clearly separated from the custom Java data structures used for algorithm execution.
 */
public class FileStorage {

    private static final String DATA_DIR = "data";
    private static final String DESTINATIONS_FILE = "data/destinations.json";
    private static final String ITINERARY_FILE = "data/itinerary.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static synchronized List<Destination> loadDestinations() {
        File file = new File(DESTINATIONS_FILE);
        if (!file.exists()) {
            List<Destination> defaults = createDefaultDestinations();
            saveDestinations(defaults);
            return defaults;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Destination>>() {}.getType();
            List<Destination> destinations = gson.fromJson(reader, listType);
            if (destinations == null || destinations.isEmpty()) {
                destinations = createDefaultDestinations();
                saveDestinations(destinations);
            }
            return destinations;
        } catch (Exception e) {
            System.err.println("Error reading destinations from file: " + e.getMessage());
            return createDefaultDestinations();
        }
    }

    public static synchronized void saveDestinations(List<Destination> destinations) {
        try {
            Path dir = Paths.get(DATA_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(DESTINATIONS_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(destinations, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving destinations: " + e.getMessage());
        }
    }

    public static synchronized List<String> loadItineraryIds() {
        File file = new File(ITINERARY_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> ids = gson.fromJson(reader, listType);
            return ids != null ? ids : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static synchronized void saveItineraryIds(List<String> ids) {
        try {
            Path dir = Paths.get(DATA_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(ITINERARY_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(ids, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving itinerary IDs: " + e.getMessage());
        }
    }

    private static List<Destination> createDefaultDestinations() {
        List<Destination> list = new ArrayList<>();

        // 1. Tokyo
        Destination d1 = new Destination("DEST-01", "Tokyo Imperial Trails", "Tokyo, Japan",
                "Ultra-modern skyscrapers blend with historic shrines, neon-lit alleys, and world-class ramen.",
                1450.0, 5.0, 4.9,
                "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=80");
        d1.addActivity(new Activity("ACT-01", "Shibuya Crossing & Sky Tour", 80.0, 92));
        d1.addActivity(new Activity("ACT-02", "Tsukiji Outer Market Food Crawl", 65.0, 88));
        d1.addActivity(new Activity("ACT-03", "Senso-ji Temple & Asakusa Walking", 30.0, 75));
        d1.addActivity(new Activity("ACT-04", "Mount Fuji Day Trip & Onsen", 220.0, 96));
        d1.addConnection(new Connection("DEST-08", 7800.0, 680.0)); // to Sydney
        d1.addConnection(new Connection("DEST-11", 4600.0, 320.0)); // to Bangkok
        d1.addConnection(new Connection("DEST-04", 5500.0, 410.0)); // to Bali
        list.add(d1);

        // 2. Paris
        Destination d2 = new Destination("DEST-02", "Parisian Art & Culture", "Paris, France",
                "The City of Light boasts timeless architecture, Louvre masterpieces, romantic bistros, and Seine strolls.",
                1600.0, 4.0, 4.8,
                "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80");
        d2.addActivity(new Activity("ACT-05", "Louvre Museum VIP Access", 95.0, 95));
        d2.addActivity(new Activity("ACT-06", "Eiffel Tower Summit & Champagne", 110.0, 90));
        d2.addActivity(new Activity("ACT-07", "Seine River Sunset Cruise", 50.0, 82));
        d2.addActivity(new Activity("ACT-08", "Versailles Palace Royal Guided Tour", 140.0, 94));
        d2.addConnection(new Connection("DEST-03", 1100.0, 120.0)); // to Rome
        d2.addConnection(new Connection("DEST-09", 830.0, 95.0));   // to Barcelona
        d2.addConnection(new Connection("DEST-10", 2200.0, 260.0)); // to Reykjavik
        d2.addConnection(new Connection("DEST-05", 5800.0, 520.0)); // to New York
        list.add(d2);

        // 3. Rome
        Destination d3 = new Destination("DEST-03", "Ancient Rome Odyssey", "Rome, Italy",
                "Walk among millennia of Roman history at the Colosseum, Vatican museums, and lively piazzas.",
                1350.0, 4.0, 4.7,
                "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=800&q=80");
        d3.addActivity(new Activity("ACT-09", "Colosseum & Roman Forum Underpass", 75.0, 93));
        d3.addActivity(new Activity("ACT-10", "Vatican City & Sistine Chapel Tour", 85.0, 94));
        d3.addActivity(new Activity("ACT-11", "Trastevere Authentic Pasta Masterclass", 90.0, 89));
        d3.addConnection(new Connection("DEST-02", 1100.0, 120.0)); // to Paris
        d3.addConnection(new Connection("DEST-09", 860.0, 110.0));  // to Barcelona
        d3.addConnection(new Connection("DEST-07", 2100.0, 240.0)); // to Cairo
        list.add(d3);

        // 4. Bali
        Destination d4 = new Destination("DEST-04", "Bali Tropical Escape", "Ubud, Indonesia",
                "Lush emerald rice terraces, serene Hindu temples, volcanic sunrises, and tranquil coastal surf.",
                850.0, 6.0, 4.8,
                "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80");
        d4.addActivity(new Activity("ACT-12", "Mount Batur Sunrise Trek", 60.0, 91));
        d4.addActivity(new Activity("ACT-13", "Ubud Monkey Forest & Sacred Springs", 35.0, 80));
        d4.addActivity(new Activity("ACT-14", "Balinese Traditional Cooking & Market Tour", 45.0, 86));
        d4.addConnection(new Connection("DEST-01", 5500.0, 410.0)); // to Tokyo
        d4.addConnection(new Connection("DEST-11", 2900.0, 190.0)); // to Bangkok
        d4.addConnection(new Connection("DEST-08", 4600.0, 340.0)); // to Sydney
        list.add(d4);

        // 5. New York
        Destination d5 = new Destination("DEST-05", "New York Skyline Discovery", "New York, USA",
                "The pulsating metropolis of Broadway shows, iconic Central Park, museums, and historic neighborhoods.",
                1950.0, 5.0, 4.6,
                "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=800&q=80");
        d5.addActivity(new Activity("ACT-15", "Broadway Hit Musical Orchestra Seat", 180.0, 95));
        d5.addActivity(new Activity("ACT-16", "Statue of Liberty & Ellis Island Ferry", 40.0, 84));
        d5.addActivity(new Activity("ACT-17", "Summit One Vanderbilt Observation Deck", 65.0, 90));
        d5.addConnection(new Connection("DEST-02", 5800.0, 520.0)); // to Paris
        d5.addConnection(new Connection("DEST-10", 4200.0, 410.0)); // to Reykjavik
        d5.addConnection(new Connection("DEST-12", 7700.0, 710.0)); // to Rio
        list.add(d5);

        // 6. Cape Town
        Destination d6 = new Destination("DEST-06", "Cape Town Coastal Wonder", "Cape Town, South Africa",
                "Spectacular Table Mountain views, penguin beaches, Atlantic panoramas, and scenic Winelands.",
                1100.0, 5.0, 4.9,
                "https://images.unsplash.com/photo-1580618672591-eb180b1a973f?auto=format&fit=crop&w=800&q=80");
        d6.addActivity(new Activity("ACT-18", "Table Mountain Cableway & Hike", 50.0, 92));
        d6.addActivity(new Activity("ACT-19", "Cape Point & Boulders Beach Penguins", 75.0, 94));
        d6.addActivity(new Activity("ACT-20", "Stellenbosch Vineyard Wine Tasting", 95.0, 87));
        d6.addConnection(new Connection("DEST-07", 7200.0, 590.0)); // to Cairo
        d6.addConnection(new Connection("DEST-12", 6000.0, 530.0)); // to Rio
        list.add(d6);

        // 7. Cairo
        Destination d7 = new Destination("DEST-07", "Giza Pyramids & Nile Heritage", "Cairo, Egypt",
                "Stand in awe before the Great Pyramids, Khan el-Khalili bazaar, and cruise the historic Nile River.",
                920.0, 4.0, 4.5,
                "https://images.unsplash.com/photo-1503177119275-0aa32b3a9368?auto=format&fit=crop&w=800&q=80");
        d7.addActivity(new Activity("ACT-21", "Private Giza Pyramids & Sphinx Camel Tour", 85.0, 96));
        d7.addActivity(new Activity("ACT-22", "Grand Egyptian Museum Discovery", 50.0, 93));
        d7.addActivity(new Activity("ACT-23", "Felucca Sailboat Ride on the Nile", 40.0, 81));
        d7.addConnection(new Connection("DEST-03", 2100.0, 240.0)); // to Rome
        d7.addConnection(new Connection("DEST-06", 7200.0, 590.0)); // to Cape Town
        d7.addConnection(new Connection("DEST-11", 7200.0, 610.0)); // to Bangkok
        list.add(d7);

        // 8. Sydney
        Destination d8 = new Destination("DEST-08", "Sydney Harbour & Coastal Walks", "Sydney, Australia",
                "World-renowned Sydney Opera House, golden Bondi Beach sands, ferry rides, and lush Botanic Gardens.",
                1750.0, 5.0, 4.7,
                "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=800&q=80");
        d8.addActivity(new Activity("ACT-24", "Sydney Harbour Bridge Climb", 210.0, 97));
        d8.addActivity(new Activity("ACT-25", "Opera House Backstage Tour", 90.0, 89));
        d8.addActivity(new Activity("ACT-26", "Bondi to Coogee Coastal Walk", 25.0, 86));
        d8.addConnection(new Connection("DEST-01", 7800.0, 680.0)); // to Tokyo
        d8.addConnection(new Connection("DEST-04", 4600.0, 340.0)); // to Bali
        list.add(d8);

        // 9. Barcelona
        Destination d9 = new Destination("DEST-09", "Barcelona Gaudí Highlights", "Barcelona, Spain",
                "Spectacular Sagrada Família, Park Güell mosaics, Mediterranean beaches, and vibrant tapas quarters.",
                1250.0, 4.0, 4.8,
                "https://images.unsplash.com/photo-1583422409516-2895a77efded?auto=format&fit=crop&w=800&q=80");
        d9.addActivity(new Activity("ACT-27", "Sagrada Família Tower Guided Tour", 70.0, 95));
        d9.addActivity(new Activity("ACT-28", "Park Güell Architectural Walk", 40.0, 89));
        d9.addActivity(new Activity("ACT-29", "Gothic Quarter Tapas & Wine Tour", 80.0, 91));
        d9.addConnection(new Connection("DEST-02", 830.0, 95.0));   // to Paris
        d9.addConnection(new Connection("DEST-03", 860.0, 110.0));  // to Rome
        list.add(d9);

        // 10. Reykjavik
        Destination d10 = new Destination("DEST-10", "Iceland Glaciers & Northern Lights", "Reykjavik, Iceland",
                "Dramatic geothermal springs, majestic waterfalls, volcanic landscapes, and surreal aurora borealis.",
                1850.0, 4.0, 4.9,
                "https://images.unsplash.com/photo-1529963183134-61a90db47eaf?auto=format&fit=crop&w=800&q=80");
        d10.addActivity(new Activity("ACT-30", "Blue Lagoon Geothermal Spa", 115.0, 93));
        d10.addActivity(new Activity("ACT-31", "Golden Circle & Gullfoss Waterfall", 95.0, 92));
        d10.addActivity(new Activity("ACT-32", "Glacier Ice Cave & Aurora Safari", 170.0, 98));
        d10.addConnection(new Connection("DEST-02", 2200.0, 260.0)); // to Paris
        d10.addConnection(new Connection("DEST-05", 4200.0, 410.0)); // to New York
        list.add(d10);

        // 11. Bangkok
        Destination d11 = new Destination("DEST-11", "Bangkok Palaces & Floating Markets", "Bangkok, Thailand",
                "Grand Palace gold spires, aromatic street food, serene Chao Phraya river ferries, and bustling night markets.",
                750.0, 4.0, 4.6,
                "https://images.unsplash.com/photo-1508009603885-50cf7c579365?auto=format&fit=crop&w=800&q=80");
        d11.addActivity(new Activity("ACT-33", "Grand Palace & Wat Pho Emerald Buddha", 45.0, 91));
        d11.addActivity(new Activity("ACT-34", "Damnoen Saduak Floating Market Tour", 55.0, 87));
        d11.addActivity(new Activity("ACT-35", "Chao Phraya Luxury Dinner Cruise", 60.0, 89));
        d11.addConnection(new Connection("DEST-01", 4600.0, 320.0)); // to Tokyo
        d11.addConnection(new Connection("DEST-04", 2900.0, 190.0)); // to Bali
        d11.addConnection(new Connection("DEST-07", 7200.0, 610.0)); // to Cairo
        list.add(d11);

        // 12. Rio de Janeiro
        Destination d12 = new Destination("DEST-12", "Rio Samba & Sugarloaf Vistas", "Rio de Janeiro, Brazil",
                "Christ the Redeemer crowning Mount Corcovado, golden Copacabana sands, and vibrant samba rhythms.",
                1150.0, 5.0, 4.7,
                "https://images.unsplash.com/photo-1483729558449-99ef09a8c325?auto=format&fit=crop&w=800&q=80");
        d12.addActivity(new Activity("ACT-36", "Corcovado & Christ the Redeemer Train", 65.0, 94));
        d12.addActivity(new Activity("ACT-37", "Sugarloaf Mountain Cable Car Sunset", 70.0, 93));
        d12.addActivity(new Activity("ACT-38", "Samba School Experience & Caipirinha", 50.0, 88));
        d12.addConnection(new Connection("DEST-05", 7700.0, 710.0)); // to New York
        d12.addConnection(new Connection("DEST-06", 6000.0, 530.0)); // to Cape Town
        list.add(d12);

        return list;
    }
}
