package com.shoploc.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.shoploc.user.service.AssociationInitializationService;

@Component
@Profile("!test")
public class AssociationInitializationRunner implements ApplicationRunner {

	@Autowired
	private AssociationInitializationService associationInitializationService;

	@Override
	public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
		associationInitializationService.initializeFirstAssociationAccount();
	}
}
