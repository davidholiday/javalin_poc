# javalin_poc
### david holiday 
### davidholiday@hush.com
---


## what is this? 

PoC service demonstrating use of Javalin as a microservice with authentication, sessions, and RBAC access to REST resources. 


## how do I make it go? 

to build from project root:
```mvn clean install```

to run from project root:
```java -jar target/javalin_poc-1.0-SNAPSHOT.jar```


## what endpoints are exposed? 

as per ask, there are the following endpoints 

* `/`serves a table displaying "security resources" currently serialized to the database
* `/login` will present a login form to - you guessed it - allow you to log into the application.
* `/logout` will log you out of the application 
* `/admin` will present a page - only accessible if you're logged in and have the ADMIN role- that allows you to add a "security resource" to the database.


## what is the default user?

Assuming you are working with the provided `mr_data.db` sqlite file, the default user is:

```
username: captaincrunch
password: 2600ismy%egacy
```


## how do I add a different user? 
In the `App` class main method, there is a block of commented out code that will populate the user database with any user you desire. 


## what other technical features are represented in the app?

* the app uses `Hibernate` ORM facilities to communicate with a `sqlite` db. There is no raw SQL used in the application layer. 
* the app uses the `Javalin` framework which itself uses `Jetty`. User session data is stored in the SQL db by way of Jetty. 
* the app uses `Javalin`'s `AccessManager` to ensure all RESTful resources are protected by an RBAC challenge. The login flow + the Jetty session handling ensures authenticated users are able to access privileged REST resources while unauthenticated users are unable to do so. 
* The app uses validation logic in the Hibernate Entity "setter" methods germane to each db table, along with db table constraints, to ensure the caller can't serialize naughty things.
  * the username field is restricted to alphanumeric characters only and bounded in size by both the application and database.
  * the password field is hashed+salted in a manner compliant with NIST standards and bounded in size by both the application and database.
  * the description field is escaped for XSS and bounded in size by both the application and database.
  * the URL field is percent-encoded and bounded in size by both the application and database. 
* Access to the db tables is restricted to only endpoints that require access to those tables. For example, only the `/login` methods have the ability to access the `User` table by way of the `hibernate` EntityManager wheraas the `/admin` and `/` handlers are exclusively able to access the `Resource` table. In this way if a given handler is somehow compromised there is some measure of compartmentalization of damage that can result from said event. 
* the `Jetty` user session will time out after 15m of inactivity. This is set by way of configuration value in `SessionHelpers`. 
* the sqlite db file, `mr_data` will automatically be created, along with all necessary tables, if it's not present when the app starts up. 


## what security scans did you run against the app?

I ran it through `OWASP` Zap to see if it found any low-hanging fruit (it didn't). I also ran an OSS scan against it using syft/grype. The SBOM for the project is file `./{PROJECT_ROOT}/sbom.json`. The OSS report is located at `./{PROJECT_ROOT}/oss_report.json`. No issues were discovered. 


## what's missing? 

Given the time constraint I was unable to include javadoc or testing. Both are necessary before something like this would go into production. Additionally I would want a pentester to work with this to see if they are able to defeat the system integrity controls with commodity attack methods. It's critical that someone other than an app author does this for the same reason writers need editors - the former is to close to the work to perform an objective evaluation. 

