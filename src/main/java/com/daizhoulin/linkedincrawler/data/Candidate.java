package com.daizhoulin.linkedincrawler.data;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;

@Entity(value = "candidates", noClassnameStored = true)
@Indexes({
    @Index(fields = @Field("candidate_id"), 
    	   options = @IndexOptions(name = "candidate_id", unique = true))
})
public class Candidate {

	@Id
	private ObjectId id;
	private String name;
	private String imgLink;
	private String candidate_id;
	private String recruiterProfileLink;
	private String curTitle;
	private String curCompany;
	private String curLocation;
	private String curIndustry;
	private String publicProfileLink;
	
	public Candidate(){}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImgLink() {
		return imgLink;
	}
	public void setImgLink(String imgLink) {
		this.imgLink = imgLink;
	}
	public String getCandidate_id() {
		return candidate_id;
	}
	public void setCandidate_id(String candidate_id) {
		this.candidate_id = candidate_id;
	}
	public String getRecruiterProfileLink() {
		return recruiterProfileLink;
	}
	public void setRecruiterProfileLink(String recruiterProfileLink) {
		this.recruiterProfileLink = recruiterProfileLink;
	}
	public String getCurTitle() {
		return curTitle;
	}
	public void setCurTitle(String curTitle) {
		this.curTitle = curTitle;
	}
	public String getCurCompany() {
		return curCompany;
	}
	public void setCurCompany(String curCompany) {
		this.curCompany = curCompany;
	}
	public String getCurLocation() {
		return curLocation;
	}
	public void setCurLocation(String curLocation) {
		this.curLocation = curLocation;
	}
	public String getCurIndustry() {
		return curIndustry;
	}
	public void setCurIndustry(String curIndustry) {
		this.curIndustry = curIndustry;
	}
	public String getPublicProfileLink() {
		return publicProfileLink;
	}
	public void setPublicProfileLink(String publicProfileLink) {
		this.publicProfileLink = publicProfileLink;
	}
	
	
}
