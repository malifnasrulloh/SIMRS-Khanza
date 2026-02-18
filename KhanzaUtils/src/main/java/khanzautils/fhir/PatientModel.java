/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils.fhir;

import khanzautils.JsonUtil;

/**
 *
 * @author malifnasrulloh
 */
public class PatientModel {

    public String idPasien;
    public String nik;
    public String nama;
    public String gender;
    public String birthDate;
    public String maritalStatus;
    public String phone;
    public String email;
    public String alamat;
    public String rt;
    public String rw;
    public String postalCode;
    public String province;
    public String city;
    public String district;
    public String village;
    public String provinceName;
    public String cityName;
    public String districtName;
    public String villageName;

    @Override
    public String toString() {
        return JsonUtil.createObject()
                .put("idPasien", idPasien)
                .put("nik", nik)
                .put("nama", nama)
                .put("gender", gender)
                .put("birthDate", birthDate)
                .put("maritalStatus", maritalStatus)
                .put("phone", phone)
                .put("email", email)
                .put("alamat", alamat)
                .put("rt", rt)
                .put("rw", rw)
                .put("postalCode", postalCode)
                .put("province", province)
                .put("city", city)
                .put("district", district)
                .put("village", village)
                .put("provinceName", provinceName)
                .put("cityName", cityName)
                .put("districtName", districtName)
                .put("villageName", villageName)
                .build();
    }

}
