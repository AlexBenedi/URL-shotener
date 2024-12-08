$(document).ready(
    function () {
        // Variable global para almacenar la IP
        var userIp = "";

        // Obtener la IP del usuario
        $.ajax({
            type: "GET",
            url: "/api/ip",
            success: function (ip) {
                userIp = ip; // Guardar la IP en una variable global
                console.log("User IP:", userIp);
                // Establecer la conexión WebSocket con el userIp como identificador
                socket = new WebSocket("ws://" + window.location.host + "/ws-endpoint?userId=" + userIp);

                // Eventos del WebSocket
                socket.onopen = function () {
                    console.log("WebSocket conectado para el usuario: " + userId);
                };

                // Cuando se recibe un mensaje del servidor
                socket.onmessage = function (event) {
                    var qrCode = event.data; // El contenido del mensaje recibido (el QR generado)
                    console.log("QR recibido:", qrCode);

                    // Mostrar el QR en la página
                    var resultDiv = $("#result");
                    resultDiv.append('<img src="data:image/png;base64,' + qrCode + '" alt="QR Code">');

                };

                // Manejar el cierre de la conexión WebSocket
                socket.onclose = function () {
                    console.log("Conexión WebSocket cerrada");
                };

                // Manejar errores en la conexión WebSocket
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
                $("#brandedNameGroup").show(); // Muestra el campo de texto
            } else {
                $("#brandedNameGroup").hide(); // Oculta el campo de texto y lo limpia
                $("#brandedName").val(''); // Borra el contenido
            }
        });

        // Configuración del formulario para acortar URLs
        $("#shortener").submit(
            function (event) {
                event.preventDefault();
                var formData = $(this).serialize();

                // Verifica si se debe agregar el campo "isBranded" y "brandedName"
                if ($("#brandedCheckbox").prop('checked')) {
                    formData += "&isBranded=true&name=" + encodeURIComponent($("#brandedName").val());
                }

                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: formData,
                    success: function (msg, status, request) {
                        var resultDiv = $("#result");
                        var shortenedUrl = request.getResponseHeader('Location');
                        resultDiv.html(
                            "<div class='alert alert-success lead'><a target='_blank' href='"
                            + shortenedUrl
                            + "'>"
                            + shortenedUrl
                            + "</a></div>"
                        );

                        console.log("QR generado:", msg.qrCodeGenerated);
                        if (msg.qrCodeGenerated === true) {
                            resultDiv.append('<p>QR Code:</p>');
                            var qrCodeDownloadUrl = "/" + shortenedUrl.split('/').pop() + "/qr";
                            resultDiv.append('<p><a href="' + qrCodeDownloadUrl + '" download="qrcode.png">Download QR Code</a></p>');
                            resultDiv.append('<p>' + window.location.origin + qrCodeDownloadUrl + '</p>');
                        }
                    },
                    error: function () {
                        $("#result").html("<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });

        // Configuración del botón para ir a /user
        $("#userButton").click(function () {
            window.location.href = "/user"; // Redirige al endpoint /user
        });
    }
);
