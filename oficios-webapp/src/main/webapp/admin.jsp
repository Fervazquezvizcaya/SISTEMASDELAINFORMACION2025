<%@ page import="java.util.List, com.tuproyecto.ListUsersServlet.User, com.tuproyecto.Oficio" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Administrador</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        .nav-link { cursor: pointer; }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
        <div class="container-fluid">
            <a class="navbar-brand" href="#"><i class="bi bi-shield-lock"></i> Panel de Administrador</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarContent">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="logout"><i class="bi bi-box-arrow-right"></i> Cerrar Sesión</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="row">
            <div class="col-lg-3">
                <div class="list-group shadow-sm" id="admin-menu" role="tablist">
                    <a class="list-group-item list-group-item-action active" data-bs-toggle="list" href="#oficio" role="tab"><i class="bi bi-file-earmark-plus"></i> Generar Oficio</a>
                    <a class="list-group-item list-group-item-action" data-bs-toggle="list" href="#add-user" role="tab"><i class="bi bi-person-plus"></i> Añadir Usuario</a>
                    <a class="list-group-item list-group-item-action" data-bs-toggle="list" href="#manage-users" role="tab"><i class="bi bi-people"></i> Gestionar Usuarios</a>
                    <a class="list-group-item list-group-item-action" data-bs-toggle="list" href="#listado-oficios" role="tab"><i class="bi bi-list-columns-reverse"></i> Ver Oficios</a>
                </div>
            </div>

            <div class="col-lg-9">
                <div class="tab-content" id="admin-content">
                    
                    <div class="tab-pane fade show active" id="oficio" role="tabpanel">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                <h4 class="card-title"><i class="bi bi-pencil-square"></i> Nuevo Oficio</h4>
                                <hr>
                                <form action="create-oficio" method="post">
                                    <div class="mb-3"><label for="persona" class="form-label">Persona Dirigida:</label><input type="text" class="form-control" id="persona" name="persona_dirigida" required></div>
                                    <div class="row mb-3"><div class="col-md-8"><label for="area" class="form-label">Área:</label><input type="text" class="form-control" id="area" name="area" required></div><div class="col-md-4"><label for="fecha" class="form-label">Fecha:</label><input type="date" class="form-control" id="fecha" name="fecha" required></div></div>
                                    <div class="mb-3"><label for="asunto" class="form-label">Asunto:</label><textarea class="form-control" id="asunto" name="asunto" rows="5" required></textarea></div>
                                    <button type="submit" class="btn btn-primary">Generar Oficio</button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <div class="tab-pane fade" id="add-user" role="tabpanel">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                <h4 class="card-title"><i class="bi bi-person-plus-fill"></i> Añadir Nuevo Usuario</h4>
                                <hr>
                                <form action="manage-users" method="post">
                                    <input type="hidden" name="action" value="add">
                                    <div class="mb-3"><label for="new_username" class="form-label">Nombre de Usuario:</label><input type="text" class="form-control" id="new_username" name="nombre_usuario" required></div>
                                    <div class="mb-3"><label for="new_password" class="form-label">Contraseña:</label><input type="password" class="form-control" id="new_password" name="password" required></div>
                                    <div class="form-check mb-3"><input class="form-check-input" type="checkbox" id="is_admin" name="es_admin"><label class="form-check-label" for="is_admin">Hacer Administrador</label></div>
                                    <button type="submit" class="btn btn-success">Crear Usuario</button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <div class="tab-pane fade" id="manage-users" role="tabpanel">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                <h4 class="card-title"><i class="bi bi-person-lines-fill"></i> Lista de Usuarios</h4>
                                <hr>
                                <div class="table-responsive">
                                    <table class="table table-hover align-middle">
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Nombre de Usuario</th>
                                                <th>Rol</th>
                                                <th class="text-center">Acción</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <%
                                                // 🚨 CLAVE: El ListUsersServlet cargó esta lista.
                                                List<com.tuproyecto.ListUsersServlet.User> userList = (List<com.tuproyecto.ListUsersServlet.User>) request.getAttribute("userList");
                                                
                                                if (userList != null && !userList.isEmpty()) {
                                                    for (com.tuproyecto.ListUsersServlet.User user : userList) {
                                                %>
                                                <tr>
                                                    <td><%= user.getId() %></td>
                                                    <td><%= user.getName() %></td>
                                                    <td><span class="badge bg-<%= user.isAdmin() ? "primary" : "secondary" %>"><%= user.isAdmin() ? "Admin" : "Usuario" %></span></td>
                                                    <td class="text-center">
                                                        <% if (!user.getName().equals("admin")) { %>
                                                        <form action="manage-users" method="post" onsubmit="return confirm('¿Estás seguro de eliminar a <%= user.getName() %>?');" class="d-inline">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="id_usuario" value="<%= user.getId() %>">
                                                            <button type="submit" class="btn btn-danger btn-sm"><i class="bi bi-trash"></i></button>
                                                        </form>
                                                        <% } %>
                                                    </td>
                                                </tr>
                                                <%
                                                    }
                                                } else {
                                                %>
                                                <tr>
                                                    <td colspan="4" class="text-center text-muted">No hay usuarios para mostrar.</td>
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
                    
                    <div class="tab-pane fade" id="listado-oficios" role="tabpanel">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                <h4 class="card-title"><i class="bi bi-file-earmark-bar-graph"></i> Lista de Oficios Creados</h4>
                                <hr>
                                <div class="table-responsive">
                                    <table class="table table-hover align-middle table-sm">
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Dirigido A</th>
                                                <th>Área</th>
                                                <th>Fecha</th>
                                                <th>Hash (Integridad)</th>
                                                <th>Acción</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:if test="${not empty oficioList}">
                                                <c:forEach var="oficio" items="${oficioList}">
                                                    <tr>
                                                        <td><c:out value="${oficio.id}"/></td>
                                                        <td><c:out value="${oficio.personaDirigida}"/></td>
                                                        <td><c:out value="${oficio.area}"/></td>
                                                        <td><c:out value="${oficio.fecha}"/></td>
                                                        <td style="font-size: 0.75rem;"><c:out value="${oficio.hash.substring(0, 10)}..."/></td>
                                                        <td>
                                                            <a href="edit-oficio?id=${oficio.id}" class="btn btn-secondary btn-sm"><i class="bi bi-pencil"></i> Editar</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:if>
                                            <c:if test="${empty oficioList}">
                                                <tr>
                                                    <td colspan="6" class="text-center text-muted">No hay oficios registrados.</td>
                                                </tr>
                                            </c:if>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>