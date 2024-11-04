package com.greenlink.utils;

import com.greenlink.config.JenaEngine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SparqlUtils {

    private final JenaEngine jenaEngine;
    private static final String AGRICULTURE_NAMESPACE = "http://www.semanticweb.org/agriculture/ontologies/2024/0/urbanAgriculture#";

    public SparqlUtils(JenaEngine jenaEngine) {
        this.jenaEngine = jenaEngine;
    }

    // Add a generic JardinUrbain instance
    public void addJardinUrbain(String nom, String localisation, double superficie, String plantes, String responsable) {
        String query = String.format(
                "INSERT DATA { " +
                        "  _:jardin a <%sJardinUrbain> ; " +
                        "            <%snom> \"%s\" ; " +
                        "            <%slocalisation> \"%s\" ; " +
                        "            <%ssuperficie> \"%f\"^^<http://www.w3.org/2001/XMLSchema#double> ; " +
                        "            <%splantes> \"%s\" ; " +
                        "            <%sresponsable> \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, localisation,
                AGRICULTURE_NAMESPACE, superficie,
                AGRICULTURE_NAMESPACE, plantes,
                AGRICULTURE_NAMESPACE, responsable
        );
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    // Add JardinPartage instance with specific attributes
    public void addJardinPartage(String nom, String localisation, double superficie, String plantes, String responsable, int nombreParticipants, String typeParticipation) {
        String query = String.format(
                "INSERT DATA { " +
                        "  _:jardinPartage a <%sJardinPartage> ; " +
                        "            <%snom> \"%s\" ; " +
                        "            <%slocalisation> \"%s\" ; " +
                        "            <%ssuperficie> \"%f\"^^<http://www.w3.org/2001/XMLSchema#double> ; " +
                        "            <%splantes> \"%s\" ; " +
                        "            <%sresponsable> \"%s\" ; " +
                        "            <%snombreParticipants> \"%d\"^^<http://www.w3.org/2001/XMLSchema#integer> ; " +
                        "            <%stypeParticipation> \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, localisation,
                AGRICULTURE_NAMESPACE, superficie,
                AGRICULTURE_NAMESPACE, plantes,
                AGRICULTURE_NAMESPACE, responsable,
                AGRICULTURE_NAMESPACE, nombreParticipants,
                AGRICULTURE_NAMESPACE, typeParticipation
        );
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    // Add JardinPrive instance with specific attributes
    public void addJardinPrive(String nom, String localisation, double superficie, String plantes, String responsable, String proprietaire, String dateCreation) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateCreation);
        String query = String.format(
                "INSERT DATA { " +
                        "  _:jardinPrive a <%sJardinPrive> ; " +
                        "            <%snom> \"%s\" ; " +
                        "            <%slocalisation> \"%s\" ; " +
                        "            <%ssuperficie> \"%f\"^^<http://www.w3.org/2001/XMLSchema#double> ; " +
                        "            <%splantes> \"%s\" ; " +
                        "            <%sresponsable> \"%s\" ; " +
                        "            <%sproprietaire> \"%s\" ; " +
                        "            <%sdateCreation> %s . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, localisation,
                AGRICULTURE_NAMESPACE, superficie,
                AGRICULTURE_NAMESPACE, plantes,
                AGRICULTURE_NAMESPACE, responsable,
                AGRICULTURE_NAMESPACE, proprietaire,
                AGRICULTURE_NAMESPACE, formattedDate
        );
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    // List all Jardins with all their properties
    public List<String> listAllJardins() {
        String query = String.format(
                "SELECT ?jardin ?nom ?localisation ?superficie ?plantes ?responsable WHERE { " +
                        "  ?jardin a <%sJardinUrbain> ; " +
                        "           <%snom> ?nom ; " +
                        "           <%slocalisation> ?localisation ; " +
                        "           <%ssuperficie> ?superficie ; " +
                        "           <%splantes> ?plantes ; " +
                        "           <%sresponsable> ?responsable . " +
                        "}",
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE
        );

        // Run the query and get the results
        List<Map<String, String>> results = jenaEngine.executeSelectMultiple(jenaEngine.getModel(), query,
                new String[]{"jardin", "nom", "localisation", "superficie", "plantes", "responsable"});

        // Format each result as a string
        List<String> formattedResults = new ArrayList<>();
        for (Map<String, String> result : results) {
            formattedResults.add(String.format("Jardin: %s, Nom: %s, Localisation: %s, Superficie: %s, Plantes: %s, Responsable: %s",
                    result.get("jardin"), result.get("nom"), result.get("localisation"),
                    result.get("superficie"), result.get("plantes"), result.get("responsable")));
        }

        return formattedResults;
    }


    // List Jardins by type (JardinPartage, JardinPrive) with all their properties
    public List<String> listGardensByType(String type) {
        String typeURI = AGRICULTURE_NAMESPACE + type;
        String query = String.format(
                "SELECT ?jardin ?nom ?localisation ?superficie ?plantes ?responsable " +
                        "WHERE { " +
                        "  ?jardin a <%s> ; " +
                        "           <%snom> ?nom ; " +
                        "           <%slocalisation> ?localisation ; " +
                        "           <%ssuperficie> ?superficie ; " +
                        "           <%splantes> ?plantes ; " +
                        "           <%sresponsable> ?responsable . " +
                        "} ",
                typeURI,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE
        );

        System.out.println("Executing SPARQL query: " + query);

        // Run the query and get the results
        List<Map<String, String>> results = jenaEngine.executeSelectMultiple(jenaEngine.getModel(), query,
                new String[]{"jardin", "nom", "localisation", "superficie", "plantes", "responsable"});

        // Format each result as a string
        List<String> formattedResults = new ArrayList<>();
        for (Map<String, String> result : results) {
            formattedResults.add(String.format("Jardin: %s, Nom: %s, Localisation: %s, Superficie: %s, Plantes: %s, Responsable: %s",
                    result.get("jardin"), result.get("nom"), result.get("localisation"),
                    result.get("superficie"), result.get("plantes"), result.get("responsable")));
        }

        return formattedResults;
    }



    // Delete a Jardin instance by name
    public void deleteJardinByName(String nom) {
        String query = String.format("DELETE WHERE { ?jardin <%snom> \"%s\" . ?jardin ?p ?o }", AGRICULTURE_NAMESPACE, nom);
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public void editJardinPartage(String nom, String description, Double superficie, String plantes, String responsable, Integer nombreParticipants, String typeParticipation) {
        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("PREFIX agri: <").append(AGRICULTURE_NAMESPACE).append("> ")
                .append("DELETE { ")
                .append("  ?ressource agri:description ?oldDescription . ")
                .append("  ?ressource agri:superficie ?oldSuperficie . ")
                .append("  ?ressource agri:plantes ?oldPlantes . ")
                .append("  ?ressource agri:responsable ?oldResponsable . ")
                .append("  ?ressource agri:nombreParticipants ?oldNombreParticipants . ")
                .append("  ?ressource agri:typeParticipation ?oldTypeParticipation . ")
                .append("} ")
                .append("INSERT { ");

        if (description != null) {
            updateQuery.append("  ?ressource agri:description \"").append(description).append("\" . ");
        }
        if (superficie != null) {
            updateQuery.append("  ?ressource agri:superficie ").append(superficie).append(" . ");
        }
        if (plantes != null) {
            updateQuery.append("  ?ressource agri:plantes \"").append(plantes).append("\" . ");
        }
        if (responsable != null) {
            updateQuery.append("  ?ressource agri:responsable \"").append(responsable).append("\" . ");
        }
        if (nombreParticipants != null) {
            updateQuery.append("  ?ressource agri:nombreParticipants ").append(nombreParticipants).append(" . ");
        }
        if (typeParticipation != null) {
            updateQuery.append("  ?ressource agri:typeParticipation \"").append(typeParticipation).append("\" . ");
        }

        updateQuery.append("} ")
                .append("WHERE { ")
                .append("  ?ressource agri:nom \"").append(nom).append("\" . ")
                .append("} ");

        // Execute the SPARQL update query
        jenaEngine.executeUpdate(jenaEngine.getModel(), updateQuery.toString());
        jenaEngine.saveModelToFile();
    }

    public void editJardinPrive(String nom, String localisation, double superficie, String plantes, String responsable, String proprietaire, String dateCreation) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateCreation);
        String updateQuery = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE { " +
                        "  ?jardin agri:localisation ?oldLocalisation . " +
                        "  ?jardin agri:superficie ?oldSuperficie . " +
                        "  ?jardin agri:plantes ?oldPlantes . " +
                        "  ?jardin agri:responsable ?oldResponsable . " +
                        "  ?jardin agri:proprietaire ?oldProprietaire . " +
                        "  ?jardin agri:dateCreation ?oldDateCreation . " +
                        "} " +
                        "INSERT { " +
                        "  ?jardin agri:localisation \"%s\" ; " +
                        "          agri:superficie %f ; " +
                        "          agri:plantes \"%s\" ; " +
                        "          agri:responsable \"%s\" ; " +
                        "          agri:proprietaire \"%s\" ; " +
                        "          agri:dateCreation %s . " +
                        "} " +
                        "WHERE { " +
                        "  ?jardin agri:nom \"%s\" . " +
                        "  OPTIONAL { ?jardin agri:localisation ?oldLocalisation . } " +
                        "  OPTIONAL { ?jardin agri:superficie ?oldSuperficie . } " +
                        "  OPTIONAL { ?jardin agri:plantes ?oldPlantes . } " +
                        "  OPTIONAL { ?jardin agri:responsable ?oldResponsable . } " +
                        "  OPTIONAL { ?jardin agri:proprietaire ?oldProprietaire . } " +
                        "  OPTIONAL { ?jardin agri:dateCreation ?oldDateCreation . } " +
                        "}",
                AGRICULTURE_NAMESPACE, localisation, superficie, plantes, responsable, proprietaire, formattedDate, nom
        );

        // Execute the update query
        jenaEngine.executeUpdate(jenaEngine.getModel(), updateQuery);
        jenaEngine.saveModelToFile();
    }






}
