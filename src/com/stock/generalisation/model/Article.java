package model;

import java.math.BigDecimal;

public class Article {
    private int idArticle;
    private String nom;
    private String modeGestion;
    private BigDecimal quantiteStock;
    private BigDecimal valeurStock;

    public Article(int idArticle, String nom, String modeGestion, BigDecimal quantiteStock, BigDecimal valeurStock) {
        this.idArticle = idArticle;
        this.nom = nom;
        this.modeGestion = modeGestion;
        this.quantiteStock = quantiteStock;
        this.valeurStock = valeurStock;
    }

    public int getIdArticle() {
        return idArticle;
    }

    public String getNom() {
        return nom;
    }

    public String getModeGestion() {
        return modeGestion;
    }

    public BigDecimal getQuantiteStock() {
        return quantiteStock;
    }

    public BigDecimal getValeurStock() {
        return valeurStock;
    }
}
