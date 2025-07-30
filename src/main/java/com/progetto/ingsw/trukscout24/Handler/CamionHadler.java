package com.progetto.ingsw.trukscout24.Handler;

import com.progetto.ingsw.trukscout24.Model.Camion;

public class CamionHadler {
    private static CamionHadler instance = null;
    private Camion camion = null;

    private CamionHadler() {}

    public static CamionHadler getInstance() {
        if(instance == null)
            instance = new CamionHadler();
        return instance;
    }

    public void setCamion(Camion camion) {
        this.camion = camion;
    }

    public Camion getCamion() {
        return camion;
    }
    public void setNullCamion(){this.camion = null;}
}

