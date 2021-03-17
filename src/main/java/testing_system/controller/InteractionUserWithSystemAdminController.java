package testing_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import testing_system.domain.dto.CaptchaResponseDto;
import testing_system.domain.message.Message;
import testing_system.domain.people.Users;
import testing_system.repos.message.MessageRepo;
import testing_system.service.AuxiliaryService;

import java.util.Collections;

@Controller
@RequestMapping("/interaction")
@PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'TEACHER_ADMIN')")
public class InteractionUserWithSystemAdminController {

    private static final String RECAPTHCA_API = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    @Autowired
    private AuxiliaryService auxiliaryService;
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${recaptcha.secret}")
    private String secret;

    @GetMapping
    public String writeMessage(Model model,
                               @AuthenticationPrincipal Users user) {
        model.addAttribute("role", auxiliaryService.getRole(user));

        return "interaction_user";
    }

    @PostMapping
    public String writeMessage(@AuthenticationPrincipal Users user,
                               @RequestParam(name = "theme") String theme,
                               @RequestParam(name = "question") String question,
                               @RequestParam(name = "g-recaptcha-response") String recaptchaResponse,
                               Model model) {

        String url = String.format(RECAPTHCA_API, secret, recaptchaResponse);
        CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);

        if (!response.isSuccess()) {
            model.addAttribute("captchaError", "Вы не выбрали это поле!");
            return "interaction_user";
        }

        Message message = new Message();
        message.setText(question);
        message.setTheme(theme);
        message.setUserEmail(user.getUsername());
        message.setUserName(user.getFullName());
        messageRepo.save(message);
        return "redirect:/about";
    }

}
