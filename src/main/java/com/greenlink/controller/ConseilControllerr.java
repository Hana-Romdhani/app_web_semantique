package com.greenlink.controller;



import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
}
