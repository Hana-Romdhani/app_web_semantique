package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ontology/ressources")
public class RessourceController {

    private final SparqlUtils sparqlUtils;

    @Autowired
    public RessourceController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    // ---------------- CRUD for Generic Ressource and Subclasses ----------------

    // Create a new resource, specifying the type for subclass differentiation
    @PostMapping("/add")
    public String addRessource(
            @RequestParam String nom,
            @RequestParam String description,
            @RequestParam int quantite,
            @RequestParam String dateAjout,
            @RequestParam String type,  // Specify "materielle", "naturelle", or "educative"
            @RequestParam(required = false) String typeMateriel,
            @RequestParam(required = false) String etat,
            @RequestParam(required = false) Boolean disponibilite,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String niveauCompetence) {

        switch (type.toLowerCase()) {
            case "materielle":
                sparqlUtils.addRessourceMaterielle(nom, description, quantite, dateAjout, typeMateriel, etat, disponibilite);
                return "Ressource Matérielle added successfully!";
            case "naturelle":
                sparqlUtils.addRessourceNaturelle(nom, description, quantite, dateAjout, source);
                return "Ressource Naturelle added successfully!";
            case "educative":
                sparqlUtils.addRessourceEducative(nom, description, titre, format, niveauCompetence);
                return "Ressource Éducative added successfully!";
            default:
                return "Invalid resource type!";
        }
    }

    // Retrieve all resources
    @GetMapping("/all")
    public String getAllRessources() {
        try {
            return sparqlUtils.getAllRessources().toString();
        } catch (Exception e) {
            return "Error retrieving resources: " + e.getMessage();
        }
    }


    // Retrieve all resources by type
    @GetMapping("/{type}/all")
    public String getAllRessourcesByType(@PathVariable String type) {
        switch (type.toLowerCase()) {
            case "materielle":
                return sparqlUtils.getAllRessourcesMaterielles();
            case "naturelle":
                return sparqlUtils.getAllRessourcesNaturelles();
            case "educative":
                return sparqlUtils.getAllRessourcesEducatives();
            default:
                return "Invalid resource type!";
        }
    }

    // Retrieve a single resource by ID
    @GetMapping("/{id}")
    public String getRessourceById(@PathVariable String id) {
        return sparqlUtils.getRessourceById(id);
    }

    // Update a resource by type
    @PutMapping("/edit/{type}/{nom}")
    public ResponseEntity<String> editRessource(
            @PathVariable String type,
            @PathVariable String nom,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) int quantite,
            @RequestParam(required = false) String dateAjout,
            @RequestParam(required = false) String typeMateriel,
            @RequestParam(required = false) String etat,
            @RequestParam(required = false) Boolean disponibilite,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String niveauCompetence) {

        // Mise à jour selon le type
        switch (type.toLowerCase()) {
            case "materielle":
                sparqlUtils.editRessourceMaterielle( nom, description, quantite, dateAjout, typeMateriel, etat, disponibilite);
                return ResponseEntity.ok("Ressource Matérielle mise à jour avec succès!");
            case "naturelle":
                sparqlUtils.editRessourceNaturelle( nom, description, quantite, dateAjout, source);
                return ResponseEntity.ok("Ressource Naturelle mise à jour avec succès!");
            case "educative":
                sparqlUtils.editRessourceEducative( nom, description,quantite, titre, format, niveauCompetence);
                return ResponseEntity.ok("Ressource Éducative mise à jour avec succès!");
            default:
                return ResponseEntity.badRequest().body("Type de ressource invalide!");
        }
    }

    // Delete a resource by type
    @DeleteMapping("/delete/{type}/{nom}")
    public String deleteRessource(@PathVariable String type, @PathVariable String nom) {
        switch (type.toLowerCase()) {
            case "materielle":
                sparqlUtils.deleteRessourceMaterielle(nom);
                return "Ressource Matérielle deleted successfully!";
            case "naturelle":
                sparqlUtils.deleteRessourceNaturelle(nom);
                return "Ressource Naturelle deleted successfully!";
            case "educative":
                sparqlUtils.deleteRessourceEducative(nom);
                return "Ressource Éducative deleted successfully!";
            default:
                return "Invalid resource type!";
        }
    }
}
