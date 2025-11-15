package com.jakubjirak.ansilog;

import org.jetbrains.annotations.NotNull;
import java.util.*;

public class LogClusteringEngine {
    
    public static class LogCluster {
        public int id;
        public List<String> members;
        public String representative;
        public double cohesion;
        public String theme;
        
        public LogCluster(int id) {
            this.id = id;
            this.members = new ArrayList<>();
        }
    }
    
    public static class ClusteringResult {
        public List<LogCluster> clusters;
        public Map<String, Integer> clusterSizes;
        public double silhouetteScore;
        public Map<String, List<String>> thematicGroups;
        
        public ClusteringResult() {
            this.clusters = new ArrayList<>();
            this.clusterSizes = new LinkedHashMap<>();
            this.thematicGroups = new LinkedHashMap<>();
        }
    }
    
    public static ClusteringResult clusterLogs(@NotNull String logContent, int k) {
        ClusteringResult result = new ClusteringResult();
        String[] lines = logContent.split("\n");
        
        // K-means clustering
        List<LogCluster> clusters = kMeansClustering(lines, k);
        result.clusters = clusters;
        
        // Calculate cluster sizes
        for (LogCluster cluster : clusters) {
            result.clusterSizes.put("Cluster " + cluster.id, cluster.members.size());
        }
        
        // Identify themes
        identifyThemes(clusters, result);
        
        // Calculate silhouette score
        result.silhouetteScore = calculateSilhouetteScore(clusters, lines);
        
        return result;
    }
    
    private static List<LogCluster> kMeansClustering(@NotNull String[] lines, int k) {
        List<LogCluster> clusters = new ArrayList<>();
        Random random = new Random(42);
        
        // Initialize clusters
        for (int i = 0; i < k; i++) {
            LogCluster cluster = new LogCluster(i);
            cluster.representative = lines[random.nextInt(lines.length)];
            clusters.add(cluster);
        }
        
        // Assignment phase
        for (String line : lines) {
            LogCluster nearest = clusters.get(0);
            double minDistance = Double.MAX_VALUE;
            
            for (LogCluster cluster : clusters) {
                double distance = calculateDistance(line, cluster.representative);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = cluster;
                }
            }
            
            nearest.members.add(line);
        }
        
        // Update representatives
        for (LogCluster cluster : clusters) {
            if (!cluster.members.isEmpty()) {
                cluster.representative = cluster.members.get(0);
                cluster.cohesion = calculateCohesion(cluster);
            }
        }
        
        return clusters;
    }
    
    private static double calculateDistance(@NotNull String s1, @NotNull String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 0.0;
        
        int matches = 0;
        for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                matches++;
            }
        }
        
        return 1.0 - ((double) matches / maxLength);
    }
    
    private static double calculateCohesion(@NotNull LogCluster cluster) {
        if (cluster.members.size() <= 1) return 1.0;
        
        double totalDistance = 0.0;
        for (String member : cluster.members) {
            totalDistance += calculateDistance(member, cluster.representative);
        }
        
        return 1.0 - (totalDistance / cluster.members.size());
    }
    
    private static void identifyThemes(@NotNull List<LogCluster> clusters, @NotNull ClusteringResult result) {
        for (LogCluster cluster : clusters) {
            Set<String> themes = new HashSet<>();
            
            for (String member : cluster.members) {
                if (member.contains("ERROR") || member.contains("FATAL")) {
                    themes.add("Errors");
                }
                if (member.contains("WARN")) {
                    themes.add("Warnings");
                }
                if (member.contains("INFO")) {
                    themes.add("Info");
                }
                if (member.contains("database") || member.contains("query")) {
                    themes.add("Database");
                }
                if (member.contains("connection") || member.contains("network")) {
                    themes.add("Network");
                }
                if (member.contains("timeout")) {
                    themes.add("Performance");
                }
            }
            
            cluster.theme = String.join(", ", themes);
            if (!cluster.theme.isEmpty()) {
                result.thematicGroups.computeIfAbsent(cluster.theme, k -> new ArrayList<>())
                        .addAll(cluster.members.stream().limit(3).toList());
            }
        }
    }
    
    private static double calculateSilhouetteScore(@NotNull List<LogCluster> clusters, @NotNull String[] lines) {
        double totalScore = 0.0;
        int count = 0;
        
        for (LogCluster cluster : clusters) {
            for (String member : cluster.members) {
                double intraDistance = 0.0;
                for (String other : cluster.members) {
                    intraDistance += calculateDistance(member, other);
                }
                intraDistance /= Math.max(1, cluster.members.size());
                
                double interDistance = Double.MAX_VALUE;
                for (LogCluster other : clusters) {
                    if (other.id != cluster.id) {
                        double distance = 0.0;
                        for (String otherMember : other.members) {
                            distance += calculateDistance(member, otherMember);
                        }
                        distance /= Math.max(1, other.members.size());
                        interDistance = Math.min(interDistance, distance);
                    }
                }
                
                if (interDistance > intraDistance) {
                    totalScore += (interDistance - intraDistance) / interDistance;
                }
                count++;
            }
        }
        
        return count > 0 ? totalScore / count : 0.0;
    }
    
    public static String generateClusteringReport(@NotNull ClusteringResult result) {
        StringBuilder report = new StringBuilder();
        
        report.append("╔════════════════════════════════════════════════════════════╗\n");
        report.append("║                 LOG CLUSTERING ANALYSIS                     ║\n");
        report.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        report.append(String.format("Silhouette Score: %.3f\n\n", result.silhouetteScore));
        
        report.append("Cluster Summary:\n");
        result.clusterSizes.forEach((name, size) -> {
            report.append(String.format("  %s: %d entries\n", name, size));
        });
        
        report.append("\nThematic Groups:\n");
        result.thematicGroups.forEach((theme, entries) -> {
            report.append(String.format("  %s:\n", theme));
            entries.forEach(entry -> {
                report.append(String.format("    • %s\n", truncate(entry, 60)));
            });
        });
        
        return report.toString();
    }
    
    private static String truncate(String text, int length) {
        return text.length() > length ? text.substring(0, length) + "..." : text;
    }
}
