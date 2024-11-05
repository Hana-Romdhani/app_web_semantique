package com.greenlink.utils;


import com.greenlink.config.JenaEngine;
import org.apache.jena.query.*;
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

    public List<Map<String, String>> getAllCategoriesPlante() {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?categorie ?nomCategorie " +
                        "WHERE { " +
                        "    ?categorie a agri:CategoriePlante ; " +
                        "               agri:nomCategoriePlante ?nomCategorie . " +
                        "}", AGRICULTURE_NAMESPACE);

        List<Map<String, String>> categories = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> categoryData = new HashMap<>();
                categoryData.put("categorie", soln.getResource("categorie").getURI());
                categoryData.put("nomCategorie", soln.getLiteral("nomCategorie").getString());
                categories.add(categoryData);
            }
        }
        return categories;
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


}
