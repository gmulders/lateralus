package io.lateralus.shared.generator;

import io.lateralus.shared.codegenerator.SourceFile;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public class SourceFileSaver {

	private final Path targetDirectory;

	private final boolean overwriteExistingFiles;

	public SourceFileSaver(Path targetDirectory, boolean overwriteExistingFiles) {
		this.targetDirectory = targetDirectory;
		this.overwriteExistingFiles = overwriteExistingFiles;
	}

	public void save(Set<SourceFile> sourceFiles) throws GeneratorException {
		final OpenOption[] openOptions = overwriteExistingFiles
				? new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING }
				: new OpenOption[] { StandardOpenOption.CREATE_NEW };

		for (SourceFile sourceFile : sourceFiles) {
			save(sourceFile, openOptions);
		}
	}

	private void save(final SourceFile sourceFile, final OpenOption[] openOptions) throws GeneratorException {

		final Path path = targetDirectory.resolve(Paths.get(sourceFile.getName()));

		try {
			Files.createDirectories(path.getParent());
		} catch (IOException e) {
			throw new GeneratorException("The path '" + path.toString() + "' could not be created.", e);
		}

		try {
			Files.write(path, sourceFile.getContents(), openOptions);
		} catch (FileAlreadyExistsException e) {
			throw new GeneratorException("The file '" + sourceFile.getName() + "' already exists.", e);
		} catch (IOException e) {
			throw new GeneratorException("The file '" + sourceFile.getName() + "' could not be saved.", e);
		}
	}
}
