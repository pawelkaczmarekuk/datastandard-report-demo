package com.stibo.demo.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stibo.demo.report.model.Datastandard;
import org.apache.logging.log4j.util.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ReportService.class, ObjectMapper.class})
public class ReportServiceTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ReportService reportService;

    private Datastandard datastandard;

    @Before
    public void before() throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("datastandard.json");
        this.datastandard = objectMapper.readValue(stream, Datastandard.class);
    }

    @Test
    public void testReportLeaf() {
        List<List<String>> report = reportService.report(datastandard, "leaf").map(row -> row.collect(toList())).collect(toList());

        Assert.assertEquals("Leaf", report.get(0).get(0));
        Assert.assertEquals("Composite Value*", report.get(0).get(1));
        Assert.assertEquals("Composite Value Description", report.get(0).get(2));
        Assert.assertEquals("composite{\n  Composite Value*: composite\n  Nested Value: integer\n}[]", report.get(0).get(3));
        Assert.assertEquals("All\nComplex", report.get(0).get(4));
    }

    @Test
    public void testReportRoot() {
        List<List<String>> report = reportService.report(datastandard, "root").map(row -> row.collect(toList())).collect(toList());
        Assert.assertEquals("Root", report.get(0).get(0));
        Assert.assertEquals("String Value", report.get(0).get(1));
        Assert.assertEquals("", report.get(0).get(2));
        Assert.assertEquals("string", report.get(0).get(3));
        Assert.assertEquals("All", report.get(0).get(4));
    }

    @Test
    public void testNonExistingCategory() {
        List<List<String>> report = reportService.report(datastandard, "doesnotexist").map(row -> row.collect(toList())).collect(toList());
        Assert.assertTrue(report.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testDataStandardNull() {
        reportService.report(null, "root");
    }

    @Test(expected = NullPointerException.class)
    public void testCategoryIdNull() {
        reportService.report(datastandard, null);
    }
}
