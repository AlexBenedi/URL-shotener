$(document).ready(
    function () {
        // Configuración del formulario para acortar URLs
        $("#shortener").submit(
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: $(this).serialize(),
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
