package mg.nandra.framework.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Fichier {

    private String nom;
    private String typeMime;
    private long taille;
    private byte[] contenu;

    public Fichier() {}

    public Fichier(String nom, String typeMime, long taille, byte[] contenu) {
        this.nom = nom;
        this.typeMime = typeMime;
        this.taille = taille;
        this.contenu = contenu;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }

    public long getTaille() { return taille; }
    public void setTaille(long taille) { this.taille = taille; }

    public byte[] getContenu() { return contenu; }
    public void setContenu(byte[] contenu) { this.contenu = contenu; }

    public String getExtension() {
        if (nom == null) return "";
        int i = nom.lastIndexOf('.');
        return (i < 0) ? "" : nom.substring(i + 1).toLowerCase();
    }
    
    public Path sauvegarder(String dossier) throws IOException {
        Path dir = Paths.get(dossier);
        Files.createDirectories(dir);
        Path cible = dir.resolve(nom);
        Files.write(cible, contenu);
        return cible;
    }
}