package com.greenlink.config;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.FileManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JenaEngine {

    private Model model;

    private final String owlFilePath;

    public JenaEngine(@Value("${owl.file.path:src/main/resources/owl/greenlink.owl}") String owlFilePath) {
        this.owlFilePath = owlFilePath;
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    // Load the model from OWL file
    private void loadModel() {
        InputStream in = FileManager.get().open(owlFilePath);
        if (in == null) {
            System.out.println("Ontology file: " + owlFilePath + " not found");
            return;
        }
        model.read(in, "");
        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Error closing input stream: " + e.getMessage());
        }
    }

    // Method to read the model from OWL file
    public static Model readModel(String owlFilePath) {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream in = FileManager.get().open(owlFilePath)) {
            if (in == null) {
                System.out.println("Ontology file: " + owlFilePath + " not found");
                return null;
            }
            model.read(in, "");
        } catch (IOException e) {
            System.err.println("Error reading ontology file: " + e.getMessage());
            return null;
        }
        return model;
    }

    // Execute a SPARQL update
    public void executeUpdate(Model model, String query) {
        UpdateRequest updateRequest = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.create(updateRequest, DatasetFactory.create(this.model));
        processor.execute();
    }


    // Save the updated model to the OWL file
    public void saveModelToFile() {
        try (FileOutputStream out = new FileOutputStream(owlFilePath)) {
            model.write(out, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Accessor for the model
    public Model getModel() {
        return model;
    }

    // Méthode pour exécuter une requête SELECT SPARQL et retourner les résultats sous forme de chaîne
    public String executeSelectQuery(String sparqlQuery) {
        StringBuilder resultsBuilder = new StringBuilder();

        // Création de la requête SPARQL
        Query query = QueryFactory.create(sparqlQuery);

        // Exécution de la requête avec le modèle
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            // Formate les résultats en chaîne de caractères pour les afficher
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                for (String var : results.getResultVars()) {
                    resultsBuilder.append(var).append(": ").append(soln.get(var)).append("\n");
                }
                resultsBuilder.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing SPARQL SELECT query: " + e.getMessage();
        }

        return resultsBuilder.toString();
    }



    public List<String> executeSelectList(Model model, String query, String ressource, String nom, String propriete, String valeur) {
        List<String> results = new ArrayList<>();
        QueryExecution queryExecution = QueryExecutionFactory.create(QueryFactory.create(query), model);
        ResultSet resultSet = queryExecution.execSelect();

        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            String ressourceValue = solution.contains(ressource) ? solution.get(ressource).toString() : "null";
            String nomValue = solution.contains(nom) ? solution.get(nom).toString() : "null";
            String proprieteValue = solution.contains(propriete) ? solution.get(propriete).toString() : "null";
            String valeurValue = solution.contains(valeur) ? solution.get(valeur).toString() : "null";

            results.add(String.format("Ressource: %s, Nom: %s, Propriété: %s, Valeur: %s", ressourceValue, nomValue, proprieteValue, valeurValue));
        }

        queryExecution.close();
        return results;
    }
    public List<Map<String, String>> executeSelectQuery(Model model, String query, String... variables) {
        List<Map<String, String>> results = new ArrayList<>();
        QueryExecution queryExecution = QueryExecutionFactory.create(QueryFactory.create(query), model);
        ResultSet resultSet = queryExecution.execSelect();

        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            Map<String, String> resultMap = new HashMap<>();

            for (String variable : variables) {
                if (solution.contains(variable)) {
                    resultMap.put(variable, solution.get(variable).toString());
                }
            }

            results.add(resultMap);
        }

        queryExecution.close();
        return results;
    }

}
