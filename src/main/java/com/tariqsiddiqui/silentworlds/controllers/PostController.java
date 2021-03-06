package com.tariqsiddiqui.silentworlds.controllers;

import com.tariqsiddiqui.silentworlds.models.*;
import com.tariqsiddiqui.silentworlds.repositories.*;
import com.tariqsiddiqui.silentworlds.models.*;
import com.tariqsiddiqui.silentworlds.services.UserService;
import com.tariqsiddiqui.silentworlds.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@Controller
public class PostController {

    private UserRepo userDao;
    private ClubRepo clubDao;
    private ClubMemberRepo clubMemberDao;
    private PostRepo postDao;
    private CommentRepo commentDao;

    @Autowired
    UserService usersSvc;

    public PostController(UserRepo userDao, ClubRepo clubDao, ClubMemberRepo clubMemberDao, PostRepo postDao, CommentRepo commentDao) {
        this.userDao = userDao;
        this.clubDao = clubDao;
        this.clubMemberDao = clubMemberDao;
        this.postDao = postDao;
        this.commentDao = commentDao;
    }

    //Create Post
    @GetMapping("/bookclub/{id}/create-post")
    public String viewCreatePostForm(Model viewModel, @PathVariable long id) {
        viewModel.addAttribute("post", new Post());
        viewModel.addAttribute("club", clubDao.getOne(id));
        return "user/create-post";
    }

    @PostMapping("/bookclub/{id}/create-post")
    public String createPost(
            @Valid Post post,
            Errors validation,
            @PathVariable long id,
            @ModelAttribute Club club,
            Model viewModel
    ) {

        if (validation.hasErrors()) {
            viewModel.addAttribute("errors", validation);
            viewModel.addAttribute("post", post);
            return "user/create-post";
        }

        Date currentDate = new Date();
        User user = usersSvc.loggedInUser();

        post.setClub(club);
        post.setUser(user);
        post.setCreatedDate(currentDate);
        post.setUpdatedDate(currentDate);

        Post dbPost = postDao.save(post);

        return "redirect:/bookclub/post/" + dbPost.getId();
    }

    //View Post
    @GetMapping("/bookclub/post/{postId}")
    public String viewPost(
            Model viewModel,
            @PathVariable long postId) {

        User user = usersSvc.loggedInUser();
        if(user == null) {
            return "redirect:/login";
        }

        Post post = postDao.getOne(postId);
        Club club = post.getClub();
        viewModel.addAttribute("post", post);
        viewModel.addAttribute("club", club);
        viewModel.addAttribute("comments", commentDao.findAllByPost(post));
        viewModel.addAttribute(user);
        ClubMember clubMember = clubMemberDao.findClubMemberByUserAndClub(user, club);
        viewModel.addAttribute("member", clubMember);
        viewModel.addAttribute("comment", new Comment());

        return "user/club-post";
    }

    //Comment on Post
    @PostMapping("/bookclub/{id}/{postId}/comment")
        public String leaveComment(
                @PathVariable long id,
                @PathVariable long postId,
                @ModelAttribute Comment comment){

        Comment dbComment = comment;
        Date currentDate = new Date();
        User user = usersSvc.loggedInUser();
        Post post = postDao.getOne(postId);

        dbComment.setBody(comment.getBody());
        dbComment.setPost(post);
        dbComment.setUser(user);
        dbComment.setCreatedDate(currentDate);
        dbComment.setUpdatedDate(currentDate);

        commentDao.save(dbComment);

        return "redirect:/bookclub/post/" + postId;
    }

    @PostMapping("/bookclub/{id}/{postId}/delete-comment-{commentId}")
    public String deleteComment(
            @PathVariable long id,
            @PathVariable long postId,
            @PathVariable long commentId){
        Comment comment = commentDao.getOne(commentId);
        User commentOwner = comment.getUser();

        if(commentOwner != usersSvc.loggedInUser()) {
            return "redirect:/bookclub/post/" + postId;
        }

        commentDao.delete(comment);
        return "redirect:/bookclub/post/" + postId;
    }

    //Edit Post
    @GetMapping("/bookclub/{id}/edit-post/{postId}")
    public String viewEditPostForm(
            Model viewModel,
            @PathVariable long id,
            @PathVariable long postId) {

        Post post = postDao.getOne(postId);
        Club club = clubDao.getOne(id);

        if(!usersSvc.isOwner(post.getUser())){
            return "redirect:/bookclub/post/" + postId;
        }
        viewModel.addAttribute("post", post);
        viewModel.addAttribute("club", club);

        System.out.println(post);

        return "user/edit-post";
    }

    @PostMapping("/bookclub/{id}/edit-post/{postId}")
    public String editPost(
            @ModelAttribute Post postToBeUpdated,
            @ModelAttribute Club club,
            @PathVariable long id,
            @PathVariable long postId) {

        Post post = postDao.getOne(postId);
        Date currentDate = new Date();

       System.out.println(post.getCreatedDate());
        System.out.println(post);

        postToBeUpdated.setId(post.getId());
        postToBeUpdated.setComments(post.getComments());
        postToBeUpdated.setUpdatedDate(currentDate);
        postToBeUpdated.setCreatedDate(post.getCreatedDate());
        postToBeUpdated.setUser(post.getUser());
        postToBeUpdated.setClub(post.getClub());

        Post dbPost = postDao.save(postToBeUpdated);
        return "redirect:/bookclub/post/" + postId;
    }



    //Delete Post
    @RequestMapping(value = "/bookclub/{id}/delete-post/{postId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String deletePost(@PathVariable long id, @PathVariable long postId) {
        Post post = postDao.getOne(postId);

        if(!usersSvc.isOwner(post.getUser())){
            return "redirect:/bookclub/" + id;
        }

        postDao.delete(post);

        return "redirect:/bookclub/" + id;
    }
}
