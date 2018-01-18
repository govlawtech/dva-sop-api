package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.sopref.parsing.implementations.model.SubFactorInfo;
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors.NewSoPStyleSubFactorParser;
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors.SubparaReferences;
import scala.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FactorPartFunctions {

    public static Function<String, Tuple2<String,String>> defaultSplitterToFunctionAndSubpart = s -> {
        Tuple2<String,String> parts = SubparaReferences.splitNewStyleSopSubParaReference(s);
        return parts;
    };

    public static BiFunction<String,Factor,Optional<String>> defaultSubPartExtractor = (ref, factor) -> {
        NewSoPStyleSubFactorParser p = new NewSoPStyleSubFactorParser();

        scala.collection.immutable.List<SubFactorInfo> r = p.divideFactorsToSubFactors(factor);
        return null;

    };

}
