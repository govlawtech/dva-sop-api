package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.exceptions.AutoUpdateRuntimeException;
import au.gov.dva.sopapi.sopref.data.updates.types.NewCompilation;
import au.gov.dva.sopapi.sopref.data.updates.types.NewInstrument;
import au.gov.dva.sopapi.sopref.data.updates.types.Revocation;
import au.gov.dva.sopapi.sopref.data.updates.types.Replacement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class InstrumentChangeBase  {
   @Override
   public String toString() {
      return "InstrumentChangeBase{" +
              "sourceRegisterId='" + sourceRegisterId + '\'' +
              ", targetRegisterId='" + targetRegisterId + '\'' +
              ", date=" + date +
              '}';
   }

   private final String sourceRegisterId;
   private final String targetRegisterId;


   protected static final String TYPE_LABEL = "type";
   protected static final String DATE_LABEL = "date";

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      InstrumentChangeBase that = (InstrumentChangeBase) o;

      if (!sourceRegisterId.equals(that.sourceRegisterId)) return false;
      return targetRegisterId.equals(that.targetRegisterId);
   }

   @Override
   public int hashCode() {
      int result = sourceRegisterId.hashCode();
      result = 31 * result + targetRegisterId.hashCode();
      return result;
   }

   private final OffsetDateTime date;

   protected InstrumentChangeBase(@Nonnull String sourceRegisterId, @Nonnull String targetRegisterId, @Nonnull OffsetDateTime date) {
      assert(!sourceRegisterId.isEmpty());
      assert(!targetRegisterId.isEmpty());
      this.sourceRegisterId = sourceRegisterId;
      this.targetRegisterId = targetRegisterId;
      this.date = date;
   }

   protected ObjectNode getCommonNode(String typeName, OffsetDateTime date) {
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode objectNode = objectMapper.createObjectNode();
      objectNode.put(TYPE_LABEL, typeName);
      objectNode.put(DATE_LABEL, date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
      return objectNode;
   }

   public static InstrumentChange fromJson(JsonNode jsonNode) {
      String type = jsonNode.findValue(TYPE_LABEL).asText();
      assert (type != null && !type.isEmpty());
      switch (type) {
         case NewInstrument.TYPE_NAME:
            return NewInstrument.fromJson(jsonNode);
         case Replacement.TYPE_NAME:
            return Replacement.fromJson(jsonNode);
         case Revocation.TYPE_NAME:
            return Revocation.fromJson(jsonNode);
         case NewCompilation.TYPE_NAME:
            return NewCompilation.fromJson(jsonNode);
         default:
            throw new AutoUpdateRuntimeException(String.format("Cannot deserialize this type of instrument change from JSON: %s", type));
      }
   }



   protected static OffsetDateTime extractDate(JsonNode jsonNode)
   {
       String dateText = jsonNode.findValue(DATE_LABEL).asText();
       OffsetDateTime date = OffsetDateTime.parse(dateText, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
       return date;
   }

   public OffsetDateTime getDate() {
      return date;
   }


   public String getSourceRegisterId() {
      return sourceRegisterId;
   }

   public String getTargetRegisterId() {
      return targetRegisterId;
   }
}
