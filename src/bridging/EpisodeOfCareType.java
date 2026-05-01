package bridging;

/**
 * @author malifnasrulloh
 */
public enum EpisodeOfCareType {

    ANC(
            "http://terminology.kemkes.go.id/CodeSystem/episodeofcare-type",
            "ANC",
            "Antenatal Care",
            new String[]{"%O%"}
    ),
    TB_SO(
            "http://terminology.kemkes.go.id/CodeSystem/episodeofcare-type",
            "TB-SO",
            "Tuberkulosis Sensitif Obat",
            new String[]{"%A15%", "%A16%", "%A17%", "%A18%", "%A19%"}
    );

    private final String system;
    private final String code;
    private final String display;
    private final String[] icdFilters;

    EpisodeOfCareType(String system, String code, String display, String[] icdFilters) {
        this.system = system;
        this.code = code;
        this.display = display;
        this.icdFilters = icdFilters;
    }

    public String getSystem() {
        return system;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    /**
     * Returns the ICD-10 LIKE patterns for SQL filtering.
     * Multiple patterns are OR-joined in the query.
     */
    public String[] getIcdFilters() {
        return icdFilters;
    }

    /**
     * Human-readable label for combo box display.
     * Format: "CODE - Display Name"
     */
    public String getLabel() {
        return code + " - " + display;
    }

    /**
     * Builds the SQL WHERE clause fragment for ICD-10 filtering.
     * For single-pattern types (e.g., ANC with "%O%"), produces:
     *   diagnosa_pasien.kd_penyakit like '%O%'
     * For multi-pattern types (e.g., TB-SO), produces:
     *   (diagnosa_pasien.kd_penyakit like '%A15%' or diagnosa_pasien.kd_penyakit like '%A16%' ... )
     *
     * @param column the fully qualified column name (e.g., "diagnosa_pasien.kd_penyakit")
     * @return SQL fragment with '?' placeholders
     */
    public String buildIcdWhereClause(String column) {
        if (icdFilters.length == 1) {
            return column + " like ?";
        }
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < icdFilters.length; i++) {
            if (i > 0) {
                sb.append(" or ");
            }
            sb.append(column).append(" like ?");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Builds a combined SQL WHERE clause for ALL episode types (used by "Semua" option).
     *
     * @param column the fully qualified column name
     * @return SQL fragment combining all types' filters with OR
     */
    public static String buildAllTypesWhereClause(String column) {
        StringBuilder sb = new StringBuilder("(");
        EpisodeOfCareType[] types = values();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(" or ");
            }
            sb.append(types[i].buildIcdWhereClause(column));
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Returns the total number of ICD filter parameters across ALL types.
     * Used when setting PreparedStatement parameters for the "Semua" query.
     */
    public static int getAllTypesFilterCount() {
        int count = 0;
        for (EpisodeOfCareType type : values()) {
            count += type.getIcdFilters().length;
        }
        return count;
    }

    /**
     * Lookup by FHIR code string (e.g., "ANC", "TB-SO").
     * @param code the FHIR code
     * @return matching EpisodeOfCareType
     * @throws IllegalArgumentException if code is not recognized
     */
    public static EpisodeOfCareType fromCode(String code) {
        for (EpisodeOfCareType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EpisodeOfCare type code: " + code);
    }

    /**
     * Determines the EpisodeOfCareType for a given ICD-10 diagnosis code.
     * Checks each type's filter patterns against the provided code.
     *
     * @param icdCode the ICD-10 diagnosis code (e.g., "O80", "A15.0")
     * @return matching EpisodeOfCareType, or null if no match
     */
    public static EpisodeOfCareType fromIcdCode(String icdCode) {
        if (icdCode == null || icdCode.isEmpty()) {
            return null;
        }
        for (EpisodeOfCareType type : values()) {
            for (String filter : type.icdFilters) {
                // Convert SQL LIKE pattern to a simple contains check
                String pattern = filter.replace("%", "");
                if (icdCode.toUpperCase().contains(pattern.toUpperCase())) {
                    return type;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
