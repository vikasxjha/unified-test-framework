package com.company.qa.unified.stepdefs;

import io.cucumber.java.en.Given;

public class CommonSteps {
    @Given("framework is initialized")
    public void init() {
        System.out.println("Framework booted");
    }

}
