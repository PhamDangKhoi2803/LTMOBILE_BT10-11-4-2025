package vn.iotstar.bt10_ltmobile.model;

import java.io.Serializable;

public class VideoModel implements Serializable {
    private String desc;
    private String title;
    private String url;
    private String email;
    private String avatarUrl;

    public String getDesc() {
        return desc;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public VideoModel() {

    }
}
