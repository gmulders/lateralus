package io.lateralus.shared.codegenerator.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.lateralus.shared.codegenerator.CodeGenerationException;
import io.lateralus.shared.codegenerator.CodeGenerator;
import io.lateralus.shared.codegenerator.SourceFile;
import io.lateralus.shared.codegenerator.StringSourceFile;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Abstract base class for a code generator that uses FreeMarker templates.
 */
public abstract class AbstractFreeMarkerCodeGenerator<S> implements CodeGenerator<S> {

	private final Configuration configuration;

	public AbstractFreeMarkerCodeGenerator(TemplateLoader templateLoader) {
		this(createConfiguration(templateLoader));
	}

	public AbstractFreeMarkerCodeGenerator(Configuration configuration) {
		this.configuration = configuration;
	}

	private static Configuration createConfiguration(TemplateLoader templateLoader) {
		Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
		configuration.setTemplateLoader(templateLoader);
		configuration.setDefaultEncoding("UTF-8");
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
		configuration.setWrapUncheckedExceptions(true);
		return configuration;
	}

	protected SourceFile createSourceFile(String templateName, String sourceFileName, String dirName,
			Map<String, Object> model) throws CodeGenerationException {
		Template template = determineTemplate(templateName);

		StringWriter writer = new StringWriter();

		try {
			template.process(model, writer);
		} catch (TemplateException | IOException e) {
			throw new CodeGenerationException("Could not process the template:", e);
		}

		return new StringSourceFile(dirName + File.separator + sourceFileName, writer.toString());
	}

	protected Template determineTemplate(String templateName) throws CodeGenerationException {
		try {
			return configuration.getTemplate(templateName);
		} catch (IOException e) {
			throw new CodeGenerationException("Could not load template:", e);
		}
	}
}
