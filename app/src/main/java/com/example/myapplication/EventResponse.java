package com.example.myapplication;

public class EventResponse {
    class EventClass{
        private String type_events;
        private Long dni;
        private String description;
        private Long id;

        public String getEventType() { return type_events; }
        public void setEventType(String eventType) { this.type_events = eventType; }

        public Long getDni() { return dni; }
        public void setDni(Long dni) { this.dni = dni; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    private Boolean success;
    private String env;
    private EventClass event;

    public Boolean getSuccess() { return success; }
    public void setEnv(Boolean success) { this.success = success; }

    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }

    public EventClass getEvent(){ return event; }
}
