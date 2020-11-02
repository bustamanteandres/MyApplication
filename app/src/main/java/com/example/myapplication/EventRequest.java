package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class EventRequest {
    @SerializedName("env")
    private String env;
    @SerializedName("type_events")
    private String eventType;
    @SerializedName("description")
    private String description;

    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }

    public String getEventType() { return eventType; }
    public void setEventType(String typeEvent) { this.eventType = typeEvent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
