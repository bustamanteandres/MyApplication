package com.example.myapplication;

public class UserResponse {
    private Boolean success;
    private String env;
    private String token;
    private String token_refresh;
    private String msg;

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getAmbiente() { return env; }
    public void setAmbiente(String env) { this.env = env; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenRefresh() { return token_refresh; }
    public void setTokenRefresh(String tokenRefresh) { this.token_refresh = tokenRefresh; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

}
