package com.freshworks.freddy.insights.constant.enums;

import java.util.ArrayList;
import java.util.List;

public enum LanguageCodeEnum {
    none("none"),
    ar("ar"),
    ca("ca"),
    cs("cs"),
    da("da"),
    de("de"),
    en("en"),
    es_LA("es-LA"),
    es("es"),
    et("et"),
    fi("fi"),
    fr("fr"),
    hu("hu"),
    id("id"),
    it("it"),
    ja_JP("ja-JP"),
    ko("ko"),
    nb_NO("nb-NO"),
    nl("nl"),
    pl("pl"),
    pt_BR("pt-BR"),
    pt_PT("pt-PT"),
    ru_RU("ru-RU"),
    sk("sk"),
    // Slovak
    sl("sl"),
    // Slovenian
    sv_SE("sv-SE"),
    tr("tr"),
    vi("vi"),
    zh_CN("zh-CN"),
    uk("uk"),
    he("he"),
    th("th"),
    ro("ro"),
    zh_TW("zh-TW"),
    lv_LV("lv-LV"),
    bs("bs"),
    bg("bg"),
    hr("hr"),
    el("el"),
    ms("ms"),
    lt("lt"),
    sr("sr"),
    is("is"),
    fil("fil"),
    hi("hi"),
    ja("ja"),
    km("km"),
    bn("bn"),
    ml("ml"),
    te("te"),
    ta("ta"),
    pa("pa"),
    kn("kn"),
    ur("ur"),
    mr("mr"),
    gu("gu"),
    or("or"),
    as("as"),
    am("am"),
    ku("ku"),
    my("my"),
    cy("cy"),
    fa("fa"),
    lv("lv");

    private final String value;

    LanguageCodeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LanguageCodeEnum getByValue(String value) {
        for (LanguageCodeEnum enumConstant : LanguageCodeEnum.values()) {
            if (enumConstant.getValue().equals(value)) {
                return enumConstant;
            }
        }
        return LanguageCodeEnum.none;
    }

    public static List<String> getAllValues() {
        List<String> enumValuesList = new ArrayList<>();
        for (LanguageCodeEnum value : LanguageCodeEnum.values()) {
            enumValuesList.add(value.getValue());
        }
        return enumValuesList;
    }
}
