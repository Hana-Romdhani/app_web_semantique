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
        String disponibiliteValue = disponibilite ? "true" : "false";
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  _:ressource a <%sRessourceMaterielle> ; " +
                        "              <%snom> \"%s\" ; " +
                        "              <%sdescription> \"%s\" ; " +
                        "              <%squantite> \"%d\"^^xsd:int ; " +
                        "              <%sdateAjout> \"%s\"^^xsd:date ; " +
                        "              <%stypeMateriel> \"%s\" ; " +
                        "              <%setat> \"%s\" ; " +
                        "              <%sdisponibilite> \"%s\"^^xsd:boolean . " +
                        "}",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, nom, AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, quantite, AGRICULTURE_NAMESPACE, dateAjout,
                AGRICULTURE_NAMESPACE, typeMateriel, AGRICULTURE_NAMESPACE, etat,
                AGRICULTURE_NAMESPACE, disponibiliteValue
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public void addRessourceNaturelle(String nom, String description, int quantite, String dateAjout, String source) {
        String query = String.format(
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                        "INSERT DATA { " +
                        "  _:ressource a <%sRessourceNaturelle> ; " +
                        "              <%snom> \"%s\" ; " +
                        "              <%sdescription> \"%s\" ; " +
                        "              <%squantite> \"%d\"^^xsd:int ; " +
                        "              <%sdateAjout> \"%s\"^^xsd:date ; " +
                        "              <%ssource> \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, nom, AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, quantite, AGRICULTURE_NAMESPACE, dateAjout,
                AGRICULTURE_NAMESPACE, source
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public void addRessourceEducative(String nom, String description, String titre, String format, String niveauCompetence) {
        String query = String.format(
                "INSERT DATA { " +
                        "  _:ressource a <%sRessourceEducative> ; " +
                        "              <%snom> \"%s\" ; " +
                        "              <%sdescription> \"%s\" ; " +
                        "              <%stitre> \"%s\" ; " +
                        "              <%sformat> \"%s\" ; " +
                        "              <%sniveauCompetence> \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE, AGRICULTURE_NAMESPACE, nom, AGRICULTURE_NAMESPACE, description,
                AGRICULTURE_NAMESPACE, titre, AGRICULTURE_NAMESPACE, format,
                AGRICULTURE_NAMESPACE, niveauCompetence
        );

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


    // Edit methods for each resource type
    public void editRessourceMaterielle(String nom, String description, int quantite, String dateAjout, String typeMateriel, String etat, Boolean disponibilite) {
        String updateQuery = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE { " +
                        "  ?ressource agri:description ?oldDescription . " +
                        "  ?ressource agri:quantite ?oldQuantite . " +
                        "  ?ressource agri:typeMateriel ?oldTypeMateriel . " +
                        "  ?ressource agri:etat ?oldEtat . " +
                        "  ?ressource agri:disponibilite ?oldDisponibilite . " +
                        "} " +
                        "INSERT { " +
                        "  ?ressource agri:description \"%s\" ; " +
                        "             agri:quantite %d ; " +
                        "             agri:typeMateriel \"%s\" ; " +
                        "             agri:etat \"%s\" ; " +
                        "             agri:disponibilite %s . " +
                        "} " +
                        "WHERE { " +
                        "  ?ressource agri:nom \"%s\" . " +
                        "  OPTIONAL { ?ressource agri:description ?oldDescription . } " +
                        "  OPTIONAL { ?ressource agri:quantite ?oldQuantite . } " +
                        "  OPTIONAL { ?ressource agri:typeMateriel ?oldTypeMateriel . } " +
                        "  OPTIONAL { ?ressource agri:etat ?oldEtat . } " +
                        "  OPTIONAL { ?ressource agri:disponibilite ?oldDisponibilite . } " +
                        "}",
                AGRICULTURE_NAMESPACE, description, quantite, typeMateriel, etat, disponibilite ? "true" : "false", nom
        );

        // Exécutez la requête de mise à jour
        jenaEngine.executeUpdate(jenaEngine.getModel(), updateQuery);
        jenaEngine.saveModelToFile();
    }



    public void editRessourceNaturelle(String nom, String description, int quantite, String dateAjout, String source) {
        String updateQuery = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE { " +
                        "  ?ressource agri:description ?oldDescription . " +
                        "  ?ressource agri:quantite ?oldQuantite . " +
                        "  ?ressource agri:dateAjout ?oldDateAjout . " +
                        "  ?ressource agri:source ?oldSource . " +
                        "} " +
                        "INSERT { " +
                        "  ?ressource agri:description \"%s\" ; " +
                        "             agri:quantite %d ; " +
                        "             agri:dateAjout \"%s\" ; " +
                        "             agri:source \"%s\" . " +
                        "} " +
                        "WHERE { " +
                        "  ?ressource agri:nom \"%s\" . " +
                        "  OPTIONAL { ?ressource agri:description ?oldDescription . } " +
                        "  OPTIONAL { ?ressource agri:quantite ?oldQuantite . } " +
                        "  OPTIONAL { ?ressource agri:dateAjout ?oldDateAjout . } " +
                        "  OPTIONAL { ?ressource agri:source ?oldSource . } " +
                        "}",
                AGRICULTURE_NAMESPACE, description, quantite, dateAjout, source, nom
        );

        // Exécutez la requête de mise à jour
        jenaEngine.executeUpdate(jenaEngine.getModel(), updateQuery);
        jenaEngine.saveModelToFile();
    }



    public void editRessourceEducative(String nom, String description, int quantite,String titre, String format, String niveauCompetence) {
        String updateQuery = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE { " +
                        "  ?ressource agri:description ?oldDescription . " +
                        "  ?ressource agri:quantite ?oldQuantite . " +
                        "  ?ressource agri:titre ?oldTitre . " +
                        "  ?ressource agri:format ?oldFormat . " +
                        "  ?ressource agri:niveauCompetence ?oldNiveauCompetence . " +
                        "} " +
                        "INSERT { " +
                        "  ?ressource agri:description \"%s\" ; " +
                        "             agri:quantite %d ; " +
                        "             agri:titre \"%s\" ; " +
                        "             agri:format \"%s\" ; " +
                        "             agri:niveauCompetence \"%s\" . " +
                        "} " +
                        "WHERE { " +
                        "  ?ressource agri:nom \"%s\" . " +
                        "  OPTIONAL { ?ressource agri:description ?oldDescription . } " +
                        "  OPTIONAL { ?ressource agri:quantite ?oldQuantite . } " +
                        "  OPTIONAL { ?ressource agri:titre ?oldTitre . } " +
                        "  OPTIONAL { ?ressource agri:format ?oldFormat . } " +
                        "  OPTIONAL { ?ressource agri:niveauCompetence ?oldNiveauCompetence . } " +
                        "}",
                AGRICULTURE_NAMESPACE, description,quantite, titre, format, niveauCompetence, nom
        );

        // Exécutez la requête de mise à jour
        jenaEngine.executeUpdate(jenaEngine.getModel(), updateQuery);
        jenaEngine.saveModelToFile();
    }


    // Delete resource method
    public void deleteRessourceMaterielle(String nom) {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE WHERE { " +
                        "  ?ressource agri:nom \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE, nom
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public void deleteRessourceNaturelle(String nom) {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE WHERE { " +
                        "  ?ressource agri:nom \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE, nom
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

    public void deleteRessourceEducative(String nom) {
        String query = String.format(
                "PREFIX agri: <%s> " +
                        "DELETE WHERE { " +
                        "  ?ressource agri:nom \"%s\" . " +
                        "}",
                AGRICULTURE_NAMESPACE, nom
        );

        jenaEngine.executeUpdate(jenaEngine.getModel(), query);
        jenaEngine.saveModelToFile();
    }

}
