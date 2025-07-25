package com.progetto.ingsw.trukscout24.Model;

/**
 * Classe modello per rappresentare un veicolo commerciale
 */
public class Camion {
    private String brand;
    private String model;
    private String vehicleType;
    private int year;
    private int price;
    private int mileage;
    private String fuelType;
    private String location;
    private String condition;

    // Constructors
    public Camion() {}

    public Camion(String brand, String model, String vehicleType,
                  int year, int price, int mileage,
                  String fuelType, String location, String condition) {
        this.brand = brand;
        this.model = model;
        this.vehicleType = vehicleType;
        this.year = year;
        this.price = price;
        this.mileage = mileage;
        this.fuelType = fuelType;
        this.location = location;
        this.condition = condition;
    }

    // Getters and Setters
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    @Override
    public String toString() {
        return String.format("%s %s (%d) - €%,d", brand, model, year, price);
    }
}
