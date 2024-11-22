## QR Code Feature
This feature is about generating a QR code for the shortened URL. The QR code is generated using the `zxing` library, which is a popular open-source library for generating QR codes. The QR code is generated from the shortened URL and is stored in the database along with the shortened URL. The QR code is then returned to the client along with the shortened URL. This feature must be optional, so the user can choose whether to generate a QR code for the shortened URL or not. In addition, the user will be able to download the QR code as a PNG image file, by clicking the link, or copy and pasting it in the browser. That link will alow the user to share it so others can download the QR code.

### Implementation:
First of all, we need how to save that QR code in the database. We have added a new field in the ShortUrlEntity to store the qrCode as a string, but we have had to specify that the field will have a length of 65535, because the string generated of the qrCode is really long.
Once we know where to save, first of all we have done a method to generate it. This method is done in the GenerateQRCodeUseCase.kt called genereateQRCode. In this use case, we have the implementation of the method which generates a QR code for a given URL. It accepts a URL and a size parameter, encodes the URL into a QR code format, converts the QR code into a PNG byte stream, and encodes it into a Base64 string for easy use in web applications.
That function is called in the UrlShortenerController.kt, in both post methods which generate the short url, depending on if you are a logged user or not. In both cases, the method recieves if the user wants to generate the QR code or not. If the user wants, the methods generateQRCode is called and stores the qrCode it return in a variable. In the case of the non-logged users, the qr is stored directly in the shortUrlEntity. In the case of the logged users, the qr is saves in the ShortUrlEntity by saving the link in the link Entity, which is joined with the shortUrlEntity.
The QR is shown to the user by appending the QR code of the response into the html.

For making able to the users to download the QR image, it was necessary to do a get method, which is going to find by hash the shortUrlEntity and then, it is going to return the qrCode as a png image. When the user clicks the link given to download the QR, that GET method is going to be called and the image is going to be downloaded.

### Library used to generate the QR code:
1. The QR code is generated using the `QRCodeWriter` class from the `zxing` library: https://github.com/zxing/zxing/tree/master/core/src/main/java/com/google/zxing/qrcode
2. With the encode method, the QR code is generated from the shortened URL to a `BitMatrix` object. https://github.com/zxing/zxing/blob/master/core/src/main/java/com/google/zxing/qrcode/QRCodeWriter.java
3. The `BitMatrix` object is then converted to a `BufferedImage` object using the `MatrixToImageWriter` class. https://github.com/zxing/zxing/blob/master/javase/src/main/java/com/google/zxing/client/j2se/MatrixToImageWriter.java

This library was chosen because it is a popular open-source library for generating QR codes and it is easy to use. It is also well-documented and has a large community of users.

### Instruction to create a qr and download it:
First you have to run the app doing gradlew bootRun. Then you have to go to the browser and write localhost:8080. Once you are there, you can write an URL you want to short and check the box of wanting a qr. If you submit, the shortUrl will be shown, along with the image of the QR, a clickable link to download the QR, and a link to copy if you want to share it for other people being able to download the QR image too.
