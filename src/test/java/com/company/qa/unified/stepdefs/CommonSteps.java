package com.company.qa.unified.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class CommonSteps {
    @Given("framework is initialized")
    public void init() {
        System.out.println("Framework booted");
    }

    @Given("a test precondition")
    public void setupPrecondition() {
        System.out.println("Test precondition setup");
    }

    @When("a test action is performed")
    public void performAction() {
        System.out.println("Test action performed");
    }

    @Then("a test assertion is validated")
    public void validateAssertion() {
        System.out.println("Test assertion validated");
    }

}
