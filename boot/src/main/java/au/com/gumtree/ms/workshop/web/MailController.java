package au.com.gumtree.ms.workshop.web;

import org.springframework.web.bind.annotation.*;

/**
 * @author mdarapour
 */
@RestController
public class MailController {

    @RequestMapping(value = "/hi/{name}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String get(@PathVariable String name) {
        return String.format("Hi %s!",name);
    }
}
