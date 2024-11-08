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
    public void deleteContenuConseil(String contenuConseil) {
        // Build the SPARQL DELETE query to remove any instance of 'contenuConseil' with the specified value
        String query = String.format(
                "PREFIX agr: <%s> " +
                        "DELETE WHERE { " +
                        "  ?s agr:contenuConseil \"%s\" . " +  // Delete any resource with the specified 'contenuConseil' value
                        "} ",
                AGRICULTURE_NAMESPACE,
                contenuConseil
        );

        // Log the query for debugging purposes
        System.out.println("Executing SPARQL DELETE query for contenuConseil value: " + query);

        // Execute the SPARQL update to delete the specific property value
        try {
            jenaEngine.executeUpdate(jenaEngine.getModel(), query);
            System.out.println("Delete operation for contenuConseil executed.");
        } catch (Exception e) {
            System.err.println("Error executing DELETE query for contenuConseil: " + e.getMessage());
        }

        // Verify if any 'contenuConseil' with the specified value still exists
        String checkQuery = String.format(
                "ASK { ?s agr:contenuConseil \"%s\" }",
                contenuConseil
        );

        try {
            boolean exists = jenaEngine.executeAskQuery(checkQuery);
            if (exists) {
                System.out.println("contenuConseil still exists after DELETE operation.");
            } else {
                System.out.println("contenuConseil successfully deleted.");
            }
        } catch (Exception e) {
            System.err.println("Error executing ASK query for contenuConseil: " + e.getMessage());
        }

        // Save the updated model to file
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
            System.out.println("Delete operation executed.");
        } catch (Exception e) {
            System.err.println("Error executing DELETE query: " + e.getMessage());
        }

        // Verify if the resource still exists after the delete operation
        String checkQuery = String.format(
                "ASK { <%s> ?p ?o }",
                URI
        );

        try {
            boolean exists = jenaEngine.executeAskQuery( checkQuery);
            if (exists) {
                System.out.println("Resource still exists after DELETE operation.");
            } else {
                System.out.println("Resource successfully deleted.");
            }
        } catch (Exception e) {
            System.err.println("Error executing ASK query: " + e.getMessage());
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







    public List<Map<String, String>> getAllPlantes() {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?plante ?classType ?id ?nom ?description ?hauteur ?type ?saison " +
                        "WHERE { " +
                        "    ?plante a ?classType ; " + // Retrieve the class type (subclass of Plante)
                        "            agri:idPlante ?id ; " +
                        "            agri:nomPlante ?nom ; " +
                        "            agri:descriptionPlante ?description ; " +
                        "            agri:hauteurMaximalePlante ?hauteur ; " +
                        "            agri:typePlante ?type ; " +
                        "            agri:saisonPlantationPlante ?saison . " +
                        "    FILTER(?classType IN (agri:Plante, agri:PlanteFruitiere, agri:PlanteLegume, agri:PlanteOrnementale)) " + // Filter for Plante and subclasses
                        "}",
                AGRICULTURE_NAMESPACE);

        List<Map<String, String>> plantes = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> planteData = new HashMap<>();
                planteData.put("classType", soln.getResource("classType").getLocalName()); // Get the specific class type
                planteData.put("id", soln.getLiteral("id").getString());
                planteData.put("nom", soln.getLiteral("nom").getString());
                planteData.put("description", soln.getLiteral("description").getString());
                planteData.put("hauteur", soln.getLiteral("hauteur").getString());
                planteData.put("type", soln.getLiteral("type").getString());
                planteData.put("saison", soln.getLiteral("saison").getString());
                plantes.add(planteData);
            }
        }
        return plantes;
    }

    public void addPlante(
            String id,
            String nom,
            String description,
            String hauteur,
            String type,
            String saison,
            String classType,
            String saisonRecolte, // Specific to PlanteFruitiere or PlanteLegume
            String couleurFruit,  // Specific to PlanteFruitiere
            String couleurFleurs  // Specific to PlanteOrnementale
    ) {
        // Generate an automatic ID if none is provided
        if (id == null || id.isEmpty()) {
            id = "Plante_" + UUID.randomUUID().toString(); // Generates a unique ID
        }

        // Construct the URI for the Plante instance
        String planteURI = AGRICULTURE_NAMESPACE + id;

        // Start building the SPARQL INSERT query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format(
                "PREFIX agri: <%s> " +
                        "INSERT DATA { " +
                        "  <%s> a agri:%s ; " + // Use classType to specify subclass (PlanteFruitiere, PlanteLegume, etc.)
                        "          agri:idPlante \"%s\" ; " +
                        "          agri:nomPlante \"%s\" ; " +
                        "          agri:descriptionPlante \"%s\" ; " +
                        "          agri:hauteurMaximalePlante \"%s\" ; " +
                        "          agri:typePlante \"%s\" ; " +
                        "          agri:saisonPlantationPlante \"%s\" . ",
                AGRICULTURE_NAMESPACE,
                planteURI,
                classType, // Dynamic class type, e.g., PlanteFruitiere
                id,
                nom,
                description,
                hauteur,
                type,
                saison
        ));

        // Conditionally add subclass-specific properties
        if ("PlanteFruitiere".equals(classType)) {
            if (saisonRecolte != null && !saisonRecolte.isEmpty()) {
                queryBuilder.append(String.format("<%s> agri:saisonRecolteFruitiere \"%s\" . ", planteURI, saisonRecolte));
            }
            if (couleurFruit != null && !couleurFruit.isEmpty()) {
                queryBuilder.append(String.format("<%s> agri:couleurFruit \"%s\" . ", planteURI, couleurFruit));
            }
        } else if ("PlanteLegume".equals(classType)) {
            if (saisonRecolte != null && !saisonRecolte.isEmpty()) {
                queryBuilder.append(String.format("<%s> agri:saisonRecolteLegume \"%s\" . ", planteURI, saisonRecolte));
            }
        } else if ("PlanteOrnementale".equals(classType)) {
            if (couleurFleurs != null && !couleurFleurs.isEmpty()) {
                queryBuilder.append(String.format("<%s> agri:couleurFleurs \"%s\" . ", planteURI, couleurFleurs));
            }
        }

        queryBuilder.append("}"); // Close the INSERT DATA block

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), queryBuilder.toString());
        jenaEngine.saveModelToFile();
    }


    public void updatePlante(String id, String nom, String description, String hauteur, String type, String saison) {
        String planteURI = AGRICULTURE_NAMESPACE + id; // Construct the URI for the Plante

        // Build the DELETE clause only for provided parameters
        StringBuilder deleteClause = new StringBuilder("DELETE { ");
        StringBuilder insertClause = new StringBuilder("INSERT { ");
        StringBuilder whereClause = new StringBuilder("WHERE { ");

        if (nom != null) {
            deleteClause.append("<").append(planteURI).append("> agri:nomPlante ?oldNom . ");
            insertClause.append("<").append(planteURI).append("> agri:nomPlante \"").append(nom).append("\" . ");
            whereClause.append("OPTIONAL { <").append(planteURI).append("> agri:nomPlante ?oldNom } ");
        }
        if (description != null) {
            deleteClause.append("<").append(planteURI).append("> agri:descriptionPlante ?oldDescription . ");
            insertClause.append("<").append(planteURI).append("> agri:descriptionPlante \"").append(description).append("\" . ");
            whereClause.append("OPTIONAL { <").append(planteURI).append("> agri:descriptionPlante ?oldDescription } ");
        }
        if (hauteur != null) {
            deleteClause.append("<").append(planteURI).append("> agri:hauteurMaximalePlante ?oldHauteur . ");
            insertClause.append("<").append(planteURI).append("> agri:hauteurMaximalePlante \"").append(hauteur).append("\" . ");
            whereClause.append("OPTIONAL { <").append(planteURI).append("> agri:hauteurMaximalePlante ?oldHauteur } ");
        }
        if (type != null) {
            deleteClause.append("<").append(planteURI).append("> agri:typePlante ?oldType . ");
            insertClause.append("<").append(planteURI).append("> agri:typePlante \"").append(type).append("\" . ");
            whereClause.append("OPTIONAL { <").append(planteURI).append("> agri:typePlante ?oldType } ");
        }
        if (saison != null) {
            deleteClause.append("<").append(planteURI).append("> agri:saisonPlantationPlante ?oldSaison . ");
            insertClause.append("<").append(planteURI).append("> agri:saisonPlantationPlante \"").append(saison).append("\" . ");
            whereClause.append("OPTIONAL { <").append(planteURI).append("> agri:saisonPlantationPlante ?oldSaison } ");
        }

        // Close the DELETE, INSERT, and WHERE clauses
        deleteClause.append("} ");
        insertClause.append("} ");
        whereClause.append("} ");

        // Combine clauses into a full SPARQL update query
        String query = String.format("PREFIX agri: <%s> %s %s %s",
                AGRICULTURE_NAMESPACE,
                deleteClause.toString(),
                insertClause.toString(),
                whereClause.toString());

        // Execute the SPARQL update and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public void deletePlante(String id) {
        String planteURI = AGRICULTURE_NAMESPACE + id; // Construct the URI from the ID

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE WHERE { " +
                        "  <%s> ?p ?o . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                planteURI
        );

        // Execute the SPARQL update and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public Map<String, String> getPlanteById(String id) {
        String planteURI = AGRICULTURE_NAMESPACE + id; // Construct the URI for the Plante

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?nom ?description ?hauteur ?type ?saison " +
                        "WHERE { " +
                        "    <%s> a agri:Plante ; " +
                        "          agri:nomPlante ?nom ; " +
                        "          agri:descriptionPlante ?description ; " +
                        "          agri:hauteurMaximalePlante ?hauteur ; " +
                        "          agri:typePlante ?type ; " +
                        "          agri:saisonPlantationPlante ?saison . " +
                        "}",
                AGRICULTURE_NAMESPACE, planteURI);

        Map<String, String> planteData = new HashMap<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                planteData.put("id", id); // Add the id to the map
                planteData.put("nom", soln.getLiteral("nom").getString());
                planteData.put("description", soln.getLiteral("description").getString());
                planteData.put("hauteur", soln.getLiteral("hauteur").getString());
                planteData.put("type", soln.getLiteral("type").getString());
                planteData.put("saison", soln.getLiteral("saison").getString());
            }
        }
        return planteData;
    }

    public List<Map<String, String>> getAllPlanteFruitieres() {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?plante ?id ?nom ?description ?hauteur ?type ?saison ?saisonRecolte ?couleurFruit " +
                        "WHERE { " +
                        "    ?plante a agri:PlanteFruitiere ; " +
                        "            agri:idPlante ?id ; " +
                        "            agri:nomPlante ?nom ; " +
                        "            agri:descriptionPlante ?description ; " +
                        "            agri:hauteurMaximalePlante ?hauteur ; " +
                        "            agri:typePlante ?type ; " +
                        "            agri:saisonPlantationPlante ?saison . " +
                        "    OPTIONAL { ?plante agri:saisonRecolteFruitiere ?saisonRecolte } " +
                        "    OPTIONAL { ?plante agri:couleurFruit ?couleurFruit } " +
                        "}",
                AGRICULTURE_NAMESPACE);

        List<Map<String, String>> planteFruitieres = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> planteData = new HashMap<>();
                planteData.put("id", soln.getLiteral("id").getString());
                planteData.put("nom", soln.getLiteral("nom").getString());
                planteData.put("description", soln.getLiteral("description").getString());
                planteData.put("hauteur", soln.getLiteral("hauteur").getString());
                planteData.put("type", soln.getLiteral("type").getString());
                planteData.put("saison", soln.getLiteral("saison").getString());
                if (soln.contains("saisonRecolte")) {
                    planteData.put("saisonRecolte", soln.getLiteral("saisonRecolte").getString());
                }
                if (soln.contains("couleurFruit")) {
                    planteData.put("couleurFruit", soln.getLiteral("couleurFruit").getString());
                }
                planteFruitieres.add(planteData);
            }
        }
        return planteFruitieres;
    }

    public List<Map<String, String>> getAllPlanteLegumes() {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?plante ?id ?nom ?description ?hauteur ?type ?saison ?saisonRecolte " +
                        "WHERE { " +
                        "    ?plante a agri:PlanteLegume ; " +
                        "            agri:idPlante ?id ; " +
                        "            agri:nomPlante ?nom ; " +
                        "            agri:descriptionPlante ?description ; " +
                        "            agri:hauteurMaximalePlante ?hauteur ; " +
                        "            agri:typePlante ?type ; " +
                        "            agri:saisonPlantationPlante ?saison . " +
                        "    OPTIONAL { ?plante agri:saisonRecolteLegume ?saisonRecolte } " +
                        "}",
                AGRICULTURE_NAMESPACE);

        List<Map<String, String>> planteLegumes = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> planteData = new HashMap<>();
                planteData.put("id", soln.getLiteral("id").getString());
                planteData.put("nom", soln.getLiteral("nom").getString());
                planteData.put("description", soln.getLiteral("description").getString());
                planteData.put("hauteur", soln.getLiteral("hauteur").getString());
                planteData.put("type", soln.getLiteral("type").getString());
                planteData.put("saison", soln.getLiteral("saison").getString());
                if (soln.contains("saisonRecolte")) {
                    planteData.put("saisonRecolte", soln.getLiteral("saisonRecolte").getString());
                }
                planteLegumes.add(planteData);
            }
        }
        return planteLegumes;
    }

    public List<Map<String, String>> getAllPlanteOrnementales() {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?plante ?id ?nom ?description ?hauteur ?type ?saison ?couleurFleurs " +
                        "WHERE { " +
                        "    ?plante a agri:PlanteOrnementale ; " +
                        "            agri:idPlante ?id ; " +
                        "            agri:nomPlante ?nom ; " +
                        "            agri:descriptionPlante ?description ; " +
                        "            agri:hauteurMaximalePlante ?hauteur ; " +
                        "            agri:typePlante ?type ; " +
                        "            agri:saisonPlantationPlante ?saison . " +
                        "    OPTIONAL { ?plante agri:couleurFleurs ?couleurFleurs } " +
                        "}",
                AGRICULTURE_NAMESPACE);

        List<Map<String, String>> planteOrnementales = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> planteData = new HashMap<>();
                planteData.put("id", soln.getLiteral("id").getString());
                planteData.put("nom", soln.getLiteral("nom").getString());
                planteData.put("description", soln.getLiteral("description").getString());
                planteData.put("hauteur", soln.getLiteral("hauteur").getString());
                planteData.put("type", soln.getLiteral("type").getString());
                planteData.put("saison", soln.getLiteral("saison").getString());
                if (soln.contains("couleurFleurs")) {
                    planteData.put("couleurFleurs", soln.getLiteral("couleurFleurs").getString());
                }
                planteOrnementales.add(planteData);
            }
        }
        return planteOrnementales;
    }

    // ******************************************************************
    // ****************************************************************** CategoriePlante

    public List<Map<String, String>> getAllCategoriesPlante() {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?categorie ?id ?nomCategorie " +
                        "WHERE { " +
                        "    ?categorie a agri:CategoriePlante ; " +
                        "               agri:nomCategoriePlante ?nomCategorie . " +
                        "    BIND(STRAFTER(STR(?categorie), \"%s\") AS ?id) " + // Extract the ID from the URI
                        "}",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE);

        List<Map<String, String>> categories = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> categoryData = new HashMap<>();
                categoryData.put("id", soln.getLiteral("id").getString()); // Extracted ID from URI
                categoryData.put("nomCategorie", soln.getLiteral("nomCategorie").getString());
                categories.add(categoryData);
            }
        }
        return categories;
    }
    public String addCategoriePlante(String id, String nomCategorie) {
        // Generate an automatic ID if none is provided
        if (id == null || id.isEmpty()) {
            id = "Categorie_" + UUID.randomUUID().toString(); // Generates a unique ID
        }
        String categorieURI = AGRICULTURE_NAMESPACE + id;

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "INSERT DATA { " +
                        "  <%s> a agri:CategoriePlante ; " +
                        "       agri:nomCategoriePlante \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                categorieURI,
                nomCategorie
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();

        return id; // Return the generated or provided ID
    }
    public void updateCategoriePlante(String id, String nomCategorie) {
        String categorieURI = AGRICULTURE_NAMESPACE + id;

        // Build the DELETE and INSERT clauses only for provided parameters
        StringBuilder deleteClause = new StringBuilder("DELETE { ");
        StringBuilder insertClause = new StringBuilder("INSERT { ");
        StringBuilder whereClause = new StringBuilder("WHERE { ");

        if (nomCategorie != null && !nomCategorie.isEmpty()) {
            deleteClause.append("<").append(categorieURI).append("> agri:nomCategoriePlante ?oldNomCategorie . ");
            insertClause.append("<").append(categorieURI).append("> agri:nomCategoriePlante \"").append(nomCategorie).append("\" . ");
            whereClause.append("OPTIONAL { <").append(categorieURI).append("> agri:nomCategoriePlante ?oldNomCategorie } ");
        }

        // Close the DELETE, INSERT, and WHERE clauses
        deleteClause.append("} ");
        insertClause.append("} ");
        whereClause.append("} ");

        // Combine clauses into a full SPARQL update query
        String query = String.format("PREFIX agri: <%s> %s %s %s",
                AGRICULTURE_NAMESPACE,
                deleteClause.toString(),
                insertClause.toString(),
                whereClause.toString());

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }
    public void deleteCategoriePlante(String id) {
        String categorieURI = AGRICULTURE_NAMESPACE + id;

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE WHERE { " +
                        "  <%s> ?p ?o . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                categorieURI
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    // ******************************************************************
    // ****************************************************************** Events
    public List<Map<String, String>> getAllEvents() {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?event ?classType ?id ?title ?description ?location ?date " +
                        "WHERE { " +
                        "    ?event a ?classType ; " +  // Retrieve the specific class type of the event
                        "           agri:idEvent ?id ; " +
                        "           agri:titreEvenement ?title ; " +
                        "           agri:descriptionEvenement ?description ; " +
                        "           agri:lieuEvenement ?location ; " +
                        "           agri:dateEvenement ?date . " +
                        "    FILTER(?classType IN (agri:EvenementLocal, agri:Conference, agri:Webinaire, agri:AtelierPratique, agri:Formation)) " +
                        "}",
                AGRICULTURE_NAMESPACE
        );

        List<Map<String, String>> events = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> eventData = new HashMap<>();
                eventData.put("classType", soln.getResource("classType").getLocalName()); // Capture the class type name
                eventData.put("id", soln.getLiteral("id").getString());
                eventData.put("title", soln.getLiteral("title").getString());
                eventData.put("description", soln.getLiteral("description").getString());
                eventData.put("location", soln.getLiteral("location").getString());
                eventData.put("date", soln.getLiteral("date").getString());
                events.add(eventData);
            }
        }
        System.out.println("Retrieved events: " + events); // Log the output to verify
        return events;
    }

    public String addEvent(String id, String title, String description, String location, String date, String classType) {
        if (id == null || id.isEmpty()) {
            id = "Event_" + UUID.randomUUID().toString(); // Generates a unique ID
        }
        String eventURI = AGRICULTURE_NAMESPACE + id;

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "INSERT DATA { " +
                        "  <%s> a agri:%s ; " +
                        "       agri:idEvent \"%s\" ; " +
                        "       agri:titreEvenement \"%s\" ; " +
                        "       agri:descriptionEvenement \"%s\" ; " +
                        "       agri:lieuEvenement \"%s\" ; " +
                        "       agri:dateEvenement \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                eventURI,
                classType,
                id,
                title,
                description,
                location,
                date
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();

        return id; // Return the generated or provided ID
    }

    public List<Map<String, String>> getEventsByClassType(String classType) {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?event ?id ?title ?description ?location ?date " +
                        "WHERE { " +
                        "    ?event a agri:%s ; " + // Dynamically match the specified classType
                        "           agri:idEvent ?id ; " +
                        "           agri:titreEvenement ?title ; " +
                        "           agri:descriptionEvenement ?description ; " +
                        "           agri:lieuEvenement ?location ; " +
                        "           agri:dateEvenement ?date . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                classType
        );

        List<Map<String, String>> events = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> eventData = new HashMap<>();
                eventData.put("id", soln.getLiteral("id").getString());
                eventData.put("title", soln.getLiteral("title").getString());
                eventData.put("description", soln.getLiteral("description").getString());
                eventData.put("location", soln.getLiteral("location").getString());
                eventData.put("date", soln.getLiteral("date").getString());
                events.add(eventData);
            }
        }
        return events;
    }

    public void deleteEvent(String id) {
        // Construct the full URI for the event using the namespace and id
        String eventURI = AGRICULTURE_NAMESPACE + id;

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE WHERE { " +
                        "  <%s> ?p ?o . " + // Delete all properties associated with the event URI
                        "}",
                AGRICULTURE_NAMESPACE,
                eventURI
        );

        // Execute the SPARQL update to delete the event and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

}
