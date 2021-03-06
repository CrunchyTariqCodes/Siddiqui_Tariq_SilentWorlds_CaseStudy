package com.tariqsiddiqui.silentworlds.controllers;

import com.tariqsiddiqui.silentworlds.models.*;
import com.tariqsiddiqui.silentworlds.repositories.*;
import com.tariqsiddiqui.silentworlds.models.*;
import com.tariqsiddiqui.silentworlds.services.UserService;
import com.tariqsiddiqui.silentworlds.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class UserController {
    private UserRepo userDao;
    private PasswordEncoder passwordEncoder;
    private BookshelfRepo bookshelfDao;
    private BookRepo bookDao;
    private ReviewRepo reviewDao;
    private ClubRepo clubDao;
    private ClubMemberRepo clubMemberDao;

    @Autowired
    UserService usersSvc;

    public UserController(UserRepo userDao, PasswordEncoder passwordEncoder, BookshelfRepo bookshelfDao, BookRepo bookDao, ReviewRepo reviewDao, ClubRepo clubDao, ClubMemberRepo clubMemberDao) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.bookshelfDao = bookshelfDao;
        this.bookDao = bookDao;
        this.reviewDao = reviewDao;
        this.clubDao = clubDao;
        this.clubMemberDao = clubMemberDao;
    }

    // Edit controls are being showed up if the user is logged in and it's the same user viewing the file
    public Boolean checkEditAuth(User user){
        return usersSvc.isLoggedIn() && (user.getId() == usersSvc.loggedInUser().getId());
    }

    @GetMapping("/sign-up")
    public String register(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "user/register";
    }

    @PostMapping("/sign-up")
    public String createUser(
            @Valid User user,
            Errors val,
            Model viewModel) {

        //Checks for existing email
        User checkUserEmail = userDao.findByEmail(user.getEmail());
        System.out.println(checkUserEmail);
        String errorMsgEmail = "The email has been taken.";
        if (checkUserEmail != null){
            val.rejectValue(
                    "email",
                    "user.email",
                    errorMsgEmail
            );
        }

        //Checks for existing username
        User checkUsername = userDao.findByUsername(user.getUsername());
        System.out.println(checkUsername);
        String errorMsgUsername = "The username has been taken.";
        if (checkUsername != null){
            val.rejectValue(
                    "username",
                    "user.username",
                    errorMsgUsername
            );
        }

        if (val.hasErrors()) {
            viewModel.addAttribute("errors", val);
            viewModel.addAttribute("user", user);
            return "user/register";
        }

        String hash = passwordEncoder.encode(user.getPassword());
        Date currentDate = new Date();
        String defaultIMG = "/img/logo.png";

        user.setPassword(hash);
        user.setCreatedDate(currentDate);
        user.setAvatarURL(defaultIMG);

        User dbUser = userDao.save(user);

        viewModel.addAttribute("user", dbUser);

        return "redirect:/create-profile/" + dbUser.getUsername();
    }

    @GetMapping("/create-profile/{username}")
    public String showCreateProfile(Model viewModel, @PathVariable String username) {
        viewModel.addAttribute("user", userDao.findByUsername(username));
        return "user/create-profile";
    }

    @PostMapping("/create-profile/{username}")
    public String createProfile(@PathVariable String username, @ModelAttribute User userToBeUpdated) {
        User user = userDao.findByUsername(username);
        userToBeUpdated.setId(user.getId());
        userToBeUpdated.setUsername(user.getUsername());
        userToBeUpdated.setEmail(user.getEmail());
        userToBeUpdated.setPassword(user.getPassword());
        userToBeUpdated.setCreatedDate(user.getCreatedDate());
        User dbUser = userDao.save(userToBeUpdated);
        return "redirect:/profile/" + dbUser.getUsername();
    }

    @GetMapping("/edit-profile/{username}")
    public String showEditProfile(Model viewModel, @PathVariable String username) {
        viewModel.addAttribute("user", userDao.findByUsername(username));
        return "user/create-profile";
    }

    @PostMapping("/edit-profile/{username}")
    public String editProfile(@PathVariable String username,
                              @ModelAttribute User userToBeUpdated
    ){
        User user = userDao.findByUsername(username);
        if(!usersSvc.canEditProfile(user)) {
            return "redirect:/profile/" + username;
        }

        userToBeUpdated.setId(user.getId());
        userToBeUpdated.setUsername(user.getUsername());
        userToBeUpdated.setEmail(user.getEmail());
        userToBeUpdated.setPassword(user.getPassword());
        userToBeUpdated.setCreatedDate(user.getCreatedDate());

        User dbUser = userDao.save(userToBeUpdated);
        return "redirect:/profile/" + dbUser.getUsername();
    }

    @GetMapping("/profile/{username}")
    public String showUserProfile(Model viewModel, @PathVariable String username) {
        User dbUser = userDao.findByUsername(username);
        viewModel.addAttribute("user", dbUser);

        viewModel.addAttribute("showEditControls", usersSvc.canEditProfile(dbUser));
        List<Bookshelf> dbBookshelf = bookshelfDao.findAllByUserId(dbUser.getId());

        //looping through all clubs and retrieving users who are members of
        // clubs and placing them into an arraylist
        List<Club> dbClubs = clubDao.findAll();
        List<Club> clubsUserIsApartOf = new ArrayList<>();
        for(Club club : dbClubs) {
            if(clubMemberDao.findClubMemberByUserAndClub(dbUser, club) == null) {
                continue;
            }
            else {
                clubsUserIsApartOf.add(clubMemberDao.findClubMemberByUserAndClub(dbUser, club).getClub());
            }
        }

        ArrayList<Bookshelf> readList = new ArrayList<>();
        ArrayList<Bookshelf> readingList = new ArrayList<>();
        ArrayList<Bookshelf> wishlist = new ArrayList<>();

        for (Bookshelf book : dbBookshelf) {
            if (book.getBookShelfStatus() == BookshelfStatus.READ) {
                readList.add(book);
            } else if (book.getBookShelfStatus() == BookshelfStatus.READING) {
                readingList.add(book);
            } else if (book.getBookShelfStatus() == BookshelfStatus.WISHLIST) {
                wishlist.add(book);
            }
        }

        viewModel.addAttribute("read", readList);
        viewModel.addAttribute("reading", readingList);
        viewModel.addAttribute("wishlist", wishlist);
        viewModel.addAttribute("review", new Review());
        viewModel.addAttribute("bookclubs", clubsUserIsApartOf);

        return "user/profile-page";
    }

    @PostMapping("/profile/{username}/delete/{id}")
    public String deleteBook(@PathVariable String username, @PathVariable Bookshelf id) {
        User dbUser = userDao.findByUsername(username);
        bookshelfDao.deleteById(id.getId());
        return "redirect:/profile/" + dbUser.getUsername();
    }

    @PostMapping("/profile/{username}/{bookshelfId}")
    public String updateBookshelfStatus(
            @RequestParam(value = "bookshelfStatus") BookshelfStatus status,
            @PathVariable String username,
            @PathVariable long bookshelfId) {
        User dbUser = userDao.findByUsername(username);

        Bookshelf dbBookshelf = bookshelfDao.getOne(bookshelfId);
        dbBookshelf.setStatus(status);
        bookshelfDao.save(dbBookshelf);
        return "redirect:/profile/" + dbUser.getUsername();
    }

    @PostMapping("/profile/{username}/review/{bookId}")
    public String profileBookReview(
            @PathVariable String username,
            @PathVariable long bookId,
            @ModelAttribute Review dbReview
    ) {
        User dbUser = userDao.findByUsername(username);
        Book dbBook = bookDao.getOne(bookId);
        dbReview.setOwner(dbUser);
        dbReview.setBook(dbBook);

        reviewDao.save(dbReview);
        return "redirect:/profile/" + dbUser.getUsername();
    }

    @GetMapping("/review.json")
    public @ResponseBody
    List<Review> getReview() {
        return reviewDao.findAll();
    }

    @PostMapping("/profile/{username}/{bookId}/editReview/{reviewId}")
    public String editBookReview(
            @PathVariable String username,
            @PathVariable long bookId,
            @PathVariable long reviewId,
            @ModelAttribute Review reviewToBeEdited
    ) {
        User dbUser = userDao.findByUsername(username);
        Book dbBook = bookDao.getOne(bookId);
        Review extractedReview = reviewDao.getOne(reviewId);

        reviewToBeEdited.setId(reviewId);
        reviewToBeEdited.setOwner(dbUser);
        reviewToBeEdited.setBook(dbBook);
        reviewToBeEdited.setCreatedDate(extractedReview.getCreatedDate());
        reviewDao.save(reviewToBeEdited);

        return "redirect:/profile/" + dbUser.getUsername();
    }

    @PostMapping("/profile/{username}/deleteReview/{reviewId}")
    public String deleteProfileReview(
            @PathVariable String username,
            @PathVariable long reviewId
    ) {
        User dbUser = userDao.findByUsername(username);
        reviewDao.deleteById(reviewId);

        return "redirect:/profile/" + dbUser.getUsername();
    }

    @PostMapping("/profile/{username}/delete")
    public String deleteProfile(
            @PathVariable String username,
            RedirectAttributes redirectAttributes
    ) {
        User dbUser = userDao.findByUsername(username);
        List<Club> clubOwnerList = clubDao.findAllByOwnerId(dbUser.getId());

        if (clubOwnerList.isEmpty()) {
            userDao.deleteById(dbUser.getId());
            SecurityContextHolder.clearContext();
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("clubErr", true);
            return "redirect:/profile/" + dbUser.getUsername();
        }
    }
}
