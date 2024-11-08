package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ontology")
public class ClassificationModuleController {
    private final SparqlUtils sparqlUtils;

    @Autowired
    public ClassificationModuleController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    @GetMapping("/classifications")
    public List<Map<String, String>> getAllClassifications() {
        System.out.println("Fetching all Event instances...");
        return sparqlUtils.getAllClassifications();
    }

    @PostMapping("/classifications/add")
    public String addClassification(
            @RequestParam(required = false) String id,
            @RequestParam String name,
            @RequestParam  String classType
    ) {
        String generatedId = sparqlUtils.addClassification(id, name, classType);
        return "Event with ID " + generatedId + " added successfully!";
    }


    // Fetch all local events
    //////////


    // Endpoint to delete an event
    @DeleteMapping("/classifications/delete")
    public String deleteClassification(@RequestParam String id) {
        sparqlUtils.deleteClassification(id);
        return "Event with ID " + id + " deleted successfully!";
    }
    @PutMapping("/classifications/update")
    //public String updateEvent(
    public ResponseEntity<Map<String, String>> updateClassification(
            @RequestParam String id,
            @RequestBody Map<String, String> data) {

        String name = data.get("name");
        String classType = data.get("classType");

        // Appel à votre service SPARQL pour mettre à jour l'événement
        String updatedId = sparqlUtils.updateClassification(id, name, classType);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Event with ID " + updatedId + " updated successfully!");
        return ResponseEntity.ok(response);
    }

}
