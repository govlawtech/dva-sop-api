package au.gov.dva;

import scala.Int;

import static spark.Spark.*;

public class Application implements spark.servlet.SparkApplication {

    private Integer diTest;

    public Application() {
        diTest = 10;
    }

    @Override
    public void init() {
        get("/hello", (req, res) -> "Hello World " + diTest);
     }
    
}
