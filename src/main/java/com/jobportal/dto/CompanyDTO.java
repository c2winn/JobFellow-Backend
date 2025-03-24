package com.jobportal.dto;

import java.util.Base64;
import java.util.List;

import com.jobportal.entity.Company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
    private Long id;
    private String picture;
    private String name;
    private String email;
    private String location;
    private String about;
    private Long size;
    private String website;
    private List<String> headquarters;
    private List<String> industries;
    private List<String> specialities;

    public Company toEntity() {
        return new Company(this.id, this.picture != null ? Base64.getDecoder().decode(this.picture) : null, this.name,
                this.email,
                this.location, this.about, this.size, this.website,
                this.headquarters, this.industries,
                this.specialities);
    }

}
