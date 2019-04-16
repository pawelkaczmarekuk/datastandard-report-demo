package com.stibo.demo.report.service;

import com.stibo.demo.report.model.*;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportService {

    public Stream<Stream<String>> report(final Datastandard datastandard, final String categoryId) {

        if (datastandard == null) {
            throw new NullPointerException("datastandard must not be null");
        }

        if (categoryId == null) {
            throw new NullPointerException("categoryId must not be null");
        }

        return datastandard
                .getCategories()
                .stream()
                .filter(category -> category.getId().equals(categoryId))
                .flatMap( category ->
                    category.getAttributeLinks().stream().map( attributeLink ->
                        findAttribute(datastandard, attributeLink)
                                .map( attribute -> {
                                    StringBuilder nestedTypes = new StringBuilder();
                                    resolveNestedTypes(datastandard, attributeLink, nestedTypes);
                                    return Stream.of(
                                            category.getName(),
                                            attributeLink.getOptional() ? String.format("%s*", attribute.getName()) : attribute.getName(),
                                            attribute.getDescription() == null ? "" : attribute.getDescription(),
                                            attribute.getType().getMultiValue() ? String.format("%s{\n%s\n}[]", attribute.getType().getId(), nestedTypes) : attribute.getType().getId(),
                                            datastandard.getAttributeGroups().stream().filter(attributeGroup -> attribute.getGroupIds().indexOf(attributeGroup.getId()) >= 0)
                                                    .map(AttributeGroup::getName).sorted().collect(Collectors.joining("\n"))
                                    );
                                }).orElseGet( null)

                    ).filter(Objects::nonNull));

    }

    private void resolveNestedTypes(final Datastandard datastandard, final AttributeLink theAttributeLink, StringBuilder stringBuilder) {
        stringBuilder.append("  ");
        findAttribute(datastandard, theAttributeLink).ifPresent(theAttribute -> {
            stringBuilder.append(String.format("%s%s: %s", theAttribute.getName(), theAttributeLink.getOptional() ? "*" : "", theAttribute.getType().getId()));
            if (theAttribute.getAttributeLinks() != null) {
                theAttribute.getAttributeLinks().forEach(attributeLink -> {
                    stringBuilder.append("\n");
                    resolveNestedTypes(datastandard, attributeLink, stringBuilder);
                });
            }
        });

    }

    private Optional<Attribute> findAttribute(final Datastandard datastandard, final AttributeLink attributeLink) {
        return datastandard.getAttributes().stream().filter(attribute -> attribute.getId().equals(attributeLink.getId())).findFirst();
    }

}

