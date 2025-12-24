package com.company.qa.unified.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/java/features",
        glue = "com.company.qa.unified.stepdefs",
        plugin = {"pretty"}
)

public class CucumberTestRunner extends AbstractTestNGCucumberTests {
}
