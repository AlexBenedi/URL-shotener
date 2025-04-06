![CI](https://github.com/juanguppy/fractallink/actions/workflows/ci.yml/badge.svg)

[![CodeScene general](https://codescene.io/images/analyzed-by-codescene-badge.svg)](https://codescene.io/projects/61628)
[![CodeScene Code Health](https://codescene.io/projects/61628/status-badges/code-health)](https://codescene.io/projects/61628)
[![CodeScene System Mastery](https://codescene.io/projects/61628/status-badges/system-mastery)](https://codescene.io/projects/61628)
<!-- [![CodeScene Missed Goals](https://codescene.io/projects/61628/status-badges/missed-goals)](https://codescene.io/projects/61628) badge not needed -->

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=bugs)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=fractallink_url-shortener-repositories&metric=coverage)](https://sonarcloud.io/summary/new_code?id=fractallink_url-shortener-repositories)

# URL Shortener project

This project is a URL Shortener similar to Bitly. It is a monolithic but asynchronous application. Kafka is used to process different requests in parallel.

A user system has been implemented, allowing users to register using Google and save a limited number of shortened links. Additionally, the system allows users to generate QR codes for the shortened URLs and create branded URLs to make them more memorable.

# Setup & execution instructions 

This project is using the following technologies: 
- Kotlin 2.0.20 as the main programming language. 
- Gradle 8.5 as the build system. 
- Spring Boot 3.3.3 as a framework that simplifies the development of web applications. It requires a Java version between 17 and 21.
- Docker compose compatible with 1.29.2

There is a script called `run.sh` for GNU/Linux & Unix and a script called `run.bat` for Windows. These scripts allow you to deploy a Kafka server on localhost:9092 (using Docker Compose) and to build (`gradlew build`) and run the project on localhost:8080 (`gradlew bootRun`).

You can also follow these steps to run the project by yourself:
1. Run `docker-compose up -d` to deploy the Kafka server.
2. Run `./gradlew build` to compile the code, run the tests, and package the artifacts. It also verifies the dependencies and ensures the project is production-ready. If the build fails, try running these commands two more times. Sometimes it needs to be run three times (never more) on a machine that has never built this project before, due to Kafka consumers' delay.
3. Run `./gradlew check` to execute all verifications and validations defined in the project. It also runs the tests.
4. Run `./gradlew bootRun` to deploy the web application at http://localhost:8080.

You can also run ```./gradlew test``` in order to execute the tests without building the project nor performing aditional verification steps. 


# Updates' Working Prototype
## Google Safe Browsing Integration 
### Description of the WP
This feature is designed to ensure the safety of links shortened using the application. It checks each link against Google's Safe Browsing API to determine if it is safe. If a link is found to be unsafe, the feature provides detailed information to the user about the nature of the threat, such as phishing, malware, or other malicious activities. This helps users make informed decisions and avoid potentially harmful websites. 

In this Working Prototipe, we have updated the functionality of the previous PoC in this way: 
- When a `createShortUrl` petition is made, `core/src/main/kotlin/es/unizar/urlshortener/core/usecases/CreateShortUrlUseCase`, using `core/src/main/kotlin/es/unizar/urlshortener/springbootkafkaexample/service/KafkaProducerService` publish a message with `topic=check-safty`. Then, it saves the ShortUrl in the database with `safe=null` and returns this ShortUrl. Controller just returns a `201`. 
- There exists a kafka consumer in `core/src/main/kotlin/es/unizar/urlshortener/springbootkafkaexample/service/KafkaConsumerService` which is subscribed to `check-safty` topic. It expects a `UrlSafetyPetition` encoded as a Json String. This Petition contains both the URL to be checked and its hash. This consumer calls `isUrlSafe` function, which implementation calls `gateway/src/main/kotlin/es/unizar/urlshortener/gateway/GoogleSafeBrowsingClient.kt`, which performs the safety verification calling Google Safe Browsing API. This function has been updated to return an `UrlSafetyCheckResponse`, which contains information about the safety of the URL (if it is safe or not, and if it is not, information about the threat -- phising, malware... --). 
- When kafka consumer receives its verification made by Google API, it publish a new message in `topic=safety-checked`. This message contains a `UrlSafetyChecked` encoded to Json String. This object contains both the URL hash and its `UrlSafetyCheckResponse` with the information. 
- There exists another kafka consumer in `core/src/main/kotlin/es/unizar/urlshortener/springbootkafkaexample/service/KafkaConsumerService` which is subscribed to `safety-checked` topic. It calls `core/src/main/kotlin/es/unizar/urlshortener/core/usecases/UpdateUrlSafetyUseCase` `updateShortUrl` function with the URL has and its safety information. 
- This UpdateUrlSafetyUseCase is a new use case which is made to update the safety information about an URL. Its metthod `updateShortUrl` expects a hash and a `UrlSafetyCheckResponse`.It attempts to update the `safe` field of the `ShortedLink` corresponding to the hash with the value from `UrlSafetyCheckResponse`. If successfull, it returns the new `ShortedUrl`. If not, it returns `null`. This behavior might sound inappropriate and somebody could think the best is to throw an exception. But it is appropiate since kafka is asyncronous and it can be faster than the first database call -- which stores the new shorted url -- and this could cause `updateShortUr` being call before the new link is saved, so an exception is innapropiate. If this happens, there is no problem, because kafka mantains its messages in the broker so the consumer is able to update the database when database is correctly populated with the new link. 
- When the user tries to access the shortened link, if its safety has been verified (as explained previously), it returns a `302` redirect if the URL is safe, and a `403` if it is not, along with relevant information explaining why it is not safe. If the safety check has not been completed, a `400` is returned to inform the user that the check is still pending.

We have also sanitized API Keys using `@Value` Spring annotation and environment variables. The machine on which the project is running should have our api keys in its environment. 

### Justification for the choice of any additional libraries or frameworks
We used Kafka to make the process asynchronous, meeting the scalability requirements of the proposal, and to learn and familiarize ourselves with Apache Kafka.

We also used Google's Gson library to encode Kotlin objects into JSON because we are using a String serializer in Kafka. We could have used a JSON serializer directly in kafka, but the process would have been similar, and Kafka's JSON is escaped (it adds quotes and escapes special JSON characters with backslashes).

### Challenged encountered and how they were addressed 
1. Sanitizing api keys: `@Value` was not working as expected and it wasn't retrieving the value of the environment variable. `System.getenv` was working properly. We address this by writing an email to our instructor and he said us the problems: 
    - Our `GoogleSafeBrowsingClientTest.kt` wasn't a Spring Boot test but a plain Kotlin test, so it couldn't inject the dependencies correctly. We updated it, but the integration test is still failing.
    - We were using a constructor to create the `GoogleSafeBrowsingClientTest` component instead of autowiring it. This prevented Spring from managing the dependencies and made it impossible to load the API key. We resolved this by injecting `GoogleSafeBrowsingClientTest` in `SafetyServiceImpl` and autowiring it in `ApplicationConfigurationUseCases`.

### Instructions to run the PoC
To run this PoC:
Deploy Kafka (`docker-compose up -d`) server, listening on localhost:9092. Then, build the project and run the tests (`./gradlew build`). Finally, run the project (`./gradlew bootRun`). If the build doesn't succeed, run it two more times. We don't know why Kafka consumers are not initializing properly, but they initialize after the third build.

All of this can be achieved by simply running `./run.sh` script, or `./run.bat` on Windows.

To manually check the effectiveness of this PoC, go to `http://localhost:8080` and try to shorten a safe URL (e.g., `http://www.unizar.es`). Everything will work as expected: the URL will be shortened. Then, go to its shortened link: The redirect will work (assuming unizar hasn't been compromised).

Next, try to shorten an unsafe URL (you can find many at `http://testsafebrowsing.appspot.com/`). Everything will work as expected: the URL will be shortened. Then, go to its shortened link: You'll see a JSON with information about the URL's unsafety.

If you are faster than Kafka, you'll see a message indicating that the URL safety hasn't been checked yet. I haven't managed to be faster than Kafka, so I haven't seen this myself, but the tests and the integration test corresponding to this use case pass (they are faster than kafka).
### Implemented Tests

This test is in  `gateway/src/test/kotlin/es/unizar/urlshortener/gateway/GoogleSafeBrowsingClientTest.kt` have been updated to manage with the ney return information. A new test has been made to check the information returned is the expected one. 

We have also updated `core/src/test/kotlin/es/unizar/urlshortener/core/CreateShortUrlUseCaseTest.kt` and `app/src/test/kotlin/es/unizar/urlshortener/app/IntegrationTests.kt` to handle the new asynchronous behavior. We have created new tests in `core/src/test/kotlin/es/unizar/urlshortener/core/UpdateShortUrlSafetyUseCaseTest.kt` to verify the new functionality: the URL is updated if it exists, and if it does not, it returns null.

## Apache Kafka
### Description of the WP
We use a `docker-compose.yml` file which creates a kafka broker server on `localhost:9092`. We have also created new package in `kafka/src/main/kotlin/es/unizar/urlshortener/springbootkafkaexample` following this tutorial: https://www.geeksforgeeks.org/spring-boot-integration-with-kafka/

This new package has two subpackages:
- `config`, which configures a Kafka Consumer and Kafka Producer, both using the previously created Kafka server (`localhost:9092`) and string encoders/decoders. The producer configuration also defines a KafkaTemplate, and the consumer configuration defines a consumer factory. This way, producers and consumers can be created.
- `service`, which defines the Kafka use cases. KafkaProducerService has one function: `sendMessage(topic, message)`, which sends the message `message` to the topic `topic`. KafkaConsumerService has one generic function - which for now does nothing but print the message to stdout - and two more functions related to safety checking. These functions are described in the Google Safe Browsing Integration section. Each of this function is subscribed to a different topic and manages the messages in a different way. 

### Justification for the choice of any additional libraries or frameworks
We used Kafka spring library because it is needed to integrate with kafka server in a easy way. 

We also used Google's Gson library to encode Kotlin objects into JSON because we are using a String serializer in Kafka. We could have used a JSON serializer directly in kafka, but the process would have been similar, and Kafka's JSON is escaped (it adds quotes and escapes special JSON characters with backslashes).

### Challenged encountered and how they were addressed 
1. Kafka consumers are not deployed until the project has been built tree times. We don't know why this is happening, but it can be fixing by building the project 3 times. We have tryed everything: Giving it more time to initialize, creating the topics using `kafka-topics` in the command line, but nothing has been successful. Finally we just decided to run the project 3 times. It is not necessary to build it 3 times always, but 3 times per machine. In a real environment this is not a problem because the build is done three times the first time, and the following builds will already work correctly. It must be something in the spring cache, but we have not been able to find out what it was and we think it is not important enough to spend more time on it. On the internet there is only information about race conditions, but this is not the case because it ALWAYS works on the third time, and from the following builds it doesn't fail again. 

### Instructions to run the PoC
To run this PoC:
Deploy Kafka (`docker-compose up -d`) server, listening on localhost:9092. Then, build the project and run the tests (`./gradlew build`). Finally, run the project (`./gradlew bootRun`). If the build doesn't succeed, run it two more times. We don't know why Kafka consumers are not initializing properly, but they initialize after the third build.

All of this can be achieved by simply running `./run.sh` script, or `./run.bat` on Windows.

You'll see in command line kafka logs messages. 
### Implemented Tests
We have created tests in `kafka/src/test/kotlin/es/unizar/urlshortener/springbootkafkaexample` in order to test both services. There is one test which tests the producer and another which test the consumer (and, therefore, a producer-consumer communication as to test the consumer somebody needs to publish a message in a topic). We use a Kafka Template as we are just testing the service. The whole kafka is tested in integration tests implicitly. 

##  Enable users to save their links
### Description of the WP

This feature enables user authentication and personalized link management within the application. Through Google OAuth integration, users can log in, create accounts, and manage their unique collections of links. The application maintains link-specific data, including click counts, target URLs, and  QR code. Each user can create, view, and delete links, with their link data stored securely in the database under a unique user identifier (sub), providing a straightforward way to track ownership and link-specific metrics.

This Working Prototipe delivers a fully operational system that satisfies all the basic requirements outlined for this feature. The prototype enables users to create an account, log in securely using Google OAuth, and manage their links efficiently. Users can save shortened links, which are stored in a relational database along with relevant metadata such as click count and the original URL. The system also allows users to retrieve their saved links, displaying details like the number of clicks, the target URL, and QR codes. Additionally, users can delete any saved link directly from their account.

- We updated the file `/core/src/main/kotlin/es/unizar/urlshortener/core/Domain.kt` to include additional attributes in the Click data class, specifically the clicks attribute, which tracks the number of times a link has been accessed. This enhancement is essential for providing users with detailed information about their saved links. Simultaneously, we updated `/repositories/src/main/kotlin/es/unizar/urlshortener/infrastructure/repositories/Entities.kt` to reflect this change in the database structure. The Click entity now includes a clicks field, ensuring that this information is stored persistently. We also updated the converters to handle the new clicks attribute during transformations between domain objects and entities, both for saving data to the database and retrieving it.

- We updated `/repositories/src/main/kotlin/es/unizar/urlshortener/infrastructure/repositories/Repositories.kt` to enhance the functionality of the User and Link repositories. New methods were added to interact with the database more effectively, including a function to delete a link by its ID, a function to count the total number of clicks associated with a specific link, and a function to retrieve a Click entity by its hash. These additions ensure comprehensive support for managing user data and link-related operations, providing the necessary tools for efficient database interaction.

- Additionally, we implemented the GetUserInformationUseCase in `core/src/main/kotlin/es/unizar/urlshortener/core/usecases/CreateShortUrlUseCase.kt`, which centralizes user-related functionalities. This use case first checks if a user already exists in the database; if not, it inserts a new user entry, ensuring proper registration before users can create links. It also allows users to insert a sample link, providing them with a quick way to test or demonstrate functionality within their accounts. Furthermore, the use case includes a method to retrieve all links associated with a specific user.

- We implemented the DeleteLinkUseCase in `core/src/main/kotlin/es/unizar/urlshortener/core/usecases/DeleteLinkUseCase.kt`, which centralizes the functionality for deleting links. This use case allows users to remove specific links from their accounts while ensuring that all associated data, such as clicks, is handled appropriately.

- Additionally, we modified the existing LogClickUseCase and CreateShortUrlUseCase in `core/src/main/kotlin/es/unizar/urlshortener/core/usecases` to align them with the requirements of this feature. The LogClickUseCase was updated to ensure that clicks are recorded in relation to specific users and their links, while the CreateShortUrlUseCase now includes logic to associate new shortened URLs with the correct user, further integrating user and link management into the system.

### Justification for the choice of any additional libraries or frameworks
No additional libraries or frameworks beyond those already present in the project have been added. We leveraged the existing dependencies to implement the required functionalities, ensuring consistency and minimizing unnecessary complexity in the codebase. This approach allowed us to build the feature efficiently while maintaining the simplicity and coherence of the project’s architecture.

### Challenged encountered and how they were addressed 
The main challenges we faced once again revolved around understanding the project’s architecture and correctly applying the necessary changes. Grasping how the various components interacted and ensuring our modifications aligned with the existing design required significant effort. This involved carefully analyzing the flow of data and the dependencies between different modules. To address these challenges, we revisited the project’s documentation and thoroughly reviewed the source code to deepen our understanding of its structure and logic. 

### Instructions to run the WP
To run the WP, you will need to open your terminal and navigate to the project's root directory. Once there, execute the command `./gradlew bootRun` to start the application. After the application is running, open your web browser and navigate to the application's UI. From there, click on the "Log In / Register" button to begin the authentication process. You will be prompted to log in or register, and once you complete the process, you can interact with the user interface to test the functionality, including link management and user registration. This approach allows you to verify the seamless integration of the core features through the UI.

### Implemented Tests
This functionality is related to the rest of the application's features. For this reason, the tests were not only based on users but also combined with the branded links functionality, redirection limit, and QR features. For this reason, new tests have been implemented in the file `UrlShortenerControllerTest.kt` to cover all of this.

Tests have been created for creating and deleting both branded and regular short URLs, as well as for QR generation and redirection limits. Additionally, edge cases have been tested.

The tests from the previous delivery (POC) have been left untouched. This provided a solid foundation, speeding up the development process.

## Redirection limit
The Redirection Limit feature is done to limit the amount of times a user can shorten a URL in a given time period. This feature is useful for preventing abuse of the URL shortening service by limiting the number of shortened URLs a user can create in a given time period. We can set the limit of the number of shortened URLs they can create in a given time period and also set the time period for which the limit is enforced. If the user exceeds the limit, they will receive an error message and will not be able to shorten any more URLs until the time period has elapsed.

### Implementation:
First of all, we have added a new field in the UserEntity to store the number of shortened URLs created by the user. This field is called `rediections` and is of type `Int`. We have also added a new field called `lastRedirectionTimeStamp` to store when the last shortened URL was created by the user. This field is of type `OffsetDateTime` and can be null if the user has not shortened an url yet.
For the logged users, in the post method, before creating the shortened url, we check how much time has passed from the last redirection, if it has been more than an hour, we restore the number of redirection of the user to 0. Then, we check if the user has reached the limit of redirections, if it has, we return an error message. If it has not, we increase the number of redirections of the user by one and we save the time of the redirection in the lastRedirectionTimeStamp field, and then we create the shortened url.
For the non-logged users, the problem was bigger, as we do not have them in our database. We have solved this problem my using a concurrent hashmap to store temporarily, the ip of the user and the number of redirections they have done. We have also stored the time of the last redirection of the user. We check as before with the logged-user but now using the hash map. 
The error message when the user has reached the limit is a 429 error, which is a too many requests error.

### Instruction to test the redirection limit:
First you have to run the app doing gradlew bootRun. Then you have to go to the browser and write localhost:8080. Once you are there, you can write an URL you want to short. If you submit, the shortUrl will be shown. If you try to shorten more than 6 urls in an hour, you will receive a 429 error. If you wait an hour, you will be able to shorten more urls.

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

##  Branded Links
### Description of the feature
By default, shortened URLs are generated using a hash of the input URL if the user is not registered, or a combination of the user and the input URL if they are. This is a good option for storing a large number of shortened URLs; however, due to the length of the hashes, they are not easy to remember. Branded links allow you to choose the name of the shortened URL, whether you are registered or not.

### Implementation
In the previously delivered POC, this functionality was almost entirely implemented, requiring only minor adjustments. The main task of this prototype was to integrate it with the rest of the functionalities without it being a major challenge or requiring significant code modifications.

Branded links were implemented from the beginning as if they were regular links. In other words, the creation process for a branded link is exactly the same as for a regular link, except that instead of generating a hash, a custom name is provided. For this reason, integrating it with the other functionalities has not been complex.

To integrate it with the rest of the functionalities, it was only necessary to modify the HTML or frontend to pass all the parameters required to create the link. To create a branded link, the HTTP request body must include name (the desired name) and isBranded (a boolean indicating whether it is branded), regardless of whether the user is registered. Additionally, slight adjustments were made to the web interface. 

These modifications can be observed in the following files: index.html and app.js (without registration), and user.html and delivery.js (with registration).

### Instructions to run the WP

To run the WP, you will need to open your terminal and navigate to the project's root directory. Once there, execute the command `./gradlew bootRun` to start the application. After the application is running, open your web browser and navigate to the application's UI.

In the interface, there is a checkbox with the following text: "Is Branded?". If you check this box, a text field will appear where you can enter the name of the branded link. After that, by clicking the shorten button, the branded link will be created.

### Implemented Tests

For this prototype, no specific tests have been created for this functionality. The tests created have mainly focused on verifying whether the rest of the functionalities correctly integrate with this one. The tests created can be found in the file UrlShortenerControllerTest.kt.

The tests created in the POC have been kept intact, as they serve as a foundation to ensure the proper functioning of this functionality.

