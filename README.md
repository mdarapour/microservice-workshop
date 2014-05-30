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

###Step 1 : create au.com.gumtree.ms.workshop.domain package and User class 

<pre>
package au.com.gumtree.ms.workshop.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long   id;
    @Column(nullable = false)
    private String mail;

    protected User() {
    }

    public User(String mail) {
        this.mail = mail;
    }

    public Long getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + mail + '\'' +
                '}';
    }
}
</pre>


###Step 2 : create au.com.gumtree.ms.workshop.repository package and UserRepository class

<pre>
package au.com.gumtree.ms.workshop.repository;

import au.com.gumtree.ms.workshop.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
</pre>

###Step 3 : create resources directory under main
###Step 4 : create application.properties under resources directory

<pre>
spring.jpa.hibernate.ddl-auto: create-drop
</pre>

###Step 5 : create import.sql under resources directory
<pre>
insert into user(id, mail) values (1, 'mdarapour@ebay.com')
</pre>

###Step 6 : change the contoller class
<pre>
package au.com.gumtree.ms.workshop.web;

import au.com.gumtree.ms.workshop.domain.User;
import au.com.gumtree.ms.workshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author mdarapour
 */
@RestController
public class MailController {
    private final UserRepository users;

    @Autowired
    public MailController(UserRepository users) {
        this.users = users;
    }
    

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public User get(@PathVariable Long id) {
        // Retrieve user by id
        return users.findOne(id);
    }
}
</pre>

###Step 6 : call the API
<pre>
curl -X GET -H "Content-Type: application/json" http://localhost:8080/user/1
</pre>