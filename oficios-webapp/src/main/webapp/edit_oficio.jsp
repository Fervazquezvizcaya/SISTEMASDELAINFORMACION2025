<%@ page import="com.tuproyecto.Oficio" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Lógica de Scriptlet: Recuperación y Validación del Objeto Oficio --%>
<%
    // Recupera el objeto 'oficio' del request, previamente cargado por el Servlet (EditOficioServlet).
    com.tuproyecto.Oficio oficio = (com.tuproyecto.Oficio) request.getAttribute("oficio");
    
    // Bloque de seguridad: Si no se encontró el objeto Oficio (ej. ID inválido o acceso directo),
    // se redirige al usuario a la lista principal para evitar errores.
    if (oficio == null) {
        response.sendRedirect("list-oficios-user");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Oficio ID <%= oficio.getId() %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="card shadow-lg">
                    <div class="card-body p-5">
                        <h4 class="card-title mb-4"><i class="bi bi-pencil-square"></i> Editar Oficio #<%= oficio.getId() %></h4>
                        <p class="text-muted small">Hash de Integridad: <%= oficio.getHash().substring(0, 20) %>...</p>
                        <hr>
                        
                        <form action="edit-oficio" method="post">
                            <input type="hidden" name="id_oficio" value="<%= oficio.getIdOficioReal() %>">
                            
                            <div class="mb-3">
                                <label for="persona" class="form-label">Persona Dirigida:</label>
                                <input type="text" class="form-control" id="persona" name="persona_dirigida" value="<%= oficio.getPersonaDirigida() %>" required>
                            </div>
                            
                            <div class="row mb-3">
                                <div class="col-md-8">
                                    <label for="area" class="form-label">Área:</label>
                                    <input type="text" class="form-control" id="area" name="area" value="<%= oficio.getArea() %>" required>
                                </div>
                                <div class="col-md-4">
                                    <label for="fecha" class="form-label">Fecha:</label>
                                    <input type="date" class="form-control" id="fecha" name="fecha" value="<%= oficio.getFecha() %>" required>
                                </div>
                            </div>
                            
                            <div class="mb-4">
                                <label for="asunto" class="form-label">Asunto:</label>
                                <textarea class="form-control" id="asunto" name="asunto" rows="5" required><%= oficio.getAsunto() %></textarea>
                            </div>
                            
                            <div class="d-flex justify-content-between">
                                <a href="list-oficios-user" class="btn btn-secondary">Cancelar</a>
                                <button type="submit" class="btn btn-success"><i class="bi bi-save"></i> Guardar Cambios</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>