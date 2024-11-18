$(document).ready(
    /**
     * Initializes the document and sets up the form submission handler.
     */
    function () {
        $("#shortener").submit(
            /**
             * Handles the form submission event.
             * Prevents the default form submission and sends an AJAX POST request.
             * @param {Event} event - The form submission event.
             */
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: $(this).serialize(),
                    /**
                     * Handles the successful AJAX response.
                     * Displays the shortened URL and QR code (if present) in the result div.
                     * @param {Object} msg - The response message.
                     * @param {string} status - The status of the response.
                     * @param {Object} request - The XMLHttpRequest object.
                     */
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

                        // Display the QR code image if it exists
                        if (msg.qrCode) {
                            resultDiv.append('<p>QR Code:</p>');
                            resultDiv.append('<img src="data:image/png;base64,' + msg.qrCode + '" alt="QR Code">');

                            // Create a download link for the QR code image
                            // The URL for downloading will be in the form /{id}/qr
                            resultDiv.append('<p>QR Code link to download the QR:</p>');
                            var qrCodeDownloadUrl = "/" + shortenedUrl.split('/').pop() + "/qr";
                            resultDiv.append(
                                '<p><a href="' + qrCodeDownloadUrl + '" download="qrcode.png">Download QR Code</a></p>'
                            );
                            // Show the URL purely to download the QR code
                            resultDiv.append('<p>' + window.location.origin + qrCodeDownloadUrl + '</p>');
                        }
                    },
                    /**
                     * Handles the AJAX error response.
                     * Displays an error message in the result div.
                     */
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });
    });