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
import com.greenlink.config.JenaEngine;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import org.springframework.stereotype.Component;


@Component
public class SparqlUtils {

    private final JenaEngine jenaEngine;
    private static final String AGRICULTURE_NAMESPACE = "http://www.semanticweb.org/agriculture/ontologies/2024/0/urbanAgriculture#";

    public SparqlUtils(JenaEngine jenaEngine) {
        this.jenaEngine = jenaEngine;
    }

    // Method to add a ConseilEnAttente instance
    public void addConseilEnAttente(String titre, String contenu, String dateSoumission) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateSoumission);
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

    // Get a resource by ID
    public String getRessourceById(String id) {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "SELECT ?nom ?description ?quantite ?type ?dateAjout " +
                        "WHERE { " +
                        "  ?ressource a agri:Ressource ; " +
                        "             agri:id \"%s\" ; " +
                        "             agri:nom ?nom ; " +
                        "             agri:description ?description ; " +
                        "             agri:quantite ?quantite ; " +
                        "             agri:type ?type ; " +
                        "             agri:dateAjout ?dateAjout . " +
                        "}",
                AGRICULTURE_NAMESPACE, id
        );

        return jenaEngine.executeSelectQuery(query);
    }


    public Map<String, String> getRessourceByIdAndType(String id, String type) {
        Map<String, String> ressource = new HashMap<>();
        String ressourceUri = AGRICULTURE_NAMESPACE + id; // URI de la ressource en fonction de l'id

        String queryStr = String.format(
                "PREFIX agr: <%s> " +
                        "SELECT ?ressource ?nom ?description ?quantite ?dateAjout ?typeMateriel ?etat ?disponibilite ?source ?titre ?format ?niveauCompetence WHERE { " +
                        "  BIND(<%s> AS ?ressource) ." +
                        "  ?ressource agr:nom ?nom ; " +
                        "            agr:description ?description ; " +
                        "            agr:quantite ?quantite ; " +
                        "            agr:dateAjout ?dateAjout . " +
                        "  OPTIONAL { ?ressource a <%s> . } " +  // Ajoutez une condition pour le type
                        "  OPTIONAL { ?ressource agr:typeMateriel ?typeMateriel . } " +
                        "  OPTIONAL { ?ressource agr:disponibilite ?disponibilite . } " +
                        "  OPTIONAL { ?ressource agr:etat ?etat . } " +
                        "  OPTIONAL { ?ressource agr:source ?source . } " +
                        "  OPTIONAL { ?ressource agr:titre ?titre . } " +
                        "  OPTIONAL { ?ressource agr:format ?format . } " +
                        "  OPTIONAL { ?ressource agr:niveauCompetence ?niveauCompetence . } " +
                        "}",
                AGRICULTURE_NAMESPACE, ressourceUri, AGRICULTURE_NAMESPACE + type  // Inclusion de type dans le filtre
        );

        try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();

                ressource.put("id", ressourceUri);
                ressource.put("nom", soln.contains("nom") ? soln.getLiteral("nom").getString() : "Nom non disponible");
                ressource.put("description", soln.contains("description") ? soln.getLiteral("description").getString() : "Description non disponible");
                ressource.put("quantite", soln.contains("quantite") ? soln.getLiteral("quantite").getString() : "Quantité non disponible");
                ressource.put("dateAjout", soln.contains("dateAjout") ? soln.getLiteral("dateAjout").getString() : "Date non disponible");

                // Type matériel, état, disponibilité, etc.
                ressource.put("typeMateriel", soln.contains("typeMateriel") ? soln.getLiteral("typeMateriel").getString() : "TypeMatériel non disponible");
                ressource.put("etat", soln.contains("etat") ? soln.getLiteral("etat").getString() : "État non disponible");
                ressource.put("disponibilite", soln.contains("disponibilite") ? soln.getLiteral("disponibilite").getString() : "Disponibilité non disponible");
                ressource.put("source", soln.contains("source") ? soln.getLiteral("source").getString() : "Source non disponible");
                ressource.put("titre", soln.contains("titre") ? soln.getLiteral("titre").getString() : "Titre non disponible");
                ressource.put("format", soln.contains("format") ? soln.getLiteral("format").getString() : "Format non disponible");
                ressource.put("niveauCompetence", soln.contains("niveauCompetence") ? soln.getLiteral("niveauCompetence").getString() : "NiveauCompétence non disponible");
            } else {
                ressource.put("error", "Ressource non trouvée ou type incompatible.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution de la requête SPARQL : " + e.getMessage());
        }

        return ressource;
    }

    public List<Map<String, String>> getAllRess() {
        List<Map<String, String>> ressources = new ArrayList<>();
        String queryStr = String.format(
                "PREFIX agri: <http://www.semanticweb.org/agriculture/ontologies/2024/0/urbanAgriculture#> " +
                        "SELECT ?ressource ?nom ?description ?quantite ?dateAjout ?type ?typeMateriel ?etat ?disponibilite ?source ?titre ?format ?niveauCompetence WHERE {\n" +
                        "  ?ressource agri:nom ?nom ;" +
                        "            agri:description ?description ; " +
                        "             agri:quantite ?quantite ;" +
                        "             agri:dateAjout ?dateAjout ; " +
                        "  OPTIONAL { ?ressource a ?type . }" +  // Changement ici pour récupérer le type via 'a' (type RDF)
                        "  OPTIONAL { ?ressource agri:typeMateriel ?typeMateriel . } " +
                        "  OPTIONAL { ?ressource agri:disponibilite ?disponibilite . } " +
                        "  OPTIONAL { ?ressource agri:etat ?etat . } " +
                        "  OPTIONAL { ?ressource agri:source ?source . } " +
                        "  OPTIONAL { ?ressource agri:titre ?titre . } " +
                        "  OPTIONAL { ?ressource agri:format ?format . } " +
                        "  OPTIONAL { ?ressource agri:niveauCompetence ?niveauCompetence . } " +
                        "}",
                AGRICULTURE_NAMESPACE
        );

        System.out.println("Executing SPARQL Query: " + queryStr);

        try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();
            if (!results.hasNext()) {
                System.out.println("No results found.");
            }

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                System.out.println("Processing result: " + soln);

                Map<String, String> ressource = new HashMap<>();
                try {
                    // Extraction des informations et ajout dans la map
                    if (soln.contains("ressource") && soln.getResource("ressource") != null) {
                        String fullUri = soln.getResource("ressource").getURI();
                        String shortId = fullUri.substring(fullUri.lastIndexOf("#") + 1);
                        ressource.put("id", shortId);
                    } else {
                        ressource.put("id", "ID non disponible");
                    }

                    // Autres informations de la ressource
                    ressource.put("nom", soln.contains("nom") ? soln.getLiteral("nom").getString() : "Nom non disponible");
                    ressource.put("description", soln.contains("description") ? soln.getLiteral("description").getString() : "Description non disponible");
                    ressource.put("quantite", soln.contains("quantite") ? soln.getLiteral("quantite").getString() : "Quantité non disponible");
                    ressource.put("dateAjout", soln.contains("dateAjout") ? soln.getLiteral("dateAjout").getString() : "Date non disponible");

                    // Type de la ressource - Vérifier et formater correctement
                    String type = "Type non disponible";  // Valeur par défaut
                    if (soln.contains("type") && soln.getResource("type") != null) {
                        String typeUri = soln.getResource("type").getURI();
                        // Vérifier si l'URI contient un '#'
                        if (typeUri.contains("#")) {
                            // Extraire le nom après le '#'
                            String extractedType = typeUri.substring(typeUri.lastIndexOf("#") + 1);
                            type = extractedType;  // Utiliser le nom extrait comme type
                        }
                    }
                    ressource.put("type", type);

                    // Type matériel, état, disponibilité, etc.
                    ressource.put("typeMateriel", soln.contains("typeMateriel") ? soln.getLiteral("typeMateriel").getString() : "TypeMatériel non disponible");
                    ressource.put("etat", soln.contains("etat") ? soln.getLiteral("etat").getString() : "État non disponible");
                    ressource.put("disponibilite", soln.contains("disponibilite") ? soln.getLiteral("disponibilite").getString() : "Disponibilité non disponible");
                    ressource.put("source", soln.contains("source") ? soln.getLiteral("source").getString() : "Source non disponible");
                    ressource.put("titre", soln.contains("titre") ? soln.getLiteral("titre").getString() : "Titre non disponible");
                    ressource.put("format", soln.contains("format") ? soln.getLiteral("format").getString() : "Format non disponible");
                    ressource.put("niveauCompetence", soln.contains("niveauCompetence") ? soln.getLiteral("niveauCompetence").getString() : "NiveauCompétence non disponible");

                    ressources.add(ressource);
                } catch (Exception e) {
                    System.err.println("Error processing resource: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing SPARQL query: " + e.getMessage());
            e.printStackTrace();
        }

        return ressources;
    }




    private String extractNameFromUri(String uri) {
        // Si l'URI est valide et contient un '#', extraire la dernière partie après le '#'
        if (uri != null && uri.contains("#")) {
            return uri.substring(uri.lastIndexOf("#") + 1);
        }
        // Sinon, retourner l'URI complet
        return uri;
    }




    public void addRessourceMaterielle(String nom, String description, int quantite, String dateAjout, String typeMateriel, String etat, Boolean disponibilite) {
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String disponibiliteValue = disponibilite ? "true" : "false";
        String ressourceUri = AGRICULTURE_NAMESPACE + "ressource" + java.util.UUID.randomUUID();

        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  <%s> a <%sRessourceMaterielle> ; " + // Directly using ressourceUri here as a URI
                        "      <%snom> \"%s\" ; " +
                        "      <%sdescription> \"%s\" ; " +
                        "      <%squantite> \"%d\"^^xsd:int ; " +
                        "      <%sdateAjout> %s ; " + // formattedDate already includes xsd:date
                        "      <%stypeMateriel> \"%s\" ; " +
                        "      <%setat> \"%s\" ; " +
                        "      <%sdisponibilite> \"%s\"^^xsd:boolean . " +
                        "}",
                ressourceUri, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, nom, AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, quantite,
                AGRICULTURE_NAMESPACE, formattedDate,
                AGRICULTURE_NAMESPACE, typeMateriel, AGRICULTURE_NAMESPACE, etat,
                AGRICULTURE_NAMESPACE, disponibiliteValue
        );

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public void addRessourceNaturelle(String nom, String description, int quantite, String dateAjout, String source) {
        // Format the date using the full IRI for xsd:date
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String ressourceUri = AGRICULTURE_NAMESPACE + "ressource" + java.util.UUID.randomUUID();

        // Correcting placeholders and adding ressourceUri in the query as a full URI
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  <%s> a <%sRessourceNaturelle> ; " +  // Using the full URI for the resource
                        "      <%snom> \"%s\" ; " +
                        "      <%sdescription> \"%s\" ; " +
                        "      <%squantite> \"%d\"^^xsd:int ; " +
                        "      <%sdateAjout> %s ; " +  // Using formattedDate directly
                        "      <%ssource> \"%s\" . " +
                        "}",
                ressourceUri,  // Insert the generated resource URI
                AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, quantite,
                AGRICULTURE_NAMESPACE, formattedDate,  // Use the pre-formatted date
                AGRICULTURE_NAMESPACE, source
        );

        // Execute the SPARQL update using JenaEngine and save the model to file
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public void addRessourceEducative(String nom, String description, String titre, String format, String niveauCompetence, int quantite, String dateAjout) {
        // Formatage de la date pour le langage RDF
        String formattedDate = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);

        // Génération d'un URI unique pour la ressource éducative
        String ressourceUri = AGRICULTURE_NAMESPACE + "ressource" + java.util.UUID.randomUUID();

        // Construction de la requête SPARQL
        String query = String.format(
                "INSERT DATA { " +
                        "  <%s> a <%sRessourceEducative> ; " +
                        "              <%snom> \"%s\" ; " +
                        "              <%sdescription> \"%s\" ; " +
                        "              <%stitre> \"%s\" ; " +
                        "              <%sformat> \"%s\" ; " +
                        "              <%sniveauCompetence> \"%s\" ; " + // Ajout du point-virgule ici
                        "              <%squantite> %d ; " + // Utiliser %d pour un entier
                        "              <%sdateAjout> %s . " +  // Utilisation de formattedDate

                        "}",
                ressourceUri, // URI généré
                AGRICULTURE_NAMESPACE , // Corrigé le namespace
                AGRICULTURE_NAMESPACE, nom,
                AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, titre,
                AGRICULTURE_NAMESPACE, format,
                AGRICULTURE_NAMESPACE, niveauCompetence,
                AGRICULTURE_NAMESPACE, quantite, // Ajustement ici pour utiliser %d
                AGRICULTURE_NAMESPACE, formattedDate // Utilisation de la date pré-formatée
        );

        // Exécution de la mise à jour de la requête
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }



    // Get all resources
    // Get all resources regardless of type
    public String getAllRessources() {
        String query = String.format("SELECT ?ressource ?nom ?propriete ?valeur WHERE { " +
                        "?ressource <%snom> ?nom . " +
                        "?ressource ?propriete ?valeur . }",
                AGRICULTURE_NAMESPACE);

        // Exécutez la requête et récupérez les résultats sous forme de liste de cartes
        List<Map<String, String>> results = jenaEngine.executeSelectQuery(jenaEngine.getModel(), query, "ressource", "nom", "propriete", "valeur");

        // Utiliser un StringBuilder pour construire le résultat formaté
        StringBuilder resultString = new StringBuilder();
        Map<String, Boolean> currentRessource = new HashMap<>(); // Utilisation d'un Boolean pour vérifier si le nom a déjà été ajouté

        for (Map<String, String> result : results) {
            String ressource = result.get("ressource");
            String nom = result.get("nom");
            String propriete = result.get("propriete");
            String valeur = result.get("valeur");

            // Si nous passons à une nouvelle ressource, nous l'ajoutons à la chaîne
            if (!currentRessource.containsKey(ressource)) {
                // Si la ressource a déjà été enregistrée, on peut ajouter une nouvelle ligne pour la séparer
                if (currentRessource.size() > 0) {
                    resultString.append("\n");
                }

                // Afficher la ressource directement sans le mot "Nom"
                resultString.append(String.format("Ressource: %s\n", nom));

                // Marquer cette ressource comme traitée
                currentRessource.put(ressource, true); // L'ID de la ressource est marqué comme déjà traité
            }

            // Extraire le nom de la propriété sans URI
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1);

            // Retirer le type si présent dans la valeur
            String valeurFormattee = valeur.contains("^^") ? valeur.split("\\^\\^")[0] : valeur;

            // Si la propriété est "type", extraire uniquement le nom sans l'URI
            if ("type".equals(proprieteNom)) {
                // Extraire et afficher uniquement le nom de la classe sans l'URL
                valeurFormattee = extractNameFromUri(valeur);
            }

            // Afficher la propriété avec la valeur formatée
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeurFormattee));
        }

        return resultString.toString();
    }

    // Méthode pour extraire le nom après le dernier '#'

    public List<Map<String, String>> listRessourcesByType(String type) {
        String typeURI = AGRICULTURE_NAMESPACE + type;
        String query = String.format(
                "SELECT ?ressource ?nom ?description ?quantite ?type ?dateAjout" +
                        "(IF(BOUND(?etat), ?etat, 'N/A') AS ?etat_value) " +
                        "(IF(BOUND(?source), ?source, 'N/A') AS ?source_value) " +
                        "(IF(BOUND(?format), ?format, 'N/A') AS ?format_value) " +
                        "(IF(BOUND(?typeMateriel), ?type, 'N/A') AS ?typeMateriel_value) " +
                        "(IF(BOUND(?disponibilite), ?disponibilite, 'N/A') AS ?disponibilite_value) " +
                        "(IF(BOUND(?niveauCompetence), ?niveauCompetence, 'N/A') AS ?niveauCompetence_value) " +
                        "WHERE { " +
                        "  ?ressource a <%s> ; " +
                        "           <%snom> ?nom ; " +
                        "           <%description> ?description ; " +
                        "           <%squantite> ?quantite ; " +
                        "           <%sdateAjout> ?dateAjout ; " +
                        "  OPTIONAL { " +
                        "     ?ressource <%setat> ?etat . " +
                        "     ?ressource <%ssource> ?source . " +
                        "     ?ressource <%sformat> ?format . " +
                        "     ?ressource <%stype> ?type . " +
                        "     OPTIONAL { " +
                        "         ?ressource <%sdisponibilite> ?disponibilite . " +
                        "         ?ressource <%sniveauCompetence> ?niveauCompetence . " +
                        "     } " +
                        "  } " +
                        "} ",
                typeURI,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE,
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE
        );

        System.out.println("Executing SPARQL query: " + query);

        // Create a list to store the results
        List<Map<String, String>> ressources = new ArrayList<>();

        // Run the query and get the results
        try (QueryExecution qexec = QueryExecutionFactory.create(query, jenaEngine.getModel())) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> ressource = new HashMap<>();

                // Extract the attributes and add them to the map
                try {
                    ressource.put("id", typeURI);
                    ressource.put("ressource", soln.contains("ressource") ? soln.getResource("ressource").getURI() : "ressource non disponible");
                    ressource.put("nom", soln.contains("nom") && soln.get("nom").isLiteral() ? soln.getLiteral("nom").getString() : "Nom non disponible");
                    ressource.put("description", soln.contains("description") && soln.get("description").isLiteral() ? soln.getLiteral("description").getString() : "description non disponible");
                    ressource.put("quantite", soln.contains("quantite") && soln.get("quantite").isLiteral() ? soln.getLiteral("quantite").getString() : "quantite non disponible");
                    ressource.put("etat", soln.contains("etat_value") && soln.get("etat_value").isLiteral() ? soln.getLiteral("etat_value").getString() : "etat non disponible");
                    ressource.put("dateAjout", soln.contains("dateAjout") && soln.get("dateAjout").isLiteral() ? soln.getLiteral("dateAjout").getString() : "Date non disponible");
                    ressource.put("source", soln.contains("source_value") && soln.get("source_value").isLiteral() ? soln.getLiteral("source_value").getString() : "source non disponible");
                    ressource.put("format", soln.contains("format_value") && soln.get("format_value").isLiteral() ? soln.getLiteral("format_value").getString() : "format non disponible");
                    ressource.put("typeMateriel", soln.contains("typeMateriel_value") && soln.get("typeMateriel_value").isLiteral() ? soln.getLiteral("typeMateriel_value").getString() : "typeMateriel non disponible");
                    ressource.put("disponibilite", soln.contains("disponibilite_value") && soln.get("disponibilite_value").isLiteral() ? soln.getLiteral("disponibilite_value").getString() : "disponibilite non disponible");
                    ressource.put("niveauCompetence", soln.contains("niveauCompetence_value") && soln.get("niveauCompetence_value").isLiteral() ? soln.getLiteral("niveauCompetence_value").getString() : "niveauCompetence non disponible");

                    // Add the map to the list (use put method on list to add the map)
                    ressources.add(ressource);
                } catch (Exception e) {
                    System.err.println("Error processing ressource: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing SPARQL query: " + e.getMessage());
            e.printStackTrace();
        }

        return ressources;
    }


    public String getAllRessourcesMaterielles() {
        String query = String.format("SELECT ?ressource ?nom ?propriete ?valeur WHERE { " +
                        "?ressource a <%sRessourceMaterielle> . " +
                        "?ressource <%snom> ?nom . " +
                        "?ressource ?propriete ?valeur . }",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE);

        // Exécutez la requête et récupérez les résultats sous forme de liste de cartes
        List<Map<String, String>> results = jenaEngine.executeSelectQuery(jenaEngine.getModel(), query, "ressource", "nom", "propriete", "valeur");

        StringBuilder resultString = new StringBuilder();
        Map<String, String> resourceInfo = new LinkedHashMap<>(); // Pour stocker les informations de ressource

        for (Map<String, String> result : results) {
            String ressourceId = result.get("ressource");
            String nom = result.get("nom");
            String propriete = result.get("propriete");
            String valeur = result.get("valeur");

            // Vérifier si la ressource a déjà été ajoutée
            if (!resourceInfo.containsKey(ressourceId)) {
                // Ajouter une nouvelle entrée pour la ressource
                resultString.append(String.format("Ressource: %s\n", ressourceId));
                resultString.append(String.format("  Nom: %s\n", nom));
                resourceInfo.put(ressourceId, nom); // Stocker la ressource avec son nom
            }

            // Extraire et afficher la propriété et sa valeur
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1); // Nom de la propriété sans URI

            // Vérifier si la valeur a un type RDF, sinon l'afficher directement
            if (valeur.contains("^^")) {
                // Extraire la valeur sans le type
                valeur = valeur.substring(0, valeur.indexOf("^^"));
            }

            // Modifier l'affichage pour éliminer les mots "Propriété" et "Valeur"
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeur));
        }

        return resultString.toString();
    }





    public String getAllRessourcesNaturelles() {
        String query = String.format("SELECT ?ressource ?nom ?propriete ?valeur WHERE { " +
                        "?ressource a <%sRessourceNaturelle> . " +
                        "?ressource <%snom> ?nom . " +
                        "?ressource ?propriete ?valeur . }",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE);

        // Exécutez la requête et récupérez les résultats sous forme de liste de cartes
        List<Map<String, String>> results = jenaEngine.executeSelectQuery(jenaEngine.getModel(), query, "ressource", "nom", "propriete", "valeur");

        StringBuilder resultString = new StringBuilder();
        Map<String, String> resourceInfo = new LinkedHashMap<>(); // Pour stocker les informations de ressource

        for (Map<String, String> result : results) {
            String ressourceId = result.get("ressource");
            String nom = result.get("nom");
            String propriete = result.get("propriete");
            String valeur = result.get("valeur");

            // Vérifier si la ressource a déjà été ajoutée
            if (!resourceInfo.containsKey(ressourceId)) {
                // Ajouter une nouvelle entrée pour la ressource
                resultString.append(String.format("Ressource: %s\n", ressourceId));
                resultString.append(String.format("  Nom: %s\n", nom));
                resourceInfo.put(ressourceId, nom); // Stocker la ressource avec son nom
            }

            // Extraire et afficher la propriété et sa valeur
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1); // Nom de la propriété sans URI

            // Vérifier si la valeur a un type RDF, sinon l'afficher directement
            if (valeur.contains("^^")) {
                // Extraire la valeur sans le type
                valeur = valeur.substring(0, valeur.indexOf("^^"));
            }

            // Modifier l'affichage pour éliminer les mots "Propriété" et "Valeur"
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeur));
        }

        return resultString.toString();
    }

    public String getAllRessourcesEducatives() {
        String query = String.format("SELECT ?ressource ?nom ?propriete ?valeur WHERE { " +
                        "?ressource a <%sRessourceEducative> . " +
                        "?ressource <%snom> ?nom . " +
                        "?ressource ?propriete ?valeur . }",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE);

        // Exécutez la requête et récupérez les résultats sous forme de liste de cartes
        List<Map<String, String>> results = jenaEngine.executeSelectQuery(jenaEngine.getModel(), query, "ressource", "nom", "propriete", "valeur");

        StringBuilder resultString = new StringBuilder();
        Map<String, String> resourceInfo = new LinkedHashMap<>(); // Pour stocker les informations de ressource

        for (Map<String, String> result : results) {
            String ressourceId = result.get("ressource");
            String nom = result.get("nom");
            String propriete = result.get("propriete");
            String valeur = result.get("valeur");

            // Vérifier si la ressource a déjà été ajoutée
            if (!resourceInfo.containsKey(ressourceId)) {
                // Ajouter une nouvelle entrée pour la ressource
                resultString.append(String.format("Ressource: %s\n", ressourceId));
                resultString.append(String.format("  Nom: %s\n", nom));
                resourceInfo.put(ressourceId, nom); // Stocker la ressource avec son nom
            }

            // Extraire et afficher la propriété et sa valeur
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1); // Nom de la propriété sans URI

            // Vérifier si la valeur a un type RDF, sinon l'afficher directement
            if (valeur.contains("^^")) {
                // Extraire la valeur sans le type
                valeur = valeur.substring(0, valeur.indexOf("^^"));
            }

            // Modifier l'affichage pour éliminer les mots "Propriété" et "Valeur"
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeur));
        }

        return resultString.toString();
    }
    public void editRessourceMaterielle(String id, String nom, String description, int quantite, String dateAjout, String typeMateriel, String etat, Boolean disponibilite) {
        // Format the date for the SPARQL query
        String formattedDateAjout = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String ressourceUri = AGRICULTURE_NAMESPACE + id;

        // Construct the SPARQL update query
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:nom ?nom ; " +
                        "        agr:description ?description ; " +
                        "        agr:quantite ?quantite ; " +
                        "        agr:typeMateriel ?typeMateriel ; " +
                        "        agr:etat ?etat ; " +
                        "        agr:disponibilite ?disponibilite ; " +
                        "        agr:dateAjout ?dateAjout ; " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:nom \"%s\" ; " +
                        "        agr:description \"%s\" ; " +
                        "        agr:quantite \"%s\"^^xsd:int ; " +
                        "        agr:typeMateriel \"%s\" ; " +
                        "        agr:etat \"%s\" ; " +
                        "        agr:disponibilite \"%s\" ; " +
                        "        agr:dateAjout %s ; " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agr:nom ?nom . } " +
                        "  OPTIONAL { <%s> agr:description ?description . } " +
                        "  OPTIONAL { <%s> agr:quantite ?quantite . } " +
                        "  OPTIONAL { <%s> agr:typeMateriel ?typeMateriel . } " +
                        "  OPTIONAL { <%s> agr:etat ?etat . } " +
                        "  OPTIONAL { <%s> agr:disponibilite ?disponibilite . } " +
                        "  OPTIONAL { <%s> agr:dateAjout ?dateAjout . } " +
                        "  <%s> a agr:RessourceMaterielle . " +  // Ensure the resource type matches
                        "}",
                AGRICULTURE_NAMESPACE, ressourceUri, ressourceUri,
                nom, description, quantite, typeMateriel,etat,disponibilite, formattedDateAjout,
                ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri,ressourceUri,ressourceUri
        );

        // Log the SPARQL update query for debugging
        System.out.println("Executing SPARQL Update: " + query);

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public void editRessourceNaturelle(String id,String nom, String description, int quantite, String dateAjout, String source) {
        // Format the date for the SPARQL query
        String formattedDateAjout = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String ressourceUri = AGRICULTURE_NAMESPACE + id;

        // Construct the SPARQL update query
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:nom ?nom ; " +
                        "        agr:description ?description ; " +
                        "        agr:quantite ?quantite ; " +
                        "        agr:source ?source ; " +
                        "        agr:dateAjout ?dateAjout ; " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:nom \"%s\" ; " +
                        "        agr:description \"%s\" ; " +
                        "        agr:quantite \"%s\"^^xsd:int ; " +
                        "        agr:source \"%s\" ; " +
                        "        agr:dateAjout %s ; " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agr:nom ?nom . } " +
                        "  OPTIONAL { <%s> agr:description ?description . } " +
                        "  OPTIONAL { <%s> agr:quantite ?quantite . } " +
                        "  OPTIONAL { <%s> agr:source ?source . } " +
                        "  OPTIONAL { <%s> agr:dateAjout ?dateAjout . } " +
                        "  <%s> a agr:RessourceNaturelle . " +  // Ensure the resource type matches
                        "}",
                AGRICULTURE_NAMESPACE, ressourceUri, ressourceUri,
                nom, description, quantite, source, formattedDateAjout,
                ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri
        );

        // Log the SPARQL update query for debugging
        System.out.println("Executing SPARQL Update: " + query);

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }


    public void editRessourceEducative(String id,String nom, String description, int quantite, String dateAjout, String titre, String format, String niveauCompetence) {
        // Format the date for the SPARQL query
        String formattedDateAjout = String.format("\"%s\"^^<http://www.w3.org/2001/XMLSchema#date>", dateAjout);
        String ressourceUri = AGRICULTURE_NAMESPACE + id;

        // Construct the SPARQL update query
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX agr: <%s> " +
                        "DELETE { " +
                        "  <%s> agr:nom ?nom ; " +
                        "        agr:description ?description ; " +
                        "        agr:quantite ?quantite ; " +
                        "        agr:titre ?titre ; " +
                        "        agr:format ?format ; " +
                        "        agr:niveauCompetence ?niveauCompetence ; " +
                        "        agr:dateAjout ?dateAjout ; " +
                        "} " +
                        "INSERT { " +
                        "  <%s> agr:nom \"%s\" ; " +
                        "        agr:description \"%s\" ; " +
                        "        agr:quantite \"%s\"^^xsd:int ; " +
                        "        agr:titre \"%s\" ; " +
                        "        agr:format \"%s\" ; " +
                        "        agr:niveauCompetence \"%s\" ; " +
                        "        agr:dateAjout %s ; " +
                        "} " +
                        "WHERE { " +
                        "  OPTIONAL { <%s> agr:nom ?nom . } " +
                        "  OPTIONAL { <%s> agr:description ?description . } " +
                        "  OPTIONAL { <%s> agr:quantite ?quantite . } " +
                        "  OPTIONAL { <%s> agr:titre ?titre . } " +
                        "  OPTIONAL { <%s> agr:format ?format . } " +
                        "  OPTIONAL { <%s> agr:niveauCompetence ?niveauCompetence . } " +
                        "  OPTIONAL { <%s> agr:dateAjout ?dateAjout . } " +
                        "  <%s> a agr:RessourceEducative . " +  // Ensure the resource type matches
                        "}",
                AGRICULTURE_NAMESPACE, ressourceUri, ressourceUri,
                nom, description, quantite, titre,format,niveauCompetence, formattedDateAjout,
                ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri, ressourceUri,ressourceUri,ressourceUri
        );

        // Log the SPARQL update query for debugging
        System.out.println("Executing SPARQL Update: " + query);

        // Execute the SPARQL update and save the model
        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    // Delete resource method

    public boolean deleteRessource(String id) {
        String ressourceUri = AGRICULTURE_NAMESPACE + id;
        String query = String.format("DELETE WHERE { <%s> ?property ?value . }", ressourceUri);

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
        return true;
    }


}
