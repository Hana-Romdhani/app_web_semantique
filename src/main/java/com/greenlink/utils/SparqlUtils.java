package com.greenlink.utils;


import com.greenlink.config.JenaEngine;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component

public class SparqlUtils {

    private final JenaEngine jenaEngine;
    private static final String AGRICULTURE_NAMESPACE = "http://www.semanticweb.org/agriculture/ontologies/2024/0/urbanAgriculture#";

    public SparqlUtils(JenaEngine jenaEngine) {
        this.jenaEngine = jenaEngine;
    }

    // Method to add a ConseilEnAttente instance
    public void addConseilEnAttente(String titre, String contenu, String dateSoumission) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateSoumission); // Use full IRI for xsd:date
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " + // Add this line to declare xsd prefix
                        "INSERT DATA { " +
                        "  _:conseil a <%sConseilEnAttente> ; " +
                        "              <%stitreConseil> \"%s\" ; " +
                        "              <%scontenuConseil> \"%s\" ; " +
                        "              <%sdateSoumission> %s . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE,
                titre,
                AGRICULTURE_NAMESPACE,
                contenu,
                AGRICULTURE_NAMESPACE,
                formattedDate
        );

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public void addEvenement(String titre, String lieu, String description, String date) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", date); // Use full IRI for xsd:date
        String eventUri = AGRICULTURE_NAMESPACE + "Evenement_" + java.util.UUID.randomUUID();
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  <%s> a <%sEvenement> ; " +
                        "              <%stitreEvenement> \"%s\" ; " +
                        "              <%slieuEvenement> \"%s\" ; " +
                        "              <%sdescriptionEvenement> \"%s\" ; " +
                        "              <%sdateEvenement> %s . " +
                        "}",
                eventUri,
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE,
                titre,
                AGRICULTURE_NAMESPACE,
                lieu,
                AGRICULTURE_NAMESPACE,
                description,
                AGRICULTURE_NAMESPACE,
                formattedDate
        );

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public List<Map<String, String>> getAllEvenements() {
        List<Map<String, String>> evenements = new ArrayList<>();
        Logger logger = LoggerFactory.getLogger(SparqlUtils.class);

        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?evenement ?titre ?lieu ?description ?date WHERE { " +
                        "  ?evenement a agr:Evenement ; " +
                        "             agr:titreEvenement ?titre ; " +
                        "             agr:lieuEvenement ?lieu ; " +
                        "             agr:descriptionEvenement ?description ; " +
                        "             agr:dateEvenement ?date . " +
                        "}",
                AGRICULTURE_NAMESPACE
        );

        try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> evenement = new HashMap<>();

                try {
                    // Extraire l'URI de l'événement
                    if (soln.contains("evenement") && soln.getResource("evenement") != null) {
                        String fullUri = soln.getResource("evenement").getURI();
                        String shortId = fullUri.substring(fullUri.lastIndexOf("#") + 1);
                        evenement.put("id", shortId);
                    } else {
                        evenement.put("id", "ID non disponible");
                    }

                    // Extraire les autres attributs en vérifiant leur existence
                    evenement.put("titre", soln.contains("titre") ? soln.getLiteral("titre").getString() : "Titre non disponible");
                    evenement.put("lieu", soln.contains("lieu") ? soln.getLiteral("lieu").getString() : "Lieu non disponible");
                    evenement.put("description", soln.contains("description") ? soln.getLiteral("description").getString() : "Description non disponible");
                    evenement.put("date", soln.contains("date") ? soln.getLiteral("date").getString() : "Date non disponible");

                    evenements.add(evenement);
                } catch (Exception e) {
                    logger.error("Erreur lors de la lecture de l'événement : ", e);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'exécution de la requête SPARQL : ", e);
        }

        return evenements;
    }

    public void updateEvenement(String id, String titre, String lieu, String description, String date) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", date);
        String eventUri = AGRICULTURE_NAMESPACE + id; // Construct the full URI for the event based on the provided id

        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:titreEvenement ?titre ; " +
                        "        agr:lieuEvenement ?lieu ; " +
                        "        agr:descriptionEvenement ?description ; " +
                        "        agr:dateEvenement ?date . " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:titreEvenement \"%s\" ; " +
                        "        agr:lieuEvenement \"%s\" ; " +
                        "        agr:descriptionEvenement \"%s\" ; " +
                        "        agr:dateEvenement %s . " +
                        "} " +
                        "WHERE { " +
                        "  <%s> a agr:Evenement ; " +
                        "        agr:titreEvenement ?titre ; " +
                        "        agr:lieuEvenement ?lieu ; " +
                        "        agr:descriptionEvenement ?description ; " +
                        "        agr:dateEvenement ?date . " +
                        "}",
                AGRICULTURE_NAMESPACE, eventUri, eventUri,
                titre, lieu, description, formattedDate,
                eventUri
        );

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public boolean deleteEvenement(String id) {
        String eventUri = AGRICULTURE_NAMESPACE + id; // Construct the full URI for the event based on the provided id

        String query = String.format(
                "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> ?property ?value . " +
                        "} " +
                        "WHERE { " +
                        "  <%s> ?property ?value . " +
                        "}",
                AGRICULTURE_NAMESPACE, eventUri, eventUri
        );

        try {
            // Execute the SPARQL update using JenaEngine and save the model to file
            jenaEngine.executeUpdate(jenaEngine.getModel(), query);
            jenaEngine.saveModelToFile();
            return true; // Return true to indicate success
        } catch (Exception e) {
            // Log the error if needed
            System.err.println("Error deleting event: " + e.getMessage());
            return false; // Return false to indicate failure
        }
    }



}
