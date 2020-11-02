package com.example.myapplication;


import com.google.gson.annotations.SerializedName;

public class UserRequest {
    @SerializedName("env")
    private String env;
    @SerializedName("name")
    private String name;
    @SerializedName("lastname")
    private String lastname;
    @SerializedName("dni")
    private Long dni;
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("commission")
    private Long commission;

    public String getAmbiente(){ return env; }
    public void setAmbiente(String env){ this.env = env; }

    public String getNombre(){ return name; }
    public void setNombre(String name){ this.name = name; }

    public String getApellido(){ return lastname; }
    public void setApellido(String lastname){ this.lastname = lastname; }

    public Long getDni(){ return dni; }
    public void setDni(Long dni){ this.dni = dni; }

    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email = email; }

    public String getContraseña(){ return password; }
    public void setContraseña(String password){ this.password = password; }

    public Long getComision(){ return commission; }
    public void setComision(Long commission){ this.commission = commission; }
}
