package com.his.ar.dao;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.his.ar.entity.ARUserMaster;

@Repository("arUserMasterDao")
public interface ARUserMasterDao extends JpaRepository<ARUserMaster, Serializable> {

	@Query("SELECT ar FROM ARUserMaster ar where ar.email=:email and ar.pwd = :pwd and ar.activeSw=:activeSw ")
	public ARUserMaster findActiveUserByEmailAndPwd(String email, String pwd, String activeSw);

	@Query("select count(1) from ARUserMaster ar where ar.email=:emailId")
	public Integer findByEmail(String emailId);
	
	
}
