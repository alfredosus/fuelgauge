package ara.renault.com.fuel_gauge;

/**
 * Created by alfredo on 22/10/2017.
 */

public class Car {

    private int fuel;
    private int mileage;
    private boolean mileageV;
    private boolean fuelV;

    public int getFuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public boolean isMileageV() {
        return mileageV;
    }

    public void setMileageV(boolean mileageV) {
        this.mileageV = mileageV;
    }

    public boolean isFuelV() {
        return fuelV;
    }

    public void setFuelV(boolean fuelV) {
        this.fuelV = fuelV;
    }
}
