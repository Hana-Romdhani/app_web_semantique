package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ontology")
public class EventModuleController {
    private final SparqlUtils sparqlUtils;

    @Autowired
    public EventModuleController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    @GetMapping("/events")
    public List<Map<String, String>> getAllEvents() {
        System.out.println("Fetching all Event instances...");
        return sparqlUtils.getAllEvents();
    }

    @PostMapping("/events/add")
    public String addEvent(
            @RequestParam(required = false) String id,
            @RequestParam String title,
            @RequestParam (required = false) String description,
            @RequestParam (required = false) String location,
            @RequestParam(required = false) String date,
            @RequestParam (required = false) String classType
    ) {
        String generatedId = sparqlUtils.addEvent(id, title, description, location, date, classType);
        return "Event with ID " + generatedId + " added successfully!";
    }


    // Fetch all local events
    @GetMapping("/events/local")
    public List<Map<String, String>> getAllLocalEvents() {
        return sparqlUtils.getEventsByClassType("EvenementLocal");
    }

    // Fetch all webinars
    @GetMapping("/events/webinaire")
    public List<Map<String, String>> getAllWebinars() {
        return sparqlUtils.getEventsByClassType("Webinaire");
    }

    // Endpoint to delete an event
    @DeleteMapping("/events/delete")
    public String deleteEvent(@RequestParam String id) {
        sparqlUtils.deleteEvent(id);
        return "Event with ID " + id + " deleted successfully!";
    }


}
