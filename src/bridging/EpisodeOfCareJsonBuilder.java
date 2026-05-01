package bridging;

/**
 * @author malifnasrulloh
 */
public class EpisodeOfCareJsonBuilder {

    /**
     * Builds the FHIR EpisodeOfCare JSON payload.
     *
     * @param type         the Episode of Care type (determines coding system, code, display)
     * @param orgId        the Satu Sehat Organization ID
     * @param noRawat      the registration number (used as identifier value)
     * @param patientId    the Satu Sehat Patient ID
     * @param patientName  the patient display name
     * @param periodStart  the period start timestamp (ISO 8601 format)
     * @return the JSON string ready to POST to /EpisodeOfCare
     */
    public static String buildJson(
            EpisodeOfCareType type,
            String orgId,
            String noRawat,
            String patientId,
            String patientName,
            String periodStart
    ) {
        return "{\n"
                + "    \"resourceType\": \"EpisodeOfCare\",\n"
                + "    \"identifier\": [\n"
                + "        {\n"
                + "            \"system\": \"http://sys-ids.kemkes.go.id/episode-of-care/" + orgId + "\",\n"
                + "            \"value\": \"" + noRawat + "\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"status\": \"active\",\n"
                + "    \"statusHistory\": [\n"
                + "        {\n"
                + "            \"status\": \"active\",\n"
                + "            \"period\": {\n"
                + "                \"start\": \"" + periodStart + "\"\n"
                + "            }\n"
                + "        }\n"
                + "    ],\n"
                + "    \"type\": [\n"
                + "        {\n"
                + "            \"coding\": [\n"
                + "                {\n"
                + "                    \"system\": \"" + type.getSystem() + "\",\n"
                + "                    \"code\": \"" + type.getCode() + "\",\n"
                + "                    \"display\": \"" + type.getDisplay() + "\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    ],\n"
                + "    \"patient\": {\n"
                + "        \"reference\": \"Patient/" + patientId + "\",\n"
                + "        \"display\": \"" + patientName + "\"\n"
                + "    },\n"
                + "    \"managingOrganization\": {\n"
                + "        \"reference\": \"Organization/" + orgId + "\"\n"
                + "    },\n"
                + "    \"period\": {\n"
                + "        \"start\": \"" + periodStart + "\"\n"
                + "    }\n"
                + "}";
    }

    private EpisodeOfCareJsonBuilder() {
        // Utility class — prevent instantiation
    }
}
