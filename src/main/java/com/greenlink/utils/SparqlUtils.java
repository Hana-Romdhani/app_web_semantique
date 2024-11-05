package com.greenlink.utils;


import com.greenlink.config.JenaEngine;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SparqlUtils {

    private final JenaEngine jenaEngine;
    private static final String AGRICULTURE_NAMESPACE = "http://www.semanticweb.org/agriculture/ontologies/2024/0/urbanAgriculture#";

    public SparqlUtils(JenaEngine jenaEngine) {
        this.jenaEngine = jenaEngine;
    }

    // Method to add a ConseilEnAttente instance
    public void addConseilEnAttente(String titre, String contenu, String dateSoumission) {
        String idConseil = UUID.randomUUID().toString(); // Generate a unique identifier

        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateSoumission);
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  _:conseil a <%sConseilEnAttente> ; " +
                        "              <%stitreConseil> \"%s\" ; " +
                        "              <%scontenuConseil> \"%s\" ; " +
                        "              <%sdateSoumission> %s ; " +
                        "              <%sidConseil> \"%s\" . " + // Add UUID as the ID
                        "}",
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE,
                titre,
                AGRICULTURE_NAMESPACE,
                contenu,
                AGRICULTURE_NAMESPACE,
                formattedDate,
                AGRICULTURE_NAMESPACE,
                idConseil // Use the generated UUID
        );

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }
    public void approuverConseil(String idConseil, String dateApprobation) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateApprobation);
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "DELETE { ?conseil a <%sConseilEnAttente> . } " +
                        "INSERT { ?conseil a <%sConseilApprouve> ; " +
                        "                      <%sdateApprobation> %s . } " +
                        "WHERE { ?conseil a <%sConseilEnAttente> ; " +
                        "              <%sidConseil> \"%s\" . }",
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE,
                formattedDate,
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE,
                idConseil
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public List<Map<String, String>> getConseilsEnAttente() {
        // Ensure AGRICULTURE_NAMESPACE is defined and populated
        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?titre ?contenu ?dateSoumission WHERE { " +
                        "  ?conseil a agr:ConseilEnAttente ; " +
                        "           agr:titreConseil ?titre ; " +
                        "           agr:contenuConseil ?contenu ; " +
                        "           agr:dateSoumission ?dateSoumission . " +
                        "}",
                AGRICULTURE_NAMESPACE
        );
        System.out.println("AGRICULTURE_NAMESPACE: " + AGRICULTURE_NAMESPACE);

        // Execute the query
        List<Map<String, String>> results = jenaEngine.executeSelectQuery(queryStr);

        // Create a new list to hold the filtered results
        List<Map<String, String>> filteredResults = new ArrayList<>();

        // Loop through the results and only include the desired keys
        for (Map<String, String> result : results) {
            Map<String, String> filteredResult = new HashMap<>();
            filteredResult.put("titre", result.get("titre"));
            filteredResult.put("contenu", result.get("contenu"));

            // Get date and remove the datatype portion
            String dateSoumission = result.get("dateSoumission");
            if (dateSoumission != null) {
                // Split the string at '^^' and take the first part
                dateSoumission = dateSoumission.split("\\^\\^")[0];
            }
            filteredResult.put("dateSoumission", dateSoumission);

            filteredResults.add(filteredResult);
        }

        // Return the filtered results
        return filteredResults;
    }


    public List<Map<String, String>> getConseilsApprouves() {
        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?id ?titre ?contenu ?dateSoumission ?dateApprobation WHERE { " +
                        "  ?conseil a agr:ConseilApprouve ; " +
                        "           agr:titreConseil ?titre ; " +
                        "           agr:contenuConseil ?contenu ; " +
                        "           agr:dateSoumission ?dateSoumission ; " +
                        "           agr:dateApprobation ?dateApprobation . " +
                        "}",
                AGRICULTURE_NAMESPACE
        );

        // Execute the query and return results
        return jenaEngine.executeSelectQuery(queryStr);
    }





}
