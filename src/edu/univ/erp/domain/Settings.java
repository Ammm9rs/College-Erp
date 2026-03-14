package edu.univ.erp.domain;

public class Settings {
    private boolean maintenanceOn = false;
    public boolean isMaintenanceOn(){ return maintenanceOn; }
    public void setMaintenanceOn(boolean v){ this.maintenanceOn = v; }
}
