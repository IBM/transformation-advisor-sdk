package com.ibm.ta.sdk.spi.assess;

import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.recommendation.*;

import java.util.*;
import java.util.stream.Collectors;

public class UTRecommendation extends RecommendationJson implements Recommendation {
    @Override
    public String getCollectionUnitName() {
        return collectionUnitName;
    }

    @Override
    public List<ComplexityContribution> getComplexityContributions() {
        List<ComplexityContribution>  cc = complexityRules.stream()
                .map(rules -> rules.getComplexityContribution())
                .collect(Collectors.toList());
        return cc;
    }

    @Override
    public List<IssueCategory> getIssueCategories() {
        return issueCategories.keySet().stream()
                .map(id -> new IssueCategory() {
                    @Override
                    public String getId() {
                        return id;
                    }

                    @Override
                    public String getTitle() {
                        return issueCategories.get(id).getIssueCategory().getTitle();
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Target> getTargets() {
        List<Map<String, Object>> targetMapList = (List<Map<String, Object>> ) assessmentUnits.get(0).get("targets");

        return targetMapList.stream()
                .map(targetMap -> new Target() {
                    @Override
                    public String getId() {
                       return (String) targetMap.get("id");
                    }

                    @Override
                    public String getRuntime() {
                        return (String) targetMap.get("runtime");
                    }

                    @Override
                    public List<ModDimension> getDimensions() {
                        List<Map<String, Object>> dimensions = (List<Map<String, Object>>) targetMap.get("dimensions");
                        return dimensions.stream()
                                .map(d -> new ModDimension(
                                       (String) d.get("name"),
                                       (List) d.get("values"),
                                       d.get("defaultValue")
                                ))
                                .collect(Collectors.toList());
                    }

                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Issue> getIssues(Target target, AssessmentUnit assessmentUnit) throws TAException {
        Map<String, Object> au = getAssessmentUnitByName(assessmentUnit.getName());
        if (au != null) {
            List<Map<String, Object>> targetMapList = (List<Map<String, Object>> ) au.get("targets");
            for (Map<String, Object> targetMap : targetMapList) {
                if (target.getId().equals(targetMap.get("id")) &&
                        target.getRuntime().equals(targetMap.get("runtime"))) {
                    Map<String, List<Map>> issues  = (Map<String, List<Map>>) targetMap.get("issues");
                    for (String key: issues.keySet()) {
                        for (Map issue : issues.get(key)) {
                            issue.put("category", key);
                        }
                    }

                    List<Issue> is = issues.values()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(issue -> new UTIssue(
                                    (String) issue.get("id"), (
                                    String) issue.get("title"),
                                    ((Double) issue.get("issueOverhead")).floatValue(),
                                    ((Double) issue.get("occurrencesCost")).floatValue(),
                                    (List<String>) issue.get("solutionText"),
                                    (String) issue.get("severity"),
                                    new IssueCategory() {
                                        @Override
                                        public String getId() {
                                            return (String) issue.get("category");
                                        }

                                        @Override
                                        public String getTitle() {
                                            return (String) issue.get("category");
                                        }
                                    },
                                    new Occurrence() {
                                        @Override
                                        public Map<String, String> getFieldKeys() {
                                            return (Map<String, String>) issue.get("occurrencesFields");
                                        }

                                        @Override
                                        public String getUniqueCountKey() {
                                            return null;
                                        }

                                        @Override
                                        public Integer getOccurrencesCount() {
                                            return ((Double) issue.get("occurrencesCount")).intValue();
                                        }

                                        @Override
                                        public List<Map<String, String>> getOccurrencesInstances() {
                                            return (List<Map<String, String>>) issue.get("occurrences");
                                        }

                                        @Override
                                        public void addOccurence(Map<String, String> occurence) {

                                        }

                                        @Override
                                        public void addOccurences(List<Map<String, String>> occurence) {

                                        }
                                    },
                                    new ComplexityContribution() {
                                        @Override
                                        public String getId() {
                                            return (String) issue.get("complexityRule");
                                        }

                                        @Override
                                        public String getName() {
                                            return (String) issue.get("complexityRule");
                                        }

                                        @Override
                                        public String getDescription() {
                                            return null;
                                        }

                                        @Override
                                        public ComplexityRating getComplexity() {
                                            return null;
                                        }

                                        @Override
                                        public List<String> getIssues() {
                                            return null;
                                        }

                                        @Override
                                        public List<String> getIssuesCategory() {
                                            return null;
                                        }
                                    }))
                            .collect(Collectors.toList());
                    return is;
                }
            }
        }

        return new ArrayList<>();
    }

    private Map<String, Object> getAssessmentUnitByName(String name) {
        for (Map<String, Object>  au : assessmentUnits) {
            if (name.equals(au.get("name"))) {
                return au;
            }
        }
        return null;
    }
}
