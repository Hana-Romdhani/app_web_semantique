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

    public JenaEngine(@Value("${owl.file.path:src/main/resources/owl/greenlinkversion2.owl}") String owlFilePath) {
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


    public List<Map<String, String>> executeSelectQuery(String query) {
        List<Map<String, String>> resultsList = new ArrayList<>();

        // Create a query using the provided SPARQL query string
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);

        try {
            // Execute the query and get the results
            ResultSet results = queryExecution.execSelect();

            // Iterate over the results
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                Map<String, String> row = new HashMap<>();

                // Iterate over the variables in each solution
                results.getResultVars().forEach(var -> {
                    if (solution.contains(var)) {
                        row.put(var, solution.get(var).toString());
                    }
                });

                // Add the row map to the results list
                resultsList.add(row);
            }
        } finally {
            queryExecution.close();
        }

        return resultsList;
    }

    //******ressource********
    // Méthode pour exécuter une requête SELECT SPARQL et retourner les résultats sous forme de chaîne
    public String executeSelectQueryRs(String sparqlQuery) {
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



    public List<Map<String, String>> executeSelectQueryRs(Model model, String query, String... variables) {
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



//****end ress *****

    // Method to execute an ASK query
    public boolean executeAskQuery(String query) {
        // Create a QueryExecution instance using the provided SPARQL query string
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);

        try {
            // Execute the ASK query and return the result (true or false)
            return queryExecution.execAsk();
        } finally {
            queryExecution.close(); // Always close the query execution
        }
    }
    public ResultSet executeQuery(String queryString) {
        // Create a query execution object
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            // Execute the query and return the results
            return qexec.execSelect();
        } catch (Exception e) {
            System.out.println("Error executing query: " + e.getMessage());
            return null; // Return null or handle error appropriately
        }
    }
    public List<Map<String, String>> executeSelectMultiple(Model model, String query, String[] variableNames) {
        List<Map<String, String>> results = new ArrayList<>();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution soln = resultSet.nextSolution();
                Map<String, String> resultMap = new HashMap<>();

                for (String varName : variableNames) {
                    if (soln.get(varName) != null) {
                        resultMap.put(varName, soln.get(varName).toString());
                    } else {
                        resultMap.put(varName, "N/A"); // Or handle null appropriately
                    }
                }
                results.add(resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
        }

        return results;
    }



}
