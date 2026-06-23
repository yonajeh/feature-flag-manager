package com.featureflag.dto;

import java.time.Instant;
import java.util.List;

public record FullDataExportDto(Instant exportedAt, int version, List<ExportedApplication> applications) {}
