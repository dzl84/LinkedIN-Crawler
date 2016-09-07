package com.daizhoulin.linkedincrawler.data;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class FalconHireDatastore {
	private Morphia morphia;
	private Datastore datastore;
	private Logger logger = LoggerFactory.getLogger(FalconHireDatastore.class);
	private static String DBNAME = "FalconHire";

	public FalconHireDatastore() {
		this.morphia = new Morphia();
		morphia.mapPackage("com.daizhoulin.linkedincrawler.data");

		ServerAddress addr = new ServerAddress("localhost", 27017);
		List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
		MongoCredential credentia = MongoCredential.createCredential("falconhire", "FalconHire",
				"passw0rd".toCharArray());
		credentialsList.add(credentia);
		MongoClient client = new MongoClient(addr, credentialsList);
		datastore = morphia.createDatastore(client, DBNAME); 
		datastore.ensureIndexes();
	}

	public void saveCandidate(String candidate_id, String name, String imgLink, String proLink, String curTitle, String curCompany,
			String curLocation, String curIndustry) {
		try {
			Candidate c = new Candidate();
			c.setCandidate_id(candidate_id);
			c.setImgLink(imgLink);
			c.setName(name);
			c.setRecruiterProfileLink(proLink);
			c.setCurTitle(curTitle);
			c.setCurCompany(curCompany);
			c.setCurLocation(curLocation);
			c.setCurIndustry(curIndustry);
			this.datastore.save(c);
		}catch(DuplicateKeyException e){
			logger.info("Candidate already exist: " + candidate_id);
		}catch(Exception e){
			logger.error("Failed to insert candidate: " + name + ", " + proLink, e);
		}
	}

	public static void main(String[] argv) {
		FalconHireDatastore f = new FalconHireDatastore();

	}
}
