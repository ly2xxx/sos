package com.ly2xxx.sos.model;

public class EmergencyContact {
    
    private String police;
    private String ambulance;
    private String fire;
    private String general;
    
    public EmergencyContact() {
        // Default constructor
    }
    
    public EmergencyContact(String police, String ambulance, String fire, String general) {
        this.police = police;
        this.ambulance = ambulance;
        this.fire = fire;
        this.general = general;
    }
    
    public String getPolice() {
        return police != null ? police : "112";
    }
    
    public void setPolice(String police) {
        this.police = police;
    }
    
    public String getAmbulance() {
        return ambulance != null ? ambulance : "112";
    }
    
    public void setAmbulance(String ambulance) {
        this.ambulance = ambulance;
    }
    
    public String getFire() {
        return fire != null ? fire : "112";
    }
    
    public void setFire(String fire) {
        this.fire = fire;
    }
    
    public String getGeneral() {
        return general != null ? general : "112";
    }
    
    public void setGeneral(String general) {
        this.general = general;
    }
    
    public boolean isValid() {
        return (police != null && !police.trim().isEmpty()) ||
               (ambulance != null && !ambulance.trim().isEmpty()) ||
               (fire != null && !fire.trim().isEmpty()) ||
               (general != null && !general.trim().isEmpty());
    }
    
    public String getPrimaryEmergencyNumber() {
        if (general != null && !general.trim().isEmpty()) {
            return general;
        } else if (police != null && !police.trim().isEmpty()) {
            return police;
        } else if (ambulance != null && !ambulance.trim().isEmpty()) {
            return ambulance;
        } else if (fire != null && !fire.trim().isEmpty()) {
            return fire;
        } else {
            return "112"; // Universal fallback
        }
    }
    
    @Override
    public String toString() {
        return "EmergencyContact{" +
                "police='" + police + '\'' +
                ", ambulance='" + ambulance + '\'' +
                ", fire='" + fire + '\'' +
                ", general='" + general + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        EmergencyContact that = (EmergencyContact) o;
        
        if (police != null ? !police.equals(that.police) : that.police != null) return false;
        if (ambulance != null ? !ambulance.equals(that.ambulance) : that.ambulance != null)
            return false;
        if (fire != null ? !fire.equals(that.fire) : that.fire != null) return false;
        return general != null ? general.equals(that.general) : that.general == null;
    }
    
    @Override
    public int hashCode() {
        int result = police != null ? police.hashCode() : 0;
        result = 31 * result + (ambulance != null ? ambulance.hashCode() : 0);
        result = 31 * result + (fire != null ? fire.hashCode() : 0);
        result = 31 * result + (general != null ? general.hashCode() : 0);
        return result;
    }
}