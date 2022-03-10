package webapp;

import com.alibaba.fastjson.JSONObject;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
public class Assignment1 {
    private static final StatsDClient statsd = new NonBlockingStatsDClient("statsd", "localhost", 8125);
    private static final MySNS mySNS = new MySNS();
    private static final MyDynamodb myDynamodb = new MyDynamodb();

    private User Auth(String token) throws SQLException {
        if (token == null || token.equals(""))
            return null;
        String[] userAndPass = new String(Base64.getDecoder().decode(token.split(" ")[1])).split(":");
        if (userAndPass.length < 2)
            return null;
        return UserDAO.getUser(userAndPass[0], userAndPass[1]);
    }

    public static boolean isMail(String str) {
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return Pattern.compile(regEx1).matcher(str).matches();
    }

    @RequestMapping(value = "/v1/user/self", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public User getUserSelf(@RequestHeader("Authorization") String token, HttpServletResponse response) throws SQLException {
        statsd.increment("get.user.self.counter");
        long startTime = System.currentTimeMillis();
        User user = Auth(token);
        if (user == null) {
            response.setStatus(400);
            return null;
        }
        statsd.recordExecutionTimeToNow("get.user.self.timer", startTime);
        return user;
    }

    @RequestMapping(value = "/v1/user/self", method = RequestMethod.PUT, produces = "application/json")
    public void putUserSelf(@RequestHeader("Authorization") String token, @RequestBody String newInfo, HttpServletResponse response) throws SQLException {
        statsd.increment("put.user.self.counter");
        long startTime = System.currentTimeMillis();
        User user = Auth(token);
        JSONObject object = JSONObject.parseObject(newInfo);
        String userName = (String) object.get("username");
        String passWord = (String) object.get("password");
        String firstName = (String) object.get("first_name");
        String lastName = (String) object.get("last_name");
        String updateTime = new Date().toString();
        if (userName == null && passWord == null && firstName == null && lastName == null) {
            response.setStatus(204);
            return;
        }
        if (user == null || object.get("username") == null || !object.get("username").equals(user.getUsername())) {
            response.setStatus(400);
            return;
        }
        user.setAccount_updated(updateTime);
        if (passWord != null)
            user.setPassword(passWord);
        if (firstName != null)
            user.setFirst_name(firstName);
        if (lastName != null)
            user.setLast_name(lastName);
        UserDAO.updateUser(user);
        statsd.recordExecutionTimeToNow("put.user.self.timer", startTime);
    }

    @RequestMapping(value = "/v1/user", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public User postUserSelf(@RequestBody String newInfo, HttpServletResponse response) throws SQLException {
        statsd.increment("post.user.counter");
        long startTime = System.currentTimeMillis();
        JSONObject object = JSONObject.parseObject(newInfo);
        String userName = (String) object.get("username");
        String passWord = (String) object.get("password");
        String firstName = (String) object.get("first_name");
        String lastName = (String) object.get("last_name");
        String createTime = new Date().toString();
        if (userName == null || passWord == null || firstName == null || lastName == null || UserDAO.getUser(userName) != null || !isMail(userName)) {
            response.setStatus(400);
            return null;
        }
        User user = new User(UUID.randomUUID().toString(), userName, passWord, BCrypt.gensalt(), firstName, lastName, createTime, createTime, false, false, "");
        UserDAO.addUser(user);
        mySNS.publish(new Message(userName, user.getId()).toString());
        response.setStatus(201);
        statsd.recordExecutionTimeToNow("post.user.timer", startTime);
        return user;
    }

    @RequestMapping(value = "/v1/user/verify/{token}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String GetUserVerify(HttpServletResponse response, @PathVariable String token) throws SQLException {
        if (myDynamodb.containKey(token)) {
            String verifiedOn = new Date().toString();
            UserDAO.verifyUser(token, verifiedOn);
            response.setStatus(200);
            return "Verified Successfully!";
        } else {
            response.setStatus(400);
            return "Wrong token!";
        }
    }

    @RequestMapping(value = "/v1/user/self/pic", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Profile postUserSelfPic(@RequestHeader("Authorization") String token, @RequestParam("image") MultipartFile file, HttpServletResponse response) throws SQLException, IOException {
        statsd.increment("post.user.self.pic.counter");
        long startTime = System.currentTimeMillis();
        User user = Auth(token);
        if (user == null) {
            response.setStatus(400);
            return null;
        }
        InputStream profilePic = file.getInputStream();
        String file_name = file.getOriginalFilename();
        String url = System.getenv("BUCKET_NAME") + "/" + user.getId() + "/" + file_name;
        String upload_date = new Date().toString();
        Profile profile = new Profile(UUID.randomUUID().toString(), file_name, url, upload_date, user.getId());
        ProfileDAO.deleteProfile(user.getId());
        ProfileDAO.addProfile(profile, profilePic);
        response.setStatus(201);
        statsd.recordExecutionTimeToNow("post.user.self.pic.timer", startTime);
        return profile;
    }

    @RequestMapping(value = "/v1/user/self/pic", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Profile getUserSelfPic(@RequestHeader("Authorization") String token, HttpServletResponse response) throws SQLException {
        statsd.increment("get.user.self.pic.counter");
        long startTime = System.currentTimeMillis();
        User user = Auth(token);
        if (user == null) {
            response.setStatus(400);
            return null;
        }
        Profile profile = ProfileDAO.getProfile(user.getId());
        if (profile == null) {
            response.setStatus(404);
            return null;
        }
        response.setStatus(200);
        statsd.recordExecutionTimeToNow("get.user.self.pic.timer", startTime);
        return profile;
    }

    @RequestMapping(value = "/v1/user/self/pic", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Profile deleteUserSelfPic(@RequestHeader("Authorization") String token, HttpServletResponse response) throws SQLException {
        statsd.increment("delete.user.self.pic.counter");
        long startTime = System.currentTimeMillis();
        User user = Auth(token);
        if (user == null) {
            response.setStatus(401);
            return null;
        }
        Profile profile = ProfileDAO.deleteProfile(user.getId());
        if (profile == null) {
            response.setStatus(404);
            return null;
        }
        response.setStatus(200);
        statsd.recordExecutionTimeToNow("delete.user.self.pic.timer", startTime);
        return profile;
    }
}