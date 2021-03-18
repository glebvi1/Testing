package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import testing_system.domain.people.Users;
import testing_system.service.AuxiliaryService;
import testing_system.service.UserService;

@Controller
@RequestMapping("/general")
public class GeneralController {
    @Autowired
    private AuxiliaryService auxiliaryService;
    @Autowired
    private UserService userService;

    @GetMapping("/edit")
    public String editYourself(@AuthenticationPrincipal Users user,
                               Model model) {
        model.addAttribute("user", user);
        model.addAttribute("role", auxiliaryService.getRole(user));
        return "edit_user";
    }

    @PostMapping("/edit")
    public String editYourself(@AuthenticationPrincipal Users user,
                               @RequestParam(name = "email") String email,
                               @RequestParam(name = "fullName") String name,
                               @RequestParam(name = "password1") String newPassword,
                               @RequestParam(name = "password2") String confirmPassword,
                               Model model) {

        if (!userService.updateUser(user, name, email, newPassword, confirmPassword)) {
            model.addAttribute("message", "Введенные данные не корректны. Пожалуйста, проверьте их.");
        } else {
            model.addAttribute("message", "Данные успешно изменены!");
        }

        return "edit_user";
    }

}
