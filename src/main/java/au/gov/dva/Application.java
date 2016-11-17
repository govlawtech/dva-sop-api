package au.gov.dva;

import static spark.Spark.*;

public class Application implements spark.servlet.SparkApplication {
    @Override
    public void init() {
        get("/hello", (req, res) -> "Hello World");
    }
}
