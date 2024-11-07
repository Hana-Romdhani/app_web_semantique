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

}
