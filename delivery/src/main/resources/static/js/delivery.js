$(document).ready(function () {
    // Variable global para el userId
    var userId = $("#userIdValue").text(); // Supone que hay un elemento en el HTML con este ID
    console.log("userId from AJAX: " + userId);

    // Establecer la conexión WebSocket con el userId como identificador
    const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
    const wsUrl = `${protocol}${window.location.host}/ws-endpoint?userId=${encodeURIComponent(userId)}`;

    // Crear conexión WebSocket
    socket = new WebSocket(wsUrl);

    // Eventos del WebSocket
    socket.onopen = function () {
        console.log("WebSocket conectado para el usuario: " + userId);

        fetchUserLinks();
    };

    // Cuando se recibe un mensaje del servidor
    socket.onmessage = function (event) {
        const data = JSON.parse(event.data);
        console.log("Data from QR", data);

        const qrCode = data.second;
        const id = data.first;

        fetchUserLinks();

        // Mostrar el QR en la página
        var resultDiv = $("#result");
        resultDiv.append('<img src="data:image/png;base64,' + qrCode + '" alt="QR Code">');
        //var qrCodeDownloadUrl = "/qr/" + id;
        //resultDiv.append('<p><a href="' + qrCodeDownloadUrl + '" download="qr.png">Descargar QR</a><p>');
        //resultDiv.append('<p><a href="' + window.location.origin + qrCodeDownloadUrl + '" target="_blank">' + window.location.origin + qrCodeDownloadUrl + '</a></p>');
    };

    // Manejar el cierre de la conexión WebSocket
    socket.onclose = function () {
        console.log("Conexión WebSocket cerrada");
    };

    // Manejar errores en la conexión WebSocket
    socket.onerror = function (error) {
        console.error("Error en la conexión WebSocket:", error);
    };

    // Mostrar/ocultar el campo de texto para el nombre de la marca
    $("#brandedCheckbox").change(function () {
        if ($(this).prop('checked')) {
            $("#brandedNameGroup").show();
        } else {
            $("#brandedNameGroup").hide();
            $("#brandedName").val('');
        }
    });

    // Manejar el formulario de creación de enlaces
    $("#shortener").submit(function (event) {
        console.log("Has pulsado el botón de acortar URL");
        event.preventDefault();

        // Agregar userId y brandedName si es necesario
        var formData = $(this).serialize();

        if ($("#brandedCheckbox").prop('checked')) {
            formData += "&isBranded=true&name=" + encodeURIComponent($("#brandedName").val());
        }

        $.ajax({
            type: "POST",
            url: "/api/link/user/" + userId,
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
                

                console.log("QR Code generated:", response.qrCodeGenerated);
                // Display the QR code if it exists
                if (response.qrCodeGenerated === true) {
                    resultDiv.append('<p>QR Code:</p>');
                    resultDiv.append('<p><a href="' + response.urlQR + '" download="qrcode.png">Download QR Code</a></p>');
                    resultDiv.append('<p><a href="' + response.urlQR + '" target="_blank">' + response.urlQR + '</a></p>');
                }

                fetchUserLinks();
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

        $.ajax({
            type: "GET",
            url: "/api/users/" + userId + "/links",
            success: function (links) {
                var tableBody = $("#linksTable tbody");
                tableBody.empty();

                links.forEach(function (link) {
                    var qrCodeHtml = link.shortUrl.qrCode
                        ? `<img src="data:image/png;base64,${link.shortUrl.qrCode}" alt="QR Code" width="100">`
                        : `<button class="btn btn-primary generate-qr" data-hash="${link.shortUrl.hash}" data-target="${link.shortUrl.redirection.target}">Generate QR</button>`;

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

                $(".delete-link").click(function () {
                    var id = $(this).data("id");
                    deleteLink(id);
                });

                $(".generate-qr").click(function () {
                    var hash = $(this).data("hash");
                    var target = $(this).data("target");
                    generateQRCode(hash);
                });
            },
            error: function () {
                alert("Error fetching user links.");
            }
        });
    }

    function fetchClicksByHash(hash) {
        $.ajax({
            type: "GET",
            url: `/clicks/${hash}`,
            success: function (totalClicks) {
                $(`#clicks-count-${hash}`).text(totalClicks);
            },
            error: function () {
                alert(`Error fetching clicks for hash: ${hash}`);
            }
        });
    }

    function generateQRCode(hash) {
        $.ajax({
            type: "GET",
            url: `/qr/${hash}`,
            xhrFields: {
                responseType: 'blob' // Asegura que recibimos el binario directamente
            },
            success: function (blob) {
                var qrImageUrl = URL.createObjectURL(blob);
                $(`button[data-hash="${hash}"]`).replaceWith(
                    `<img src="${qrImageUrl}" alt="QR Code" width="100">`
                );
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
                    location.reload();
                },
                error: function () {
                    alert("Error al eliminar el enlace. Por favor, inténtalo de nuevo.");
                }
            });
        }
    }
});
