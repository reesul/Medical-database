package edu.tamu.ecen.capstone.patientmd.plot;

/**
 * Created by Jonathan on 4/7/2018.
 */

public class PlotField {
    private String test;
    private String date_low;
    private String date_high;

    @Override
    public String toString() {
        return "PlotField{" +
                "test='" + test + '\'' +
                ", date_low='" + date_low + '\'' +
                ", date_high='" + date_high + '\'' +
                '}';
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getDate_low() {
        return date_low;
    }

    public void setDate_low(String date_low) {
        this.date_low = date_low;
    }

    public String getDate_high() {
        return date_high;
    }

    public void setDate_high(String date_high) {
        this.date_high = date_high;
    }
}
