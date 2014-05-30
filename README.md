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
    Step 3 : DELETE


###Step 1 : update the controller class 

<pre>
    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE, produces = "text/plain")
    @ResponseBody
    public String delete(@PathVariable Long id) {
        // Retrieve user by id
        User user = users.findOne(id);
        if(Objects.isNull(user))
            return "User id '"+id+"' not found.";

        // Delete user
        users.delete(user);

        // Return result
        return "User " + user.getMail() + " has been deleted.";
    }
</pre>

###Step 2 : start the boot app
###Step 3 : delete a new user
<pre>
curl -X DELETE -H "Content-Type: application/json" http://localhost:8080/user/1
</pre>

###Step 4 : retrieve the deleted user
<pre>
curl -X GET -H "Content-Type: application/json" http://localhost:8080/user/1
</pre>