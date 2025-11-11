package com.tuproyecto;

// Clase auxiliar para guardar los datos del Oficio (MODELO)
public class Oficio {
    private int idOficioReal; // Clave primaria real (con saltos). Se usa para Editar/Eliminar.
    private int id;           // 🚨 ESTO ES EL NUMERO_OFICIO (Consecutivo). Se usa para mostrar en la tabla.
    private String personaDirigida;
    private String area;
    private String asunto;
    private String fecha;
    private String hash;

    // 🚨 Constructor Modificado: Acepta ID real y el NUMERO_OFICIO
    public Oficio(int idOficioReal, int numeroOficio, String personaDirigida, String area, String asunto, String fecha, String hash) {
        this.idOficioReal = idOficioReal; 
        this.id = numeroOficio;           // ⬅️ Asignación clave: 'id' ahora es el número consecutivo
        this.personaDirigida = personaDirigida;
        this.area = area;
        this.asunto = asunto;
        this.fecha = fecha;
        this.hash = hash;
    }
    
    // 🚨 Getter CLAVE: Cuando el JSP llama a getId(), obtiene el NUMERO_OFICIO (consecutivo).
    public int getId() { return id; }
    
    // Getter para obtener la clave real (si se necesita para eliminar o editar)
    public int getIdOficioReal() { return idOficioReal; } 
    
    public String getPersonaDirigida() { return personaDirigida; }
    public String getArea() { return area; }
    public String getAsunto() { return asunto; }
    public String getFecha() { return fecha; }
    public String getHash() { return hash; }
}