#Microservice Workshop
=====================

##Topic 1 : Microservice Architecture
    * What is it?
    * Microservice vs SOA vs Monolithic
    * How can we use it?
##Topic 2 : Spring Boot
    * What is it?
    * How can it help?
##Topic 3 : Demo 1 (Echo service) 
    Step 1 : PUT
    Step 2 : GET
    Step 3 : POST
    Step 4 : DELETE
##Topic 4 : Demo 2 (Mail Service)
    Step 1 : Spring Reactor ?
    Step 2 : MessageDescriptor
    Step 3 : MailServer & MessageGenerator    
    Step 4 : User & UserRepository
    Step 5 : MailController
    Step 6 : BootMailServer
##Topic 5 : Pros & Cons
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
