package org.mitre.synthea.world.concepts;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.helpers.SimpleCSV;
import org.mitre.synthea.helpers.Utilities;
import org.mitre.synthea.world.concepts.HealthRecord.Entry;

public class Costs {
  // all of these are CSVs with these columns: code, cost in $, comments
  private static final Map<String, Double> PROCEDURE_COSTS =
      parseCsvToMap("costs/procedures.csv");
  private static final Map<String, Double> MEDICATION_COSTS =
      parseCsvToMap("costs/medications.csv");
  private static final Map<String, Double> ENCOUNTER_COSTS =
      parseCsvToMap("costs/encounters.csv");
  private static final Map<String, Double> IMMUNIZATION_COSTS =
      parseCsvToMap("costs/immunizations.csv");
  
  private static final double DEFAULT_PROCEDURE_COST =
      Double.parseDouble(Config.get("generate.costs.default_procedure_cost"));
  private static final double DEFAULT_MEDICATION_COST =
      Double.parseDouble(Config.get("generate.costs.default_medication_cost"));
  private static final double DEFAULT_ENCOUNTER_COST =
      Double.parseDouble(Config.get("generate.costs.default_encounter_cost"));
  private static final double DEFAULT_IMMUNIZATION_COST =
      Double.parseDouble(Config.get("generate.costs.default_immunization_cost"));
  
  private static Map<String, Double> parseCsvToMap(String filename) {
    try {
      String rawData = Utilities.readResource(filename);
      List<LinkedHashMap<String, String>> lines = SimpleCSV.parse(rawData);
      
      Map<String, Double> costMap = new HashMap<>();
      for (Map<String,String> line : lines) {
        String code = line.get("CODE");
        String cost = line.get("COST");
        
        costMap.put(code, Double.valueOf(cost));
      }
      
      return costMap;
    } catch (IOException e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError("Unable to read required file: " + filename);
    }
  }

  /**
   * Calculate the cost of this Procedure, Encounter, Medication, etc.
   * 
   * @param entry Entry to calculate cost of.
   * @param isFacility Whether to use facility-based cost factors.
   * @return Cost, in USD.
   */
  public static double calculateCost(Entry entry, boolean isFacility) {
    String code = entry.codes.get(0).code;
    
    if (entry instanceof HealthRecord.Procedure) {
      return PROCEDURE_COSTS.getOrDefault(code, DEFAULT_PROCEDURE_COST);
    } else if (entry instanceof HealthRecord.Medication) {
      return MEDICATION_COSTS.getOrDefault(code, DEFAULT_MEDICATION_COST);
    } else if (entry instanceof HealthRecord.Encounter) {
      return ENCOUNTER_COSTS.getOrDefault(code, DEFAULT_ENCOUNTER_COST);
    } else {
      // Immunizations, Conditions, and Allergies are all just Entries,
      // but this should only be called for Immunizations
      return IMMUNIZATION_COSTS.getOrDefault(code, DEFAULT_IMMUNIZATION_COST);
    }
  }
}
