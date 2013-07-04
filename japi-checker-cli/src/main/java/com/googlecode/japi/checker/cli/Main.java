/*
 * Copyright 2013 Tomas Rohovsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.japi.checker.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.googlecode.japi.checker.BCChecker;
import com.googlecode.japi.checker.Reporter;
import com.googlecode.japi.checker.Rule;
import com.googlecode.japi.checker.Utils;
import com.googlecode.japi.checker.rules.AllRules;
import com.googlecode.japi.checker.rules.CheckMethodVariableArity;


/**
 * 
 * @author Tomas Rohovsky
 * @author William Bernardet
 *
 */
public class Main {
	private static final String HELP_CMDLINE = "japi-checker-cli [-bin] [-cp <arg>] [-h] [-rcp <arg>] REFERENCE_LIBRARY NEW_LIBRARY";
	private static final String HELP_HEADER = "Check API and ABI compatiblity of Java libraries.";
	private String[] args;
	
	public Main(String[] args) {
		this.args = args;
	}
	
	
	public int run() {
		boolean reportSourceIncompatibilities = true;		
		// configuring the CLI options
		Options options = new Options();
		options.addOption("bin", false, "check only binary compatibility (default - source and binary compatibility)");
		options.addOption("rcp", true, "reference classpath.");
		options.addOption("cp", true, "classpath.");
		options.addOption("h", "help", false, "This help message.");

		CommandLineParser parser = new GnuParser();
		CommandLine cmdLine = null;
		try {
			cmdLine = parser.parse(options, args);
			if (cmdLine.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(HELP_CMDLINE, HELP_HEADER, options, null);
				return 0;
			}
			if (cmdLine.getArgs().length != 2) {
				throw new ParseException("Missing REFERENCE_LIBRARY and/or NEW_LIBRARY.");
			}
			if (cmdLine.hasOption("bin")) {
				reportSourceIncompatibilities = false;
			}
		} catch (ParseException e) {
			System.out.println("Error parsing command line: " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(HELP_CMDLINE, HELP_HEADER, options, null);
			return -1;
		}
		
		// proceeding of arguments
		/*List<File> referenceClasspath = new ArrayList<File>();
		List<File> newArtifactClasspath = new ArrayList<File>();
		List<AntPatternMatcher> includes = new ArrayList<AntPatternMatcher>();
		List<AntPatternMatcher> excludes = new ArrayList<AntPatternMatcher>();*/

		String referencePath = cmdLine.getArgs()[0];
		String newPath = cmdLine.getArgs()[1];		
		File reference = new File(referencePath);
		File newArtifact = new File(newPath);
		if (!reference.isDirectory() && !Utils.isArchive(reference)) {
			throw new IllegalArgumentException(
					"reference must be either a directory"
							+ " or a jar (or a zip kind of archive) file");
		}
		if (!newArtifact.isDirectory() && !Utils.isArchive(newArtifact)) {
			throw new IllegalArgumentException(
					"new artifact must be either a directory"
							+ " or a jar (or a zip kind of archive) file");
		}

		BCChecker checker = new BCChecker(reference, newArtifact);

		// Populating the classpaths, reference and then for tested artifact.
		if (cmdLine.hasOption("rcp")) {
			for (String filename : cmdLine.getOptionValues("rcp")) {
				checker.addToReferenceClasspath(new File(filename));
			}
		}
		
		if (cmdLine.hasOption("cp")) {
			for (String filename : cmdLine.getOptionValues("cp")) {
				checker.addToNewArtifactClasspath(new File(filename));
			}
		}

		// checker initialization
		CLIReporter reporter = new CLIReporter();

		// checking
        // Load rules
        List<Rule> rules = new ArrayList<Rule>();
        if (reportSourceIncompatibilities) {
        	rules.add(new CheckMethodVariableArity());
        } else {
        	rules.add(new AllRules());
        	rules.add(new CheckMethodVariableArity());
        }
        // Running the check...
		try {
			checker.checkBacwardCompatibility(reporter, rules);
			System.out.println("Error count: " + reporter.getCount(Reporter.Level.ERROR));
			System.out.println("Warning count: " + reporter.getCount(Reporter.Level.WARNING));
			if (reporter.getCount(Reporter.Level.ERROR) > 0) {
				return -1;
			}
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			return -1;
		}
		return 0;
	}
	
	public static void main(String[] args) {
		System.exit(new Main(args).run());
	}

}