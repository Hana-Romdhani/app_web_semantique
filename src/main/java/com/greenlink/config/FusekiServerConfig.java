package com.greenlink.config;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.fuseki.main.FusekiServer;

public class FusekiServerConfig {
    private static final String DATASET_PATH = "src/main/resources/data/tdb1";
    private static final String OWL_FILE_PATH = "src/main/resources/owl/greenlinkversion2.owl";
    private static final String DATASET_NAME = "/greenlink";
    private static FusekiServer server;

    public static void startFusekiServer() {
        try {
            // Load the ontology model using JenaEngine
            Model model = JenaEngine.readModel(OWL_FILE_PATH);
            if (model == null) {
                System.out.println("Failed to load the ontology model.");
                return;
            }

            // Create or open the TDB dataset
            Dataset dataset = TDBFactory.createDataset(DATASET_PATH);

            // Start a transaction to set the default model
            dataset.begin(ReadWrite.WRITE);
            try {
                dataset.setDefaultModel(model);
                dataset.commit(); // Commit the transaction
            } finally {
                dataset.end(); // Ensure the transaction ends
            }

            // Create and start the Fuseki server
            server = FusekiServer.create()
                    .port(3030)
                    .add(DATASET_NAME, dataset, true) // Allow read/write access
                    .build();

            server.start();
            System.out.println("Fuseki server started at http://localhost:3030" + DATASET_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error starting Fuseki server: " + e.getMessage());
        }
    }

    public static void stopFusekiServer() {
        if (server != null) {
            server.stop();
            System.out.println("Fuseki server stopped.");
        }
    }
}