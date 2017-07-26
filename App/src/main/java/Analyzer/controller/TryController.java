package Analyzer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by pawel on 07.07.17.
 */

@Controller
public class TryController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

}
