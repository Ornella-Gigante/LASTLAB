package es.ifp.labsalut;

public class Medicamento {
    private int idMedicamento;
    private String nombre;
    private int dosis;
    private float frecuencia;
    private float recordatorio;

    public Medicamento(String nombre, int dosis, float frecuencia, float recordatorio) {
        this.nombre = nombre;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.recordatorio = recordatorio;
    }
    public int getIdMedicamento() {
        return idMedicamento;
    }

    public void setIdMedicamento(int idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getDosis() {
        return dosis;
    }

    public void setDosis(int dosis) {
        this.dosis = dosis;
    }

    public float getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(float frecuencia) {
        this.frecuencia = frecuencia;
    }

    public float getRecordatorio() {
        return recordatorio;
    }

    public void setRecordatorio(float recordatorio) {
        this.recordatorio = recordatorio;
    }


}