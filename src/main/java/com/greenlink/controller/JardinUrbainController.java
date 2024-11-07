package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ontology/jardins")
public class JardinUrbainController {

    private final SparqlUtils sparqlUtils;

    @Autowired
    public JardinUrbainController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    // Add JardinPartage
    @PostMapping("/addPartage")
    public String addJardinPartage(
            @RequestParam(required = false) String id,
            @RequestParam String nom,
            @RequestParam String localisation,
            @RequestParam Double superficie,
            @RequestParam String responsable,
            @RequestParam Integer nombreParticipants,
            @RequestParam String typeParticipation) {
        sparqlUtils.addJardinPartage(id,nom, localisation, superficie, responsable, nombreParticipants, typeParticipation);
        return "Shared garden added and saved to OWL file successfully!";
    }

    // Add JardinPrive
    @PostMapping("/addPrive")
    public String addJardinPrive(
            @RequestParam(required = false) String id,
            @RequestParam String nom,
            @RequestParam String localisation,
            @RequestParam Double superficie,
            @RequestParam String responsable,
            @RequestParam String proprietaire,
            @RequestParam String dateCreation) {
        sparqlUtils.addJardinPrive(id,nom, localisation, superficie, responsable, proprietaire, dateCreation);
        return "Private garden added and saved to OWL file successfully!";
    }

    // List all gardens
    @GetMapping("/list")
    @ResponseBody
    public List<Map<String, String>> jardins() {
        return sparqlUtils.getAllJardins();
    }


    // Update JardinPartage or JardinPrive by ID
    @PutMapping("/updateJardinPrive/{id}")
    public String updateJardin(
            @PathVariable String id,
            @RequestParam String nom,
            @RequestParam String localisation,
            @RequestParam Double superficie,
            @RequestParam String responsable,
            @RequestParam(required = false) String proprietaire,
            @RequestParam(required = false) String dateCreation) {
        sparqlUtils.updateJardinPrive(id, nom, localisation, superficie,  responsable, proprietaire, dateCreation);
        return "Garden updated successfully!";
    }

    @PutMapping("/updateJardinPartage/{id}")
    public String updateJardin(
            @PathVariable String id,
            @RequestParam String nom,
            @RequestParam String localisation,
            @RequestParam Double superficie,
            @RequestParam(required = false) Integer nombreParticipants,
            @RequestParam(required = false) String typeParticipation) {
        sparqlUtils.updateJardinPartage(id, nom, localisation, superficie,   nombreParticipants, typeParticipation);
        return "Garden updated successfully!";
    }

    // Delete a garden by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteJardin(@PathVariable String id) {
        boolean isDeleted = sparqlUtils.deleteJardin(id);
        if (isDeleted) {
            return ResponseEntity.ok("Garden with ID " + id + " deleted successfully!");
        } else {
            return ResponseEntity.status(404).body("Garden with ID " + id + " not found.");
        }
    }


    // Get a Jardin by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<Map<String, String>> getJardinById(@PathVariable String id) {
        Map<String, String> jardin = sparqlUtils.getJardinById(id);
        if (jardin.containsKey("error")) {
            return ResponseEntity.status(404).body(jardin); // Jardin not found
        }
        return ResponseEntity.ok(jardin); // Return jardin details
    }




    // Lister les jardins par type (JardinPartage ou JardinPrive)
    @GetMapping("/listByType/{type}")
    public List<String> listGardensByType(@PathVariable String type) {
        if (!type.equalsIgnoreCase("JardinPartage") && !type.equalsIgnoreCase("JardinPrive")) {
            throw new IllegalArgumentException("Type de jardin invalide. Utilisez JardinPartage ou JardinPrive.");
        }
        System.out.println("Listing gardens of type: " + type);  // Debug output
        return sparqlUtils.listGardensByType(type);
    }


}
