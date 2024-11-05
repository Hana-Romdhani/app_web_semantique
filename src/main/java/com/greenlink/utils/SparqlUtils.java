package com.greenlink.utils;


import com.greenlink.config.JenaEngine;
import org.apache.jena.query.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                        "SELECT ?id ?titre ?contenu ?dateSoumission WHERE { " +
                        "  ?conseil a agr:ConseilEnAttente ; " +
                        "           agr:idConseil ?id ; " + // Use agr prefix for idConseil
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
            filteredResult.put("id", result.get("id"));
            filteredResult.put("titre", result.get("titre"));
            filteredResult.put("contenu", result.get("contenu"));

            // Extract date without datatype suffix
            String dateSoumission = result.get("dateSoumission");
            if (dateSoumission != null) {
                dateSoumission = dateSoumission.split("\\^\\^")[0];
            }
            filteredResult.put("dateSoumission", dateSoumission);

            filteredResults.add(filteredResult);
        }

        // Return the filtered results
        return filteredResults;
    }



    public List<Map<String, String>> getConseilsApprouves() {
        // Ensure AGRICULTURE_NAMESPACE is defined and populated
        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?id ?titre ?contenu ?dateSoumission ?dateApprobation WHERE { " +
                        "  ?conseil a agr:ConseilApprouve ; " +
                        "           agr:idConseil ?id ; " +
                        "           agr:titreConseil ?titre ; " +
                        "           agr:contenuConseil ?contenu ; " +
                        "           agr:dateSoumission ?dateSoumission ; " +
                        "           agr:dateApprobation ?dateApprobation . " +
                        "}",
                AGRICULTURE_NAMESPACE
        );

        // Execute the query
        List<Map<String, String>> results = jenaEngine.executeSelectQuery(queryStr);

        // Create a new list to hold the filtered results
        List<Map<String, String>> filteredResults = new ArrayList<>();

        // Loop through the results and only include the desired keys
        for (Map<String, String> result : results) {
            Map<String, String> filteredResult = new HashMap<>();
            filteredResult.put("id", result.get("id"));
            filteredResult.put("titre", result.get("titre"));
            filteredResult.put("contenu", result.get("contenu"));
            // Get dateSoumission and remove the datatype portion if present
            String dateSoumission = result.get("dateSoumission");
            if (dateSoumission != null) {
                dateSoumission = dateSoumission.split("\\^\\^")[0];
            }
            filteredResult.put("dateSoumission", dateSoumission);

            // Get dateApprobation and remove the datatype portion if present
            String dateApprobation = result.get("dateApprobation");
            if (dateApprobation != null) {
                dateApprobation = dateApprobation.split("\\^\\^")[0];
            }
            filteredResult.put("dateApprobation", dateApprobation);

            filteredResults.add(filteredResult);
        }

        // Return the filtered results
        return filteredResults;
    }
    public void ajouterCommentaire(String idConseil, String contenuCommentaire, String auteur, boolean isVisiteur) {
        // Generate a new comment ID
        String idCommentaire = UUID.randomUUID().toString();

        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String dateCommentaire = now.format(formatter);

        // Format the date for SPARQL
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#dateTime>", dateCommentaire);
        String typeCommentaire = isVisiteur ? "CommentaireJardinier" : "CommentaireVisiteur";

        // Build the SPARQL insert query
        String query = String.format(
                "PREFIX agr: <%s> " +  // The AGRICULTURE_NAMESPACE
                        "INSERT DATA { " +
                        "  _:commentaire a agr:%s ; " +  // Type of the comment (e.g., CommentaireVisiteur or CommentaireJardinier)
                        "                 agr:contenuCommentaire \"%s\" ; " +  // Content of the comment
                        "                 agr:dateCommentaire %s ; " +  // Date of the comment
                        "                 agr:auteurCommentaire \"%s\" ; " +  // Author of the comment
                        "                 agr:idCommentaire \"%s\" . " +  // ID of the comment
                        "  <%s> agr:aCommentaire _:commentaire . " +  // Relationship to the advice
                        "}",
                AGRICULTURE_NAMESPACE,
                typeCommentaire,
                contenuCommentaire,
                formattedDate,
                auteur,
                idCommentaire,
                idConseil
        );


        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }
    public List<Map<String, String>> getCommentairesForConseil(String idConseil) {
        // SPARQL query to get the comments associated with the given idConseil
        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?contenuCommentaire ?dateCommentaire ?auteurCommentaire ?idCommentaire " +
                        "WHERE { " +
                        "  <%s> agr:aCommentaire ?commentaire . " +
                        "  ?commentaire a ?type ; " +
                        "             agr:contenuCommentaire ?contenuCommentaire ; " +
                        "             agr:dateCommentaire ?dateCommentaire ; " +
                        "             agr:auteurCommentaire ?auteurCommentaire ; " +
                        "             agr:idCommentaire ?idCommentaire . " +
                        "} ",
                AGRICULTURE_NAMESPACE, idConseil
        );

        // Execute the query
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.create(query, jenaEngine.getModel());
        ResultSet results = qexec.execSelect();

        // Create a new list to hold the filtered results
        List<Map<String, String>> filteredResults = new ArrayList<>();

        // Collect results into a list of maps
        while (results.hasNext()) {
            QuerySolution sol = results.nextSolution();
            Map<String, String> commentMap = new HashMap<>();

            commentMap.put("contenuCommentaire", sol.getLiteral("contenuCommentaire").getString());
            commentMap.put("dateCommentaire", sol.getLiteral("dateCommentaire").getString());
            commentMap.put("auteurCommentaire", sol.getLiteral("auteurCommentaire").getString());
            commentMap.put("idCommentaire", sol.getLiteral("idCommentaire").getString());

            // Add the comment map to the filtered results
            filteredResults.add(commentMap);
        }

        // Return the list of comments as maps
        return filteredResults;
    }


    public void soumettreReponse(String idCommentaire, String contenuReponse, String auteur) {
        // Generate a new response ID
        String idReponse = UUID.randomUUID().toString();

        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String dateReponse = now.format(formatter);

        // Format the date for SPARQL
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#dateTime>", dateReponse);

        // Build the SPARQL insert query
        String query = String.format(
                "PREFIX agr: <%s> " +  // The AGRICULTURE_NAMESPACE
                        "INSERT DATA { " +
                        "  _:reponse a agr:Reponse ; " +  // Type of the response
                        "             agr:contenuReponse \"%s\" ; " +  // Content of the response
                        "             agr:dateReponse %s ; " +  // Date of the response
                        "             agr:auteurReponse \"%s\" ; " +  // Author of the response
                        "             agr:idReponse \"%s\" . " +  // ID of the response
                        "  <%s> agr:aReponse _:reponse . " +  // Relationship to the comment
                        "}",
                AGRICULTURE_NAMESPACE,    // %s for AGRICULTURE_NAMESPACE
                contenuReponse,           // %s for response content
                formattedDate,            // %s for date of response
                auteur,                   // %s for author of response
                idReponse,                // %s for response ID
                idCommentaire             // %s for the comment ID
        );

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }





}
