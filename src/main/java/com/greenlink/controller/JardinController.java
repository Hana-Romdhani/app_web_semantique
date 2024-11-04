package com.greenlink.controller;

import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ontology/jardins")
public class JardinController {

    private final SparqlUtils sparqlUtils;

    @Autowired
    public JardinController(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    // Ajouter une instance de JardinUrbain
    @PostMapping("/addJardinUrbain")
    public String addJardinUrbain(
            @RequestParam String nom,
            @RequestParam String localisation,
            @RequestParam double superficie,
            @RequestParam String plantes,
            @RequestParam String responsable) {
        sparqlUtils.addJardinUrbain(nom, localisation, superficie, plantes, responsable);
        return "JardinUrbain ajouté avec succès!";
    }

    @PostMapping("/addJardinPartage")
    public String addJardinPartage(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) double superficie,
            @RequestParam(required = false) String plantes,
            @RequestParam(required = false) String responsable,
            @RequestParam(required = false) int nombreParticipants,
            @RequestParam(required = false) String typeParticipation) {
        if (nom == null || localisation == null || plantes == null || responsable == null) {
            return "Erreur: Certains paramètres requis sont manquants.";
        }
        sparqlUtils.addJardinPartage(nom, localisation, superficie, plantes, responsable, nombreParticipants, typeParticipation);
        return "JardinPartagé ajouté avec succès!";
    }


    // Ajouter une instance de JardinPrivé avec des attributs spécifiques
    @PostMapping("/addJardinPrive")
    public String addJardinPrive(
            @RequestParam String nom,
            @RequestParam String localisation,
            @RequestParam double superficie,
            @RequestParam String plantes,
            @RequestParam String responsable,
            @RequestParam String proprietaire,
            @RequestParam String dateCreation) {
        sparqlUtils.addJardinPrive(nom, localisation, superficie, plantes, responsable, proprietaire, dateCreation);
        return "JardinPrivé ajouté avec succès!";
    }

    // Lister tous les jardins
    @GetMapping("/list")
    public List<String> listAllJardins() {
        return sparqlUtils.listAllJardins();
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

    // Supprimer un jardin par nom
    @DeleteMapping("/deleteByName/{nom}")
    public String deleteJardinByName(@PathVariable String nom) {
        sparqlUtils.deleteJardinByName(nom);
        return "Jardin supprimé avec succès!";
    }

    @PutMapping("/updateJardinPartage/{nom}")
    public ResponseEntity<String> updateJardinPartage(
            @PathVariable String nom,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) Double superficie,
            @RequestParam(required = false) String plantes,
            @RequestParam(required = false) String responsable,
            @RequestParam(required = false) Integer nombreParticipants,
            @RequestParam(required = false) String typeParticipation) {

        double superficieValue = (superficie != null) ? superficie : 0.0;
        int nombreParticipantsValue = (nombreParticipants != null) ? nombreParticipants : 0;

        sparqlUtils.editJardinPartage(nom, description, superficie, plantes, responsable, nombreParticipantsValue, typeParticipation);
        return ResponseEntity.ok("JardinPartagé mis à jour avec succès!");
    }

    // Update an existing JardinPrivé
    @PutMapping("/updateJardinPrive/{nom}")
    public ResponseEntity<String> updateJardinPrive(
            @PathVariable String nom,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) Double superficie,
            @RequestParam(required = false) String plantes,
            @RequestParam(required = false) String responsable,
            @RequestParam(required = false) String proprietaire,
            @RequestParam(required = false) String dateCreation) {

        // Call the method to edit the JardinPrivé
        sparqlUtils.editJardinPrive(nom, localisation, superficie != null ? superficie : 0.0,
                plantes, responsable, proprietaire, dateCreation);

        return ResponseEntity.ok("JardinPrivé mis à jour avec succès!");
    }


}



