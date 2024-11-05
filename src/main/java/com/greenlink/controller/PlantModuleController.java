package com.greenlink.controller;

import com.greenlink.dto.PlanteUpdateDTO;
import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ontology")
public class PlantModuleController {
    private final SparqlUtils sparqlUtils;

    @Autowired
    public PlantModuleController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    @GetMapping("/plantes")
    public List<Map<String, String>> getAllPlantes() {
        System.out.println("Fetching all Plante instances...");
        return sparqlUtils.getAllPlantes();
    }

    @GetMapping("/categories")
    public List<Map<String, String>> getAllCategoriesPlante() {
        System.out.println("Fetching all CategoriePlante instances...");
        return sparqlUtils.getAllCategoriesPlante();
    }

    @PostMapping("/plantes/add")
    public String addPlante(
            @RequestParam(required = false) String id,
            @RequestParam String nom,
            @RequestParam String description,
            @RequestParam String hauteur,
            @RequestParam String type,
            @RequestParam String saison) {

        // Generate an automatic ID if none is provided
        if (id == null || id.isEmpty()) {
            id = "Plante_" + UUID.randomUUID().toString(); // Generates a unique ID
        }

        sparqlUtils.addPlante(id, nom, description, hauteur, type, saison);
        return "Plante with ID " + id + " added successfully!";
    }


    // Endpoint to update a Plante
    @PutMapping("/plantes/update")
    public String updatePlante(
            @RequestParam String id,
            @RequestBody PlanteUpdateDTO planteUpdateDTO) {
        sparqlUtils.updatePlante(id,
                planteUpdateDTO.getNom(),
                planteUpdateDTO.getDescription(),
                planteUpdateDTO.getHauteur(),
                planteUpdateDTO.getType(),
                planteUpdateDTO.getSaison());
        return "Plante with ID " + id + " updated successfully!";
    }



    // Endpoint to delete a Plante
    @DeleteMapping("/plantes/delete")
    public String deletePlante(@RequestParam String id) {
        sparqlUtils.deletePlante(id);
        return "Plante with ID " + id + " deleted successfully!";
    }





}
