package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ontology")
public class DechetController {
    private final SparqlUtils sparqlUtils;

    @Autowired
    public DechetController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    @GetMapping("/dechets")
    public List<Map<String, String>> getAllDechets() {
        System.out.println("Fetching all Dechet instances...");
        return sparqlUtils.getAllDechets();
    }

    @PostMapping("/dechets/add")
    public String addDechet(
            @RequestParam(required = false) String id,
            @RequestParam String nomDechet,
            @RequestParam(required = false) String descriptionDechet,
            @RequestParam(required = false) String typeDechet,
            @RequestParam(required = false) String methodeTraitement,
            @RequestParam(required = false) String dangerosite,
            @RequestParam(required = false) String classType
    ) {
        // Generate ID if not provided
        if (id == null || id.isEmpty()) {
            id = "Dechet_" + UUID.randomUUID().toString();
        }

        String generatedId = sparqlUtils.addDechet(id, nomDechet, descriptionDechet, typeDechet, methodeTraitement, dangerosite, classType);
        return "Dechet with ID " + generatedId + " added successfully!";
    }

    // Fetch all dechets of type Organique
    @GetMapping("/dechets/organique")
    public List<Map<String, String>> getAllOrganiques() {
        return sparqlUtils.getDechetsByClassType("Organique");
    }

    // Fetch all dechets of type Plastique
    @GetMapping("/dechets/plastique")
    public List<Map<String, String>> getAllPlastiques() {
        return sparqlUtils.getDechetsByClassType("Plastique");
    }

    // Fetch all dechets of type Metal
    @GetMapping("/dechets/metal")
    public List<Map<String, String>> getAllMetals() {
        return sparqlUtils.getDechetsByClassType("Metal");
    }

    // Endpoint to delete a dechet
    @DeleteMapping("/dechets/delete")
    public String deleteDechet(@RequestParam String id) {
        sparqlUtils.deleteDechet(id);
        return "Dechet with ID " + id + " deleted successfully!";
    }

    // Endpoint to update a dechet
    @PutMapping("/dechets/update")
    public String updateDechet(
            @RequestParam String id,
            @RequestParam String nomDechet,
            @RequestParam(required = false) String descriptionDechet,
            @RequestParam(required = false) String typeDechet,
            @RequestParam(required = false) String methodeTraitement,
            @RequestParam(required = false) String dangerosite,
            @RequestParam(required = false) String classType
    ) {
        String updatedId = sparqlUtils.updateDechet(id, nomDechet, descriptionDechet, typeDechet, methodeTraitement, dangerosite, classType);
        return "Dechet with ID " + updatedId + " updated successfully!";
    }
}
