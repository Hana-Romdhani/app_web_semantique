package com.greenlink.utils;


import com.greenlink.config.JenaEngine;
import org.apache.jena.query.*;


import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.jena.rdf.model.Model;


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

    public String updateEvent(String id, String title, String description, String location, String date, String classType) {

        // Construct the full URI for the event using the namespace and id
        String eventURI = AGRICULTURE_NAMESPACE + id;

        // SPARQL query to delete the old values and insert the new ones
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE { " +
                        "  <%s> agri:titreEvenement ?oldTitle ; " +
                        "       agri:descriptionEvenement ?oldDescription ; " +
                        "       agri:lieuEvenement ?oldLocation ; " +
                        "       agri:dateEvenement ?oldDate ; " +
                        "       a ?oldClassType . " +
                        "} " +
                        "INSERT { " +
                        "  <%s> a agri:%s ; " +
                        "       agri:titreEvenement \"%s\" ; " +
                        "       agri:descriptionEvenement \"%s\" ; " +
                        "       agri:lieuEvenement \"%s\" ; " +
                        "       agri:dateEvenement \"%s\" . " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agri:titreEvenement ?oldTitle } " +
                        "  OPTIONAL { <%s> agri:descriptionEvenement ?oldDescription } " +
                        "  OPTIONAL { <%s> agri:lieuEvenement ?oldLocation } " +
                        "  OPTIONAL { <%s> agri:dateEvenement ?oldDate } " +
                        "  OPTIONAL { <%s> a ?oldClassType } " +
                        "}",
                AGRICULTURE_NAMESPACE, eventURI,
                eventURI, classType, title, description, location, date,
                eventURI, eventURI, eventURI, eventURI, eventURI
        );

        // Execute the SPARQL update query and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();

        return id; // Return the updated event ID
    }

    public String updateClassification(String id, String name, String classType) {
        // Construct the full URI for the event using the namespace and id
        String eventURI = AGRICULTURE_NAMESPACE + id;

        // SPARQL query to delete the old values and insert the new ones
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE { " +
                        "  <%s> agri:nomClassificationEvenement ?oldName ; " +
                        "       a ?oldClassType . " +
                        "} " +
                        "INSERT { " +
                        "  <%s> a agri:%s ; " +
                        "       agri:nomClassificationEvenement \"%s\" ; " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agri:nomClassificationEvenement ?oldName } " +
                        "  OPTIONAL { <%s> a ?oldClassType } " +
                        "}",
                AGRICULTURE_NAMESPACE, eventURI,
                eventURI, classType, name,
                eventURI, eventURI, eventURI, eventURI, eventURI
        );

        // Execute the SPARQL update query and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();

        return id; // Return the updated event ID

    }

    public void deleteClassification(String id) {
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

    public String addClassification(String id, String name, String classType) {
        if (id == null || id.isEmpty()) {
            id = "Event_" + UUID.randomUUID().toString(); // Generates a unique ID
        }
        String eventURI = AGRICULTURE_NAMESPACE + id;

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "INSERT DATA { " +
                        "  <%s> a agri:%s ; " +
                        "       agri:idClassification \"%s\" ; " +
                        "       agri:nomClassificationEvenement \"%s\" ; " +
                        "}",
                AGRICULTURE_NAMESPACE,
                eventURI,
                classType,
                id,
                name

        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();

        return id; // Return the generated or provided ID
    }

    public List<Map<String, String>> getAllClassifications() {

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?classificationevenement ?classType ?id ?name " +
                        "WHERE { " +
                        "    ?classification a ?classType ; " +  // Retrieve the specific class type of the event
                        "           agri:idClassification ?id ; " +
                        "           agri:nomClassificationEvenement ?name ; " +

                        "    FILTER(?classType IN (agri:AtelierPratique, agri:Conference)) " +
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
                eventData.put("name", soln.getLiteral("name").getString());

                events.add(eventData);
            }
        }
        System.out.println("Retrieved events: " + events); // Log the output to verify
        return events;
    }
    // Method to add JardinPartage
    public void addJardinPartage(String id,String nom, String localisation, Double superficie, String responsable, Integer nombreParticipants, String typeParticipation) {

        if (id == null || id.isEmpty()) {
            id = "JardinPartage_" + UUID.randomUUID().toString(); // Generates a unique ID
        }
        String jardinUri = AGRICULTURE_NAMESPACE + id;
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  <%s> a <%sJardinPartage> ; " +
                        "              <%snom> \"%s\" ; " +
                        "              <%slocalisation> \"%s\" ; " +
                        "              <%ssuperficie> \"%s\"^^xsd:double ; " +
                        "              <%sresponsable> \"%s\" ; " +
                        "              <%snombreParticipants> \"%s\"^^xsd:integer ; " +
                        "              <%stypeParticipation> \"%s\" . " +
                        "}",
                jardinUri,
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, localisation,
                AGRICULTURE_NAMESPACE, superficie.toString(),
                AGRICULTURE_NAMESPACE, responsable,
                AGRICULTURE_NAMESPACE, nombreParticipants.toString(),
                AGRICULTURE_NAMESPACE, typeParticipation
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    // Method to add JardinPrive
    public void addJardinPrive(String id,String nom, String localisation, Double superficie,  String responsable, String proprietaire, String dateCreation) {


        if (id == null || id.isEmpty()) {
            id = "JardinPrive_" + UUID.randomUUID().toString(); // Generates a unique ID
        }
        String jardinUri = AGRICULTURE_NAMESPACE + id;

        String formattedDate = String.format("\"%s\"^^xsd:date", dateCreation);

        // Convert the plantes list into a comma-separated string


        // Construct the SPARQL query with the local ID
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  <%s> a <%sJardinPrive> ; " +
                        "              <%snom> \"%s\" ; " +
                        "              <%slocalisation> \"%s\" ; " +
                        "              <%ssuperficie> \"%s\"^^xsd:double ; " +
                        "              <%sresponsable> \"%s\" ; " +
                        "              <%sproprietaire> \"%s\" ; " +
                        "              <%sdateCreation> %s . " +
                        "} ",
                jardinUri, // Use the full URI here
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, localisation,
                AGRICULTURE_NAMESPACE, superficie.toString(),
                AGRICULTURE_NAMESPACE, responsable,
                AGRICULTURE_NAMESPACE, proprietaire,
                AGRICULTURE_NAMESPACE, formattedDate
        );

        // Execute the SPARQL query to add the JardinPrive data
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();


    }


    public List<Map<String, String>> getAllJardins() {
        List<Map<String, String>> jardins = new ArrayList<>();
        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?jardin ?nom ?localisation ?superficie ?responsable ?nombreParticipants ?typeParticipation ?proprietaire ?dateCreation WHERE { " +
                        "  { " +
                        "    ?jardin a agr:JardinPrive ; " +
                        "           agr:nom ?nom ; " +
                        "           agr:localisation ?localisation ; " +
                        "           agr:superficie ?superficie ; " +
                        "           agr:responsable ?responsable . " +
                        "    OPTIONAL { ?jardin agr:nombreParticipants ?nombreParticipants . } " +
                        "    OPTIONAL { ?jardin agr:typeParticipation ?typeParticipation . } " +
                        "    OPTIONAL { ?jardin agr:proprietaire ?proprietaire . } " +
                        "    OPTIONAL { ?jardin agr:dateCreation ?dateCreation . } " +
                        "  } " +
                        "  UNION " +
                        "  { " +
                        "    ?jardin a agr:JardinPartage ; " +
                        "           agr:nom ?nom ; " +
                        "           agr:localisation ?localisation ; " +
                        "           agr:superficie ?superficie ; " +
                        "           agr:responsable ?responsable . " +
                        "    OPTIONAL { ?jardin agr:nombreParticipants ?nombreParticipants . } " +
                        "    OPTIONAL { ?jardin agr:typeParticipation ?typeParticipation . } " +
                        "    OPTIONAL { ?jardin agr:proprietaire ?proprietaire . } " +
                        "    OPTIONAL { ?jardin agr:dateCreation ?dateCreation . } " +
                        "  } " +
                        "}",
                AGRICULTURE_NAMESPACE
        );

        System.out.println("Executing SPARQL Query: " + queryStr);

        try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> jardin = new HashMap<>();

                try {
                    // Extract the URI and get the short ID (similar to the 'getAllEvenements' method)
                    if (soln.contains("jardin") && soln.getResource("jardin") != null) {
                        String fullUri = soln.getResource("jardin").getURI();
                        String shortId = fullUri.substring(fullUri.lastIndexOf("#") + 1);
                        jardin.put("id", shortId); // Using short ID
                    } else {
                        jardin.put("id", "ID non disponible");
                    }

                    // Extract the other attributes, ensuring they exist before adding them
                    jardin.put("nom", soln.contains("nom") && soln.get("nom").isLiteral() ? soln.getLiteral("nom").getString() : "Nom non disponible");
                    jardin.put("localisation", soln.contains("localisation") && soln.get("localisation").isLiteral() ? soln.getLiteral("localisation").getString() : "Localisation non disponible");
                    jardin.put("superficie", soln.contains("superficie") && soln.get("superficie").isLiteral() ? soln.getLiteral("superficie").getDouble() + "" : "Superficie non disponible");
                    jardin.put("responsable", soln.contains("responsable") && soln.get("responsable").isLiteral() ? soln.getLiteral("responsable").getString() : "Responsable non disponible");
                    jardin.put("nombreParticipants", soln.contains("nombreParticipants") && soln.get("nombreParticipants").isLiteral() ? soln.getLiteral("nombreParticipants").getInt() + "" : "Nombre non disponible");
                    jardin.put("typeParticipation", soln.contains("typeParticipation") && soln.get("typeParticipation").isLiteral() ? soln.getLiteral("typeParticipation").getString() : "Type non disponible");
                    jardin.put("proprietaire", soln.contains("proprietaire") && soln.get("proprietaire").isLiteral() ? soln.getLiteral("proprietaire").getString() : "Propriétaire non disponible");

                    // Handle the date creation separately
                    if (soln.contains("dateCreation") && soln.get("dateCreation").isLiteral()) {
                        String dateCreation = soln.getLiteral("dateCreation").getString();
                        // If the date is valid, we can store it, else return a default message
                        if (isValidDate(dateCreation)) {
                            jardin.put("dateCreation", dateCreation);
                        } else {
                            jardin.put("dateCreation", "Date non disponible");
                        }
                    } else {
                        jardin.put("dateCreation", "Date non disponible");
                    }

                    jardins.add(jardin);
                } catch (Exception e) {
                    System.err.println("Error processing jardin: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing SPARQL query: " + e.getMessage());
            e.printStackTrace();
        }

        return jardins;
    }

    // Helper method to validate date format (if needed)
    private boolean isValidDate(String dateStr) {
        try {
            // Try to parse the date (use an appropriate date format here)
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); // Example format
            format.setLenient(false);
            format.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }




    // Method to delete a Jardin by ID
    public boolean deleteJardin(String id) {
        String jardinUri = AGRICULTURE_NAMESPACE + id;
        String query = String.format("DELETE WHERE { <%s> ?property ?value . }", jardinUri);

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
        return true;
    }



    public void updateJardinPrive(String id, String nom, String localisation, Double superficie,  String responsable, String proprietaire, String dateCreation) {
        // Format the date for the SPARQL query
        String formattedDateCreation = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateCreation);
        String jardinUri = AGRICULTURE_NAMESPACE + id;


        // Construct the SPARQL update query
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:nom ?nom ; " +
                        "        agr:localisation ?localisation ; " +
                        "        agr:superficie ?superficie ; " +
                        "        agr:responsable ?responsable ; " +
                        "        agr:proprietaire ?proprietaire ; " +
                        "        agr:dateCreation ?dateCreation ; " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:nom \"%s\" ; " +
                        "        agr:localisation \"%s\" ; " +
                        "        agr:superficie \"%s\"^^xsd:double ; " +
                        "        agr:responsable \"%s\" ; " +
                        "        agr:proprietaire \"%s\" ; " +
                        "        agr:dateCreation %s ; " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agr:nom ?nom . } " +
                        "  OPTIONAL { <%s> agr:localisation ?localisation . } " +
                        "  OPTIONAL { <%s> agr:superficie ?superficie . } " +
                        "  OPTIONAL { <%s> agr:responsable ?responsable . } " +
                        "  OPTIONAL { <%s> agr:proprietaire ?proprietaire . } " +
                        "  OPTIONAL { <%s> agr:dateCreation ?dateCreation . } " +
                        "  <%s> a agr:JardinPrive . " +  // Ensure the resource type matches
                        "}",
                AGRICULTURE_NAMESPACE, jardinUri, jardinUri,
                nom, localisation, superficie, responsable, proprietaire, formattedDateCreation,
                jardinUri, jardinUri, jardinUri, jardinUri, jardinUri, jardinUri, jardinUri
        );

        // Log the SPARQL update query for debugging
        System.out.println("Executing SPARQL Update: " + query);

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public void updateJardinPartage(String id, String nom, String localisation, Double superficie, Integer nombreParticipants, String typeParticipation) {
        // Construct the SPARQL update query
        String jardinUri = AGRICULTURE_NAMESPACE + id;

        // Construct the SPARQL update query
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:nom ?nom ; " +
                        "        agr:localisation ?localisation ; " +
                        "        agr:superficie ?superficie ; " +
                        "        agr:nombreParticipants ?nombreParticipants ; " +
                        "        agr:typeParticipation ?typeParticipation ; " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:nom \"%s\" ; " +
                        "        agr:localisation \"%s\" ; " +
                        "        agr:superficie \"%s\"^^xsd:double ; " +
                        "        agr:nombreParticipants %d ; " +  // Updated attribute
                        "        agr:typeParticipation \"%s\" ; " +  // Updated attribute
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agr:nom ?nom . } " +
                        "  OPTIONAL { <%s> agr:localisation ?localisation . } " +
                        "  OPTIONAL { <%s> agr:superficie ?superficie . } " +
                        "  OPTIONAL { <%s> agr:nombreParticipants ?nombreParticipants . } " + // Ensure to include optional for old value
                        "  OPTIONAL { <%s> agr:typeParticipation ?typeParticipation . } " + // Ensure to include optional for old value
                        "  <%s> a agr:JardinPartage . " +  // Ensure the resource type matches
                        "}",
                AGRICULTURE_NAMESPACE, jardinUri, jardinUri,
                nom, localisation, superficie, nombreParticipants, typeParticipation, // Insert new values
                jardinUri, jardinUri, jardinUri, jardinUri, jardinUri, jardinUri
        );

        // Log the SPARQL update query for debugging
        System.out.println("Executing SPARQL Update: " + query);

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }



    public Map<String, String> getJardinById(String id) {
        Map<String, String> jardin = new HashMap<>();
        String jardinUri = AGRICULTURE_NAMESPACE + id; // Construct the full URI for the jardin based on the provided id

        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?nom ?localisation ?superficie ?responsable ?nombreParticipants ?typeParticipation ?proprietaire ?dateCreation WHERE { " +
                        "  <%s> a ?type ; " +
                        "         agr:nom ?nom ; " +
                        "         agr:localisation ?localisation ; " +
                        "         agr:superficie ?superficie ; " +
                        "         agr:responsable ?responsable . " +
                        "  OPTIONAL { <%s> agr:nombreParticipants ?nombreParticipants . } " +
                        "  OPTIONAL { <%s> agr:typeParticipation ?typeParticipation . } " +
                        "  OPTIONAL { <%s> agr:proprietaire ?proprietaire . } " +
                        "  OPTIONAL { <%s> agr:dateCreation ?dateCreation . } " +
                        "}",
                AGRICULTURE_NAMESPACE, jardinUri, jardinUri, jardinUri, jardinUri, jardinUri
        );

        try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();

                jardin.put("id", jardinUri);
                jardin.put("nom", soln.contains("nom") ? soln.getLiteral("nom").getString() : "Nom non disponible");
                jardin.put("localisation", soln.contains("localisation") ? soln.getLiteral("localisation").getString() : "Localisation non disponible");
                jardin.put("superficie", soln.contains("superficie") ? soln.getLiteral("superficie").getDouble() + "" : "Superficie non disponible");
                jardin.put("responsable", soln.contains("responsable") ? soln.getLiteral("responsable").getString() : "Responsable non disponible");

                // Check for optional attributes
                jardin.put("nombreParticipants", soln.contains("nombreParticipants") ? soln.getLiteral("nombreParticipants").getInt() + "" : "");
                jardin.put("typeParticipation", soln.contains("typeParticipation") ? soln.getLiteral("typeParticipation").getString() : "");
                jardin.put("proprietaire", soln.contains("proprietaire") ? soln.getLiteral("proprietaire").getString() : "");
                jardin.put("dateCreation", soln.contains("dateCreation") ? soln.getLiteral("dateCreation").getString() : "");
            } else {
                jardin.put("error", "Jardin non trouvé");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution de la requête SPARQL : " + e.getMessage());
        }

        return jardin;
    }



    public List<Map<String, String>> listGardensByType(String type) {
        String typeURI = AGRICULTURE_NAMESPACE + type;
        String query = String.format(
                "SELECT ?jardin ?nom ?localisation ?superficie " +
                        "(IF(BOUND(?proprietaire), ?proprietaire, 'N/A') AS ?proprietaire_value) " +
                        "(IF(BOUND(?dateCreation), ?dateCreation, 'N/A') AS ?dateCreation_value) " +
                        "(IF(BOUND(?nombreParticipants), ?nombreParticipants, 'N/A') AS ?nombreParticipants_value) " +
                        "(IF(BOUND(?typeParticipation), ?typeParticipation, 'N/A') AS ?typeParticipation_value) " +
                        "WHERE { " +
                        "  ?jardin a <%s> ; " +
                        "           <%snom> ?nom ; " +
                        "           <%slocalisation> ?localisation ; " +
                        "           <%ssuperficie> ?superficie ; " +
                        "  OPTIONAL { " +
                        "     ?jardin <%sproprietaire> ?proprietaire . " +
                        "     ?jardin <%sdateCreation> ?dateCreation . " +
                        "     OPTIONAL { " +
                        "         ?jardin <%snombreParticipants> ?nombreParticipants . " +
                        "         ?jardin <%stypeParticipation> ?typeParticipation . " +
                        "     } " +
                        "  } " +
                        "} ",
                typeURI,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE
        );

        System.out.println("Executing SPARQL query: " + query);

        // Create a list to store the results
        List<Map<String, String>> jardins = new ArrayList<>();

        // Run the query and get the results
        try (QueryExecution qexec = QueryExecutionFactory.create(query, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> jardin = new HashMap<>();

                // Extract the attributes and add them to the map
                try {
                    jardin.put("id", typeURI);
                    jardin.put("jardin", soln.contains("jardin") ? soln.getResource("jardin").getURI() : "Jardin non disponible");
                    jardin.put("nom", soln.contains("nom") && soln.get("nom").isLiteral() ? soln.getLiteral("nom").getString() : "Nom non disponible");
                    jardin.put("localisation", soln.contains("localisation") && soln.get("localisation").isLiteral() ? soln.getLiteral("localisation").getString() : "Localisation non disponible");
                    jardin.put("superficie", soln.contains("superficie") && soln.get("superficie").isLiteral() ? soln.getLiteral("superficie").getString() : "Superficie non disponible");
                    jardin.put("proprietaire", soln.contains("proprietaire_value") && soln.get("proprietaire_value").isLiteral() ? soln.getLiteral("proprietaire_value").getString() : "Propriétaire non disponible");
                    jardin.put("dateCreation", soln.contains("dateCreation_value") && soln.get("dateCreation_value").isLiteral() ? soln.getLiteral("dateCreation_value").getString() : "Date non disponible");
                    jardin.put("nombreParticipants", soln.contains("nombreParticipants_value") && soln.get("nombreParticipants_value").isLiteral() ? soln.getLiteral("nombreParticipants_value").getString() : "Nombre non disponible");
                    jardin.put("typeParticipation", soln.contains("typeParticipation_value") && soln.get("typeParticipation_value").isLiteral() ? soln.getLiteral("typeParticipation_value").getString() : "Type non disponible");

                    // Add the map to the list
                    jardins.add(jardin);
                } catch (Exception e) {
                    System.err.println("Error processing jardin: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing SPARQL query: " + e.getMessage());
            e.printStackTrace();
        }

        return jardins;
    }
    public List<Map<String, String>> getAllDechets() {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?dechet ?classType ?id ?nomDechet ?descriptionDechet ?typeDechet ?methodeTraitement ?dangerosite " +
                        "WHERE { " +
                        "    ?dechet a ?classType ; " +
                        "           agri:idDechet ?id ; " +
                        "           agri:nomDechet ?nomDechet ; " +
                        "           agri:descriptionDechet ?descriptionDechet ; " +
                        "           agri:typeDechet ?typeDechet ; " +
                        "           agri:methodeTraitement ?methodeTraitement ; " +
                        "           agri:dangerosite ?dangerosite . " +
                        "    FILTER(?classType IN (agri:Organique, agri:Plastique, agri:Metal)) " +
                        "}",
                AGRICULTURE_NAMESPACE
        );

        List<Map<String, String>> dechets = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> dechetData = new HashMap<>();
                dechetData.put("classType", soln.getResource("classType").getLocalName());
                dechetData.put("id", soln.getLiteral("id").getString());
                dechetData.put("nomDechet", soln.getLiteral("nomDechet").getString());
                dechetData.put("descriptionDechet", soln.getLiteral("descriptionDechet").getString());
                dechetData.put("typeDechet", soln.getLiteral("typeDechet").getString());
                dechetData.put("methodeTraitement", soln.getLiteral("methodeTraitement").getString());
                dechetData.put("dangerosite", soln.getLiteral("dangerosite").getString());
                dechets.add(dechetData);
            }
        }
        System.out.println("Retrieved dechets: " + dechets);
        return dechets;
    }

    // Method to add a new Dechet
    public String addDechet(String id, String nomDechet, String descriptionDechet, String typeDechet, String methodeTraitement, String dangerosite, String classType) {
        if (id == null || id.isEmpty()) {
            id = "Dechet_" + UUID.randomUUID().toString();
        }
        String dechetURI = AGRICULTURE_NAMESPACE + id;

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "INSERT DATA { " +
                        "  <%s> a agri:%s ; " +
                        "       agri:idDechet \"%s\" ; " +
                        "       agri:nomDechet \"%s\" ; " +
                        "       agri:descriptionDechet \"%s\" ; " +
                        "       agri:typeDechet \"%s\" ; " +
                        "       agri:methodeTraitement \"%s\" ; " +
                        "       agri:dangerosite \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                dechetURI,
                classType,
                id,
                nomDechet,
                descriptionDechet,
                typeDechet,
                methodeTraitement,
                dangerosite
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();

        return id;
    }

    // Method to get Dechets by class type
    public List<Map<String, String>> getDechetsByClassType(String classType) {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?dechet ?id ?nomDechet ?descriptionDechet ?typeDechet ?methodeTraitement ?dangerosite " +
                        "WHERE { " +
                        "    ?dechet a agri:%s ; " +
                        "           agri:idDechet ?id ; " +
                        "           agri:nomDechet ?nomDechet ; " +
                        "           agri:descriptionDechet ?descriptionDechet ; " +
                        "           agri:typeDechet ?typeDechet ; " +
                        "           agri:methodeTraitement ?methodeTraitement ; " +
                        "           agri:dangerosite ?dangerosite . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                classType
        );

        List<Map<String, String>> dechets = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> dechetData = new HashMap<>();
                dechetData.put("id", soln.getLiteral("id").getString());
                dechetData.put("nomDechet", soln.getLiteral("nomDechet").getString());
                dechetData.put("descriptionDechet", soln.getLiteral("descriptionDechet").getString());
                dechetData.put("typeDechet", soln.getLiteral("typeDechet").getString());
                dechetData.put("methodeTraitement", soln.getLiteral("methodeTraitement").getString());
                dechetData.put("dangerosite", soln.getLiteral("dangerosite").getString());
                dechets.add(dechetData);
            }
        }
        return dechets;
    }

    // Method to delete a Dechet
    public void deleteDechet(String id) {
        String dechetURI = AGRICULTURE_NAMESPACE + id;

        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE WHERE { " +
                        "  <%s> ?p ?o . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                dechetURI
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }
    public String updateDechet(String id, String nomDechet, String descriptionDechet, String typeDechet, String methodeTraitement, String dangerosite, String classType) {

        // Construct the full URI for the dechet using the namespace and id
        String dechetURI = AGRICULTURE_NAMESPACE + id;

        // SPARQL query to delete the old values and insert the new ones
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE { " +
                        "  <%s> agri:nomDechet ?oldNomDechet ; " +
                        "       agri:descriptionDechet ?oldDescriptionDechet ; " +
                        "       agri:typeDechet ?oldTypeDechet ; " +
                        "       agri:methodeTraitement ?oldMethodeTraitement ; " +
                        "       agri:dangerosite ?oldDangerosite ; " +
                        "       a ?oldClassType . " +
                        "} " +
                        "INSERT { " +
                        "  <%s> a agri:%s ; " +
                        "       agri:nomDechet \"%s\" ; " +
                        "       agri:descriptionDechet \"%s\" ; " +
                        "       agri:typeDechet \"%s\" ; " +
                        "       agri:methodeTraitement \"%s\" ; " +
                        "       agri:dangerosite \"%s\" . " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agri:nomDechet ?oldNomDechet } " +
                        "  OPTIONAL { <%s> agri:descriptionDechet ?oldDescriptionDechet } " +
                        "  OPTIONAL { <%s> agri:typeDechet ?oldTypeDechet } " +
                        "  OPTIONAL { <%s> agri:methodeTraitement ?oldMethodeTraitement } " +
                        "  OPTIONAL { <%s> agri:dangerosite ?oldDangerosite } " +
                        "  OPTIONAL { <%s> a ?oldClassType } " +
                        "}",
                AGRICULTURE_NAMESPACE, dechetURI,
                dechetURI, classType, nomDechet, descriptionDechet, typeDechet, methodeTraitement, dangerosite,
                dechetURI, dechetURI, dechetURI, dechetURI, dechetURI, dechetURI
        );

        // Execute the SPARQL update query and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();

        return id; // Return the updated dechet ID
    }
    // Method to filter Dechets based on classType
    public List<Map<String, String>> filterDechetsByClassType(String classType) {
        if (classType == null || classType.isEmpty()) {
            // Return all Dechets if no classType is provided
            return getAllDechets();
        }

        // Construct the SPARQL query to filter by classType
        String query = "SELECT ?id ?nomDechet ?descriptionDechet ?typeDechet ?methodeTraitement ?dangerosite ?classType WHERE {"
                + " ?dechet rdf:type <DechetType> ."
                + " ?dechet <hasClassType> \"" + classType + "\" ."
                + " ?dechet <hasNomDechet> ?nomDechet ."
                + " ?dechet <hasDescriptionDechet> ?descriptionDechet ."
                + " ?dechet <hasTypeDechet> ?typeDechet ."
                + " ?dechet <hasMethodeTraitement> ?methodeTraitement ."
                + " ?dechet <hasDangerosite> ?dangerosite ."
                + " ?dechet <hasClassType> ?classType ."
                + " }";

        // Execute the SPARQL query and return the results
        return executeSparqlQuery(query);
    }

    // Dummy method for executing SPARQL queries, implement the actual logic
    private List<Map<String, String>> executeSparqlQuery(String query) {
        // Implement the logic to execute SPARQL queries and return the results
        return null; // Replace with actual SPARQL query execution
    }
    //************************************************************************
    //****************************************ressource*************************************************************************
//************************************************************************
    public String getRessourceById(String id) {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?nom ?description ?quantite ?type ?dateAjout " +
                        "WHERE { " +
                        "  ?ressource a agri:Ressource ; " +
                        "             agri:id \"%s\" ; " +
                        "             agri:nom ?nom ; " +
                        "             agri:description ?description ; " +
                        "             agri:quantite ?quantite ; " +
                        "             agri:type ?type ; " +
                        "             agri:dateAjout ?dateAjout . " +
                        "}",
                AGRICULTURE_NAMESPACE, id
        );

        return jenaEngine.executeSelectQueryRs(query);
    }



    public Map<String, String> getRessourceByIdAndType(String id, String type) {
        Map<String, String> ressource = new HashMap<>();
        String ressourceUri = AGRICULTURE_NAMESPACE + id; // URI de la ressource en fonction de l'id

        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?ressource ?nom ?description ?quantite ?dateAjout ?typeMateriel ?etat ?disponibilite ?source ?titre ?format ?niveauCompetence WHERE { " +
                        "  BIND(<%s> AS ?ressource) ." +
                        "  ?ressource agr:nom ?nom ; " +
                        "            agr:description ?description ; " +
                        "            agr:quantite ?quantite ; " +
                        "            agr:dateAjout ?dateAjout . " +
                        "  OPTIONAL { ?ressource a <%s> . } " +  // Ajoutez une condition pour le type
                        "  OPTIONAL { ?ressource agr:typeMateriel ?typeMateriel . } " +
                        "  OPTIONAL { ?ressource agr:disponibilite ?disponibilite . } " +
                        "  OPTIONAL { ?ressource agr:etat ?etat . } " +
                        "  OPTIONAL { ?ressource agr:source ?source . } " +
                        "  OPTIONAL { ?ressource agr:titre ?titre . } " +
                        "  OPTIONAL { ?ressource agr:format ?format . } " +
                        "  OPTIONAL { ?ressource agr:niveauCompetence ?niveauCompetence . } " +
                        "}",
                AGRICULTURE_NAMESPACE, ressourceUri, AGRICULTURE_NAMESPACE + type  // Inclusion de type dans le filtre
        );

        try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();

                ressource.put("id", ressourceUri);
                ressource.put("nom", soln.contains("nom") ? soln.getLiteral("nom").getString() : "Nom non disponible");
                ressource.put("description", soln.contains("description") ? soln.getLiteral("description").getString() : "Description non disponible");
                ressource.put("quantite", soln.contains("quantite") ? soln.getLiteral("quantite").getString() : "Quantité non disponible");
                ressource.put("dateAjout", soln.contains("dateAjout") ? soln.getLiteral("dateAjout").getString() : "Date non disponible");

                // Type matériel, état, disponibilité, etc.
                ressource.put("typeMateriel", soln.contains("typeMateriel") ? soln.getLiteral("typeMateriel").getString() : "TypeMatériel non disponible");
                ressource.put("etat", soln.contains("etat") ? soln.getLiteral("etat").getString() : "État non disponible");
                ressource.put("disponibilite", soln.contains("disponibilite") ? soln.getLiteral("disponibilite").getString() : "Disponibilité non disponible");
                ressource.put("source", soln.contains("source") ? soln.getLiteral("source").getString() : "Source non disponible");
                ressource.put("titre", soln.contains("titre") ? soln.getLiteral("titre").getString() : "Titre non disponible");
                ressource.put("format", soln.contains("format") ? soln.getLiteral("format").getString() : "Format non disponible");
                ressource.put("niveauCompetence", soln.contains("niveauCompetence") ? soln.getLiteral("niveauCompetence").getString() : "NiveauCompétence non disponible");
            } else {
                ressource.put("error", "Ressource non trouvée ou type incompatible.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution de la requête SPARQL : " + e.getMessage());
        }

        return ressource;
    }

    public List<Map<String, String>> getAllRess() {
        List<Map<String, String>> ressources = new ArrayList<>();
        String queryStr = String.format(
                "PREFIX agri: <http://www.semanticweb.org/agriculture/ontologies/2024/0/urbanAgriculture#> " +
                        "SELECT ?ressource ?nom ?description ?quantite ?dateAjout ?type ?typeMateriel ?etat ?disponibilite ?source ?titre ?format ?niveauCompetence WHERE {\n" +
                        "  ?ressource agri:nom ?nom ;" +
                        "            agri:description ?description ; " +
                        "             agri:quantite ?quantite ;" +
                        "             agri:dateAjout ?dateAjout ; " +
                        "  OPTIONAL { ?ressource a ?type . }" +  // Changement ici pour récupérer le type via 'a' (type RDF)
                        "  OPTIONAL { ?ressource agri:typeMateriel ?typeMateriel . } " +
                        "  OPTIONAL { ?ressource agri:disponibilite ?disponibilite . } " +
                        "  OPTIONAL { ?ressource agri:etat ?etat . } " +
                        "  OPTIONAL { ?ressource agri:source ?source . } " +
                        "  OPTIONAL { ?ressource agri:titre ?titre . } " +
                        "  OPTIONAL { ?ressource agri:format ?format . } " +
                        "  OPTIONAL { ?ressource agri:niveauCompetence ?niveauCompetence . } " +
                        "}",
                AGRICULTURE_NAMESPACE
        );

        System.out.println("Executing SPARQL Query: " + queryStr);

        try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            if (!results.hasNext()) {
                System.out.println("No results found.");
            }

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                System.out.println("Processing result: " + soln);

                Map<String, String> ressource = new HashMap<>();
                try {
                    // Extraction des informations et ajout dans la map
                    if (soln.contains("ressource") && soln.getResource("ressource") != null) {
                        String fullUri = soln.getResource("ressource").getURI();
                        String shortId = fullUri.substring(fullUri.lastIndexOf("#") + 1);
                        ressource.put("id", shortId);
                    } else {
                        ressource.put("id", "ID non disponible");
                    }

                    // Autres informations de la ressource
                    ressource.put("nom", soln.contains("nom") ? soln.getLiteral("nom").getString() : "Nom non disponible");
                    ressource.put("description", soln.contains("description") ? soln.getLiteral("description").getString() : "Description non disponible");
                    ressource.put("quantite", soln.contains("quantite") ? soln.getLiteral("quantite").getString() : "Quantité non disponible");
                    ressource.put("dateAjout", soln.contains("dateAjout") ? soln.getLiteral("dateAjout").getString() : "Date non disponible");

                    // Type de la ressource - Vérifier et formater correctement
                    String type = "Type non disponible";  // Valeur par défaut
                    if (soln.contains("type") && soln.getResource("type") != null) {
                        String typeUri = soln.getResource("type").getURI();
                        // Vérifier si l'URI contient un '#'
                        if (typeUri.contains("#")) {
                            // Extraire le nom après le '#'
                            String extractedType = typeUri.substring(typeUri.lastIndexOf("#") + 1);
                            type = extractedType;  // Utiliser le nom extrait comme type
                        }
                    }
                    ressource.put("type", type);

                    // Type matériel, état, disponibilité, etc.
                    ressource.put("typeMateriel", soln.contains("typeMateriel") ? soln.getLiteral("typeMateriel").getString() : "TypeMatériel non disponible");
                    ressource.put("etat", soln.contains("etat") ? soln.getLiteral("etat").getString() : "État non disponible");
                    ressource.put("disponibilite", soln.contains("disponibilite") ? soln.getLiteral("disponibilite").getString() : "Disponibilité non disponible");
                    ressource.put("source", soln.contains("source") ? soln.getLiteral("source").getString() : "Source non disponible");
                    ressource.put("titre", soln.contains("titre") ? soln.getLiteral("titre").getString() : "Titre non disponible");
                    ressource.put("format", soln.contains("format") ? soln.getLiteral("format").getString() : "Format non disponible");
                    ressource.put("niveauCompetence", soln.contains("niveauCompetence") ? soln.getLiteral("niveauCompetence").getString() : "NiveauCompétence non disponible");

                    ressources.add(ressource);
                } catch (Exception e) {
                    System.err.println("Error processing resource: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing SPARQL query: " + e.getMessage());
            e.printStackTrace();
        }

        return ressources;
    }




    private String extractNameFromUri(String uri) {
        // Si l'URI est valide et contient un '#', extraire la dernière partie après le '#'
        if (uri != null && uri.contains("#")) {
            return uri.substring(uri.lastIndexOf("#") + 1);
        }
        // Sinon, retourner l'URI complet
        return uri;
    }




    public void addRessourceMaterielle(String nom, String description, int quantite, String dateAjout, String typeMateriel, String etat, Boolean disponibilite) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String disponibiliteValue = disponibilite ? "true" : "false";
        String ressourceUri = AGRICULTURE_NAMESPACE + "ressource" + java.util.UUID.randomUUID();

        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  <%s> a <%sRessourceMaterielle> ; " + // Directly using ressourceUri here as a URI
                        "      <%snom> \"%s\" ; " +
                        "      <%sdescription> \"%s\" ; " +
                        "      <%squantite> \"%d\"^^xsd:int ; " +
                        "      <%sdateAjout> %s ; " + // formattedDate already includes xsd:date
                        "      <%stypeMateriel> \"%s\" ; " +
                        "      <%setat> \"%s\" ; " +
                        "      <%sdisponibilite> \"%s\"^^xsd:boolean . " +
                        "}",
                ressourceUri, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, nom, AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, quantite,
                AGRICULTURE_NAMESPACE, formattedDate,
                AGRICULTURE_NAMESPACE, typeMateriel, AGRICULTURE_NAMESPACE, etat,
                AGRICULTURE_NAMESPACE, disponibiliteValue
        );

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public void addRessourceNaturelle(String nom, String description, int quantite, String dateAjout, String source) {
        // Format the date using the full IRI for xsd:date
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String ressourceUri = AGRICULTURE_NAMESPACE + "ressource" + java.util.UUID.randomUUID();

        // Correcting placeholders and adding ressourceUri in the query as a full URI
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  <%s> a <%sRessourceNaturelle> ; " +  // Using the full URI for the resource
                        "      <%snom> \"%s\" ; " +
                        "      <%sdescription> \"%s\" ; " +
                        "      <%squantite> \"%d\"^^xsd:int ; " +
                        "      <%sdateAjout> %s ; " +  // Using formattedDate directly
                        "      <%ssource> \"%s\" . " +
                        "}",
                ressourceUri,  // Insert the generated resource URI
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, quantite,
                AGRICULTURE_NAMESPACE, formattedDate,  // Use the pre-formatted date
                AGRICULTURE_NAMESPACE, source
        );

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public void addRessourceEducative(String nom, String description, String titre, String format, String niveauCompetence, int quantite, String dateAjout) {
        // Formatage de la date pour le langage RDF
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);

        // Génération d'un URI unique pour la ressource éducative
        String ressourceUri = AGRICULTURE_NAMESPACE + "ressource" + java.util.UUID.randomUUID();

        // Construction de la requête SPARQL
        String query = String.format(
                "INSERT DATA { " +
                        "  <%s> a <%sRessourceEducative> ; " +
                        "              <%snom> \"%s\" ; " +
                        "              <%sdescription> \"%s\" ; " +
                        "              <%stitre> \"%s\" ; " +
                        "              <%sformat> \"%s\" ; " +
                        "              <%sniveauCompetence> \"%s\" ; " + // Ajout du point-virgule ici
                        "              <%squantite> %d ; " + // Utiliser %d pour un entier
                        "              <%sdateAjout> %s . " +  // Utilisation de formattedDate

                        "}",
                ressourceUri, // URI généré
                AGRICULTURE_NAMESPACE , // Corrigé le namespace
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, titre,
                AGRICULTURE_NAMESPACE, format,
                AGRICULTURE_NAMESPACE, niveauCompetence,
                AGRICULTURE_NAMESPACE, quantite, // Ajustement ici pour utiliser %d
                AGRICULTURE_NAMESPACE, formattedDate // Utilisation de la date pré-formatée
        );

        // Exécution de la mise à jour de la requête
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }



    // Get all resources
    // Get all resources regardless of type
    public String getAllRessources() {
        String query = String.format("SELECT ?ressource ?nom ?propriete ?valeur WHERE { " +
                        "?ressource <%snom> ?nom . " +
                        "?ressource ?propriete ?valeur . }",
                AGRICULTURE_NAMESPACE);

        // Exécutez la requête et récupérez les résultats sous forme de liste de cartes
        List<Map<String, String>> results = jenaEngine.executeSelectQueryRs(jenaEngine.getModel(), query, "ressource", "nom", "propriete", "valeur");

        // Utiliser un StringBuilder pour construire le résultat formaté
        StringBuilder resultString = new StringBuilder();
        Map<String, Boolean> currentRessource = new HashMap<>(); // Utilisation d'un Boolean pour vérifier si le nom a déjà été ajouté

        for (Map<String, String> result : results) {
            String ressource = result.get("ressource");
            String nom = result.get("nom");
            String propriete = result.get("propriete");
            String valeur = result.get("valeur");

            // Si nous passons à une nouvelle ressource, nous l'ajoutons à la chaîne
            if (!currentRessource.containsKey(ressource)) {
                // Si la ressource a déjà été enregistrée, on peut ajouter une nouvelle ligne pour la séparer
                if (currentRessource.size() > 0) {
                    resultString.append("\n");
                }

                // Afficher la ressource directement sans le mot "Nom"
                resultString.append(String.format("Ressource: %s\n", nom));

                // Marquer cette ressource comme traitée
                currentRessource.put(ressource, true); // L'ID de la ressource est marqué comme déjà traité
            }

            // Extraire le nom de la propriété sans URI
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1);

            // Retirer le type si présent dans la valeur
            String valeurFormattee = valeur.contains("^^") ? valeur.split("\\^\\^")[0] : valeur;

            // Si la propriété est "type", extraire uniquement le nom sans l'URI
            if ("type".equals(proprieteNom)) {
                // Extraire et afficher uniquement le nom de la classe sans l'URL
                valeurFormattee = extractNameFromUri(valeur);
            }

            // Afficher la propriété avec la valeur formatée
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeurFormattee));
        }

        return resultString.toString();
    }

    // Méthode pour extraire le nom après le dernier '#'

    public List<Map<String, String>> listRessourcesByType(String type) {
        String typeURI = AGRICULTURE_NAMESPACE + type;
        String query = String.format(
                "SELECT ?ressource ?nom ?description ?quantite ?type ?dateAjout" +
                        "(IF(BOUND(?etat), ?etat, 'N/A') AS ?etat_value) " +
                        "(IF(BOUND(?source), ?source, 'N/A') AS ?source_value) " +
                        "(IF(BOUND(?format), ?format, 'N/A') AS ?format_value) " +
                        "(IF(BOUND(?typeMateriel), ?type, 'N/A') AS ?typeMateriel_value) " +
                        "(IF(BOUND(?disponibilite), ?disponibilite, 'N/A') AS ?disponibilite_value) " +
                        "(IF(BOUND(?niveauCompetence), ?niveauCompetence, 'N/A') AS ?niveauCompetence_value) " +
                        "WHERE { " +
                        "  ?ressource a <%s> ; " +
                        "           <%snom> ?nom ; " +
                        "           <%description> ?description ; " +
                        "           <%squantite> ?quantite ; " +
                        "           <%sdateAjout> ?dateAjout ; " +
                        "  OPTIONAL { " +
                        "     ?ressource <%setat> ?etat . " +
                        "     ?ressource <%ssource> ?source . " +
                        "     ?ressource <%sformat> ?format . " +
                        "     ?ressource <%stype> ?type . " +
                        "     OPTIONAL { " +
                        "         ?ressource <%sdisponibilite> ?disponibilite . " +
                        "         ?ressource <%sniveauCompetence> ?niveauCompetence . " +
                        "     } " +
                        "  } " +
                        "} ",
                typeURI,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE
        );

        System.out.println("Executing SPARQL query: " + query);

        // Create a list to store the results
        List<Map<String, String>> ressources = new ArrayList<>();

        // Run the query and get the results
        try (QueryExecution qexec = QueryExecutionFactory.create(query, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> ressource = new HashMap<>();

                // Extract the attributes and add them to the map
                try {
                    ressource.put("id", typeURI);
                    ressource.put("ressource", soln.contains("ressource") ? soln.getResource("ressource").getURI() : "ressource non disponible");
                    ressource.put("nom", soln.contains("nom") && soln.get("nom").isLiteral() ? soln.getLiteral("nom").getString() : "Nom non disponible");
                    ressource.put("description", soln.contains("description") && soln.get("description").isLiteral() ? soln.getLiteral("description").getString() : "description non disponible");
                    ressource.put("quantite", soln.contains("quantite") && soln.get("quantite").isLiteral() ? soln.getLiteral("quantite").getString() : "quantite non disponible");
                    ressource.put("etat", soln.contains("etat_value") && soln.get("etat_value").isLiteral() ? soln.getLiteral("etat_value").getString() : "etat non disponible");
                    ressource.put("dateAjout", soln.contains("dateAjout") && soln.get("dateAjout").isLiteral() ? soln.getLiteral("dateAjout").getString() : "Date non disponible");
                    ressource.put("source", soln.contains("source_value") && soln.get("source_value").isLiteral() ? soln.getLiteral("source_value").getString() : "source non disponible");
                    ressource.put("format", soln.contains("format_value") && soln.get("format_value").isLiteral() ? soln.getLiteral("format_value").getString() : "format non disponible");
                    ressource.put("typeMateriel", soln.contains("typeMateriel_value") && soln.get("typeMateriel_value").isLiteral() ? soln.getLiteral("typeMateriel_value").getString() : "typeMateriel non disponible");
                    ressource.put("disponibilite", soln.contains("disponibilite_value") && soln.get("disponibilite_value").isLiteral() ? soln.getLiteral("disponibilite_value").getString() : "disponibilite non disponible");
                    ressource.put("niveauCompetence", soln.contains("niveauCompetence_value") && soln.get("niveauCompetence_value").isLiteral() ? soln.getLiteral("niveauCompetence_value").getString() : "niveauCompetence non disponible");

                    // Add the map to the list (use put method on list to add the map)
                    ressources.add(ressource);
                } catch (Exception e) {
                    System.err.println("Error processing ressource: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing SPARQL query: " + e.getMessage());
            e.printStackTrace();
        }

        return ressources;
    }


    public String getAllRessourcesMaterielles() {
        String query = String.format("SELECT ?ressource ?nom ?propriete ?valeur WHERE { " +
                        "?ressource a <%sRessourceMaterielle> . " +
                        "?ressource <%snom> ?nom . " +
                        "?ressource ?propriete ?valeur . }",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE);

        // Exécutez la requête et récupérez les résultats sous forme de liste de cartes
        List<Map<String, String>> results = jenaEngine.executeSelectQueryRs(jenaEngine.getModel(), query, "ressource", "nom", "propriete", "valeur");

        StringBuilder resultString = new StringBuilder();
        Map<String, String> resourceInfo = new LinkedHashMap<>(); // Pour stocker les informations de ressource

        for (Map<String, String> result : results) {
            String ressourceId = result.get("ressource");
            String nom = result.get("nom");
            String propriete = result.get("propriete");
            String valeur = result.get("valeur");

            // Vérifier si la ressource a déjà été ajoutée
            if (!resourceInfo.containsKey(ressourceId)) {
                // Ajouter une nouvelle entrée pour la ressource
                resultString.append(String.format("Ressource: %s\n", ressourceId));
                resultString.append(String.format("  Nom: %s\n", nom));
                resourceInfo.put(ressourceId, nom); // Stocker la ressource avec son nom
            }

            // Extraire et afficher la propriété et sa valeur
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1); // Nom de la propriété sans URI

            // Vérifier si la valeur a un type RDF, sinon l'afficher directement
            if (valeur.contains("^^")) {
                // Extraire la valeur sans le type
                valeur = valeur.substring(0, valeur.indexOf("^^"));
            }

            // Modifier l'affichage pour éliminer les mots "Propriété" et "Valeur"
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeur));
        }

        return resultString.toString();
    }





    public String getAllRessourcesNaturelles() {
        String query = String.format("SELECT ?ressource ?nom ?propriete ?valeur WHERE { " +
                        "?ressource a <%sRessourceNaturelle> . " +
                        "?ressource <%snom> ?nom . " +
                        "?ressource ?propriete ?valeur . }",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE);

        // Exécutez la requête et récupérez les résultats sous forme de liste de cartes
        List<Map<String, String>> results = jenaEngine.executeSelectQueryRs(jenaEngine.getModel(), query, "ressource", "nom", "propriete", "valeur");

        StringBuilder resultString = new StringBuilder();
        Map<String, String> resourceInfo = new LinkedHashMap<>(); // Pour stocker les informations de ressource

        for (Map<String, String> result : results) {
            String ressourceId = result.get("ressource");
            String nom = result.get("nom");
            String propriete = result.get("propriete");
            String valeur = result.get("valeur");

            // Vérifier si la ressource a déjà été ajoutée
            if (!resourceInfo.containsKey(ressourceId)) {
                // Ajouter une nouvelle entrée pour la ressource
                resultString.append(String.format("Ressource: %s\n", ressourceId));
                resultString.append(String.format("  Nom: %s\n", nom));
                resourceInfo.put(ressourceId, nom); // Stocker la ressource avec son nom
            }

            // Extraire et afficher la propriété et sa valeur
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1); // Nom de la propriété sans URI

            // Vérifier si la valeur a un type RDF, sinon l'afficher directement
            if (valeur.contains("^^")) {
                // Extraire la valeur sans le type
                valeur = valeur.substring(0, valeur.indexOf("^^"));
            }

            // Modifier l'affichage pour éliminer les mots "Propriété" et "Valeur"
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeur));
        }

        return resultString.toString();
    }

    public String getAllRessourcesEducatives() {
        String query = String.format("SELECT ?ressource ?nom ?propriete ?valeur WHERE { " +
                        "?ressource a <%sRessourceEducative> . " +
                        "?ressource <%snom> ?nom . " +
                        "?ressource ?propriete ?valeur . }",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE);

        // Exécutez la requête et récupérez les résultats sous forme de liste de cartes
        List<Map<String, String>> results = jenaEngine.executeSelectQueryRs(jenaEngine.getModel(), query, "ressource", "nom", "propriete", "valeur");

        StringBuilder resultString = new StringBuilder();
        Map<String, String> resourceInfo = new LinkedHashMap<>(); // Pour stocker les informations de ressource

        for (Map<String, String> result : results) {
            String ressourceId = result.get("ressource");
            String nom = result.get("nom");
            String propriete = result.get("propriete");
            String valeur = result.get("valeur");

            // Vérifier si la ressource a déjà été ajoutée
            if (!resourceInfo.containsKey(ressourceId)) {
                // Ajouter une nouvelle entrée pour la ressource
                resultString.append(String.format("Ressource: %s\n", ressourceId));
                resultString.append(String.format("  Nom: %s\n", nom));
                resourceInfo.put(ressourceId, nom); // Stocker la ressource avec son nom
            }

            // Extraire et afficher la propriété et sa valeur
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1); // Nom de la propriété sans URI

            // Vérifier si la valeur a un type RDF, sinon l'afficher directement
            if (valeur.contains("^^")) {
                // Extraire la valeur sans le type
                valeur = valeur.substring(0, valeur.indexOf("^^"));
            }

            // Modifier l'affichage pour éliminer les mots "Propriété" et "Valeur"
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeur));
        }

        return resultString.toString();
    }
    public void editRessourceMaterielle(String id, String nom, String description, int quantite, String dateAjout, String typeMateriel, String etat, Boolean disponibilite) {
        // Format the date for the SPARQL query
        String formattedDateAjout = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String ressourceUri = AGRICULTURE_NAMESPACE + id;

        // Construct the SPARQL update query
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:nom ?nom ; " +
                        "        agr:description ?description ; " +
                        "        agr:quantite ?quantite ; " +
                        "        agr:typeMateriel ?typeMateriel ; " +
                        "        agr:etat ?etat ; " +
                        "        agr:disponibilite ?disponibilite ; " +
                        "        agr:dateAjout ?dateAjout ; " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:nom \"%s\" ; " +
                        "        agr:description \"%s\" ; " +
                        "        agr:quantite \"%s\"^^xsd:int ; " +
                        "        agr:typeMateriel \"%s\" ; " +
                        "        agr:etat \"%s\" ; " +
                        "        agr:disponibilite \"%s\" ; " +
                        "        agr:dateAjout %s ; " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agr:nom ?nom . } " +
                        "  OPTIONAL { <%s> agr:description ?description . } " +
                        "  OPTIONAL { <%s> agr:quantite ?quantite . } " +
                        "  OPTIONAL { <%s> agr:typeMateriel ?typeMateriel . } " +
                        "  OPTIONAL { <%s> agr:etat ?etat . } " +
                        "  OPTIONAL { <%s> agr:disponibilite ?disponibilite . } " +
                        "  OPTIONAL { <%s> agr:dateAjout ?dateAjout . } " +
                        "  <%s> a agr:RessourceMaterielle . " +  // Ensure the resource type matches
                        "}",
                AGRICULTURE_NAMESPACE, ressourceUri, ressourceUri,
                nom, description, quantite, typeMateriel,etat,disponibilite, formattedDateAjout,
                ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri,ressourceUri,ressourceUri
        );

        // Log the SPARQL update query for debugging
        System.out.println("Executing SPARQL Update: " + query);

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public void editRessourceNaturelle(String id,String nom, String description, int quantite, String dateAjout, String source) {
        // Format the date for the SPARQL query
        String formattedDateAjout = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String ressourceUri = AGRICULTURE_NAMESPACE + id;

        // Construct the SPARQL update query
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:nom ?nom ; " +
                        "        agr:description ?description ; " +
                        "        agr:quantite ?quantite ; " +
                        "        agr:source ?source ; " +
                        "        agr:dateAjout ?dateAjout ; " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:nom \"%s\" ; " +
                        "        agr:description \"%s\" ; " +
                        "        agr:quantite \"%s\"^^xsd:int ; " +
                        "        agr:source \"%s\" ; " +
                        "        agr:dateAjout %s ; " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agr:nom ?nom . } " +
                        "  OPTIONAL { <%s> agr:description ?description . } " +
                        "  OPTIONAL { <%s> agr:quantite ?quantite . } " +
                        "  OPTIONAL { <%s> agr:source ?source . } " +
                        "  OPTIONAL { <%s> agr:dateAjout ?dateAjout . } " +
                        "  <%s> a agr:RessourceNaturelle . " +  // Ensure the resource type matches
                        "}",
                AGRICULTURE_NAMESPACE, ressourceUri, ressourceUri,
                nom, description, quantite, source, formattedDateAjout,
                ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri
        );

        // Log the SPARQL update query for debugging
        System.out.println("Executing SPARQL Update: " + query);

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public void editRessourceEducative(String id,String nom, String description, int quantite, String dateAjout, String titre, String format, String niveauCompetence) {
        // Format the date for the SPARQL query
        String formattedDateAjout = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String ressourceUri = AGRICULTURE_NAMESPACE + id;

        // Construct the SPARQL update query
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:nom ?nom ; " +
                        "        agr:description ?description ; " +
                        "        agr:quantite ?quantite ; " +
                        "        agr:titre ?titre ; " +
                        "        agr:format ?format ; " +
                        "        agr:niveauCompetence ?niveauCompetence ; " +
                        "        agr:dateAjout ?dateAjout ; " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:nom \"%s\" ; " +
                        "        agr:description \"%s\" ; " +
                        "        agr:quantite \"%s\"^^xsd:int ; " +
                        "        agr:titre \"%s\" ; " +
                        "        agr:format \"%s\" ; " +
                        "        agr:niveauCompetence \"%s\" ; " +
                        "        agr:dateAjout %s ; " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agr:nom ?nom . } " +
                        "  OPTIONAL { <%s> agr:description ?description . } " +
                        "  OPTIONAL { <%s> agr:quantite ?quantite . } " +
                        "  OPTIONAL { <%s> agr:titre ?titre . } " +
                        "  OPTIONAL { <%s> agr:format ?format . } " +
                        "  OPTIONAL { <%s> agr:niveauCompetence ?niveauCompetence . } " +
                        "  OPTIONAL { <%s> agr:dateAjout ?dateAjout . } " +
                        "  <%s> a agr:RessourceEducative . " +  // Ensure the resource type matches
                        "}",
                AGRICULTURE_NAMESPACE, ressourceUri, ressourceUri,
                nom, description, quantite, titre,format,niveauCompetence, formattedDateAjout,
                ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri,ressourceUri,ressourceUri
        );

        // Log the SPARQL update query for debugging
        System.out.println("Executing SPARQL Update: " + query);

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    // Delete resource method

    public boolean deleteRessource(String id) {
        String ressourceUri = AGRICULTURE_NAMESPACE + id;
        String query = String.format("DELETE WHERE { <%s> ?property ?value . }", ressourceUri);

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
        return true;
    }



}
