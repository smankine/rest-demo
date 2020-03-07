# Design considerations
I tried to complete this project by addressing all the requirements without unnecessarily complicating the code by adding features that could have been needed or useful but were not explicitly stated in the requirements (such as currency unit operations, product counts in orders etc.). I chose to use H2 in-memory database as it is extremely easy to setup and there was no requirement for more permanent persistence. In case that it is needed, with a minor configuration change, H2 can also write to the local hard drive.

# Setup
  * install Java >= 8
  * If you want to run/build via gradle, then gradle is needed
  * I coded everything using IntelliJ as my IDE but any other should work too, I didn't add any libraries requiring any extra plugins, however gradle plugin would be quite useful if you want to develop the project 

# Run
* Clone the project
* To run the project you can simply run the included jar by typing the following into a command line from the root directory
```
cd rest-demo/
java -jar rest-demo-0.0.1-SNAPSHOT.jar
```
* after successfully launching, visit http://localhost:8080/api/product to check that it is running, this should return an empty array
* A postman collection is available in the etc/ folder, if that is your thing
* Alternatively, you can run the project with gradle task ```'bootRun'```

# API Documentation
* You can access the API docs at http://localhost:8080/api/swagger-ui.html
# Database access
* You can access the H2 database by going to http://localhost:8080/api/h2
  * JDBC URL: jdbc:h2:mem:testdb
  * User Name:sa
  * Password:
  
# Unit tests
I chose not to have any unit tests on the repository/service level as there are no complicated methods or algorithms that need to be tested. All functionality can be tested over the HTTP layer, giving the best ‘bang for your buck’ in test writing effort vs. actual client functionality covered. When writing unit tests I choose to group related unit tests under one test method so that not to have gigantic unit test files with very elaborate test names that require a lot of thinking and maintenance and eventually get out of date. I try to write the unit tests so that the test names are less important as the test code itself reads like a book. I like to use a lot of helper methods in tests making them easy to read and maintain, but also allowing an easy hook up of any behavior driven testing patterns if desired.

* You can run the unit tests with gradle task ```'test'``` or from the IDE of your choice

# Questions
**You do not need to add authentication to your web service, but propose a protocol / method and justify your choice:**
For securing the end points I would use JWT tokens. JWT based security model is modern, stateless, compact and provides a good granular access level configuration. The tokens can carry any kind of needed information, still being light weight, but most useful pieces of data are the authenticated user's identify and the authorization claims to access API resources. JWT can easily be implemented with spring boot by adding the spring security related libraries and configurations.


**How can you make the service redundant? What considerations should you do?**
There are many ways you could deploy the service to introduce redundancy. We could for example package the project into a docker container and then deploy it to a container orchestration platform such as kubernetes. Kuberneter would then take care of the provisioning, deploying, scheduling and managing the individual containers according to its configuration and the on demand load of the API service. 
