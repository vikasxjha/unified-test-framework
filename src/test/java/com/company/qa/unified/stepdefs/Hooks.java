package com.company.qa.unified.stepdefs;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import com.company.qa.unified.drivers.PlaywrightDriverFactory;

public class Hooks {
    @Before("@web")
    public void setup() {
        PlaywrightDriverFactory.init();
    }

    @After("@web")
    public void tearDown() {
        PlaywrightDriverFactory.cleanup();
    }

}
