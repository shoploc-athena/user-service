package com.shoploc.user.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import com.shoploc.user.service.AssociationInitializationService;

@Component
@Profile("!test")
public class AssociationInitializationRunner implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(AssociationInitializationRunner.class);

	@Autowired
	private AssociationInitializationService associationInitializationService;

	@Override
	public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
		try {
			associationInitializationService.initializeFirstAssociationAccount();
		} catch (DataAccessException | TransactionException e) {
			logger.debug("Association account already created by another instance during concurrent startup");
		}
	}
}
