package com.lowcode.pipeline.model;
import java.util.List;

public record ProblemDefinitionOld(
		String researchTitle,
		String problemDescription,
		String motivation,
		List<String> goals,
        DataDescription dataDescription,
        TaskDefinition taskDefinition,
        Evaluation evaluation,
        ConstraintsAndDeployment constraintsAndDeployment
		) {}


//Maybe having different classes, each for a type of data
record DataDescription(
        String dataType,
        String dataFormat,
        String location,
        String organization,
//        Tabular
        List<String> features,
//        Computer vision
        List<String> classLabels,
        String datasetSize,
        Boolean labeled,
        String labelingMethod,
        List<String> knownDataIssues
		) {}

record TaskDefinition(
		String category,
        String expectedOutput
		) {}

record ConstraintsAndDeployment(
		String privacyConsiderations,
        String legalRequirements,
        String ethicalConcerns,
        List<String> intendedUsers
		) {}

record Evaluation(
		List<String> quantitativeMetrics,
		List<String> baseLines,
		List<String> qualitativeMetrics
		) {}