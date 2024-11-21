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
