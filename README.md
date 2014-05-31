#Microservice Workshop
=====================

##Topic 1 : Microservice Architecture
    * What is it?
    * Microservice vs SOA vs Monolithic
    * How can we use it?
##Topic 2 : Spring Boot
    * What is it?
    * How can it help?
##Topic 3 : Demo (Mail service) 
    Step 1 : GET
    Step 2 : POST
    Step 3 : DELETE
    Step 4 : PUT
    Step 5 : Send Notification
##Topic 4 : Pros & Cons
    * Microservice Group
    
#API
```
curl -X GET -H "Content-Type: application/json" http://localhost:8080
curl -X POST -H "Content-Type: application/json" http://localhost:8080/user -d '{"id":2,"mail":"mail2@ebay.com","messageCount":0}'
curl -X GET -H "Content-Type: application/json" http://localhost:8080/user/1
curl -X DELETE -H "Content-Type: application/json" http://localhost:8080/user/1
curl -X PUT -H "Content-Type: application/json" http://localhost:8080/user/1/to/buyer@email.com/hi/hello
curl -X POST -H "Content-Type: application/json" http://localhost:8080/shutdown
```

<p><a href="https://slides.com/mattdarapour/microservices" title="Slides">Slides</a></p>