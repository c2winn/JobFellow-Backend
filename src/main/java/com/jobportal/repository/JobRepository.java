package com.jobportal.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jobportal.dto.ApplicationStatus;
import com.jobportal.dto.JobDTO;
import com.jobportal.entity.Job;

public interface JobRepository extends MongoRepository<Job, Long> {
	@Query("{ 'applicants': { $elemMatch: { 'applicantId': ?0, 'applicationStatus': ?1 } } }")
	List<Job> findByApplicantIdAndApplicationStatus(Long applicantId, ApplicationStatus applicationStatus);

	List<Job> findByPostedBy(Long postedBy);

	void deleteByPostedBy(Long postedBy); // Delete jobs associated with the Employer

	Page<JobDTO> findByJobStatus(String status, Pageable pageable);

	List<Job> findAllByPostedBy(Long postedBy);
}
