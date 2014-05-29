package au.com.gumtree.ms.workshop.web;

import au.com.gumtree.ms.workshop.domain.MessageDescriptor;
import au.com.gumtree.ms.workshop.domain.User;
import au.com.gumtree.ms.workshop.repository.UserRepository;
import au.com.gumtree.ms.workshop.util.MessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.Reactor;
import reactor.event.Event;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author mdarapour
 */
@RestController
public class MailController {
    private final UserRepository users;
    private final Reactor reactor;

    @Autowired
    public MailController(UserRepository users,
                          Reactor reactor) {
        this.users = users;
        this.reactor = reactor;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Iterable<User> list() {
        return users.findAll();
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

    @RequestMapping(value = "/user/{id}/to/{mail}/{subject}/{body}", method = RequestMethod.PUT, produces = "text/plain")
    @ResponseBody
    public String send(@PathVariable Long id, @PathVariable String mail, @PathVariable String subject, @PathVariable String body) {
        // Retrieve user by id
        User user = users.findOne(id);
        if(Objects.isNull(user))
            return "User id '"+id+"' not found.";

        // Send un-registration notification
        MessageDescriptor message = MessageGenerator.buildMessage(subject,
                mail,
                user.getMail(),
                body);
        reactor.notify("mail.execute", Event.wrap(message));

        // Update message count
        user = users.save(user.setMessageCount(user.getMessageCount() + 1));

        // Return result
        return "User " + user.getMail() + " has sent a message.";
    }
}
