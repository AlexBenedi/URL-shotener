## Branded Links
### Overwiew

- Points: 15
- Description: Allow for the creation and management of custom, branded links.
- Validation:
    - Correctness: Ensure the short URL generated contains the customized name the
user has selected, if he/she wants to.
    - Scalability: Verify that the URL shortener brand the link given even under high-load levels and give a branded link which redirect to the original URL you have
given.
    - Professionalism: Well structured code and complete documentation.

### Implementation
1. ***New atribute in ShortUrlEntity***: ShortUrlEntity has been modified to introduce a new atribute: `isBranded`. This new atribute allows distinguishing between a normal link and a branded link directly in the database.

2. ***New properties in the body of the POST request***: The properties `isBranded` and `name` have been introduced as optional in the request. `isBranded` allows distinguishing between a normal and a branded link, while `name` indicates the name that the branded link will use.

3. ***New excepctions***: `InvalidNameBrandedUrl` has been added to handle an excepcional case dureing the creation of branded links when atrubute `name` is empty. This exception has been integrated into `ExceptionHandler`to inform the client of the error.

4. ***Modification UrlShortenerController***: The implementation of the `create` function at `UrlShortenerController`class has been slightly modified. The `ìsBranded` attribute of `data` distinguish between a branded link and a normal link. If it is a branded Link  `name` will be use to create the link and it would be saved as a normal link.

### Implemented Tests

New test have been implemented to ensure the correct functioning of the branded links. These have been introduced in two differents files: `UrlShortenerController.kt`and `CreateShortUrlUseCaseTest.kt`. In both files, both correct and incorrect creation of a branded link have been tested, along with other rare cases.

### Future 

In the future, `index.html` and `app.js` will be modified to enable users to introduce the branded links in a more user-friendly way. Basic back-end has been introduced for this POC but the front-end part has not been implemented yet. 

