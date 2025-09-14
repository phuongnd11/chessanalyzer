package com.inspireon.chessanalyzer.web.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerProfile {
    
    @JsonProperty("@id")
    private String id;
    
    private String url;
    private String username;
    
    @JsonProperty("player_id")
    private Integer playerId;
    
    private String title;
    private String status;
    private String name;
    private String avatar;
    private String location;
    private String country;
    private Long joined;
    
    @JsonProperty("last_online")
    private Long lastOnline;
    
    private Integer followers;
    
    @JsonProperty("is_streamer")
    private Boolean isStreamer;
    
    @JsonProperty("twitch_url")
    private String twitchUrl;
    
    private Integer fide;
    
    public PlayerProfile() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Integer getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public Long getJoined() {
        return joined;
    }
    
    public void setJoined(Long joined) {
        this.joined = joined;
    }
    
    public Long getLastOnline() {
        return lastOnline;
    }
    
    public void setLastOnline(Long lastOnline) {
        this.lastOnline = lastOnline;
    }
    
    public Integer getFollowers() {
        return followers;
    }
    
    public void setFollowers(Integer followers) {
        this.followers = followers;
    }
    
    public Boolean getIsStreamer() {
        return isStreamer;
    }
    
    public void setIsStreamer(Boolean isStreamer) {
        this.isStreamer = isStreamer;
    }
    
    public String getTwitchUrl() {
        return twitchUrl;
    }
    
    public void setTwitchUrl(String twitchUrl) {
        this.twitchUrl = twitchUrl;
    }
    
    public Integer getFide() {
        return fide;
    }
    
    public void setFide(Integer fide) {
        this.fide = fide;
    }
}