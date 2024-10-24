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
   New class QRCode to represent the QR code data which will be used after in the response.
        Reason: This class represents the QR code data, including the Base64-encoded image, the URL associated with the QR code and the size of it. It is used to store the QR code data before sending it to the client.
2. New Use Case: GenerateQRCodeUseCase.kt
        In this use case, we have the implementation of the method which generates a QR code for a given URL. It accepts a URL and a size parameter, encodes the URL into a QR code format, converts the QR code into a PNG byte stream, and encodes it into a Base64 string for easy use in web applications.

3. In the UrlShortenerController.kt:
   We add directly instantiate the QR Code use case implementation and then, in the post method which create the short URL, we call the generateQRCode method from the use case and store the QR code data in the database.