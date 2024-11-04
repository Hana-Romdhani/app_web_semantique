package com.greenlink.config;

import org.apache.jena.query.DatasetFactory;
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
}
