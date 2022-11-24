package org.sunbird.passbook.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PassbookParserHandler {

	@Autowired
	private CompetencyPassbookParser competencyPassbookParser;

	public PassbookParser getPassbookParser(String typeName) {
		switch (typeName) {
		case "competency":
			return competencyPassbookParser;
		default:
			return null;
		}
	}
}
