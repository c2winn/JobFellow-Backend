package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.jobportal.dto.AccountType;
import com.jobportal.dto.ApplicantDTO;
import com.jobportal.dto.Application;
import com.jobportal.dto.ApplicationStatus;
import com.jobportal.dto.JobDTO;
import com.jobportal.dto.JobStatus;
import com.jobportal.dto.NotificationDTO;
import com.jobportal.entity.Applicant;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.utility.Utilities;

@Service("jobService")
public class JobServiceImpl implements JobService {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private NotificationService notificationService;

	@Override
	public JobDTO postJob(JobDTO jobDTO) throws JobPortalException {
		// Fetch the company user directly (assuming 'EMPLOYER' is the AccountType for a
		// company)
		User user = userRepository.findByProfileIdAndAccountType(jobDTO.getPostedBy(), AccountType.EMPLOYER)
				.orElseThrow(() -> new JobPortalException("USER_NOT_FOUND"));

		// Check if the company is verified
		if (!user.isVerified()) {
			throw new JobPortalException("USER_NOT_VERIFIED");
		}

		// Proceed with job posting
		if (jobDTO.getId() == 0) {
			jobDTO.setId(Utilities.getNextSequenceId("jobs"));
			jobDTO.setPostTime(LocalDateTime.now());

			NotificationDTO notiDto = new NotificationDTO();
			notiDto.setAction("Job Posted");
			notiDto.setMessage("Job Posted Successfully for " + jobDTO.getJobTitle() + " at " + jobDTO.getCompany());
			notiDto.setUserId(user.getId());
			notiDto.setRoute("/posted-jobs/" + jobDTO.getId());
			notificationService.sendNotification(notiDto);
		} else {
			Job job = jobRepository.findById(jobDTO.getId())
					.orElseThrow(() -> new JobPortalException("JOB_NOT_FOUND"));
			if (job.getJobStatus().equals(JobStatus.DRAFT) || jobDTO.getJobStatus().equals(JobStatus.CLOSED))
				jobDTO.setPostTime(LocalDateTime.now());
		}
		return jobRepository.save(jobDTO.toEntity()).toDTO();
	}

	@Override
	public List<JobDTO> getAllJobs() throws JobPortalException {
		return jobRepository.findAll().stream().map((x) -> x.toDTO()).toList();
	}

	@Override
	public JobDTO getJob(Long id) throws JobPortalException {
		return jobRepository.findById(id).orElseThrow(() -> new JobPortalException("JOB_NOT_FOUND")).toDTO();
	}

	@Override
	public void applyJob(Long id, ApplicantDTO applicantDTO) throws JobPortalException {
		User user = userRepository.findByEmail(applicantDTO.getEmail())
				.orElseThrow(() -> new JobPortalException("USER_NOT_FOUND"));
		if (!user.isVerified())
			throw new JobPortalException("USER_NOT_VERIFIED");


		Job job = jobRepository.findById(id).orElseThrow(() -> new JobPortalException("JOB_NOT_FOUND"));
		List<Applicant> applicants = job.getApplicants();
		if (applicants == null)
			applicants = new ArrayList<>();
		if (applicants.stream().filter((x) -> x.getApplicantId() == applicantDTO.getApplicantId()).toList().size() > 0)
			throw new JobPortalException("JOB_APPLIED_ALREADY");
		applicantDTO.setApplicationStatus(ApplicationStatus.APPLIED);
		applicantDTO.setTimestamp(LocalDateTime.now());
		applicants.add(applicantDTO.toEntity());
		job.setApplicants(applicants);
		jobRepository.save(job);
	}

	@Override
	public List<JobDTO> getHistory(Long id, ApplicationStatus applicationStatus) {
		return jobRepository.findByApplicantIdAndApplicationStatus(id, applicationStatus).stream().map((x) -> x.toDTO())
				.toList();
	}

	@Override
	public List<JobDTO> getJobsPostedBy(Long id) throws JobPortalException {
		return jobRepository.findByPostedBy(id).stream().map((x) -> x.toDTO()).toList();
	}

	@Override
	public void changeAppStatus(Application application) throws JobPortalException {
		
		Job job = jobRepository.findById(application.getId())
				.orElseThrow(() -> new JobPortalException("JOB_NOT_FOUND"));
		List<Applicant> apps = job.getApplicants().stream().map((x) -> {
			if (application.getApplicantId() == x.getApplicantId()) {
				x.setApplicationStatus(application.getApplicationStatus());
				if (application.getApplicationStatus().equals(ApplicationStatus.INTERVIEWING)) {
					x.setInterviewTime(application.getInterviewTime());
					NotificationDTO notiDto = new NotificationDTO();
					notiDto.setAction("Interview Scheduled");
					notiDto.setMessage("Interview scheduled for job : " + job.getJobTitle());
					
					User user;
					try {
						user = userRepository.findByProfileIdAndAccountType(application.getApplicantId(), AccountType.APPLICANT)
							.orElseThrow(() -> new JobPortalException("USER_NOT_FOUND"));

						notiDto.setUserId(user.getId());
						notiDto.setRoute("/job-history");
						notificationService.sendNotification(notiDto);
					} catch (JobPortalException e) {
						// Handle the exception appropriately (logging, returning an error response, etc.)
						e.printStackTrace();
						
					}
				}
			}
			return x;
		}).toList();
		job.setApplicants(apps);
		jobRepository.save(job);

	}

	@Override
	public Page<JobDTO> getPaginatedJobs(PageRequest pageable) {
		return jobRepository.findByJobStatus("ACTIVE", pageable);
	}

}
