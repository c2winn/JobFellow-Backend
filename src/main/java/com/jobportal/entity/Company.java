package com.jobportal.entity;

import java.util.Base64;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.jobportal.dto.CompanyDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "companies")
public class Company {

    @Id
    private Long id;
    private byte[] picture;
    private String name;
    private String email;
    private String location;
    private String about;
    private Long size;
    private String website;
    private List<String> headquarters;
    private List<String> industries;
    private List<String> specialities;

    public CompanyDTO toDTO() {
        return new CompanyDTO(this.id, this.picture != null ? Base64.getEncoder().encodeToString(this.picture) : null,
                this.name, this.email, this.location, this.about, this.size, this.website,
                this.headquarters,
                this.industries,
                this.specialities);
    }

}
