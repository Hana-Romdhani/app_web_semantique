package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ontology/evenements")
public class EvenementController {

    private final SparqlUtils sparqlUtils;

    @Autowired
    public EvenementController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    // Endpoint to add a ConseilEnAttente
    @PostMapping("/add")
    public String addEvenement(
            @RequestParam String titre,
            @RequestParam String lieu,
            @RequestParam String description,
            @RequestParam String date) {
        sparqlUtils.addEvenement(titre, lieu, description, date);
        return "Event added and saved to OWL file successfully!";
    }

    @GetMapping("/list")
    @ResponseBody
    public List<Map<String, String>> listEvenements() {
        return sparqlUtils.getAllEvenements();
    }

    @PutMapping("/update/{id}")
    public String updateEvenement(
            @PathVariable String id,
            @RequestParam String titre,
            @RequestParam String lieu,
            @RequestParam String description,
            @RequestParam String date) {
        sparqlUtils.updateEvenement(id, titre, lieu, description, date);
        return "Event updated successfully!";
    }

    // Endpoint to delete an event
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEvenement(@PathVariable String id) {
        boolean isDeleted = sparqlUtils.deleteEvenement(id); // Assuming you will implement this method in SparqlUtils

        if (isDeleted) {
            return ResponseEntity.ok("Event with ID " + id + " deleted successfully!");
        } else {
            return ResponseEntity.status(404).body("Event with ID " + id + " not found.");
        }
    }


}
