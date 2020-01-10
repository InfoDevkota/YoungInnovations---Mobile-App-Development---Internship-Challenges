package com.devkotasagar.listMe.test;

public class User {
    String name;
    int id;
    String email;
    String phone;
    Address address;

    public User(String name, int id, String email, String phone, String street, String zipCode) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.phone = phone;
        Address aAddress = new Address(street, zipCode);
        address = aAddress;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    String getAddress() {
        return address + "";
    }

    private class Address {
        String street;
        String zipCode;

        Address(String street, String zipCode) {
            this.street = street;
            this.zipCode = zipCode;
        }

        @Override
        public String toString(){
            return street + ", " + zipCode;
        }
    }
}
