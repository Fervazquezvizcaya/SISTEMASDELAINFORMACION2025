package com.tuproyecto;

// Clase auxiliar para guardar los datos del Oficio (MODELO)
public class Oficio {
    private int id;
    private String personaDirigida;
    private String area;
    private String asunto;
    private String fecha;
    private String hash;

    public Oficio(int id, String personaDirigida, String area, String asunto, String fecha, String hash) {
        this.id = id;
        this.personaDirigida = personaDirigida;
        this.area = area;
        this.asunto = asunto;
        this.fecha = fecha;
        this.hash = hash;
    }
    
    // Getters (Necesarios para que JSTL pueda leer los campos con ${oficio.id})
    public int getId() { return id; }
    public String getPersonaDirigida() { return personaDirigida; }
    public String getArea() { return area; }
    public String getAsunto() { return asunto; }
    public String getFecha() { return fecha; }
    public String getHash() { return hash; }
    
    // Setters opcionales si necesitas actualizar la base de datos
    // public void setPersonaDirigida(String personaDirigida) { this.personaDirigida = personaDirigida; }
    // ...
}