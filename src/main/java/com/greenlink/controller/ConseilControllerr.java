package com.greenlink.controller;



import com.greenlink.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;

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
}
