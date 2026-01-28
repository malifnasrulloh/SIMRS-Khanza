/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi;

import java.io.File;
import java.io.IOException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 *
 * @author malifnasrulloh
 */
public class DataWilayah {

    private final JsonNode dataPropinsi;
    private final JsonNode dataKabupaten;
    private final JsonNode dataKecamatan;
    private final JsonNode dataKelurahan;
    private final ObjectMapper mapper = new ObjectMapper();

    public DataWilayah(String propinsiFile, String kabupatenFile, String kecamatanFile, String kelurahanFile) throws IOException {
        dataPropinsi = mapper.readTree(new File(propinsiFile)).path("propinsi");
        dataKabupaten = mapper.readTree(new File(kabupatenFile)).path("kabupaten");
        dataKecamatan = mapper.readTree(new File(kecamatanFile)).path("kecamatan");
        dataKelurahan = mapper.readTree(new File(kelurahanFile)).path("kelurahan");
    }

    public String mapProvinceName(String provinceCode) {
        for (JsonNode p : dataPropinsi) {
            if (p.path("id").asString().equalsIgnoreCase(provinceCode)) {
                return p.path("nama").asString();
            }
        }
        return "";
    }

    public String mapCityName(String cityCode, String provinceCode) {
        for (JsonNode c : dataKabupaten) {
            if (c.path("id").asString().equalsIgnoreCase(cityCode)
                    && c.path("id_propinsi").asString().equalsIgnoreCase(provinceCode)) {
                return c.path("nama").asString();
            }
        }
        return "";
    }

    public String mapDistrictName(String districtCode, String cityCode) {
        for (JsonNode d : dataKecamatan) {
            if (d.path("id").asString().equalsIgnoreCase(districtCode)
                    && d.path("id_kabupaten").asString().equalsIgnoreCase(cityCode)) {
                return d.path("nama").asString();
            }
        }
        return "";
    }

    public String mapVillageName(String villageCode, String districtCode) {
        for (JsonNode v : dataKelurahan) {
            if (v.path("id").asString().equalsIgnoreCase(villageCode)
                    && v.path("id_kecamatan").asString().equalsIgnoreCase(districtCode)) {
                return v.path("nama").asString();
            }
        }
        return "";
    }

}
