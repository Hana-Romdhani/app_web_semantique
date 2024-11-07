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
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
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

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    // Method to get all Dechets
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

}
