package com.jobportal.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.jobportal.entity.Company;

public interface CompanyRepository extends MongoRepository<Company, Long> {

}
