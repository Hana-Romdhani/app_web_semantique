package com.greenlink.utils;


import com.greenlink.config.JenaEngine;
import org.apache.jena.assembler.JA;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component

public class SparqlUtils {
    private Model model;
    private final JenaEngine jenaEngine;
    private static final String AGRICULTURE_NAMESPACE = "http://www.semanticweb.org/agriculture/ontologies/2024/0/urbanAgriculture#";

    public SparqlUtils(JenaEngine jenaEngine) {
        this.jenaEngine = jenaEngine;
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






}
