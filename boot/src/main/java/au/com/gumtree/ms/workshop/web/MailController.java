package au.com.gumtree.ms.workshop.web;

import au.com.gumtree.ms.workshop.domain.MessageDescriptor;
import au.com.gumtree.ms.workshop.domain.User;
import au.com.gumtree.ms.workshop.repository.UserRepository;
import au.com.gumtree.ms.workshop.service.MailServer;
import au.com.gumtree.ms.workshop.util.MessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.Reactor;
import reactor.event.Event;

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
    public Iterable<User> listUsers() {
        return users.findAll();
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.POST, produces = "text/plain")
    @ResponseBody
    public String message(@PathVariable Long userId) {
        // Retrieve user by id
        User user = users.findOne(userId);
        if (null == user) {
            throw new IllegalArgumentException("No User found for id " + userId);
        }

        MessageDescriptor message = MessageGenerator.buildMessage(MessageGenerator.nextString(),
                MessageGenerator.nextMail(),
                user.getMail(),
                user.toString());
        reactor.notify("mail.execute", Event.wrap(message));

        // Update message count
        user = users.save(user.setMessageCount(user.getMessageCount() + 1));

        // Return result
        return "Hello " + user.getMail() + "! You now have " + user.getMessageCount() + " messages.";
    }

}
