package com.greenlink.utils;


import com.greenlink.config.JenaEngine;
import org.apache.jena.query.*;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
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
    public void deleteAttente(String idConseil) {
        // Construct the URI for the Conseil using its ID
        String URI = AGRICULTURE_NAMESPACE + idConseil;

        // Build the SPARQL DELETE query to remove the entire resource
        String query = String.format(
                "PREFIX agr: <%s> " +
                        "DELETE WHERE { " +
                        "  <%s> ?p ?o . " +  // Delete all properties related to the resource
                        "} ",
                AGRICULTURE_NAMESPACE,
                URI
        );

        // Log the query for debugging purposes
        System.out.println("Executing SPARQL DELETE query: " + query);

        // Execute the SPARQL update to delete the data from the model
        try {
            jenaEngine.executeUpdate(jenaEngine.getModel(), query);
            System.out.println("Delete operation successful.");
        } catch (Exception e) {
            System.err.println("Error executing DELETE query: " + e.getMessage());
        }

        // Save the updated model to file
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
        String typeCommentaire = isVisiteur ? "CommentaireVisiteur" : "CommentaireJardinier";

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





    public List<Map<String, String>> getCommentairesByType(boolean isVisiteur) {
        // Determine the type of comment based on the visitor flag
        String typeCommentaire = isVisiteur ? "CommentaireJardinier" : "CommentaireVisiteur";

        // Build the SPARQL query to select comments by type
        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?idCommentaire ?contenuCommentaire ?dateCommentaire ?auteurCommentaire " +
                        "WHERE { " +
                        "  ?commentaire a agr:%s ; " +  // Match comments by type
                        "               agr:idCommentaire ?idCommentaire ; " +  // Select the comment ID
                        "               agr:contenuCommentaire ?contenuCommentaire ; " +  // Select the comment content
                        "               agr:dateCommentaire ?dateCommentaire ; " +  // Select the comment date
                        "               agr:auteurCommentaire ?auteurCommentaire . " +  // Select the comment author
                        "} ",
                AGRICULTURE_NAMESPACE,
                typeCommentaire
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

            // Add the result fields to the map
            commentMap.put("idCommentaire", sol.getLiteral("idCommentaire").getString());
            commentMap.put("contenuCommentaire", sol.getLiteral("contenuCommentaire").getString());
            commentMap.put("dateCommentaire", sol.getLiteral("dateCommentaire").getString());
            commentMap.put("auteurCommentaire", sol.getLiteral("auteurCommentaire").getString());

            // Add the comment map to the filtered results
            filteredResults.add(commentMap);
        }

        // Return the list of filtered comments as maps
        return filteredResults;
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


    /*public void soumettreReponse(String idCommentaire, String contenuReponse, String auteur) {
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
    }*/



    public void soumettreReponse(String idCommentaire, String contenuReponse, String auteur) {
        // Generate a new response ID
        String idReponse = UUID.randomUUID().toString();

        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String dateReponse = now.format(formatter);

        // Format the date for SPARQL
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#dateTime>", dateReponse);

        // Ensure no double # is included in the URI
        String responseUri = AGRICULTURE_NAMESPACE + idReponse;  // Correct URI format without an additional #

        // Build the SPARQL insert query with the response URI
        String query = String.format(
                "PREFIX agr: <%s> " +  // The AGRICULTURE_NAMESPACE
                        "INSERT DATA { " +
                        "  <%s> a agr:Reponse ; " +  // Type of the response (using the generated URI)
                        "             agr:contenuReponse \"%s\" ; " +  // Content of the response
                        "             agr:dateReponse %s ; " +  // Date of the response
                        "             agr:auteurReponse \"%s\" ; " +  // Author of the response
                        "             agr:idReponse \"%s\" . " +  // ID of the response
                        "  <%s> agr:aReponse <%s> . " +  // Relationship to the comment
                        "}",
                AGRICULTURE_NAMESPACE,    // %s for AGRICULTURE_NAMESPACE
                responseUri,              // %s for response URI
                contenuReponse,           // %s for response content
                formattedDate,            // %s for date of response
                auteur,                   // %s for author of response
                idReponse,                // %s for response ID
                idCommentaire,            // %s for the comment ID
                responseUri               // %s for response URI
        );

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public List<Map<String, String>> getReponsesByCommentaire(String idCommantaire) {
        String selectQuery = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?contenuReponse ?dateReponse ?auteurReponse ?idReponse " +
                        "WHERE { " +
                        "  <file:///G:/websemantique/%s> agr:aReponse ?reponse . " +
                        "  ?reponse agr:contenuReponse ?contenuReponse ; " +
                        "           agr:dateReponse ?dateReponse ; " +
                        "           agr:auteurReponse ?auteurReponse ; " +
                        "           agr:idReponse ?idReponse . " +
                        "} ",
                AGRICULTURE_NAMESPACE, idCommantaire
        );

        Query query = QueryFactory.create(selectQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, jenaEngine.getModel());
        ResultSet results = qexec.execSelect();

        List<Map<String, String>> responseList = new ArrayList<>();

        while (results.hasNext()) {
            QuerySolution sol = results.nextSolution();
            Map<String, String> responseMap = new HashMap<>();

            responseMap.put("contenuReponse", sol.getLiteral("contenuReponse").getString());
            responseMap.put("dateReponse", sol.getLiteral("dateReponse").getString());
            responseMap.put("auteurReponse", sol.getLiteral("auteurReponse").getString());
            responseMap.put("idReponse", sol.getLiteral("idReponse").getString());

            responseList.add(responseMap);
        }

        qexec.close();

        return responseList;
    }



    public void updateReponse(String contenuReponse, String auteur) {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String dateReponse = now.format(formatter);

        // Format the date for SPARQL
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#dateTime>", dateReponse);

        // Build the SPARQL update query to modify the existing response based on the author
        String query = String.format(
                "PREFIX agr: <%s> " +  // The AGRICULTURE_NAMESPACE
                        "DELETE { " +
                        "  ?response agr:contenuReponse ?oldContenuReponse ; " +  // Delete the old content of the response
                        "             agr:dateReponse ?oldDateReponse ; " +  // Delete the old date of the response
                        "             agr:auteurReponse ?oldAuteurReponse . " +  // Delete the old author of the response
                        "} " +
                        "INSERT { " +
                        "  ?response agr:contenuReponse \"%s\" ; " +  // Insert the new content of the response
                        "             agr:dateReponse %s ; " +  // Insert the new date of the response
                        "             agr:auteurReponse \"%s\" . " +  // Insert the new author of the response
                        "} " +
                        "WHERE { " +
                        "  ?response a agr:Reponse ; " +  // Identify the response by its type
                        "             agr:auteurReponse \"%s\" . " +  // Match the response by author
                        "} ",
                AGRICULTURE_NAMESPACE,    // %s for AGRICULTURE_NAMESPACE
                contenuReponse,           // %s for response content
                formattedDate,            // %s for date of response
                auteur,                   // %s for author of response
                auteur                    // %s for matching author in WHERE clause
        );

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

 /*   public void deleteResponse(String idResponse) {
        // Build the SPARQL query to delete all triples associated with the response based on idResponse
        String query = String.format(
                "PREFIX agr: <%s> " +  // The AGRICULTURE_NAMESPACE
                        "DELETE { " +
                        "  ?response ?predicate ?object . " +  // Delete all properties associated with the response
                        "} " +
                        "WHERE { " +
                        "  ?response a agr:Reponse ; " +       // Identify the response by its type
                        "             agr:idResponse \"%s\" ; " +  // Match the response by idResponse
                        "             ?predicate ?object . " +     // Select all properties and objects linked to the response
                        "} ",
                AGRICULTURE_NAMESPACE,  // %s for AGRICULTURE_NAMESPACE
                idResponse              // %s for id of the response
        );

        // Execute the SPARQL update to delete the response
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }*/





}
