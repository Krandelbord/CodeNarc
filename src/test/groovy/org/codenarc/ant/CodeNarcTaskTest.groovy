/*
 * Copyright 2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codenarc.ant

import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Project
import org.apache.tools.ant.types.FileSet
import org.codenarc.ant.CodeNarcTask
import org.codenarc.ant.Report
import org.codenarc.report.HtmlReportWriter
import org.codenarc.test.AbstractTest

/**
 * Tests for the CodeNarc Ant Task
 *
 * @author Chris Mair
 * @version $Revision$ - $Date$
 */
class CodeNarcTaskTest extends AbstractTest {
    static final BASE_DIR = 'src/test/resources'
    static final RULESET_FILE = 'rulesets/RuleSet1.xml'
    static final RULESET_FILES = 'rulesets/RuleSet1.xml,rulesets/RuleSet2.xml'
    static final REPORT_FILE = 'NarcTaskHtmlReport.html'

    private codeNarcTask
    private fileSet
    private outputFile

    void testExecute_SingleRuleSetFile() {
        codeNarcTask.addFileset(fileSet)
        codeNarcTask.execute()
        assert codeNarcTask.ruleSet.rules.size() == 1
        verifyReportFile()
    }

    void testExecute_MultipleRuleSetFiles() {
        codeNarcTask.ruleSetFiles = RULESET_FILES
        codeNarcTask.addFileset(fileSet)
        codeNarcTask.execute()
        assert codeNarcTask.ruleSet.rules.size() == 3
        verifyReportFile()
    }

    void testExecute_RuleSetFileDoesNotExist() {
        codeNarcTask.ruleSetFiles = 'DoesNotExist.xml'
        codeNarcTask.addFileset(fileSet)
        shouldFailWithMessageContaining('DoesNotExist.xml') { codeNarcTask.execute() }
    }

    void testExecute_NullRuleSetFiles() {
        codeNarcTask.ruleSetFiles = null
        shouldFailWithMessageContaining('ruleSetFile') { codeNarcTask.execute() }
    }

    void testExecute_NullFileSet() {
        shouldFailWithMessageContaining('fileSet') { codeNarcTask.execute() }
    }

    void testAddConfiguredReport() {
        assert codeNarcTask.reportWriters.size() == 1
        assert codeNarcTask.reportWriters[0].class == HtmlReportWriter
        assert codeNarcTask.reportWriters[0].outputFile == REPORT_FILE
    }

    void testAddConfiguredReport_Second() {
        codeNarcTask.addConfiguredReport(new Report(type:'html', toFile:REPORT_FILE))
        assert codeNarcTask.reportWriters.size() == 2
    }

    void testAddConfiguredReport_InvalidReportType() {
        shouldFail(BuildException) { codeNarcTask.addConfiguredReport(new Report(type:'XXX', toFile:REPORT_FILE)) }
    }

    void testAddFileSet_Null() {
        shouldFailWithMessageContaining('fileSet') { codeNarcTask.addFileset(null) }
    }

    void testAddFileSet_Twice() {
        codeNarcTask.addFileset(fileSet)
        shouldFail(BuildException) { codeNarcTask.addFileset(fileSet) }
    }

    void setUp() {
        super.setUp()

        def project = new Project(basedir:'.')
        fileSet = new FileSet(dir:new File(BASE_DIR), project:project)
        fileSet.setIncludes('sourcewithdirs/**/*.groovy')

        codeNarcTask = new CodeNarcTask(project:project)
        codeNarcTask.addConfiguredReport(new Report(type:'html', toFile:REPORT_FILE))
        codeNarcTask.ruleSetFiles = RULESET_FILE
        outputFile = new File(REPORT_FILE)
    }

    void tearDown() {
        super.tearDown()
        outputFile.delete()
    }

    private void verifyReportFile() {
        assert outputFile.exists()
    }

}