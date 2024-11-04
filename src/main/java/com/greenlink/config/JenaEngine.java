package com.greenlink.config;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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

    // Execute a SPARQL SELECT query and return results as a String
    public String executeSelectQuery(Model model, String query) {
        QueryExecution queryExecution = QueryExecutionFactory.create(QueryFactory.create(query), model);
        ResultSet resultSet = queryExecution.execSelect();
        StringBuilder resultString = new StringBuilder();

        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            solution.varNames().forEachRemaining(var -> {
                resultString.append(var).append(": ").append(solution.get(var).toString()).append("\n");
            });
            resultString.append("\n");
        }

        queryExecution.close();
        return resultString.toString();
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

    // Execute a SPARQL SELECT query and return a list of results for a specific variable
    public List<String> executeSelectList(Model model, String query, String variable) {
        List<String> results = new ArrayList<>();
        QueryExecution queryExecution = QueryExecutionFactory.create(QueryFactory.create(query), model);
        ResultSet resultSet = queryExecution.execSelect();

        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            if (solution.contains(variable)) {
                results.add(solution.get(variable).toString());
            }
        }

        queryExecution.close();
        return results;
    }
}
