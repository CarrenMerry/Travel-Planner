package com.wayfarer.service;

import com.wayfarer.dao.DestinationDAO;
import com.wayfarer.model.Destination;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wayfarer — Destination Catalog Service
 */
public class DestinationService {

    private static final DestinationService INSTANCE = new DestinationService();
    private final DestinationDAO destinationDAO = new DestinationDAO();

    public static DestinationService getInstance() {
        return INSTANCE;
    }

    private DestinationService() {}

    public List<Destination> getAllDestinations() {
        return destinationDAO.findAll();
    }

    public Destination getById(int id) {
        return destinationDAO.findById(id);
    }

    public Destination getByCode(String code) {
        return destinationDAO.findByCode(code);
    }

    public List<Destination> searchAndFilter(String query, String category) {
        List<Destination> all = destinationDAO.findAll();
        return all.stream()
                .filter(d -> {
                    boolean matchesQuery = true;
                    if (query != null && !query.trim().isEmpty()) {
                        String q = query.trim().toLowerCase();
                        matchesQuery = (d.getName() != null && d.getName().toLowerCase().contains(q)) ||
                                       (d.getCountry() != null && d.getCountry().toLowerCase().contains(q)) ||
                                       (d.getCity() != null && d.getCity().toLowerCase().contains(q)) ||
                                       (d.getDescription() != null && d.getDescription().toLowerCase().contains(q));
                    }

                    boolean matchesCategory = true;
                    if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
                        matchesCategory = d.getCategory() != null && d.getCategory().equalsIgnoreCase(category.trim());
                    }

                    return matchesQuery && matchesCategory;
                })
                .collect(Collectors.toList());
    }

    public Set<String> getAvailableCategories() {
        List<Destination> all = destinationDAO.findAll();
        Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Destination d : all) {
            if (d.getCategory() != null && !d.getCategory().isBlank()) {
                categories.add(d.getCategory());
            }
        }
        if (categories.isEmpty()) {
            categories.addAll(List.of("Cultural", "Adventure", "Relaxation", "Historical"));
        }
        return categories;
    }

    public boolean createDestination(Destination destination) {
        boolean ok = destinationDAO.insert(destination);
        if (ok) {
            RouteService.getInstance().refreshGraph();
        }
        return ok;
    }

    public boolean updateDestination(Destination destination) {
        boolean ok = destinationDAO.update(destination);
        if (ok) {
            RouteService.getInstance().refreshGraph();
        }
        return ok;
    }

    public boolean deleteDestination(int id) {
        boolean ok = destinationDAO.delete(id);
        if (ok) {
            RouteService.getInstance().refreshGraph();
        }
        return ok;
    }

    public int count() {
        return destinationDAO.count();
    }
}
