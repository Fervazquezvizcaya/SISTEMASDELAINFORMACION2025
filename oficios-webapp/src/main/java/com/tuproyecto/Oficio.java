package com.tuproyecto;

/**
 * Clase modelo (Java Bean) que representa un Oficio.
 * Contiene los atributos y la lógica de acceso a los datos de un documento formal.
 */
public class Oficio {
    // Atributos de la clase
    
    // Identificador único de la base de datos (Primary Key). Se usa para manipular el registro (Editar/Eliminar).
    private int idOficioReal; 
    
    // Número de Oficio consecutivo. Se usa únicamente para mostrar al usuario en la interfaz.
    private int id; 
    
    private String personaDirigida;
    private String area;
    private String asunto;
    private String fecha;
    // Hash criptográfico del documento para verificar su integridad.
    private String hash;

    /**
     * Constructor que inicializa todos los atributos del Oficio.
     * @param idOficioReal La clave primaria del registro en la DB.
     * @param numeroOficio El número consecutivo visible del Oficio.
     * @param personaDirigida El destinatario del Oficio.
     * @param area El área o departamento del remitente.
     * @param asunto El tema principal del Oficio.
     * @param fecha La fecha de creación del Oficio.
     * @param hash El hash de seguridad del contenido.
     */
    public Oficio(int idOficioReal, int numeroOficio, String personaDirigida, String area, String asunto, String fecha, String hash) {
        // Asigna el ID real usado para DB.
        this.idOficioReal = idOficioReal; 
        // Asigna el número consecutivo al campo 'id' para su fácil visualización.
        this.id = numeroOficio; 
        this.personaDirigida = personaDirigida;
        this.area = area;
        this.asunto = asunto;
        this.fecha = fecha;
        this.hash = hash;
    }
    
    // --- Métodos Getters (Acceso a datos) ---
    
    /**
     * Retorna el número de Oficio consecutivo (usado para mostrar en tablas).
     */
    public int getId() { return id; }
    
    /**
     * Retorna la clave primaria real de la base de datos (usada para operaciones de backend).
     */
    public int getIdOficioReal() { return idOficioReal; } 
    
    public String getPersonaDirigida() { return personaDirigida; }
    public String getArea() { return area; }
    public String getAsunto() { return asunto; }
    public String getFecha() { return fecha; }
    public String getHash() { return hash; }
}