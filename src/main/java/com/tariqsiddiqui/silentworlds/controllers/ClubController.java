package com.tariqsiddiqui.silentworlds.controllers;

import com.tariqsiddiqui.silentworlds.models.Club;
import com.tariqsiddiqui.silentworlds.models.ClubMember;
import com.tariqsiddiqui.silentworlds.models.Genre;
import com.tariqsiddiqui.silentworlds.models.User;
import com.tariqsiddiqui.silentworlds.models.*;
import com.tariqsiddiqui.silentworlds.repositories.*;
import com.tariqsiddiqui.silentworlds.services.UserService;
import com.tariqsiddiqui.silentworlds.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Controller
public class ClubController {

    private UserRepo userDao;
    private ClubRepo clubDao;
    private ClubMemberRepo clubMemberDao;
    private PostRepo postDao;
    private GenreRepo genreDao;

    @Autowired
    UserService usersSvc;

    public ClubController(UserRepo userDao, ClubRepo clubDao, ClubMemberRepo clubMemberDao, PostRepo postDao, GenreRepo genreDao) {
        this.userDao = userDao;
        this.clubDao = clubDao;
        this.clubMemberDao = clubMemberDao;
        this.postDao = postDao;
        this.genreDao = genreDao;
    }

    // Create Club
    @GetMapping("/create-club/{username}")
    public String showCreateClub(Model viewModel, @PathVariable String username) {
        List<Genre> genreList = genreDao.findAll();
        Club club = new Club();
        viewModel.addAttribute("user", userDao.findByUsername(username));
        viewModel.addAttribute("club", club);
        viewModel.addAttribute("genres", genreList);

        String defaultIMG = "/img/logo.png";
        club.setHeaderImageUrl(defaultIMG);
        return "user/create-club";
    }


    @PostMapping("/create-club")
    public String createClub(@ModelAttribute @Valid Club club,
                             Errors validation,
                             Model viewModel) {

        User user = usersSvc.loggedInUser();
        Date currentDate = new Date();
        List<Genre> genreList = genreDao.findAll();

        if (validation.hasErrors()) {
            System.err.println("===== CREATE Club Validation errors FOUND, redirecting back =====");
            viewModel.addAttribute("errors", validation);
            viewModel.addAttribute("club", club);
            viewModel.addAttribute("genres", genreList);
            return "user/create-club";
        }

        club.setOwner(user);
        club.setCreatedDate(currentDate);


        Club dbClub = clubDao.save(club);

        ClubMember clubMember = new ClubMember();

        clubMember.setClub(dbClub);
        clubMember.setUser(user);
        clubMember.setIsAdmin(true);
        clubMemberDao.save(clubMember);

        return "redirect:/bookclub/" + dbClub.getId();
    }

    // View Club Page
    @GetMapping("/bookclub/{id}")
    public String showBookClub(
            Model viewModel,
            @PathVariable long id,
            @ModelAttribute Club club) {
        viewModel.addAttribute("user", usersSvc.loggedInUser());
        viewModel.addAttribute("club", clubDao.getOne(id));
        viewModel.addAttribute("members", clubMemberDao.findAllByClub(club));
        viewModel.addAttribute("posts", postDao.findAllByClubOrderByIdDesc(club));

        // For the conditional in the bookclub template; prevents users from joining a club multiple times!

        User clubUser = usersSvc.loggedInUser();
        ClubMember clubMember = clubMemberDao.findClubMemberByUserAndClub(clubUser, club);
        viewModel.addAttribute("member", clubMember);


        return "user/bookclub";
    }

    //Join Club
    @PostMapping("/bookclub/{id}/join")
    public String joinBookClub(@PathVariable long id) {
        User clubUser = usersSvc.loggedInUser();
        Club club = clubDao.getOne(id);
        ClubMember existingClubMember = clubMemberDao.findClubMemberByUserAndClub(clubUser, club);

        //Checks to see if a user is already a member
        if (existingClubMember == null) {
            ClubMember clubMember = new ClubMember();
            clubMember.setClub(clubDao.getOne(id));
            clubMember.setUser(clubUser);
            clubMember.setIsAdmin(false);

            clubMemberDao.save(clubMember);
            System.out.println(clubUser.getId());
            System.out.println(clubMember.getId());

        }
        return "redirect:/bookclub/" + id;
    }

    //Leave Club
    @PostMapping("/bookclub/{id}/leave")
    public String leaveBookClub(@PathVariable long id) {
        User clubUser = usersSvc.loggedInUser();
        Club club = clubDao.getOne(id);
        ClubMember existingClubMember = clubMemberDao.findClubMemberByUserAndClub(clubUser, club);

        if(existingClubMember != null) {
            clubMemberDao.deleteById(existingClubMember.getId());
            return "redirect:/bookclub/" + id;
        }

        return "redirect:/bookclub/" + id;
    }

    //Edit Club Page
    @GetMapping("/edit-bookclub/{id}")
    public String showEditBookClub (Model viewModel, @PathVariable long id) {
        User user = usersSvc.loggedInUser();
        Club club = clubDao.getOne(id);
        ClubMember clubMember = clubMemberDao.findClubMemberByUserAndClub(user, club);

        if(!usersSvc.isAdmin(clubMember)){
            return "redirect:/bookclub/" + id;
        }

        viewModel.addAttribute("club", club);
        List<Genre> genreList = genreDao.findAll();
        viewModel.addAttribute("genres", genreList);
        System.out.println(club.getId());
        System.out.println(club.getCreatedDate());

        return "user/edit-bookclub";
    }

    @PostMapping("/edit-bookclub/{id}")
    public String editProfile(@PathVariable long id,
                              @ModelAttribute @Valid Club clubToBeUpdated,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model viewModel,
                              Errors validation) {
        Club club = clubDao.getOne(id);

//        if (result.hasErrors()) {
//              List<Genre> genreList = genreDao.findAll();
////            viewModel.addAttribute("errors", validation);
////            viewModel.addAttribute("club", club);
////            viewModel.addAttribute("genres", genreList);
//            System.err.println("===== EDIT Club Validation errors FOUND, redirecting back =====");
//            redirectAttributes.addFlashAttribute("errors", validation);
////            redirectAttributes.addFlashAttribute("club", club);
////            redirectAttributes.addFlashAttribute("genres", genreList);
////            return  "user/edit-bookclub";
//            return  "redirect:/edit-bookclub/" + id;
//        }

        clubToBeUpdated.setOwner(club.getOwner());
        clubToBeUpdated.setCreatedDate(club.getCreatedDate());

        Club dbClub = clubDao.save(clubToBeUpdated);
        return "redirect:/bookclub/" + dbClub.getId();
    }


    //Delete club
    @RequestMapping(value = "/bookclub/{id}/delete", method = { RequestMethod.POST })
    public String deleteClub(@PathVariable long id) {
        Club club = clubDao.getOne(id);
        User user = usersSvc.loggedInUser();

        if(user != club.getOwner()){
            return "redirect:/bookclub/" + id;
        }

        clubDao.delete(club);

        return "redirect:/profile/" + user.getUsername();
    }

    //Assign Admin
    @PostMapping("/bookclub/{id}/admin/{userId}")
    public String makeAdmin(@PathVariable long id, @PathVariable long userId) {
        User user = userDao.getOne(userId);
        Club club = clubDao.getOne(id);
        ClubMember clubMember = clubMemberDao.findClubMemberByUserAndClub(user, club);

        clubMember.setIsAdmin(true);
        clubMemberDao.save(clubMember);

        return "redirect:/bookclub/" + id;
    }

    //Revoke Admin Status
    @PostMapping("/bookclub/{id}/member/{userId}")
    public String removeAdminStatus(@PathVariable long id, @PathVariable long userId) {
        User user = userDao.getOne(userId);
        Club club = clubDao.getOne(id);
        ClubMember clubMember = clubMemberDao.findClubMemberByUserAndClub(user, club);

        if(clubMember.getUser() == club.getOwner()){
            return "redirect:/bookclub/" + id;
        }

        clubMember.setIsAdmin(false);
        clubMemberDao.save(clubMember);

        return "redirect:/bookclub/" + id;
    }

    //Go to bookclubs page
    @GetMapping("/bookclubs")
    public String showBookclubs(Model viewModel,
                                @ModelAttribute Genre genre
    ) {
        List<Club> bookclubs = clubDao.findAll();
        List<Genre> filterGenreList = genreDao.findAll();
        viewModel.addAttribute("bookclubs", bookclubs);
        viewModel.addAttribute("filterGenreList", filterGenreList);
        return "user/all-bookclubs";
    }

    //handle bookclub search
    @PostMapping("/bookclub/search")
    public String bookclubSearch(@RequestParam(name = "query") String query,
                                 @RequestParam(name = "genreSelect") long genreSelect,
                                 Model viewModel,
                                 RedirectAttributes redirectAttributes
    ) {
        List<Genre> filterGenreList = genreDao.findAll();
        String queryMod = "%" + query + "%";

        if (genreSelect == 0 && query.equals("")) {
            redirectAttributes.addFlashAttribute("noQueryMsg", "You didn't search any values...");
            return "redirect:/bookclubs";
        } else if (genreSelect > 0) {
            List<Club> retreivedClubs = clubDao.findByGenreIdAndNameIsLike(genreSelect, queryMod);
            viewModel.addAttribute("filterGenre", genreSelect);
            viewModel.addAttribute("bookclubs", retreivedClubs);
            viewModel.addAttribute("filterGenreList", filterGenreList);
            viewModel.addAttribute("currentGenre", genreSelect);
            return "user/search-bookclub";
        } else {
            List<Club> retreivedClubs = clubDao.findByNameIsLike(queryMod);
            viewModel.addAttribute("bookclubs", retreivedClubs);
            viewModel.addAttribute("filterGenreList", filterGenreList);
            viewModel.addAttribute("currentGenre", genreSelect);
            return "user/search-bookclub";
        }
    }
}
