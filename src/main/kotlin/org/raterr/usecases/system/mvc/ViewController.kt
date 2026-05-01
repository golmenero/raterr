package org.raterr.usecases.system.mvc

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ViewController {

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }

    @GetMapping("/register")
    fun register(): String {
        return "register"
    }

    @GetMapping("/rate")
    fun rate(): String {
        return "rate"
    }

    @GetMapping("/top")
    fun top(): String {
        return "top"
    }
}
