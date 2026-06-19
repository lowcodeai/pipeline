package com.lowcode.pipeline.model;

public record Researcher(
		String name,
		String researchDomain,
		AIExperience aiExperience
		) {}


enum AIExperience {
	NONE,
	BEGINNER,
	INTERMEDIATE,
	ADVANCED,
	EXPERT
}
