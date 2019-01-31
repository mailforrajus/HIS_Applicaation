package com.his.ar.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.his.ar.dao.ARUserMasterDao;
import com.his.ar.entity.ARUserMaster;
import com.his.ar.model.UserMaster;
import com.his.util.ARConstants;
import com.his.util.EmailService;
import com.his.util.PasswordService;

@Service("arService")
public class ARServiceImpl implements ARService {

	@Autowired(required = true)
	private ARUserMasterDao arUserMasterDao;

	@Autowired(required = true)
	private EmailService emailService;

	/**
	 * This method is used to insert user record
	 */
	@Override
	public UserMaster saveUser(UserMaster um) {
		ARUserMaster entity = new ARUserMaster();

		um.setActiveSw("Y");
		um.setCreatedBy("Admin");

		// copying data from model to entity
		BeanUtils.copyProperties(um, entity);

		// Encrypting User Password
		String encryptedPwd = PasswordService.encrypt(um.getPwd());
		entity.setPwd(encryptedPwd);

		ARUserMaster savedEntity = arUserMasterDao.save(entity);

		// Sending Email with Pwd
		if (savedEntity != null) {
			String text;
			try {
				text = getRegEmailBody(um);
				emailService.sendEmail(um.getEmail(), ARConstants.EMAIL_FROM, ARConstants.EMAIL_SUBJECT, text);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// setting generated pk value to model
		um.setUserId(savedEntity.getUserId());

		return um;
	}

	@Override
	public List<UserMaster> findAllUsers() {
		List<UserMaster> users = new ArrayList<UserMaster>();
		List<ARUserMaster> entities = arUserMasterDao.findAll();
		for (ARUserMaster entity : entities) {
			UserMaster master = new UserMaster();
			BeanUtils.copyProperties(entity, master);
			users.add(master);
		}
		return users;
	}

	@Override
	public Page<ARUserMaster> findAllUsers(int pageNo, int pageSize) {
		Pageable pageble = new PageRequest(pageNo, pageSize);
		List<UserMaster> users = new ArrayList<UserMaster>();
		Page<ARUserMaster> pages = arUserMasterDao.findAll(pageble);
		return pages;
	}

	@Override
	public UserMaster findById(Integer userId) {
		return null;
	}

	@Override
	public UserMaster update(UserMaster um) {
		return null;
	}

	@Override
	public UserMaster findActiveUserByEmailAndPwd(String email, String pwd, String activeSw) {
		UserMaster um = new UserMaster();
		String encryptedPwd = PasswordService.encrypt(pwd);
		ARUserMaster arUserMaster = arUserMasterDao.findActiveUserByEmailAndPwd(email, encryptedPwd, "Y");
		BeanUtils.copyProperties(arUserMaster, um);
		return um;
	}

	public String getRegEmailBody(UserMaster um) throws Exception {
		String fileName = "Registration_Email_Template.txt";
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		StringBuilder mailBody = new StringBuilder("");
		while (line != null) {

			// Processing mail body content
			if (line.contains("USER_NAME")) {
				line = line.replace("USER_NAME", um.getFirstName() + " " + um.getLastName());
			}

			if (line.contains("APP_USER_EMAIL")) {
				line = line.replace("APP_USER_EMAIL", um.getEmail());
			}

			if (line.contains("APP_URL")) {
				line = line.replace("APP_URL", "<a href='http://localhost:9090/HIS/loginForm'>RI HIS</a>");
			}

			if (line.contains("APP_USER_PWD")) {
				line = line.replace("APP_USER_PWD", um.getPwd());
			}

			// Appending processed line to StringBuilder
			mailBody.append(line);

			// reading next line
			line = br.readLine();
		}

		fr.close();
		br.close();

		// Returning mail body content
		return mailBody.toString();
	}

	@Override
	public String findByEmail(String email) {
		Integer cnt = arUserMasterDao.findByEmail(email);
		System.out.println("Cnt:"+cnt);
		return (cnt >= 1) ? ARConstants.DUPLICATE : ARConstants.UNIQUE;
	}

}
