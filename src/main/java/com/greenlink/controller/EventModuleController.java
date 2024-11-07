package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String date,
            @RequestParam  String classType
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
    @PutMapping("/events/update")
    //public String updateEvent(
    public ResponseEntity<Map<String, String>> updateEvent(
            @RequestParam String id,
            @RequestBody Map<String, String> data) {

        String title = data.get("title");
        String description = data.get("description");
        String location = data.get("location");
        String date = data.get("date");
        String classType = data.get("classType");

        // Appel à votre service SPARQL pour mettre à jour l'événement
        String updatedId = sparqlUtils.updateEvent(id, title, description, location, date, classType);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Event with ID " + updatedId + " updated successfully!");
        return ResponseEntity.ok(response);
    }



}
