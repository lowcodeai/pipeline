package com.lowcode.pipeline.service;

import java.util.List;

public record UserProfile(String name, String domain, List<String> skills) {}
