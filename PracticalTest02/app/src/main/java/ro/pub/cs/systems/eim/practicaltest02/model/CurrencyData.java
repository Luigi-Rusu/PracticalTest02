package ro.pub.cs.systems.eim.practicaltest02.model;

public class CurrencyData {

   private String updated;

   private String rate;


    public CurrencyData(String updated, String rate) {
        this.updated = updated;
        this.rate = rate;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "CurrencyData{" +
                "updated='" + updated + '\'' +
                ", rate='" + rate + '\'' +
                '}';
    }
}
