package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportAiEvidenceItemRequestDTO {

    private String evidenceId;

    private String envelopeVersion;

    private String evidenceKind;

    private Subject subject;

    private Source source;

    private Capture capture;

    private Integrity integrity;

    private Availability availability;

    private Quality quality;

    private Privacy privacy;

    private String intendedUse;

    private List<String> collectedForRuleIds;

    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Subject {

        private ReportTargetType targetType;

        private String targetAlias;

        private String snapshotVersion;

        private String snapshotHash;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Source {

        private String sourceType;

        private String sourceSystem;

        private String sourceEntityType;

        private String sourceEntityAlias;

        private String sourceFieldPath;

        private String verificationStatus;

        private String authorityScope;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Capture {

        private String collectorName;

        private String collectorVersion;

        private OffsetDateTime capturedAt;

        private OffsetDateTime sourceUpdatedAt;

        private List<Transformation> transformations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transformation {

        private String type;

        private String version;

        private String materiality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Integrity {

        private String canonicalization;

        private String digestAlgorithm;

        private String payloadDigest;

        private String sourceDigest;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Availability {

        private String status;

        private String reasonCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Quality {

        private String level;

        private List<String> reasonCodes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Privacy {

        private String classification;

        private Boolean containsPersonalData;

        private String redactionStatus;

        private String retentionClass;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {

        private String representation;

        private String mediaType;

        private Map<String, Object> value;

        private Map<String, Object> reference;

        private Boolean truncated;
    }
}
