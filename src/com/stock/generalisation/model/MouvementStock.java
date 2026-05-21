package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MouvementStock {
    private int idMouvementStock;
    private String typeMouvement;
    private BigDecimal nombre;
    private BigDecimal prixUnitaire;
    private Timestamp dateMouvement;
    private String idSource;
    private BigDecimal coutTotal;
    private BigDecimal coutUnitaireMoyenPondere;

    public MouvementStock(String typeMouvement, BigDecimal nombre, BigDecimal prixUnitaire,
            Timestamp dateMouvement, String idSource) {
        this(0, typeMouvement, nombre, prixUnitaire, dateMouvement, idSource,
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public MouvementStock(int idMouvementStock, String typeMouvement, BigDecimal nombre, BigDecimal prixUnitaire,
            Timestamp dateMouvement, String idSource, BigDecimal coutTotal, BigDecimal coutUnitaireMoyenPondere) {
        this.idMouvementStock = idMouvementStock;
        this.typeMouvement = typeMouvement;
        this.nombre = nombre;
        this.prixUnitaire = prixUnitaire;
        this.dateMouvement = dateMouvement;
        this.idSource = idSource;
        this.coutTotal = coutTotal;
        this.coutUnitaireMoyenPondere = coutUnitaireMoyenPondere;
    }

    public int getIdMouvementStock() {
        return idMouvementStock;
    }

    public String getTypeMouvement() {
        return typeMouvement;
    }

    public BigDecimal getNombre() {
        return nombre;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public Timestamp getDateMouvement() {
        return dateMouvement;
    }

    public String getIdSource() {
        return idSource;
    }

    public BigDecimal getCoutTotal() {
        return coutTotal;
    }

    public BigDecimal getCoutUnitaireMoyenPondere() {
        return coutUnitaireMoyenPondere;
    }
}
