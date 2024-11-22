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

