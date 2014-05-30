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
    Step 4 : PUT
    Step 5 : Send Notification


###Step 1 : Add notification to the add user function 
<pre>
    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = "text/plain")
    @ResponseBody
    public String add(@RequestBody User user) {
        // Save user
        User updated = users.save(user);

        // Send registration notification
        MessageDescriptor message = MessageGenerator.buildMessage(MessageGenerator.nextString(),
                updated.getMail(),
                MessageGenerator.nextMail(),
                user.toString());
        reactor.notify("mail.execute", Event.wrap(message));

        // Update message count
        updated = users.save(updated.setMessageCount(user.getMessageCount() + 1));

        // Return result
        return "User " + updated.getMail() + " has been registered.";
    }
</pre>

###Step 2 : Add notification to the delete user function 
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

        // Send un-registration notification
        MessageDescriptor message = MessageGenerator.buildMessage(MessageGenerator.nextString(),
                user.getMail(),
                MessageGenerator.nextMail(),
                user.toString());
        reactor.notify("mail.execute", Event.wrap(message));

        // Return result
        return "User " + user.getMail() + " has been deleted.";
    }
</pre>

###Step 3 : get all user 
<pre>
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
        @ResponseBody
        public Iterable<User> list() {
            return users.findAll();
    }
</pre>

###Step 4 : APIs
<pre>
curl -X GET -H "Content-Type: application/json" http://localhost:8080
curl -X POST -H "Content-Type: application/json" http://localhost:8080/user -d '{"id":2,"mail":"mail2@ebay.com","messageCount":0}'
curl -X GET -H "Content-Type: application/json" http://localhost:8080/user/1
curl -X DELETE -H "Content-Type: application/json" http://localhost:8080/user/1
curl -X PUT -H "Content-Type: application/json" http://localhost:8080/user/1/to/buyer@email.com/hi/hello
curl -X POST -H "Content-Type: application/json" http://localhost:8080/shutdown
</pre>
