package org.waoss.oculus.apertor;

public class Contact {
    private String name;
    private String number;

    public Contact(final String name, final String number) {
        this.name = name;
        this.number = number;
    }

    public Contact() {
    }

    @Override
    public String toString() {
        return getName() + "\n" + getNumber();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }
}
