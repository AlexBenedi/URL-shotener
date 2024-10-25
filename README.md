# Getting Started with the URL Shortener project

2024-08-31

## System requirements

This application leverages cutting-edge technologies to deliver a robust
and versatile user experience:

1.  **Programming Language**: The application is written in [Kotlin
    2.0.20](https://kotlinlang.org/), a versatile, open-source,
    statically-typed language. Kotlin is renowned for its adaptability
    and is commonly used for Android mobile app development. Beyond
    that, it finds application in server-side development, making it a
    versatile choice.

2.  **Build System**: The application utilizes [Gradle
    8.5](https://gradle.org/) as its build system. Gradle is renowned
    for its flexibility in automating the software building process.
    This build automation tool streamlines tasks such as compiling,
    linking, and packaging code, ensuring consistency and reliability
    throughout development.

3.  **Framework**: The application employs [Spring Boot
    3.3.3](https://docs.spring.io/spring-boot/) as a framework. This
    technology requires Java 17 and is fully compatible up to and
    including Java 21. Spring Boot simplifies the creation of
    production-grade [Spring-based applications](https://spring.io/). It
    adopts a highly opinionated approach to the Spring platform and
    third-party libraries, enabling developers to initiate projects with
    minimal hassle.

## Overall structure

The structure of this project is heavily influenced by [the clean
architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html):

- A `core` module where we define the domain entities and the
  functionalities (also known as use cases, business rules, etc.). They
  do not know that this application has a web interface or that data is
  stored in relational databases.
- A `repositories` module that knows how to store domain entities in a
  relational database.
- A `delivery` module that knows how to expose the functionalities on
  the web.
- An `app` module that contains the main application, the configuration
  (i.e., it links `core`, `delivery`, and `repositories`), and the
  static assets (i.e., HTML files, JavaScript files, etc.).

```mermaid
flowchart LR;
    User-- HTTP -->Tomcat("Embedded<br>Web Server<br><b>Apache Tomcat")
    subgraph "Application <b>UrlShortener</b>"
        Tomcat== "Dynamic<br>resources" ==>Delivery("Module<br><b>delivery</b>")
        Tomcat== "Static<br>resources" ==>App("Module<br><b>app</b>")
        Tomcat~~~App("Module<br><b>app</b>")
        App-. configure .->Tomcat
        App-. configure .->Delivery
        App-. configure .->Core
        App-. configure .->Repositories
        subgraph Core [Module<br><b>core</b>]
            PortA("Port")==>UseCases("Use<br>Cases")
            UseCases==>PortB("Port")
        end
        PortB==>Repositories("Module<br><b>repositories</b>")
        Delivery==>PortA
    end
    Repositories-- JDBC -->Database[(Database)]
```

Usually, if you plan to add a new feature:

- You will add a new use case to the `core` module.
- If required, you will modify the persistence model in the
  `repositories` module.
- You will implement a web-oriented solution to expose it to clients in
  the `delivery` module.

Sometimes, your feature will not be as simple, and it may require:

- Connecting to a third party (e.g., an external server). In this case,
  you will add a new module named `gateway` responsible for such a task.
- An additional application. In this case, you can create a new
  application module (e.g., `app2`) with the appropriate configuration
  to run this second server.

Features that require connecting to a third party or having more than a
single app will be rewarded.

## Run

The application can be run as follows:

``` bash
./gradlew bootRun
```

Now you have a shortener service running at port 8080. You can test that
it works as follows:

``` bash
$ curl -v -d "url=http://www.unizar.es/" http://localhost:8080/api/link
*   Trying ::1:8080...
* Connected to localhost (::1) port 8080 (#0)
> POST /api/link HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.71.1
> Accept: */*
> Content-Length: 25
> Content-Type: application/x-www-form-urlencoded
> 
* upload completely sent off: 25 out of 25 bytes
* Mark bundle as not supporting multiuse
< HTTP/1.1 201 
< Location: http://localhost:8080/tiny-6bb9db44
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 28 Sep 2021 17:06:01 GMT
< 
* Connection #0 to host localhost left intact
{"url":"http://localhost:8080/tiny-6bb9db44","properties":{"safe":true}}%   
```

And now, we can navigate to the shortened URL.

``` bash
$ curl -v http://localhost:8080/6bb9db44
*   Trying ::1:8080...
* Connected to localhost (::1) port 8080 (#0)
> GET /tiny-6bb9db44 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.71.1
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 307 
< Location: http://www.unizar.es/
< Content-Length: 0
< Date: Tue, 28 Sep 2021 17:07:34 GMT
< 
* Connection #0 to host localhost left intact
```

## Build and Run

The uberjar can be built and then run with:

``` bash
./gradlew build
java -jar app/build/libs/app-0.2024.1-SNAPSHOT.jar
```

## Functionalities

The project offers a minimum set of functionalities:

- **Create a short URL**. See in `core` the use case
  `CreateShortUrlUseCase` and in `delivery` the REST controller
  `UrlShortenerController`.

- **Redirect to a URL**. See in `core` the use case `RedirectUseCase`
  and in `delivery` the REST controller `UrlShortenerController`.

- **Log redirects**. See in `core` the use case `LogClickUseCase` and in
  `delivery` the REST controller `UrlShortenerController`.

The objects in the domain are:

- `ShortUrl`: the minimum information about a short URL
- `Redirection`: the remote URI and the redirection mode
- `ShortUrlProperties`: a handy way to extend data about a short URL
- `Click`: the minimum data captured when a redirection is logged
- `ClickProperties`: a handy way to extend data about a click

## Delivery

The above functionality is available through a simple API:

- `POST /api/link` which creates a short URL from data send by a form.
- `GET /{id}` where `{id}` identifies the short URL, deals with
  redirects, and logs use (i.e. clicks).

In addition, `GET /` returns the landing page of the system.

## Repositories

All the data is stored in a relational database. There are only two
tables.

- **shorturl** that represents short URLs and encodes in each row
  `ShortUrl` related data,
- **click** that represents clicks and encodes in each row `Click`
  related data.

## Reference Documentation

For further reference, please consider the following sections:

- [Official Gradle documentation](https://docs.gradle.org)
- [Spring Boot Gradle Plugin Reference
  Guide](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/)
- [Spring
  Web](https://docs.spring.io/spring-boot/reference/web/index.html)
- [Spring SQL
  Databases](https://docs.spring.io/spring-boot/reference/data/sql.html)

## Guides

The following guides illustrate how to use some features concretely:

- [Building a RESTful Web
  Service](https://spring.io/guides/gs/rest-service/)
- [Serving Web Content with Spring
  MVC](https://spring.io/guides/gs/serving-web-content/)
- [Building REST services with
  Spring](https://spring.io/guides/tutorials/rest/)
- [Accessing Data with
  JPA](https://spring.io/guides/gs/accessing-data-jpa/)

## Enable users to save their links
### Description of the PoC
This feature enables user authentication and personalized link management within the application. Through Google OAuth integration, users can log in, create accounts, and manage their unique collections of links. The application maintains link-specific data, including click counts, target URLs, and optional QR code generation. Each user can create, view, and delete links, with their link data stored securely in the database under a unique user identifier (sub), providing a straightforward way to track ownership and link-specific metrics.

This Proof of Concept (PoC) introduces a local database system that enables structured data storage for user accounts and link management. Key components include the creation of database tables, data classes for structured data representation, and a basic use case for inserting new users and links. The system is designed to simplify future expansion, making it straightforward to add functionalities for more complex user and link operations.

We modified the file to add the new data classes User and Link, which are essential for managing user information and their links, this is done in `/core/src/main/kotlin/es/unizar/urlshortener/core/Domain.kt`. Afterward, we updated the entity file to enable the storage of a User entity and a Link entity in the database. A Link contains the Click, the ShortUrl, and the user it belongs to. I also modified the converters for transforming between domain objects and entities, both ways. You can find the updates in the following path: `/repositories/src/main/kotlin/es/unizar/urlshortener/infrastructure/repositories/Entities.kt`.

We created the repositories in `/repositories/src/main/kotlin/es/unizar/urlshortener/infrastructure/repositories/Repositories.kt` to manage the persistence of User and Link entities in the database. These repositories facilitate essential operations, including saving new entries, inserting data, and retrieving records by their IDs, ensuring efficient interaction with the underlying database.

Additionally, we implemented the GetUserInformationUseCase in `core/src/main/kotlin/es/unizar/urlshortener/core/usecases/CreateShortUrlUseCase.kt`, which centralizes user-related functionalities. This use case first checks if a user already exists in the database; if not, it inserts a new user entry, ensuring proper registration before users can create links. It also allows users to insert a sample link, providing them with a quick way to test or demonstrate functionality within their accounts. Furthermore, the use case includes a method to retrieve all links associated with a specific user.

Finally we implemented a security configuration bean that manages access controls for Google OAuth authentication. This bean, done in `/app/src/main/kotlin/es/unizar/urlshortener/ApplicationConfiguration.kt`,is responsible for configuring the security filter chain to specify how authentication and authorization should be handled within the application.

### Justification for the choice of any additional libraries or frameworks
We chose the spring-boot-starter-oauth2-client to simplify the implementation of OAuth2 and OpenID Connect authentication in our application. This library provides a comprehensive and easy-to-use framework for integrating third-party authentication providers, such as Google, enabling users to log in using their existing accounts. By leveraging established OAuth2 flows, we enhance the user experience by streamlining the authentication process and reducing friction during login. Additionally, this library handles the complexities of token management and user session handling, allowing us to focus on building application features rather than dealing with the intricacies of authentication protocols. 

### Challenged encountered and how they were addressed 
The main challenges we encountered revolved around understanding the project's structure and familiarizing ourselves with Kotlin's features. Navigating the architecture of the project required time and effort, as it involved grasping how different components interacted and the overall flow of data. To address this, we dedicated time to thoroughly review the project's documentation and source code, which provided valuable insights into its design principles. Additionally, we engaged with Kotlin's language features, which were somewhat different from those of other languages we had previously used. By utilizing online resources, tutorials, and community forums, we gradually built our understanding and proficiency in Kotlin, enabling us to effectively contribute to the project. 

### Instructions to run the PoC
To run the Proof of Concept (PoC), you will need to open your terminal and navigate to the project's root directory. Once there, execute the command `./gradlew bootRun` to start the application. After the application is running, open your web browser and navigate to `http://localhost:8080/user`. You will then be prompted to authenticate using your Google account. Upon successful authentication, you will be able to view user data along with a link that is automatically added when a user registers. This process not only showcases the application's core functionality but also demonstrates how user registration and link management work seamlessly together.

### Implemented Tests
In our development process, we have implemented an integration test, done in `app/src/test/kotlin/es/unizar/urlshortener/IntegrationTests.kt` that validates the requirement for authentication when accessing the `/user` route. This test ensures that only authenticated users can gain access, thereby reinforcing the security measures we have put in place. Additionally, we have developed unit tests to thoroughly check the functionality of the GetUserInformationUseCase in `core/src/test/kotlin/es/unizar/urlshortener/core/usecases/GetUserInformationUseCaseTest.kt`. These unit tests verify that the use case behaves as expected under various conditions, ensuring the reliability and correctness of the user management features within the application. 







