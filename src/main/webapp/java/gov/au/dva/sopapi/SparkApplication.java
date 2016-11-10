package gov.au.dva.sopapi;

import static spark.Spark.*;

public class SparkApplication implements spark.servlet.SparkApplication {
    @Override
    public void init() {
        get("/hello", (req, res) -> "Hello World");
    }
}
