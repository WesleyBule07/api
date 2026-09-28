package com.example.demo.name;

public record NameView(Long id,
                       String name,
                       Gender gender,
                       String origin,
                       String province,
                       String meaning,
                       int popularity) {

    public static NameView from(NameRecord record) {
        return new NameView(record.getId(), record.getName(), record.getGender(),
                record.getOrigin(), record.getProvince(), record.getMeaning(), record.getPopularity());
    }
}