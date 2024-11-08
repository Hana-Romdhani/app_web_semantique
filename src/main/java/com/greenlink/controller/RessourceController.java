package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8000") // Frontend Laravel
@RequestMapping("/api/ontology/ressources")
public class RessourceController {

    private final SparqlUtils sparqlUtils;

    @Autowired
    public RessourceController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }
    private static final Logger logger = LoggerFactory.getLogger(RessourceController.class);

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

        // Log les données reçues pour déboguer
        logger.info("Données reçues dans la requête POST :");
        logger.info("nom: {}", nom);
        logger.info("description: {}", description);
        logger.info("quantite: {}", quantite);
        logger.info("dateAjout: {}", dateAjout);
        logger.info("type: {}", type);
        logger.info("typeMateriel: {}", typeMateriel);
        logger.info("etat: {}", etat);
        logger.info("disponibilite: {}", disponibilite);
        logger.info("source: {}", source);
        logger.info("titre: {}", titre);
        logger.info("format: {}", format);
        logger.info("niveauCompetence: {}", niveauCompetence);

        // Traitement en fonction du type
        switch (type.toLowerCase()) {
            case "materielle":
                sparqlUtils.addRessourceMaterielle(nom, description, quantite, dateAjout, typeMateriel, etat, disponibilite);
                return "Ressource Matérielle added successfully!";
            case "naturelle":
                sparqlUtils.addRessourceNaturelle(nom, description, quantite, dateAjout, source);
                return "Ressource Naturelle added successfully!";
            case "educative":
                sparqlUtils.addRessourceEducative(nom, description, titre, format, niveauCompetence, quantite, dateAjout);
                return "Ressource Éducative added successfully!";
            default:
                return "Invalid resource type!";
        }
    }


    // Retrieve all resources
    @GetMapping("/{id}/{type}")
    public ResponseEntity<Map<String, String>> getRessourceByIdAndType(
            @PathVariable String id,
            @PathVariable String type) {

        // Appel du service pour obtenir la ressource avec l'ID et le type spécifiés
        Map<String, String> ressource = sparqlUtils.getRessourceByIdAndType(id, type);

        // Vérification si une erreur est présente dans la réponse
        if (ressource.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ressource);
        }

        // Retourne la ressource si elle a été trouvée
        return ResponseEntity.ok(ressource);
    }
    @GetMapping("/list")
    @ResponseBody
    public List<Map<String, String>> jardins() {
        return sparqlUtils.getAllRess();
    }
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
    @PutMapping("/edit/{id}")
    public ResponseEntity<String> editRessource(
            @PathVariable String id,
            @PathVariable String type,
            @RequestParam(required = false) String nom,
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
                sparqlUtils.editRessourceMaterielle( id,nom, description, quantite, dateAjout, typeMateriel, etat, disponibilite);
                return ResponseEntity.ok("Ressource Matérielle mise à jour avec succès!");
            case "naturelle":
                sparqlUtils.editRessourceNaturelle(id,nom, description, quantite, dateAjout, source);
                return ResponseEntity.ok("Ressource Naturelle mise à jour avec succès!");
            case "educative":
                sparqlUtils.editRessourceEducative( id,nom, description,quantite,dateAjout, titre, format, niveauCompetence);
                return ResponseEntity.ok("Ressource Éducative mise à jour avec succès!");
            default:
                return ResponseEntity.badRequest().body("Type de ressource invalide!");
        }
    }

    // Delete a resource by type
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteRessource(@PathVariable String id) {
        boolean isDeleted = sparqlUtils.deleteRessource(id); // Assuming you will implement this method in SparqlUtils

        if (isDeleted) {
            return ResponseEntity.ok("Ressource with ID " + id + " deleted successfully!");
        } else {
            return ResponseEntity.status(404).body("Ressource with ID " + id + " not found.");
        }
    }

}
