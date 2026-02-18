/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils.bpjs;

import khanzautils.JsonUtil;

/**
 *
 * @author malifnasrulloh
 */
public class AplicareHelper {

    public static String buildUpdateKamarJson(String kodeKelas, String kodeRuang, String namaRuang, Integer kapasitas, Integer tersedia, Integer tersediaPria, Integer tersediaWanita, Integer tersediaPriaWanita) {
        return JsonUtil.createObject()
                .put("kodekelas", kodeKelas)
                .put("koderuang", kodeRuang)
                .put("namaruang", namaRuang)
                .put("kapasitas", kapasitas)
                .put("tersedia", tersedia)
                .put("tersediapria", tersedia)
                .put("tersediawanita", tersedia)
                .put("tersediapriawanita", tersedia)
                .build();
    }
}
