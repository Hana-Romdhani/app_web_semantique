package com.greenlink.controller;



import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ontology/conseils")
public class ConseilControllerr {

    private final SparqlUtils sparqlUtils;

    @Autowired
    public ConseilControllerr(SparqlUtils sparqlUtils) {
        this.sparqlUtils = sparqlUtils;
    }

    // Endpoint to add a ConseilEnAttente
    @PostMapping("/add")
    public String addConseilEnAttente(
            @RequestParam String titre,
            @RequestParam String contenu,
            @RequestParam String dateSoumission) {
        sparqlUtils.addConseilEnAttente(titre, contenu, dateSoumission);
        return "ConseilEnAttente added and saved to OWL file successfully!";
    }
    @PostMapping("/approve")
    public String approveConseil(
            @RequestParam String idConseil,
            @RequestParam String dateApprobation) {
        sparqlUtils.approuverConseil(idConseil, dateApprobation);
        return "ConseilEnAttente has been approved successfully!";
    }
    // Endpoint to list all ConseilEnAttente
    @GetMapping("/listenattente")
    public ResponseEntity<List<Map<String, String>>> listConseilsEnAttente() {
        try {
            List<Map<String, String>> conseilsEnAttente = sparqlUtils.getConseilsEnAttente();
            return ResponseEntity.ok(conseilsEnAttente); // 200 OK with the list
        } catch (Exception e) {
            // Log the error (optional)
            // Log.error("Error fetching conseils en attente: " + e.getMessage());
            return ResponseEntity.status(500).body(Collections.emptyList()); // 500 Internal Server Error
        }
    }
    @GetMapping("/listapprouves")
    public List<Map<String, String>> listConseilsApprouves() {
        return sparqlUtils.getConseilsApprouves();
    }

    @PostMapping("/comment")
    public String addComment(
            @RequestParam String idConseil,
            @RequestParam String contenuCommentaire,
            @RequestParam String auteur,
            @RequestParam boolean isVisiteur) {
        // Call the updated method without dateCommentaire parameter
        sparqlUtils.ajouterCommentaire(idConseil, contenuCommentaire, auteur, isVisiteur);
        return "Comment added successfully!";
    }

    @GetMapping("/{idConseil}")
    public List<Map<String, String>> getCommentaires(@PathVariable String idConseil) {
        // Call the method to get comments for the given idConseil
        return sparqlUtils.getCommentairesForConseil(idConseil);
    }

    @PostMapping("/reply")
    public String submitReply(@RequestBody Map<String, String> requestBody) {
        String idCommentaire = requestBody.get("idCommentaire");
        String contenuReponse = requestBody.get("contenuReponse");
        String auteur = requestBody.get("auteur");

        sparqlUtils.soumettreReponse(idCommentaire, contenuReponse, auteur);
        return "Reply added successfully!";
    }
    @GetMapping ("/getReplies/{idCommantaire}")
    public List<Map<String, String>> getReplies(@PathVariable String idCommantaire) {
        return sparqlUtils.getReponsesByCommentaire(idCommantaire); // Return the list of responses
    }
    @PutMapping("/update")
    public ResponseEntity<String> updateReponse(
            @RequestParam String contenuReponse,  // New content of the response
            @RequestParam String auteur) {        // Author of the response

        try {
            // Call the service method to update the response
            sparqlUtils.updateReponse(contenuReponse, auteur);

            // Return a success message
            return ResponseEntity.ok("Response updated successfully.");
        } catch (Exception e) {
            // Return an error message in case of failure
            return ResponseEntity.status(500).body("Failed to update response: " + e.getMessage());
        }
    }
    @DeleteMapping("reponsedelete/{idResponse}")
    public ResponseEntity<String> deleteResponse(@PathVariable String idResponse) {
        try {
            // Call the service method to delete the response by idResponse
            sparqlUtils.deleteResponse(idResponse);

            // Return success response
            return ResponseEntity.ok("Response deleted successfully.");
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();

            // Return error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the response.");
        }
    }


}
