package com.example.demo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ログインページ表示（Spring Security が処理を担当）
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 会員登録フォーム
    @GetMapping("/register")
    public String showRegisterForm(Model model,
                                   @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("error", error);
        return "register";
    }

    // 登録処理
    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String nickname,
            @RequestParam String university,
            Model model) {
        String trimmedNickname = nickname == null ? "" : nickname.trim();
        if (trimmedNickname.isEmpty()) {
            model.addAttribute("error", "ニックネームは必須です。");
            return "register";
        }

        if (trimmedNickname.length() > 40) {
            model.addAttribute("error", "ニックネームは40文字以内で入力してください。");
            return "register";
        }

        // ① すでに登録済みメールならNG
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "このメールアドレスは既に登録されています。");
            return "register";
        }

        // パスワードをハッシュ化
        String encoded = passwordEncoder.encode(password);

        // ROLE_STUDENT で保存
        User user = new User(email, encoded, "ROLE_STUDENT", trimmedNickname, university);
        userRepository.save(user);

        // 登録後はログインページへ
        return "redirect:/login?registered";
    }
}
