Redirection limit
    • Description: Enforce a limit on the number of URLs that can be shortened by both logged-in and non-logged-in users to avoid misuse of the service.
    • Validation:
        – Correctness: Verify that the system accurately tracks and enforces the limit on shortened URLs for each user (logged-in or not).
        – Scalability: Test how the system performs under heavy loads where many users are  shortening URLs simultaneously, ensuring limits are enforced without performance degradation.
        – Professionalism: Provide detailed documentation and robust tests.

Implementation:
    For checking how many URLs a user has shortened, we can maintain a field on the table of each user which has the count if the number of ShortenedUrl in the last hour. When a user tries to shorten a URL, we can quickly check the table to see if the user has reached their limit. If the user has not reached the limit, we can increment the count and allow the user to shorten the URL. If the user has reached the limit, we can reject the request.
    When we call the method "create" in the CreateShortUrlUseCase.kt, firstly we do the check if the user has reached the limit. If the user has reached the limit, we return an error message. Otherwise, we increment the count and create the shortened URL.
    For checking that field in the database, we will have to use a query declared in the Repository.
    We will have to create a function which clear that field every hour for every user. We can use a cron job for that. 