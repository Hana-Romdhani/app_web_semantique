package com.greenlink.dto;

public class PlanteUpdateDTO  {

    private String nom;
    private String description;
    private String hauteur;
    private String type;
    private String saison;

    // Getters and Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHauteur() { return hauteur; }
    public void setHauteur(String hauteur) { this.hauteur = hauteur; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSaison() { return saison; }
    public void setSaison(String saison) { this.saison = saison; }

}
