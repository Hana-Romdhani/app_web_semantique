package com.greenlink.utils;

import com.greenlink.config.JenaEngine;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, String> currentRessource = new HashMap<>(); // Pour suivre la ressource actuelle

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
                resultString.append(String.format("Ressource: %s\n", ressource));
                resultString.append(String.format("  Nom: %s\n", nom));
                currentRessource.put(ressource, nom); // Mémoriser la ressource pour la prochaine itération
            }

            // Extraire le nom de la propriété sans URI
            String proprieteNom = propriete.substring(propriete.lastIndexOf('#') + 1);

            // Retirer le type si présent dans la valeur
            String valeurFormattee = valeur.contains("^^") ? valeur.split("\\^\\^")[0] : valeur;

            // Modifier l'affichage pour éliminer les mots "Propriété" et "Valeur"
            resultString.append(String.format("  %s : %s\n", proprieteNom, valeurFormattee));
        }

        return resultString.toString();
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
