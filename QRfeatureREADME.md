Feauture: QR
• Points: 5
• Description: Generate a QR code for any shortened URL, offering an alternative access method.
• Validation:
    – Correctness: Ensure a QR code is generated given a URL, and stored in the  database associated with the link.
    – Scalability: Run stress tests under increased load in order to check if the system  is able to generate the QR at high-load levels.
    – Professionalism: Code is well-structured and documented.
This API generates a QR code for a given shortened URL, providing an alternative method of access. The generated QR code must contain the shortened URL.

How te QR is going to be generated:
1. The QR code is generated using the `QRCodeWriter` class from the `zxing` library: https://github.com/zxing/zxing/tree/master/core/src/main/java/com/google/zxing/qrcode
2. With the encode method, the QR code is generated from the shortened URL to a `BitMatrix` object. https://github.com/zxing/zxing/blob/master/core/src/main/java/com/google/zxing/qrcode/QRCodeWriter.java
3. The `BitMatrix` object is then converted to a `BufferedImage` object using the `MatrixToImageWriter` class. https://github.com/zxing/zxing/blob/master/javase/src/main/java/com/google/zxing/client/j2se/MatrixToImageWriter.java

Functionality:
1. In the Domain.kt:
   New Method for QR Code Generation:
        Functionality: A new private function named generateQRCode(data: String, outputFilePath: String). 
        Reason: This method encapsulates the logic for creating QR codes from a given URL, making it reusable throughout the project.
        Location: Inside the UrlShortenerControllerImpl class, as it directly relates to the functionality of generating a short URL and providing a QR code for it.

2. In the UrlShortenerController.kt:
   New Method for QR Code Generation: private fun generateQRCode(url: String, size: Int = 250): String
        Reason: This method generates a QR code as a Base64-encoded string. It accepts a URL and a size parameter, encodes the URL into a QR code format, converts the QR code into a PNG byte stream, and encodes it into a Base64 string for easy use in web applications
   
   Modification in the shortener Method: val qrCodeBase64 = generateQRCode(url.toString())
        Reason: After creating a short URL, this line calls the generateQRCode method to create a QR code for the newly generated URL. The resulting Base64 string is then included in the response.

   Including QR Code in the Response: qrCode" to qrCodeBase64
        Reason: The QR code Base64 string is added to the ShortUrlDataOut response object. This allows the client to receive the QR code along with the short URL.