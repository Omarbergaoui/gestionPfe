package com.Application.Gestion.des.PFE.algorithme;

// Data structure to hold the analysis results
public record AnalysisResult(double fitness, int roomConflicts, int teacherConflicts, int unavailabilityConflicts, double totalTeacherIdleTimeHours, String error) {
    // Convenient constructor for success cases
    AnalysisResult(double fitness, int roomConflicts, int teacherConflicts, int unavailabilityConflicts, double totalTeacherIdleTimeHours) {
        this(fitness, roomConflicts, teacherConflicts, unavailabilityConflicts, totalTeacherIdleTimeHours, null);
    }
    // Convenient constructor for error cases
    AnalysisResult(String error) {
        this(0, -1, -1, -1, -1, error);
    }
}
