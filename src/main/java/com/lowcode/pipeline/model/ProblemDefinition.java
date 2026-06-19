package com.lowcode.pipeline.model;

import java.util.List;

public class ProblemDefinition {

	String researchTitle;
	String problemDescription;
	String motivation;
	List<String> goals;
    DataDescription dataDescription;
    TaskDefinition taskDefinition;
    Evaluation evaluation;
    ConstraintsAndDeployment constraintsAndDeployment;

}
