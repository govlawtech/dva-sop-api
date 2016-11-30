package au.gov.dva.sopref.casesummary;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ChartGenerator {

    public static void generatePieChart() throws FileNotFoundException, ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    }

}
