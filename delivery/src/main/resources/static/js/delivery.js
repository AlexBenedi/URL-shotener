$(document).ready(function () {
    // Variable global para el userId (puedes setearlo desde el backend dinámicamente)
    var userId = $("#userIdValue").text(); // Suponiendo que tienes un input hidden con este ID en el HTML
    console.log("userId from ajax: " + userId);

    // Mostrar/ocultar el campo de texto para el nombre de la marca
    $("#brandedCheckbox").change(function () {
        if ($(this).prop('checked')) {
            $("#brandedNameGroup").show(); // Muestra el campo de texto
        } else {
            $("#brandedNameGroup").hide(); // Oculta el campo de texto
            $("#brandedName").val(''); // Borra el contenido
        }
    });

    // Manejar el formulario de creación de enlaces
    $("#shortener").submit(function (event) {
        console.log("Has pulsado el botón de acortar URL");
        event.preventDefault();
        
        // Agregar userId y brandedName si es necesario
        var formData = $(this).serialize() + "&userId=" + userId;
        
        // Si el checkbox está marcado, incluir el nombre de la marca
        if ($("#brandedCheckbox").prop('checked')) {
            formData += "&isBranded=true&name=" + encodeURIComponent($("#brandedName").val());
        }

        $.ajax({
            type: "POST",
            url: "/api/linkUser",
            data: formData,
            success: function (response) {
                var resultDiv = $("#result");

                // Display the shortened URL
                resultDiv.html(
                    "<div class='alert alert-success lead'>" +
                    "<a target='_blank' href='" +
                    response.url +
                    "'>" +
                    response.url +
                    "</a>" +
                    "</div>"
                );

                // Display the QR code if it exists
                if (response.qrCode) {
                    resultDiv.append("<p>QR Code:</p>");
                    resultDiv.append(
                        "<img src='data:image/png;base64," + response.qrCode + "' alt='QR Code'>"
                    );
                    var qrCodeDownloadUrl = "/" + response.url.split('/').pop() + "/qr"; // Use response.url
                    resultDiv.append('<p><a href="' + qrCodeDownloadUrl + '" download="qrcode.png">Download QR Code</a></p>');
                    resultDiv.append('<p>' + window.location.origin + qrCodeDownloadUrl + '</p>');
                }
                fetchUserLinks(); // Actualizar la tabla
            },
            error: function () {
                $("#result").html(
                    "<div class='alert alert-danger lead'>ERROR</div>"
                );
            }
        });
    });

    // Cargar los links del usuario al cargar la página
    fetchUserLinks();

    function fetchUserLinks() {
        var formData = "userId=" + userId;

        $.ajax({
            type: "GET",
            url: "/api/getUserLink",
            data: formData,
            success: function (links) {
                var tableBody = $("#linksTable tbody");
                tableBody.empty(); // Limpiar la tabla antes de llenarla

                links.forEach(function (link) {
                    var qrCodeHtml = link.shortUrl.qrCode
                        ? `<img src="data:image/png;base64,${link.shortUrl.qrCode}" alt="QR Code" width="100">`
                        : `<button class="btn btn-primary generate-qr" data-hash="${link.shortUrl.hash}">Generate QR</button>`;

                    // Generar HTML de la fila
                    var rowHtml = `
                        <tr id="link-row-${link.shortUrl.id}">
                            <td>${link.shortUrl.redirection.target}</td>
                            <td id="clicks-count-${link.shortUrl.hash}">Cargando...</td>
                            <td>${qrCodeHtml}</td>
                            <td><a href="/${link.shortUrl.hash}" target="_blank">${window.location.origin}/${link.shortUrl.hash}</a></td>
                            <td>
                                <button class="btn btn-danger delete-link" data-id="${link.id}" >Delete</button>
                            </td>
                        </tr>
                    `;
                    tableBody.append(rowHtml);

                    fetchClicksByHash(link.shortUrl.hash);
                });

                // Asociar evento al botón de eliminar
                $(".delete-link").click(function () {
                    var id = $(this).data("id");
                    deleteLink(id);
                });

                // Asociar evento al botón "Generate QR"
                $(".generate-qr").click(function () {
                    var hash = $(this).data("hash");
                    generateQRCode(hash);
                });
            },
            error: function () {
                alert("Error fetching user links.");
            }
        });
    }

    // Función para obtener los clics totales por hash
    function fetchClicksByHash(hash) {
        $.ajax({
            type: "GET",
            url: `/clicks/${hash}`, // Endpoint para obtener los clics totales
            success: function (totalClicks) {
                console.log(`Total clicks for hash ${hash}: ${totalClicks}`);
                // Actualizar la celda correspondiente con el total de clics
                $(`#clicks-count-${hash}`).text(totalClicks);
            },
            error: function () {
                alert(`Error fetching clicks for hash: ${hash}`);
            }
        });
    }

    // Función para generar el QR Code
    function generateQRCode(hash) {
        // Solicitar el QR al backend
        console.log("Generando QR para hash: " + hash);
        $.ajax({
            type: "GET",
            url: `/${hash}/qr`,  // URL generada // Llamar al endpoint para obtener el QR
            success: function (qrCodeImage) {
                // Convertir la imagen en base64 a una URL para mostrarla
                var qrImageUrl = URL.createObjectURL(new Blob([qrCodeImage], { type: 'image/png' }));
                // Mostrar el QR en la tabla
                $(`button[data-hash="${hash}"]`).replaceWith(`<img src="${qrImageUrl}" alt="QR Code" width="50">`);
            },
            error: function () {
                alert("Error generating QR Code.");
            }
        });
    }

    function deleteLink(idLink) {
        if (confirm("¿Estás seguro de que deseas eliminar este enlace?")) {
            $.ajax({
                type: "DELETE",
                url: `/delete/${idLink}`,
                success: function () {
                    alert("Enlace eliminado con éxito.");
                    location.reload(); // Recarga la página
                },
                error: function () {
                    alert("Error al eliminar el enlace. Por favor, inténtalo de nuevo.");
                }
            });
        }
    }
});
