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

    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = "text/plain")
    @ResponseBody
    public String add(@RequestBody User user) {
        // Save user
        User updated = users.save(user);

        // Return result
        return "User " + updated.getMail() + " has been registered.";
    }
}
