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
    Step 1 : GET
    Step 2 : POST


###Step 1 : update the controller class 

<pre>
    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = "text/plain")
    @ResponseBody
    public String add(@RequestBody User user) {
        // Save user
        User updated = users.save(user);

        // Return result
        return "User " + updated.getMail() + " has been registered.";
    }
</pre>

###Step 2 : start the boot app
###Step 3 : create a new user
<pre>
curl -X POST -H "Content-Type: application/json" http://localhost:8080/user -d '{"id":2,"mail":"mail2@ebay.com"}'
</pre>

###Step 4 : retrieve the user
<pre>
curl -X GET -H "Content-Type: application/json" http://localhost:8080/user/2
</pre>