package model;

public class TypeGestion {
    private int idTypeGestion;
    private String libeller;

    public TypeGestion(int idTypeGestion, String libeller) {
        this.idTypeGestion = idTypeGestion;
        this.libeller = libeller;
    }

    public int getIdTypeGestion() {
        return idTypeGestion;
    }

    public String getLibeller() {
        return libeller;
    }

    public String toString() {
        return libeller;
    }
}
