package au.gov.dva.sopref;

import static spark.Spark.*;

public class DvaSopApiSparkApplication implements spark.servlet.SparkApplication {
    @Override
    public void init() {
        get("/hello", (req, res) -> "Hello World");
    }
}
