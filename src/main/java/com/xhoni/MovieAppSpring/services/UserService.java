package com.xhoni.MovieAppSpring.services;

import com.xhoni.MovieAppSpring.models.LoginUser;
import com.xhoni.MovieAppSpring.models.PasswordReset;
import com.xhoni.MovieAppSpring.models.User;
import com.xhoni.MovieAppSpring.utils.EmailUtil;
import com.xhoni.MovieAppSpring.utils.env;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.xhoni.MovieAppSpring.repositories.UserRepo;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

import java.io.IOException;
import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    //find all
    public List<User> findUser(){
        return userRepo.findAll();
    }
    //find by id
    public User findUserId(Long id){
        Optional<User> optional = userRepo.findById(id);
        return optional.orElse(null);
    }

    public Optional<User> findbyEmail(String email){
        return userRepo.findByEmail(email);
    }
    //create
    public User createUser(User user){
        return userRepo.save(user);
    }
    //update
    public void updateUser(User user, User editUser, BindingResult result){
        if (result.hasFieldErrors("email")||result.hasFieldErrors("firstName")||result.hasFieldErrors("lastName")){
            return;
        }
        editUser.setFirstName(user.getFirstName());
        editUser.setLastName(user.getLastName());
        editUser.setEmail(user.getEmail());
        userRepo.save(editUser);
    }

    public void updateUserPassword(PasswordReset passwordReset, User editUser, BindingResult result){
        if (result.hasFieldErrors("password")||result.hasFieldErrors("passwordConfirmation")){
            return;
        }
        if (!passwordReset.getNewPassword().equals(passwordReset.getConfirmPassword())){
            result.rejectValue("passwordConfirmation", "Match", "Passwords must match!");
            return;
        }
        if (!BCrypt.checkpw(passwordReset.getOldPassword(), editUser.getPassword())){
            result.rejectValue("oldPassword", "Match", "Old password must match!");
            return;
        }
        editUser.setPassword(BCrypt.hashpw(passwordReset.getNewPassword(), BCrypt.gensalt()));
        userRepo.save(editUser);
    }

    public void updateUserVerificationCode(String verificationCode, User editUser){
        editUser.setVerificationCode(verificationCode);
        userRepo.save(editUser);
    }


    public void deleteUserId(Long id){
        userRepo.deleteById(id);
    }

    public User register(User createUser , BindingResult result) {
        Optional<User> optionalUser = findbyEmail(createUser.getEmail());
        if (optionalUser.isPresent()){
            result.rejectValue("email", "Match", "Email already take!");
        }
        if (!createUser.getPassword().equals(createUser.getPasswordConfirmation())) {
            result.rejectValue("passwordConfirmation", "Match", "Passwords must match!");
        }
        if (result.hasErrors()){
            return null;
        }
        String code = generateCode();
        sendEmail(createUser.getEmail(),code);
        String hashed = BCrypt.hashpw(createUser.getPassword(), BCrypt.gensalt());
        createUser.setPassword(hashed);
        createUser.setVerificationCode(code);
        createUser.setIsVerified(false);
        return userRepo.save(createUser);
    }

    public User loggin(LoginUser loginUser, BindingResult result){
        Optional<User> optionalUser = userRepo.findByEmail(loginUser.getEmail());
        if (optionalUser.isEmpty()){
            result.rejectValue("email", "Match", "Email must match!");
            return null;
        }
        User user = optionalUser.get();
        if (!BCrypt.checkpw(loginUser.getPassword(), user.getPassword())){
            result.rejectValue("password", "Match", "Passwords must match!");
            return null;
        }
        return user;
    }

    public User verify(User user, User userId, BindingResult result){
        if (!user.getVerificationCode().equals(userId.getVerificationCode())){
            result.rejectValue("verificationCode", "Match", "Verification code must match!");
            return null;
        }
        userId.setIsVerified(true);
        return userRepo.save(userId);
    }

    public void sendEmail(String emailAddr, String verificationCode) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(env.EMAIL, env.PASSWORD);
            }
        };
        Session session = Session.getInstance(props, auth);

        EmailUtil.sendEmail(session, emailAddr,"Verify Your Email", "Use this verification code to activate your account: "+verificationCode);
    }

    public String generateCode(){
        String string = "0123456789ABCDEFGHIJKELNOPKQSTUV";
        StringBuilder vCode = new StringBuilder("");
        Random rand = new Random();
        for (int i=0;i<6;i++){
            vCode.append(string.charAt(rand.nextInt(string.length())));
        }
        return vCode.toString();
    }

    public JSONArray getDetails(int id) throws IOException {
        JSONArray results = new JSONArray();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/"+id+"?language=en-US")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response response = client.newCall(request).execute();
        JSONObject responsejson = new JSONObject(response.body().string());
        results.put(responsejson);
        Request videoRequest = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/"+id+"/videos?language=en-US")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response videoResponse = client.newCall(videoRequest).execute();
        String trailerUrl = "notrailer";
        JSONObject videoRESP = new JSONObject(videoResponse.body().string());
        JSONArray videos = (JSONArray) videoRESP.get("results");
        if (!videos.isEmpty()) {
            for (int i = 0; i < videos.length(); i++) {
                JSONObject video = videos.getJSONObject(i);
                if (video.getString("type").equals("Trailer")&&video.getString("site").equals("YouTube")) {
                    trailerUrl = video.getString("key");
                    break;
                }
            }
        }
        results.put(new JSONObject().put("trailer", trailerUrl));
        JSONArray genresArray2 = responsejson.getJSONArray("genres");
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < genresArray2.length() - 1; i++) {
            JSONObject genre = genresArray2.getJSONObject(i);
            genres.append(genre.getInt("id")).append("|");
        }

        if (!genresArray2.isEmpty()) {
            JSONObject lastGenre = genresArray2.getJSONObject(genresArray2.length() - 1);
            genres.append(lastGenre.getInt("id"));
        }

        String genresString = genres.toString();
        Request recommendationRequest = new Request.Builder()
                .url("https://api.themoviedb.org/3/discover/movie?include_adult=false&language=en-US&page=1&sort_by=popularity.desc&with_genres="+genresString)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        results.put(new JSONObject(client.newCall(recommendationRequest).execute().body().string()));
        results.put(new JSONObject(env.genreMap));
        return results;
    }

    public JSONArray getDashboard() throws IOException {
        JSONArray results = new JSONArray();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/popular?language=en-US&page=1&sort_by=popularity.desc")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response response = client.newCall(request).execute();
        results.put(new JSONObject(response.body().string()));
        Request request2 = new Request.Builder()
                .url("https://api.themoviedb.org/3/trending/movie/day?language=en-US")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response response2 = client.newCall(request2).execute();
        results.put(new JSONObject(response2.body().string()));
        Request request3 = new Request.Builder()
                .url("https://api.themoviedb.org/3/trending/movie/week?language=en-US")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response response3 = client.newCall(request3).execute();
        results.put(new JSONObject(response3.body().string()));
        Request request4 = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/upcoming?language=en-US&page=1")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response response4 = client.newCall(request4).execute();
        results.put(new JSONObject(response4.body().string()));
        results.put(new JSONObject(env.genreMap));
        return results;
    }

    public JSONArray getCatalog() throws IOException {
        JSONArray results = new JSONArray();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/popular?language=en-US&page=1&sort_by=popularity.desc")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response response = client.newCall(request).execute();
        results.put(new JSONObject(response.body().string()));
        results.put(new JSONObject(env.genreMap));
        return results;
    }

    public JSONArray getCatalogGenre(int genre) throws IOException {
        JSONArray results = new JSONArray();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/discover/movie?include_adult=false&language=en-US&page=1&sort_by=popularity.desc&with_genres="+genre)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response response = client.newCall(request).execute();
        results.put(new JSONObject(response.body().string()));
        results.put(env.genreMap);
        return results;
    }

    public JSONArray search(String search) throws IOException {
        JSONArray results = new JSONArray();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/search/movie?include_adult=false&page=1&query="+search+"&language=en-US")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer "+env.ACCESS_TOKEN)
                .build();
        Response response = client.newCall(request).execute();
        results.put(new JSONObject(response.body().string()));
        results.put(new JSONObject(env.genreMap));
        return results;
    }

}