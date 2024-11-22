$(document).ready(
    function () {
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

                        if (msg.qrCode) {
                            resultDiv.append('<p>QR Code:</p>');
                            resultDiv.append('<img src="data:image/png;base64,' + msg.qrCode + '" alt="QR Code">');
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
