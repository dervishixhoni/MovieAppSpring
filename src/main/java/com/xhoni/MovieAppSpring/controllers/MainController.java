package com.xhoni.MovieAppSpring.controllers;

import com.xhoni.MovieAppSpring.services.UserService;
import com.xhoni.MovieAppSpring.services.WatchlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.xhoni.MovieAppSpring.models.*;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Objects;

@Controller
public class MainController {
    @Autowired
    private UserService userService;

    @Autowired
    private WatchlistService watchlistService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/dashboard";
    }

    @GetMapping("/registerPage")
    public String registerPage(@ModelAttribute("user") User user, HttpSession session, Model model) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/register")
    public String registration(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session, Model model) {
        User createUsers = userService.register(user, result);
        if (result.hasErrors()) {
            return "signup";
        }
        session.setAttribute("userId", createUsers.getId());
        return "redirect:/verifyemail";
    }

    @GetMapping("/loginPage")
    public String loginPage(@ModelAttribute("loginUser") LoginUser loginUser, HttpSession session, Model model) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("loginUser", new LoginUser());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginUser") LoginUser loginUser, BindingResult result, HttpSession session, Model model) {
        User user = userService.loggin(loginUser, result);
        if (result.hasErrors()) {
            return "login";
        }
        session.setAttribute("userId", user.getId());
        return "redirect:/verifyemail";
    }

    @GetMapping("/verifyemail")
    public String verifyEmail(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.findUserId(userId);
        if (user.getIsVerified()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", user);
        return "verifyEmail";
    }

    @PostMapping("/activateaccount")
    public String activateAccount(@ModelAttribute("user") User POSTuser, BindingResult result, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        Long userId = (Long) session.getAttribute("userId");
        userService.verify(POSTuser, userService.findUserId(userId), result);
        if (result.hasErrors()) {
            return "verifyEmail";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/resendcode")
    public String resendCode(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.findUserId(userId);
        if (user.getIsVerified()) {
            return "redirect:/dashboard";
        }
        userService.updateUserVerificationCode(userService.generateCode(), user);
        return "redirect:/verifyemail";
    }

    @GetMapping("/incatalog")
    public String inCatalog(HttpSession session, Model model) {
        return "redirect:/";
    }

    @GetMapping("/about")
    public String about(HttpSession session, Model model) {
        return "about";
    }

    @GetMapping("/contact")
    public String contact(HttpSession session, Model model) {
        if (session.getAttribute("userId") != null) {
            model.addAttribute("user", userService.findUserId((Long) session.getAttribute("userId")));
        }
        return "contacts";
    }

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        if (!Objects.equals(id, session.getAttribute("userId"))) {
            return "redirect:/logout";
        }
        model.addAttribute("user", userService.findUserId(id));
        model.addAttribute("watchlist", watchlistService.getByUser(userService.findUserId(id)));
        model.addAttribute("passwordReset", new PasswordReset());
        return "profile";
    }

    @PostMapping("/editPassword")
    public String editPassword(@Valid @ModelAttribute("passwordReset") PasswordReset passwordReset, BindingResult result, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        if (result.hasErrors()) {
            return "redirect:/profile/" + session.getAttribute("userId");
        }
        User editUser = userService.findUserId((Long) session.getAttribute("userId"));
        userService.updateUserPassword(passwordReset, editUser, result);
        if (result.hasErrors()) {
            model.addAttribute("user", editUser);
            model.addAttribute("watchlist", watchlistService.getByUser(editUser));
            return "profile";
        }
        return "redirect:/profile/" + session.getAttribute("userId");
    }

    @PostMapping("/editProfile")
    public String editProfile(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        User editUser = userService.findUserId((Long) session.getAttribute("userId"));
        userService.updateUser(user, editUser, result);
        if (result.hasErrors()) {
            model.addAttribute("watchlist", watchlistService.getByUser(editUser));
            model.addAttribute("passwordReset", new PasswordReset());
            return "profile";
        }
        return "redirect:/profile/" + session.getAttribute("userId");
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") int id, HttpSession session, Model model) throws IOException {
        JSONArray results = userService.getDetails(id);
        model.addAttribute("movie", results.getJSONObject(0));
        model.addAttribute("trailerUrl", results.getJSONObject(1));
        model.addAttribute("recommendations", results.getJSONObject(2).getJSONArray("results").toList().subList(0, 18));
        model.addAttribute("genres", results.getJSONObject(3));
        if (session.getAttribute("userId") != null) {
            model.addAttribute("newWatchlist",new Watchlist());
            model.addAttribute("user", userService.findUserId((Long) session.getAttribute("userId")));
            model.addAttribute("watchlist", watchlistService.getWatchlistIdsByUser(userService.findUserId((Long) session.getAttribute("userId"))));
        }
        return "details";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) throws IOException {
        JSONArray results = userService.getDashboard();
        model.addAttribute("movies", results.getJSONObject(0).getJSONArray("results").toList().subList(0, 18));
        model.addAttribute("trendingToday", results.getJSONObject(1).getJSONArray("results").toList().subList(0, 18));
        model.addAttribute("trendingWeek", results.getJSONObject(2).getJSONArray("results").toList().subList(0, 18));
        model.addAttribute("upcoming", results.getJSONObject(3).getJSONArray("results").toList().subList(0, 18));
        model.addAttribute("genres", (JSONObject)results.getJSONObject(4));
        if (session.getAttribute("userId") != null) {
            model.addAttribute("user", userService.findUserId((Long) session.getAttribute("userId")));
        }
        return "dashboard";
    }

    @GetMapping("/catalog")
    public String catalog(HttpSession session, Model model) throws IOException {
        JSONArray results = userService.getCatalog();
        model.addAttribute("base", results.getJSONObject(0).getJSONArray("results").toList().subList(0, 18));
        model.addAttribute("genres", results.getJSONObject(1));
        if (session.getAttribute("userId") != null) {
            model.addAttribute("user", userService.findUserId((Long) session.getAttribute("userId")));
        }
        return "catalog";
    }

    @GetMapping("/catalog/{genre}")
    public String catalogGenre(@PathVariable("genre") int genre, HttpSession session, Model model) throws IOException {
        JSONArray results = userService.getCatalogGenre(genre);
        model.addAttribute("base", results.getJSONObject(0).getJSONArray("results").toList().subList(0, 18));
        model.addAttribute("genres", results.get(1));
        model.addAttribute("presentGenre", results.getJSONObject(1).get(String.valueOf(genre)));
        if (session.getAttribute("userId") != null) {
            model.addAttribute("user", userService.findUserId((Long) session.getAttribute("userId")));
        }
        return "catalog";
    }

    @PostMapping("/search")
    public String search(@RequestParam("search") String search, HttpSession session, Model model, HttpServletRequest request) throws IOException {
        JSONArray results = userService.search(search);
        JSONObject r = results.getJSONObject(0);
        model.addAttribute("search", search);
        model.addAttribute("results", r.getJSONArray("results").toList().subList(0, 18));
        model.addAttribute("genres", results.getJSONObject(1));
        if (session.getAttribute("userId") != null) {
            model.addAttribute("user", userService.findUserId((Long) session.getAttribute("userId")));
        }
        if (r.has("success"))
            return "redirect:"+request.getHeader("Referer");
        return "result";
    }

    @PostMapping("/watchlist")
    public String watchlist(@ModelAttribute("newWatchlist") Watchlist watchlist, HttpSession session, Model model, HttpServletRequest request) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        User user = userService.findUserId((Long) session.getAttribute("userId"));
        watchlistService.createWatchlist(watchlist, user);
        return "redirect:"+request.getHeader("Referer");
    }

    @PostMapping("/remove/{id}")
    public String remove(@PathVariable("id") Long id, HttpSession session, Model model, HttpServletRequest request) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        Watchlist watchlist = watchlistService.getWatchlistByUserAndMovieId(userService.findUserId((Long) session.getAttribute("userId")), id);
        watchlistService.deleteWatchlist(watchlist);
        return "redirect:"+request.getHeader("Referer");
    }

}
