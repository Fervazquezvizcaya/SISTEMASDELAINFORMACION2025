<%@ page import="java.util.List, com.tuproyecto.Oficio" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Usuario</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        body { background-color: #f8f9fa; }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark bg-secondary shadow-sm">
        <div class="container-fluid">
            <a class="navbar-brand" href="#"><i class="bi bi-person-circle"></i> Panel de Usuario</a>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="logout"><i class="bi bi-box-arrow-right"></i> Cerrar Sesión</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <div class="container mt-5">
        <div class="row justify-content.center">
            <div class="col-lg-10">
                
                <!-- SECCIÓN 1: FORMULARIO GENERAR OFICIO -->
                <div class="card shadow-sm mb-5">
                    <div class="card-body p-4">
                        <h4 class="card-title mb-4"><i class="bi bi-pencil-square"></i> Generar Nuevo Oficio</h4>
                        <hr>
                        <form action="create-oficio" method="post">
                            <div class="mb-3">
                                <label for="persona" class="form-label">Persona Dirigida:</label>
                                <input type="text" class="form-control" id="persona" name="persona_dirigida" required>
                            </div>
                            <div class="row mb-3">
                                <div class="col-md-8">
                                    <label for="area" class="form-label">Área:</label>
                                    <input type="text" class="form-control" id="area" name="area" required>
                                </div>
                                <div class="col-md-4">
                                    <label for="fecha" class="form-label">Fecha:</label>
                                    <input type="date" class="form-control" id="fecha" name="fecha" required>
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="asunto" class="form-label">Asunto:</label>
                                <textarea class="form-control" id="asunto" name="asunto" rows="5" required></textarea>
                            </div>
                            <div class="d-grid">
                                <button type="submit" class="btn btn-primary btn-lg">Generar Oficio</button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- 🚨 SECCIÓN 2: LISTADO DE OFICIOS CREADOS POR ESTE USUARIO (Con Scriptlets) -->
                <div class="card shadow-sm">
                    <div class="card-body p-4">
                        <h4 class="card-title mb-4"><i class="bi bi-list-columns-reverse"></i> Lista de Oficios</h4>
                        <hr>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle table-sm">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Persona</th>
                                        <th>Área</th>
                                        <th>Asunto</th>
                                        <th>Fecha</th>
                                        <th>Hash</th>
                                        <th>Acción</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        // 🚨 USAMOS SCRIPTLETS para evitar el error 500 de JSTL
                                        List<com.tuproyecto.Oficio> oficioList = (List<com.tuproyecto.Oficio>) request.getAttribute("oficioList");
                                        
                                        if (oficioList != null && !oficioList.isEmpty()) {
                                            for (com.tuproyecto.Oficio oficio : oficioList) {
                                    %>
                                    <tr>
                                        <td><%= oficio.getId() %></td>
                                        <td><%= oficio.getPersonaDirigida() %></td>
                                        <td><%= oficio.getArea() %></td>
                                        <td><%= oficio.getAsunto() %></td>
                                        <td><%= oficio.getFecha() %></td>
                                        <td><%= oficio.getHash() %></td>
                                        <td><a href="edit-oficio?id=<%= oficio.getIdOficioReal() %>" class="btn btn-secondary btn-sm">Editar</a></td>
                                    </tr>
                                    <%
                                            }
                                        } else {
                                    %>
                                    <tr>
                                        <td colspan="7" class="text-center text-muted">Aún no has creado ningún oficio.</td>
                                    </tr>
                                    <%
                                        }
                                    %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>