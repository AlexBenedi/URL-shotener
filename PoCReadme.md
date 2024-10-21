Written Report in README.md: Each group is required to document their work in the README.md file of the GitHub repository. The report should include:
A brief description of the PoC for each feature.*
Justification for the choice of any additional libraries or frameworks.
Challenges encountered during implementation and how they were addressed.
Clear instructions on how to run the PoC.
A brief explanation of how the tests demonstrate the functionality of each feature.
## Google Safe Browsing Integration 
This feature is designed to ensure the safety of links shortened using the application. It checks each link against Google's Safe Browsing API to determine if it is safe. If a link is found to be unsafe, the feature provides detailed information to the user about the nature of the threat, such as phishing, malware, or other malicious activities. This helps users make informed decisions and avoid potentially harmful websites. 

In this Proof of Concept, we integrate with the Google Safe Browsing API to ensure the safety of URLs. It only verifies if the URL is safe or unsafe (boolean), but does not provide additional information about why is unsafe. This is done in `gateway/src/main/kotlin/es/unizar/urlshortener/gateway/GoogleSafeBrowsingClient.kt`. We define the use case for checking URLs against the API and create unit tests to validate the integration and functionality, with a good URL and a malicious one. This is done in `gateway/src/test/kotlin/es/unizar/urlshortener/gatewayGoogleSafeBrowsingClientTest.kt`. 

This feature has been implemented without the use of any additional libraries or frameworks. It operates on a simple request/response model with Google's API. Since it integrates with other services, the integration class has been placed in the "gateway" module.

To ensure the security of our API Key, it has been added as an environment variable. For future CI tests to pass, we should add the API Key into GitHub secrets once the GitHub Actions workflow is set up.

Challenges & instructions: TODO 
--> Challenge 1: Adding new module 'gateway'  -->(implementation(project(":gateway")))
--> Inyectar el rest template al google's API sin interferir en el modelo de proyecto limpio --> TODO

The tests validate the functionality by invoking the `isSafe()` function of the `GoogleSafeBrowsingClient`, which connects to the Google Safe Browsing API. The results are then compared with the expected outcomes. We use two test cases: a non-malicious website (www.unizar.es) and a malicious website (taken from an online database of malicious websites, https://testsafebrowsing.appspot.com/). This ensures that our implementation correctly identifies safe and unsafe URLs.