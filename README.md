![CI](https://github.com/juanguppy/fractallink/actions/workflows/ci.yml/badge.svg)
# URL Shortener project by FractalLink

This repository is a modification of the `https://github.com/UNIZAR-30246-WebEngineering/urlshortener` project. Read its README to understand its architecture, requirements, and main functionalities.
# Setup & execution instructions 

This project is using the following technologies: 
- Kotlin 2.0.20 as the main programming language. 
- Gradle 8.5 as the build system. 
- Spring Boot 3.3.3 as a framework that simplifies the development of web applications. It requires a Java version between 17 and 21.

As Gradle is used as the build tool, to build, test & deploy the application you must follow these steps: 
1. Run ```./gradlew build``` to compile the code, run the tests and pack the artifacts. It also verify the dependencies and secures the project is production-ready. 
2. Run ```./gradlew check``` in order to execute all verifications and validations defined in the project. It also runs the tests.
3. Run ```./gradlew bootrun``` to deploy the web application at http://localhost:8080. 

You can also run ```./gradlew test``` in order to execute the tests without building the project nor performing aditional verification steps. 

# Updates' Proof of Concept

## Google Safe Browsing Integration 
### Description of the PoC
This feature is designed to ensure the safety of links shortened using the application. It checks each link against Google's Safe Browsing API to determine if it is safe. If a link is found to be unsafe, the feature provides detailed information to the user about the nature of the threat, such as phishing, malware, or other malicious activities. This helps users make informed decisions and avoid potentially harmful websites. 

In this Proof of Concept, we integrate with the Google Safe Browsing API to ensure the safety of URLs. It only verifies if the URL is safe or unsafe (boolean), but does not provide additional information about why is unsafe. This is done in `gateway/src/main/kotlin/es/unizar/urlshortener/gateway/GoogleSafeBrowsingClient.kt`. We define the use case for checking URLs against the API and create unit tests to validate the integration and functionality, with a good URL and a malicious one. This is done in `gateway/src/test/kotlin/es/unizar/urlshortener/gateway/GoogleSafeBrowsingClientTest.kt`. 

We have modified `core/src/main/kotlin/es/unizar/urlshortener/core/Ports.kt` to add the `SafetyService` port, added its implementation in `delivery/src/main/kotlin/es/unizar/urlshortener/infrastructure/delivery/PortsImpl.kt`, defined an `UnsafeUrlException` in `core/src/main/kotlin/es/unizar/urlshortener/core/Exceptions.kt`, and updated `core/src/main/kotlin/es/unizar/urlshortener/core/usecases/CreateShortUrlUseCase.kt` to verify if a URL is safe or not (using `SafetyService`). If it isn't, the exception is thrown and a warning is printed in the terminal, and then a handler returns `403 FORBIDDEN` HTTP code. 

In the future, it won't work like this: The use case will return `201` and then post a message to a broker, setting the `safe` URL property to `null`. The `SafetyService` will then read the message and verify if the URL is safe or not. If someone tries to access the URL during verification, the URL property will be `null` and a `400` error will be returned. When the service finishes its verification, it will update the URL property: if it is safe, it will set it to `true`; otherwise, it will set it to `false`. If it is not safe, an additional property `Information` will be populated with the reason why it is not safe. Whenever a user tries to access the URL, a `403 FORBIDDEN` error will be returned with the information about why it is not safe. This is not difficult to implement: instead of throwing the exception when creating the URL, we just create the URL, set `safe` to `null`, and put the message in the Kafka broker. We need two Kafka listeners: one waiting for incoming URL verifications to launch `SafetyService`, and another waiting for incoming safety updates which modify `url.properties.safe` and `url.properties.information`.

To integrate with Google Safe Browsing API, we have followed this guide: https://developers.google.com/safe-browsing/v4/get-started?hl=es-419 

### Justification for the choice of any additional libraries or frameworks
This feature has been implemented without the use of any additional libraries or frameworks. It operates on a simple request/response model with Google's API. Since it integrates with other services, the integration class has been placed in the "gateway" module.

Our API key has been added directly in the code, which is not a recommended practice as it can be stolen. To ensure its security, in a real scenario, it should be added as an environment variable on the hosts used to implement, test and deploy this functionality. For CI tests to pass, it should also be added to GitHub Secrets. However, to make it easier for the instructor to review our project, we have not done this.

### Challenged encountered and how they were addressed 
The first challenge we faced was adding the new module 'gateway' to the modules that use it. It was very easy: we just needed to import it in the module's `settings.gradle.kts` file by adding the following line: `implementation(project(":gateway"))`. We addressed it by looking at other `settings.gradle.kts` files.

Another challenge we faced was injecting `restTemplate` into `GoogleSafeBrowsingAPIClient` without violating Clean Architecture principles. Since this is just a PoC, we decided to build it inside the class if it is not provided. We are studying how to inject it in a better way.

### Instructions to run the PoC
To run this PoC, project should be built and passing the tests, essencialy `gateway/src/test/kotlin/es/unizar/urlshortener/gateway/GoogleSafeBrowsingClientTest.kt`, `creates returns unsafe URL exception if the URL is not safe` on `core/src/test/kotlin/es/unizar/urlshortener/core/CreateShortUrlUseCaseTest.kt` and `creates returns forbidden if the URL is not safe` on `app/src/test/kotlin/es/unizar/urlshortener/app/IntegrationTests.kt`. This can be achieved running `./gradlew build` (or `gradlew.bat build` on Windows) command. 

To manually check this PoC's effectiveness, go to `http://localhost:8080` and try to shorten a safe URL (e.g., `http://www.unizar.es`). Everything will work as expected: the URL will be shortened. Then, try to shorten an unsafe one (you can find many at `http://testsafebrowsing.appspot.com/`). An ERROR will be returned, and if you check the server response, you'll see a 403 FORBIDDEN. In the terminal, "URL is not safe" will be printed.

### Implemented Tests

The tests validate the functionality by invoking the `isSafe()` function of the `GoogleSafeBrowsingClient`, which connects to the Google Safe Browsing API. The results are then compared with the expected outcomes. We use two test cases: a non-malicious website (www.unizar.es) and a malicious website (taken from an online database of malicious websites, https://testsafebrowsing.appspot.com/). This ensures that our implementation correctly identifies safe and unsafe URLs. This test is in  `gateway/src/test/kotlin/es/unizar/urlshortener/gateway/GoogleSafeBrowsingClientTest.kt` 

We have also updated `core/src/test/kotlin/es/unizar/urlshortener/core/CreateShortUrlUseCaseTest.kt` with a new test `creates returns unsafe URL exception if the URL is not safe`, which verifies that `UnsafeUrlException` is thrown if the URL is not safe. Additionally, we updated `app/src/test/kotlin/es/unizar/urlshortener/app/IntegrationTests.kt` with a test `creates returns forbidden if the URL is not safe`, which checks that a 403 status code is returned if the URL is not safe.

## Enable users to save their links
### Description of the PoC
This feature enables user authentication and personalized link management within the application. Through Google OAuth integration, users can log in, create accounts, and manage their unique collections of links. The application maintains link-specific data, including click counts, target URLs, and optional QR code generation. Each user can create, view, and delete links, with their link data stored securely in the database under a unique user identifier (sub), providing a straightforward way to track ownership and link-specific metrics.

This Proof of Concept (PoC) introduces a local database system that enables structured data storage for user accounts and link management. Key components include the creation of database tables, data classes for structured data representation, and a basic use case for inserting new users and links. The system is designed to simplify future expansion, making it straightforward to add functionalities for more complex user and link operations.

We modified the file to add the new data classes User and Link, which are essential for managing user information and their links, this is done in `/core/src/main/kotlin/es/unizar/urlshortener/core/Domain.kt`. Afterward, we updated the entity file to enable the storage of a User entity and a Link entity in the database. A Link contains the Click, the ShortUrl, and the user it belongs to. I also modified the converters for transforming between domain objects and entities, both ways. You can find the updates in the following path: `/repositories/src/main/kotlin/es/unizar/urlshortener/infrastructure/repositories/Entities.kt`.

We created the repositories in `/repositories/src/main/kotlin/es/unizar/urlshortener/infrastructure/repositories/Repositories.kt` to manage the persistence of User and Link entities in the database. These repositories facilitate essential operations, including saving new entries, inserting data, and retrieving records by their IDs, ensuring efficient interaction with the underlying database.

Additionally, we implemented the GetUserInformationUseCase in `core/src/main/kotlin/es/unizar/urlshortener/core/usecases/CreateShortUrlUseCase.kt`, which centralizes user-related functionalities. This use case first checks if a user already exists in the database; if not, it inserts a new user entry, ensuring proper registration before users can create links. It also allows users to insert a sample link, providing them with a quick way to test or demonstrate functionality within their accounts. Furthermore, the use case includes a method to retrieve all links associated with a specific user.

Finally we implemented a security configuration bean that manages access controls for Google OAuth authentication. This bean, done in `/app/src/main/kotlin/es/unizar/urlshortener/ApplicationConfiguration.kt`, is responsible for configuring the security filter chain to specify how authentication and authorization should be handled within the application.

In future updates, we'll move integration logic into `gateway` module and authentication logic into `core/UseCases`, insted of doing it in the same function. We did it because it was the faster way to make the proof of concept and test the feature's scope. 

### Justification for the choice of any additional libraries or frameworks
We chose the spring-boot-starter-oauth2-client to simplify the implementation of OAuth2 and OpenID Connect authentication in our application. This library provides a comprehensive and easy-to-use framework for integrating third-party authentication providers, such as Google, enabling users to log in using their existing accounts. By leveraging established OAuth2 flows, we enhance the user experience by streamlining the authentication process and reducing friction during login. Additionally, this library handles the complexities of token management and user session handling, allowing us to focus on building application features rather than dealing with the intricacies of authentication protocols. 

### Challenged encountered and how they were addressed 
The main challenges we encountered revolved around understanding the project's structure and familiarizing ourselves with Kotlin's features. Navigating the architecture of the project required time and effort, as it involved grasping how different components interacted and the overall flow of data. To address this, we dedicated time to thoroughly review the project's documentation and source code, which provided valuable insights into its design principles. Additionally, we engaged with Kotlin's language features, which were somewhat different from those of other languages we had previously used. By utilizing online resources, tutorials, and community forums, we gradually built our understanding and proficiency in Kotlin, enabling us to effectively contribute to the project. 

### Instructions to run the PoC
To run the Proof of Concept (PoC), you will need to open your terminal and navigate to the project's root directory. Once there, execute the command `./gradlew bootRun` to start the application. After the application is running, open your web browser and navigate to `http://localhost:8080/user`. You will then be prompted to authenticate using your Google account. Upon successful authentication, you will be able to view user data along with a link that is automatically added when a user registers. This process not only showcases the application's core functionality but also demonstrates how user registration and link management work seamlessly together.

### Implemented Tests
In our development process, we have implemented an integration test, done in `app/src/test/kotlin/es/unizar/urlshortener/IntegrationTests.kt` that validates the requirement for authentication when accessing the `/user` route. This test ensures that only authenticated users can gain access, thereby reinforcing the security measures we have put in place. Additionally, we have developed unit tests to thoroughly check the functionality of the GetUserInformationUseCase in `core/src/test/kotlin/es/unizar/urlshortener/core/usecases/GetUserInformationUseCaseTest.kt`. These unit tests verify that the use case behaves as expected under various conditions, ensuring the reliability and correctness of the user management features within the application. 

## QR
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

## Redirection limit
Description: Enforce a limit on the number of URLs that can be shortened by both logged-in and non-logged-in users to avoid misuse of the service.

Implementation:
    For checking how many URLs a user has shortened, we can maintain a field on the table of each user which has the count if the number of ShortenedUrl in the last hour. When a user tries to shorten a URL, we can quickly check the table to see if the user has reached their limit. If the user has not reached the limit, we can increment the count and allow the user to shorten the URL. If the user has reached the limit, we can reject the request.
    When we call the method "create" in the CreateShortUrlUseCase.kt, firstly we do the check if the user has reached the limit. If the user has reached the limit, we return an error message. Otherwise, we increment the count and create the shortened URL.
    For checking that field in the database, we will have to use a query declared in the Repository.
    We will have to create a function which clear that field every hour for every user. We can use a cron job for that. 

## Branded Links
Allow for the creation and management of custom, branded links.

### Implementation
1. ***New atribute in ShortUrlEntity***: ShortUrlEntity has been modified to introduce a new atribute: `isBranded`. This new atribute allows distinguishing between a normal link and a branded link directly in the database.

2. ***New properties in the body of the POST request***: The properties `isBranded` and `name` have been introduced as optional in the request. `isBranded` allows distinguishing between a normal and a branded link, while `name` indicates the name that the branded link will use.

3. ***New excepctions***: `InvalidNameBrandedUrl` has been added to handle an excepcional case dureing the creation of branded links when atrubute `name` is empty. This exception has been integrated into `ExceptionHandler`to inform the client of the error.

4. ***Modification UrlShortenerController***: The implementation of the `create` function at `UrlShortenerController`class has been slightly modified. The `ìsBranded` attribute of `data` distinguish between a branded link and a normal link. If it is a branded Link  `name` will be use to create the link and it would be saved as a normal link.

### Implemented Tests

New test have been implemented to ensure the correct functioning of the branded links. These have been introduced in two differents files: `UrlShortenerController.kt`and `CreateShortUrlUseCaseTest.kt`. In both files, both correct and incorrect creation of a branded link have been tested, along with other rare cases.

### Future 

In the future, `index.html` and `app.js` will be modified to enable users to introduce the branded links in a more user-friendly way. Basic back-end has been introduced for this POC but the front-end part has not been implemented yet. 

# Work distribution
We have distributed the work equally. Sergio Garcés has been in charge of the QR PoC and Redirection Limits, Alejandro Benedí has been in charge of the branded links, Kamal Bouzi for authentication and user creation and Juan Almodóvar about checking whether a link is safe or not by integrating with the Google Safe Browsing API. We also had a meeting to decide how to divide up the work and decide everyone's roles.


