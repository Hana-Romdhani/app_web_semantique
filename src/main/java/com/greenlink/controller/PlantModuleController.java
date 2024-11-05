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

    @GetMapping("/plantes/{id}")
    public Map<String, String> getPlanteById(@PathVariable String id) {
        return sparqlUtils.getPlanteById(id);
    }

    @PostMapping("/plantes/add")
    public String addPlante(
            @RequestParam (required = false) String id,
            @RequestParam String nom,
            @RequestParam (required = false) String description,
            @RequestParam (required = false) String hauteur,
            @RequestParam (required = false) String type,
            @RequestParam (required = false) String saison,
            @RequestParam (required = false) String classType,
            @RequestParam(required = false) String saisonRecolte, // Optional for PlanteFruitiere and PlanteLegume
            @RequestParam(required = false) String couleurFruit,  // Optional for PlanteFruitiere
            @RequestParam(required = false) String couleurFleurs  // Optional for PlanteOrnementale
    ) {
        sparqlUtils.addPlante(id, nom, description, hauteur, type, saison, classType, saisonRecolte, couleurFruit, couleurFleurs);
        return "Plante with ID " + id + " of type " + classType + " added successfully!";
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

    // Endpoint to get all PlanteFruitieres
    @GetMapping("/plantes/fruitieres")
    public List<Map<String, String>> getAllPlanteFruitieres() {
        return sparqlUtils.getAllPlanteFruitieres();
    }

    // Endpoint to retrieve all PlanteLegume
    @GetMapping("/plantes/legumes")
    public List<Map<String, String>> getAllPlanteLegumes() {
        return sparqlUtils.getAllPlanteLegumes();
    }

    // Endpoint to retrieve all PlanteOrnementale
    @GetMapping("/plantes/ornementales")
    public List<Map<String, String>> getAllPlanteOrnementales() {
        return sparqlUtils.getAllPlanteOrnementales();
    }

    @GetMapping("/categories")
    public List<Map<String, String>> getAllCategoriesPlante() {
        System.out.println("Fetching all CategoriePlante instances...");
        return sparqlUtils.getAllCategoriesPlante();
    }




}
