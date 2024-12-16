$(document).ready(function () {
    var userIp = "";

    // Obtener la IP del usuario
    $.ajax({
        type: "GET",
        url: "/api/ip",
        success: function (ip) {
            userIp = ip;
            console.log("User IP:", userIp);

            // Establecer la conexión WebSocket con el userIp como identificador
            const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
            const wsUrl = `${protocol}${window.location.host}/ws-endpoint?userId=${encodeURIComponent(userIp)}`;

            // Crear conexión WebSocket
            socket = new WebSocket(wsUrl);

            socket.onopen = function () {
                console.log("WebSocket conectado para el usuario: " + userIp);
            };

            socket.onmessage = function (event) {
                const data = JSON.parse(event.data);
                console.log("Data from QR", data);

                const qrCode = data.second;
                const id = data.first;

                // Mostrar el QR en la sección específica
                const qrDiv = $("#qrSection");
                qrDiv.html('<img src="data:image/png;base64,' + qrCode + '" alt="QR Code">');
                //var qrCodeDownloadUrl = "/qr/" + id;
                //qrDiv.append('<p><a href="' + qrCodeDownloadUrl + '" download="qr.png">Descargar QR</a><p>');
                //qrDiv.append('<p><a href="' + window.location.origin + qrCodeDownloadUrl + '" target="_blank">' + window.location.origin + qrCodeDownloadUrl + '</a></p>');

            };

            socket.onclose = function () {
                console.log("Conexión WebSocket cerrada");
            };

            socket.onerror = function (error) {
                console.error("Error en la conexión WebSocket:", error);
            };
        },
        error: function () {
            console.error("No se pudo obtener la IP del usuario.");
        }
    });

    // Muestra/oculta el campo de texto para el nombre de la marca
    $("#brandedCheckbox").change(function () {
        if ($(this).prop('checked')) {
            $("#brandedNameGroup").show();
        } else {
            $("#brandedNameGroup").hide();
            $("#brandedName").val('');
        }
    });

    // Configuración del formulario para acortar URLs
    $("#shortener").submit(function (event) {
        event.preventDefault();
        let formData = $(this).serialize();

        if ($("#brandedCheckbox").prop('checked')) {
            formData += "&isBranded=true&name=" + encodeURIComponent($("#brandedName").val());
        }

        $.ajax({
            type: "POST",
            url: "/api/link",
            data: formData,
            success: function (msg, status, request) {
                const resultDiv = $("#result");
                const shortenedUrl = request.getResponseHeader('Location');
                resultDiv.html(
                    "<div class='alert alert-success lead'><a target='_blank' href='"
                    + shortenedUrl
                    + "'>"
                    + shortenedUrl
                    + "</a></div>"
                );
                // Display the QR code if it exists
                if (msg.qrCodeGenerated === true) {
                    resultDiv.append('<p>QR Code:</p>');
                    resultDiv.append('<p><a href="' + msg.urlQR + '" download="qrcode.png">Download QR Code</a></p>');
                    resultDiv.append('<p><a href="' + msg.urlQR + '" target="_blank">' + msg.urlQR + '</a></p>');
                }
            },
            error: function () {
                $("#result").html("<div class='alert alert-danger lead'>ERROR</div>");
            }
        });
    });

    // Configuración del botón para ir a /user
    $("#userButton").click(function () {
        window.location.href = "/user";
    });
});
