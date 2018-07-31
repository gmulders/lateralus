package org.gertje.regular.codegenerator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;

public abstract class AbstractFreeMarkerCodeGenerator implements CodeGenerator {

	private Configuration configuration;

	public AbstractFreeMarkerCodeGenerator() {
		createConfiguration();
	}

	public AbstractFreeMarkerCodeGenerator(Configuration configuration) {
		this.configuration = configuration;
	}

	private void createConfiguration() {
		configuration = new Configuration(Configuration.VERSION_2_3_28);
		configuration.setClassForTemplateLoading(AbstractFreeMarkerCodeGenerator.class, "/templates");
		configuration.setDefaultEncoding("UTF-8");
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
		configuration.setWrapUncheckedExceptions(true);
	}

	protected Template determineTemplate(String templateName) throws CodeGenerationException {
		try {
			return configuration.getTemplate(templateName);
		} catch (IOException e) {
			throw new CodeGenerationException("Could not load template:", e);
		}
	}

}
