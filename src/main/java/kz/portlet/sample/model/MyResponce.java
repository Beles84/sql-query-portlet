package kz.portlet.sample.model;

public class MyResponce {
    public String name;
    public String error;

    public MyResponce(String name, String error) {
        this.name = name;
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "MyResponce{" +
                "name='" + name + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
