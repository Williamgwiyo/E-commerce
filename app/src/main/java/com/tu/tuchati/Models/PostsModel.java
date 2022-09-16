package com.tu.tuchati.Models;


public class PostsModel  {
    private String description,email,postimage,posttime,pid,uid,username,userprofile;

    public PostsModel() {
    }

    public PostsModel(String description, String email, String postimage, String posttime, String pid, String uid, String username, String userprofile) {
        this.description = description;
        this.email = email;
        this.postimage = postimage;
        this.posttime = posttime;
        this.pid = pid;
        this.uid = uid;
        this.username = username;
        this.userprofile = userprofile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getPosttime() {
        return posttime;
    }

    public void setPosttime(String posttime) {
        this.posttime = posttime;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserprofile() {
        return userprofile;
    }

    public void setUserprofile(String userprofile) {
        this.userprofile = userprofile;
    }
}
