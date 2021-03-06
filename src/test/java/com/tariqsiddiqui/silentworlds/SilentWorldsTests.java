package com.tariqsiddiqui.silentworlds;

import com.tariqsiddiqui.silentworlds.models.*;
import com.tariqsiddiqui.silentworlds.repositories.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SilentWorldsApplication.class)
@AutoConfigureMockMvc
public class SilentWorldsTests {

    private User testUser;
    private Book testBook;
    private Book testBook2;
    private HttpSession httpSession;

    @Autowired
    private MockMvc mvc;

    @Autowired
    UserRepo userDao;

    @Autowired
    PostRepo postDao;

    @Autowired
    ClubRepo clubDao;

    @Autowired
    BookRepo bookDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReviewRepo reviewDao;


    @Before
    public void setup() throws Exception {

        testUser = userDao.findByUsername("TestUsername");
        testBook = bookDao.findBookById(1);
        testBook2 = bookDao.findBookById(2);

        //CREATE

        // Creates the test user if not exists
        if (testUser == null) {
            User newUser = new User();
            newUser.setEmail("TestEmail@Test.com");
            newUser.setUsername("TestUsername");
            newUser.setPassword("TestPassword");
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            newUser.setFirstName("TestFirstName");
            newUser.setMiddleName("TestMiddleName");
            newUser.setLastName("TestLastName");
            newUser.setPronouns("TestPronoun");
            newUser.setAboutMe("TestAboutMe");
            newUser.setCountry("TestCountry");
            newUser.setWebsiteURL("TestWebsiteURL");
            newUser.setAvatarURL("TestAvatarURL");
            newUser.setDob(new Date());
            newUser.setCreatedDate(new Date());
            testUser = userDao.save(newUser);
        }

        if (testBook == null) {
            Book newBook = new Book();
            newBook.setGbreference("TestGbreference");
            testBook = bookDao.save(newBook);
        }
        if (testBook2 == null) {
            Book newBook = new Book();
            newBook.setGbreference("TestGbreference2");
            testBook2 = bookDao.save(newBook);
        }


        // Throws a Post request to /login and expect a redirection to the home page after being logged in
        httpSession = this.mvc.perform(post("/login").with(csrf())
                .param("username", "TestUsername")
                .param("password", "TestPassword"))
                .andExpect(status().is(HttpStatus.FOUND.value()))
                .andExpect(redirectedUrl("/"))
                .andReturn()
                .getRequest()
                .getSession();
    }

    //Sanity Check
    // just to make sure the MVC bean is working
    @Test
    public void contextLoads() {
        assertNotNull(mvc);
    }

    @Test
    public void testIfUserSessionIsActive() throws Exception {
        // It makes sure the returned session is not null
        assertNotNull(httpSession);
    }

    @Test
    public void testShowRegistration() throws Exception {
        this.mvc.perform(get("/sign-up"))
                .andExpect(status().isOk());
    }



// CLUB TESTING

    //Create Club
    @Test
    public void testCreateClub() throws Exception {
        this.mvc.perform(
                post("/create-club").with(csrf())
                        .session((MockHttpSession) httpSession)
                        // Add all the required parameters to your request like this
                        .param("name", "test")
                        .param("about", "book club about")
                        .param("genre", "1")
                        .param("headerImageUrl", "/img/logo.png"))
                .andExpect(status().is3xxRedirection());
    }

    //Read club
    @Test
    public void testShowClub() throws Exception {

        Club existingClub = clubDao.findAll().get(0);

        this.mvc.perform(get("/bookclub/" + existingClub.getId()))
                .andExpect(status().isOk())
                // Test the dynamic content of the page
                .andExpect(content().string(containsString(existingClub.getName())));
    }

    //Read all clubs
    @Test
    public void testAdsIndex() throws Exception {
        Club existingClub = clubDao.findAll().get(0);

        this.mvc.perform(get("/bookclubs"))
                .andExpect(status().isOk())
                // Test the static content of the page
                .andExpect(content().string(containsString("<label for=\"genreSelect\">Filter By Genre:</label>")))
                // Test the dynamic content of the page
                .andExpect(content().string(containsString(existingClub.getName())));
    }

    //Edit Club
    @Test
    public void testEditClub() throws Exception {
        Club existingClub = clubDao.findAll().get(0);

        this.mvc.perform(
                post("/edit-bookclub/" + existingClub.getId()).with(csrf())
                        .session((MockHttpSession) httpSession)
                        .param("name", "edited name")
                        .param("about", "edited about")
                        .param("genre", "1"))
                .andExpect(status().is3xxRedirection());

        this.mvc.perform(get("/bookclub/" + existingClub.getId()))
                .andExpect(status().isOk())
                // Test the dynamic content of the page
                .andExpect(content().string(containsString("edited name")))
                .andExpect(content().string(containsString("edited about")));
    }

    //    Delete Club
    // Owner was being assigned by username instead of user that is logged in, solved this issue.
    @Test
    public void testDeleteClub() throws Exception {
        this.mvc.perform(
                post("/create-club").with(csrf())
                        .session((MockHttpSession) httpSession)
                        .param("name", "club yay")
                        .param("about", "won't last long"))
                .andExpect(status().is3xxRedirection());

        Club existingClub = clubDao.findByName("club yay");
        System.out.println(existingClub.getId());

        this.mvc.perform(
                post("/bookclub/" + existingClub.getId() + "/delete").with(csrf())
                        .session((MockHttpSession) httpSession))
                .andExpect(status().is3xxRedirection());

    }

// CLUB MEMBERS TESTING

    // Join Club
    //User can join a club multiple times, fixed the issue in the Club Controller.
    @Test
    public void testJoinClub() throws Exception {
        Club existingClub = clubDao.findAll().get(0);

        this.mvc.perform(
                post("/bookclub/" + existingClub.getId() + "/join").with(csrf())
                        .session((MockHttpSession) httpSession))
                .andExpect(status().is3xxRedirection());
    }

    //Leave Club
    //Throws an error because a user cannot "leave" a club they are not already a part of, solved issue by adding a conditional to check if the user is a member of the club first, and then deletes them from the club.
    @Test
    public void testLeaveClub() throws Exception {
        Club existingClub = clubDao.findAll().get(0);

        this.mvc.perform(
                post("/bookclub/" + existingClub.getId() + "/leave").with(csrf())
                        .session((MockHttpSession) httpSession))
                .andExpect(status().is3xxRedirection());
    }

//CLUB BLOG TESTING

    //Create Post
    @Test
    public void testCreatePost() throws Exception {
        Club existingClub = clubDao.findAllByOwner(testUser).get(0);

        this.mvc.perform(
                post("/bookclub/" + existingClub.getId() + "/create-post").with(csrf())
                        .session((MockHttpSession) httpSession)
                        // Add all the required parameters to your request like this
                        .param("body", "test body")
                        .param("title", "test title"))
                .andExpect(status().is3xxRedirection());
    }

//    //View Post
//    @Test
//    public void testViewPost() throws Exception {
//        Club existingClub = clubDao.findAllByOwner(testUser).get(0);
//        Post existingPost = postDao.findAllByClub(existingClub).get(0);
//        System.out.println(existingClub.getId());
//        System.out.println(existingPost.getId());
//
//        this.mvc.perform(get("/bookclub/post/" + existingPost.getId()).with(csrf()))
//                .andExpect(status().isOk())
//                // Test the dynamic content of the page
//                .andExpect(content().string(containsString(existingClub.getName())));
//    }
//
//    //Edit Post
//    @Test
//    public void testEditPost() throws Exception {
//        Club existingClub = clubDao.findAllByOwner(testUser).get(0);
//        Post existingPost = postDao.findAllByClub(existingClub).get(0);
//
//        this.mvc.perform(
//                post("/bookclub/" + existingClub.getId() + "/edit-post/" + existingPost.getId()).with(csrf())
//                        .session((MockHttpSession) httpSession)
//                        .param("title", "edited title")
//                        .param("body", "edited body"))
//                .andExpect(status().is3xxRedirection());
//
//        this.mvc.perform(get("/bookclub/" + existingClub.getId() + "/" + existingPost.getId()).with(csrf()))
//                .andExpect(status().isOk())
//                // Test the dynamic content of the page
//                .andExpect(content().string(containsString("edited title")))
//                .andExpect(content().string(containsString("edited body")));
//    }


    //PROFILE TESTING

    //Get to profile page
    @Test
    public void testAuthGetProfile() throws Exception {
        this.mvc.perform(
                get("/profile/" + testUser.getUsername()).with(csrf())
                        .session((MockHttpSession) httpSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(testUser.getUsername()))
                );

    }


    //REVIEW TESTING

    //Create Review From Profile Page
    @Test
    public void testCreateReview() throws Exception {

        this.mvc.perform(
                post("/profile/" + testUser.getUsername() + "/review/" + testBook.getId()).with(csrf())
                        .session((MockHttpSession) httpSession)
                        .param("rating", "5")
                        .param("body", "Testing Body"))
                .andExpect(status().is3xxRedirection());

    }

    //Read Review From /review.json Called on Profile Page
    @Test
    public void testShowReview() throws Exception {

        this.mvc.perform(get("/review.json"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id\":1,\"rating\":5,\"body\":\"Testing Body")));

    }

    //Update Review From Profile Page
    @Test
    public void testUpdateReview() throws Exception {
        Review existingReview = reviewDao.findByOwnerIdAndBookId(testUser.getId(), testBook.getId());

        this.mvc.perform(
                post("/profile/" + testUser.getUsername() + "/" + testBook.getId() + "/editReview/" + existingReview.getId()).with(csrf())
                        .session((MockHttpSession) httpSession)
                        .param("rating", "3")
                        .param("body", "Testing Update"))
                .andExpect(status().is3xxRedirection());

        this.mvc.perform(get("/review.json"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(("id\":1,\"rating\":3,\"body\":\"Testing Update"))));

    }

    //Delete Review From Profile Page
    @Test
    public void testDeleteReview() throws Exception {
        this.mvc.perform(
                post("/profile/" + testUser.getUsername() + "/review/" + testBook2.getId()).with(csrf())
                        .session((MockHttpSession) httpSession)
                        .param("rating", "1")
                        .param("body", "Testing Delete"))
                .andExpect(status().is3xxRedirection());

        Review existingReview = reviewDao.findByOwnerIdAndBookId(testUser.getId(), testBook2.getId());

        this.mvc.perform(
                post("/profile/" + testUser.getUsername() + "/deleteReview/" + existingReview.getId()).with(csrf())
                        .session((MockHttpSession) httpSession)
                        .param("id", String.valueOf(existingReview.getId())))
                .andExpect(status().is3xxRedirection());
    }

    //Create Bookshelf Test
    @Test
    public void testAddBookToBookshelf() throws Exception {
        this.mvc.perform(
                post("/book/" + testBook.getGbreference()).with(csrf())
                        .session((MockHttpSession) httpSession)
                        .param("ownerId", String.format("%s", testUser.getId()))
                        .param("bookId", String.format("%s", testBook.getId()))
                        .param("bookshelfStatus", "READ"))
                .andExpect(status().is3xxRedirection());
    }


    // BOOKSEARCH TESTING
    // Public get
    @Test
    public void testAnonGetBooksearch() throws Exception {
        this.mvc.perform(
                get("/booksearch"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Search")));
    }

    // Authenticated get
    @Test
    public void testAuthGetBooksearch() throws Exception {
        this.mvc.perform(
                get("/booksearch").with(csrf())
                        .session((MockHttpSession) httpSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Search")));
    }

    // Query string public get
    @Test
    public void testAnonQueryStringBooksearch() throws Exception {
        String query = "The%20Hobbit";

        this.mvc.perform(
                get("/booksearch?searchvalue=" + query))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The%20Hobbit")));
    }

    // Query string auth get
    @Test
    public void testAuthQueryStringBooksearch() throws Exception {
        String query = "The%20Hobbit";

        this.mvc.perform(
                get("/booksearch?searchvalue=" + query).with(csrf())
                        .session((MockHttpSession) httpSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The%20Hobbit")));
    }

    // VIEWBOOK TESTING
    // Public get
    @Test
    public void testAnonGetBook() throws Exception {
        String bookRef = "rH9mA0MpLM4C";
        Book testbook = bookDao.findBookByGbreferenceEquals(bookRef);
        Long bookId = testbook.getId();

        this.mvc.perform(
                get("/book/" + bookRef))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format("data-book-id=\"%d\"", bookId))))
                .andExpect(content().string(containsString("Login to add this book")))
                .andExpect(content().string(containsString("Login to leave a review")));
    }

    // Authenticated get
    @Test
    public void testAuthGetBook() throws Exception {
        String bookRef = "rH9mA0MpLM4C";
        Book testbook = bookDao.findBookByGbreferenceEquals(bookRef);
        Long bookId = testbook.getId();

        this.mvc.perform(
                get("/book/" + bookRef).with(csrf())
                        .session((MockHttpSession) httpSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.format("data-book-id=\"%d\"", bookId))))
                .andExpect(content().string(containsString("Choose a Status")));
    }

    // ERROR PAGE TESTS
    @Test
    public void testAnon404Error() throws Exception {
        this.mvc.perform(
                get("/this-page-not-found"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testAuth404Error() throws Exception {
        this.mvc.perform(
                get("/this-page-not-found")
                        .with(csrf())
                        .session((MockHttpSession) httpSession))
                .andExpect(status().is4xxClientError());
    }





}

