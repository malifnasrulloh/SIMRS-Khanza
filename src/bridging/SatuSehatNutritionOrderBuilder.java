package bridging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SatuSehatNutritionOrderBuilder
 *
 * Pure POJO builder for FHIR R4 NutritionOrder resources matching Kemenkes profile:
 * https://fhir.kemkes.go.id/r4/StructureDefinition/NutritionOrder
 *
 * Implements 100% parity with php-service/lib/satusehat/PayloadBuilder.php::nutritionOrder()
 *
 * @author malifnasrulloh
 */
public class SatuSehatNutritionOrderBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static class DietTypeInfo {
        public final String code;
        public final String display;

        public DietTypeInfo(String code, String display) {
            this.code = code;
            this.display = display;
        }
    }

    public static class NutrientInfo {
        public final String code;
        public final String display;
        public final double value;
        public final String unit;
        public final String system;

        public NutrientInfo(String code, String display, double value, String unit, String system) {
            this.code = code;
            this.display = display;
            this.value = value;
            this.unit = unit;
            this.system = system;
        }
    }

    public static class ScheduleInfo {
        public final int frequency;
        public final int period;
        public final String periodUnit;

        public ScheduleInfo(int frequency, int period, String periodUnit) {
            this.frequency = frequency;
            this.period = period;
            this.periodUnit = periodUnit;
        }
    }

    /**
     * Resolve oralDiet.type using SNOMED CT International codes based on combined text.
     */
    public static DietTypeInfo resolveOralDietType(String combinedText) {
        if (combinedText == null) {
            combinedText = "";
        }
        String upper = combinedText.toUpperCase();

        if (Pattern.compile("\\b(DM|DIABETES|DIABETIK)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("160670007", "Diabetic diet");
        } else if (Pattern.compile("\\b(TKTP|TINGGI KALORI|TINGGI PROTEIN)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("68097001", "Increased calorie diet");
        } else if (Pattern.compile("\\b(RG|RENDAH GARAM|LOW SODIUM)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("386619000", "Low sodium diet");
        } else if (Pattern.compile("\\b(RENDAH PURIN|R\\.PURIN|RP|PURIN)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("22745007", "Purine restricted diet");
        } else if (Pattern.compile("\\b(RENDAH PROTEIN|R\\.PROTEIN)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("160673009", "Low protein diet");
        } else if (Pattern.compile("\\b(CAIR|LIQUID|SONDE)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("10888001", "Liquid diet");
        } else if (Pattern.compile("\\b(HEPAR|HATI)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("438588004", "Dietary education for hepatic disorder");
        } else if (Pattern.compile("\\b(GINJAL|RENAL)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("33489005", "Renal disease diet");
        } else if (Pattern.compile("\\b(RENDAH LEMAK|LOW FAT)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("16208003", "Low fat diet");
        } else if (Pattern.compile("\\b(LUNAK|SOFT)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return new DietTypeInfo("78150000", "Soft diet");
        }

        return new DietTypeInfo("41449007", "General diet");
    }

    /**
     * Extract nutrient breakdown (Calorie, Protein, Fat, Carbohydrate) from intervensi text.
     */
    public static List<NutrientInfo> extractNutrients(String intervensi) {
        List<NutrientInfo> list = new ArrayList<>();
        if (intervensi == null || intervensi.trim().isEmpty()) {
            return list;
        }

        // Calorie / Energy (SNOMED 258790008, unit: cal)
        Matcher mCal = Pattern.compile("(?:ENERGI|KALORI|CALORIE|ENERGY|KKAL)\\s*[:=]?\\s*([0-9]+(?:[\\.,][0-9]+)?)", Pattern.CASE_INSENSITIVE).matcher(intervensi);
        if (mCal.find()) {
            double val = parseDoubleSafe(mCal.group(1));
            if (val > 0) {
                list.add(new NutrientInfo("258790008", "calorie", val, "cal", "http://unitsofmeasure.org"));
            }
        }

        // Protein (SNOMED 88878007, unit: g)
        Matcher mProt = Pattern.compile("(?:PROTEIN)\\s*[:=]?\\s*([0-9]+(?:[\\.,][0-9]+)?)", Pattern.CASE_INSENSITIVE).matcher(intervensi);
        if (mProt.find()) {
            double val = parseDoubleSafe(mProt.group(1));
            if (val > 0) {
                list.add(new NutrientInfo("88878007", "Protein", val, "g", "http://unitsofmeasure.org"));
            }
        }

        // Fat / Lemak (SNOMED 256674009, unit: g)
        Matcher mFat = Pattern.compile("(?:LEMAK|FAT)\\s*[:=]?\\s*([0-9]+(?:[\\.,][0-9]+)?)", Pattern.CASE_INSENSITIVE).matcher(intervensi);
        if (mFat.find()) {
            double val = parseDoubleSafe(mFat.group(1));
            if (val > 0) {
                list.add(new NutrientInfo("256674009", "Fat", val, "g", "http://unitsofmeasure.org"));
            }
        }

        // Carbohydrate / Karbohidrat (SNOMED 2331003, unit: g)
        Matcher mCarb = Pattern.compile("(?:KARBOHIDRAT|CARBOHYDRATE|KH)\\s*[:=]?\\s*([0-9]+(?:[\\.,][0-9]+)?)", Pattern.CASE_INSENSITIVE).matcher(intervensi);
        if (mCarb.find()) {
            double val = parseDoubleSafe(mCarb.group(1));
            if (val > 0) {
                list.add(new NutrientInfo("2331003", "Carbohydrate", val, "g", "http://unitsofmeasure.org"));
            }
        }

        return list;
    }

    /**
     * Resolve oralDiet.texture modifier code.
     */
    public static String resolveTexture(String combinedText) {
        if (combinedText == null || combinedText.trim().isEmpty()) {
            return null;
        }
        String upper = combinedText.toUpperCase();
        if (Pattern.compile("\\b(CAIR|LIQUID|SONDE)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return "228055009"; // Liquidized food
        } else if (Pattern.compile("\\b(LUNAK|BUBUR|SOFT)\\b", Pattern.CASE_INSENSITIVE).matcher(upper).find()) {
            return "228053002"; // Cut-up food
        }
        return null;
    }

    /**
     * Resolve oralDiet.schedule.
     */
    public static ScheduleInfo resolveSchedule(String combinedText) {
        if (combinedText == null || combinedText.trim().isEmpty()) {
            return null;
        }

        Matcher mHours = Pattern.compile("(\\d+)\\s*(jam|h|hour)", Pattern.CASE_INSENSITIVE).matcher(combinedText);
        if (mHours.find()) {
            int hours = Integer.parseInt(mHours.group(1));
            if (hours > 0) {
                return new ScheduleInfo(1, hours, "h");
            }
        }

        if (Pattern.compile("\\b(3X|3 X|TIGA KALI|PAGI|SIANG|SORE|MALAM)\\b", Pattern.CASE_INSENSITIVE).matcher(combinedText).find()) {
            return new ScheduleInfo(3, 1, "d");
        }

        return null;
    }

    /**
     * Clean alphanumeric no_rawat + HHmmss.
     */
    public static String cleanIdentifier(String noRawat, String tanggalAdime) {
        String cleanNoRawat = (noRawat != null) ? noRawat.replaceAll("[^0-9A-Za-z]", "") : "";
        String timePart = "000000";

        if (tanggalAdime != null && !tanggalAdime.trim().isEmpty()) {
            String str = tanggalAdime.trim();
            // Match HH:mm:ss anywhere
            Matcher m = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})").matcher(str);
            if (m.find()) {
                timePart = m.group(1) + m.group(2) + m.group(3);
            }
        }

        return cleanNoRawat + timePart;
    }

    /**
     * Format date time to ISO-8601 with +07:00 timezone offset.
     */
    public static String formatDateTime(String tanggal) {
        if (tanggal == null || tanggal.trim().isEmpty()) {
            return "";
        }
        String str = tanggal.trim();
        if (str.length() == 10) { // YYYY-MM-DD
            str += "T00:00:00+07:00";
        } else if (str.length() == 19) { // YYYY-MM-DD HH:mm:ss
            str = str.replace(" ", "T") + "+07:00";
        } else if (str.endsWith(".0") && str.length() == 21) {
            str = str.substring(0, 19).replace(" ", "T") + "+07:00";
        } else if (!str.contains("+") && !str.endsWith("Z")) {
            str = str.replace(" ", "T") + "+07:00";
        }
        return str;
    }

    /**
     * Assemble oralDiet.instruction from intervention, instruction, and diet name.
     */
    public static String composeInstruction(String namaDiet, String intervensi, String instruksi) {
        List<String> parts = new ArrayList<>();
        if (intervensi != null && !intervensi.trim().isEmpty() && !intervensi.trim().equals("-")) {
            parts.add(intervensi.trim());
        }
        if (instruksi != null && !instruksi.trim().isEmpty() && !instruksi.trim().equals("-")) {
            parts.add(instruksi.trim());
        }
        if (namaDiet != null && !namaDiet.trim().isEmpty() && !namaDiet.trim().equals("-")) {
            parts.add("Diet: " + namaDiet.trim());
        }
        if (parts.isEmpty()) {
            return "Diet Standar Rumah Sakit";
        }
        return String.join(" | ", parts);
    }

    /**
     * Build full FHIR R4 NutritionOrder JSON payload.
     */
    public static String buildPayload(
        String orgId,
        String noRawat,
        String tanggalAdime,
        String idPasien,
        String nmPasien,
        String idEncounter,
        String idPraktisi,
        String namaPetugas,
        String namaDiet,
        String intervensi,
        String instruksi,
        String diagnosis,
        String idNutritionOrder
    ) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("resourceType", "NutritionOrder");

        if (idNutritionOrder != null && !idNutritionOrder.trim().isEmpty()) {
            root.put("id", idNutritionOrder.trim());
        }

        // identifier
        ArrayNode identifierArray = root.putArray("identifier");
        ObjectNode idObj = identifierArray.addObject();
        idObj.put("system", "http://sys-ids.kemkes.go.id/nutrition-order/" + (orgId != null ? orgId.trim() : ""));
        idObj.put("value", cleanIdentifier(noRawat, tanggalAdime));

        root.put("status", "active");
        root.put("intent", "order");

        // patient
        ObjectNode patientObj = root.putObject("patient");
        patientObj.put("reference", "Patient/" + (idPasien != null ? idPasien.trim() : ""));
        patientObj.put("display", nmPasien != null ? nmPasien.trim() : "");

        // encounter
        ObjectNode encounterObj = root.putObject("encounter");
        encounterObj.put("reference", "Encounter/" + (idEncounter != null ? idEncounter.trim() : ""));

        // dateTime
        root.put("dateTime", formatDateTime(tanggalAdime));

        // orderer
        ObjectNode ordererObj = root.putObject("orderer");
        ordererObj.put("reference", "Practitioner/" + (idPraktisi != null ? idPraktisi.trim() : ""));
        ordererObj.put("display", namaPetugas != null ? namaPetugas.trim() : "");

        // oralDiet
        ObjectNode oralDietObj = root.putObject("oralDiet");

        // 1. type
        String combined = (namaDiet != null ? namaDiet : "") + " " +
                          (intervensi != null ? intervensi : "") + " " +
                          (diagnosis != null ? diagnosis : "");
        DietTypeInfo dietType = resolveOralDietType(combined);
        ArrayNode typeArray = oralDietObj.putArray("type");
        ObjectNode typeItem = typeArray.addObject();
        ArrayNode typeCoding = typeItem.putArray("coding");
        ObjectNode codeObj = typeCoding.addObject();
        codeObj.put("system", "http://snomed.info/sct");
        codeObj.put("code", dietType.code);
        codeObj.put("display", dietType.display);

        // 2. schedule
        ScheduleInfo sched = resolveSchedule(combined);
        if (sched != null) {
            ArrayNode schedArray = oralDietObj.putArray("schedule");
            ObjectNode schedItem = schedArray.addObject();
            ObjectNode repeatObj = schedItem.putObject("repeat");
            repeatObj.put("frequency", sched.frequency);
            repeatObj.put("period", sched.period);
            repeatObj.put("periodUnit", sched.periodUnit);
        }

        // 3. nutrient
        List<NutrientInfo> nutrients = extractNutrients(intervensi);
        if (!nutrients.isEmpty()) {
            ArrayNode nutrientArray = oralDietObj.putArray("nutrient");
            for (NutrientInfo nut : nutrients) {
                ObjectNode nutItem = nutrientArray.addObject();

                ObjectNode modifier = nutItem.putObject("modifier");
                ArrayNode modCoding = modifier.putArray("coding");
                ObjectNode modCode = modCoding.addObject();
                modCode.put("system", "http://snomed.info/sct");
                modCode.put("code", nut.code);
                modCode.put("display", nut.display);

                ObjectNode amount = nutItem.putObject("amount");
                amount.put("value", nut.value);
                amount.put("unit", nut.unit);
                amount.put("system", nut.system);
                amount.put("code", nut.unit);
            }
        }

        // 4. texture
        String textureCode = resolveTexture(combined);
        if (textureCode != null) {
            ArrayNode textureArray = oralDietObj.putArray("texture");
            ObjectNode textItem = textureArray.addObject();
            ObjectNode textMod = textItem.putObject("modifier");
            ArrayNode textCoding = textMod.putArray("coding");
            ObjectNode textCode = textCoding.addObject();
            textCode.put("system", "http://snomed.info/sct");
            textCode.put("code", textureCode);
            textCode.put("display", textureCode.equals("228055009") ? "Liquidized food" : "Cut-up food");
        }

        // 5. instruction
        oralDietObj.put("instruction", composeInstruction(namaDiet, intervensi, instruksi));

        try {
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing NutritionOrder JSON: " + e.getMessage(), e);
        }
    }

    private static double parseDoubleSafe(String str) {
        if (str == null) return 0.0;
        try {
            return Double.parseDouble(str.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
