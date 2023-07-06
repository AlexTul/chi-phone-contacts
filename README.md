# Phone contacts

Test task

Design and implement “Phone contacts” application. Phone contacts application allows adding/editing and deleting contacts data. Single contact is represented by the following data:
1.     Contact name
2.     Contact emails. One contact may have multiple emails
3.     Contact phone phone. One contact may have multiple phone phones
User should have a possibility to:
1.     Register in the app, login and password should be provided during registration
2.     Login to the app
3.     Add new contact
4.     Edit existing contact
5.     Delete existing contact
6.     Get list of existing contacts
The app to be implemented should:
-       Use any relational database to store contacts data. Take into account that schema should be designed in accordance with 1st and 2nd normal forms
-       Use Spring Boot, Spring Web, Spring Security and Spring Data + Hibernate
-       Should give access only to authorized users, so each user should have his own list of phone contacts (Use Spring Security)
-       Be a RESTful webservice from a client perspective
-       When contact is added or edited, emails and phone phones should be validated so thatit is not possible to add phone phone like “+38-asdas” or email like “aa@”. Also every phone phone and email should be unique per contact. So it should not be possible to add already existing email, the same for phone phones
Contact names should be unique
-       Have unit and integration tests
(Optional) Additional work as bonus:
-       Run backend application in Docker
-       Add swagger documentation to the project
-       Add ability to upload image for each contact when create/update contact
-       Add ability to export/import contacts
-       Use any free API email/ phone validators (please don’t store your credentials in the project, please change it to fake data)
 As soon as the work is done, please send a link to public git repository containing source code of the application (send link in this form).

  
Please note that no UI is necessary, only a backend part.
Here are some examples of server usage:
-       User requests security token using his credentials:
HTTP POST %SERVER_URL%:%SERVER_PORT%/auth
Body
{
    "login": "xxx",
    "password": "yyy"
}
Server responses with a valid token that later will be included in HTTP requests for authorization
-       User adds a new contact:
HTTP PUT %SERVER_URL%:%SERVER_PORT%/contacts
Body
{
    "name": "xxx",
    "emails": ["xxx@xxx.com", "yyy@yyy.com"],
    "phones": ["+380939333333", "+380939333334", "+380939333335"]
}
 -       User deletes a contact:
HTTP DELETE %SERVER_URL%:%SERVER_PORT%/contacts
-       User edits a contact:
HTTP POST %SERVER_URL%:%SERVER_PORT%/contacts
Body
{
    "name": "xxx",
    "emails": ["xxxx@xxxx.com", "yyyy@yyyy.com"],
    "phones": ["+380939333331", "+380939333332", "+380939333333"]
}
-       User gets a list of his contacts
HTTP GET %SERVER_URL%:%SERVER_PORT%/contacts

Response Body:
[
   {
       "name": "xxxx",
       "emails": ["xxxxx@xxxx.com", "yyyyx@yyyy.com"],
       "phones": ["+380939333330", "+380939333330", "+380939333330"]
   } ,
   {
       "name": "xxxy",
       "emails": ["xxxxy@xxxx.com", "yyyyy@yyyy.com"],
       "phones": ["+380939333331", "+380939333331", "+380939333331"]
   } ,
   {
       "name": "xxxz",
       "emails": ["xxxxz@xxxx.com", "yyyyz@yyyy.com"],
       "phones": ["+380939333332", "+380939333332", "+380939333332"]
   }
 ]

To see available endpoints, go to [http://localhost:8080/swagger-ui.html]()

To run the application using the Docker command line, run in Maven "mvn package -Dspring.datasource.username=your_username -Dspring.datasource.password=your_password" and in Docker "docker-compose up -d"