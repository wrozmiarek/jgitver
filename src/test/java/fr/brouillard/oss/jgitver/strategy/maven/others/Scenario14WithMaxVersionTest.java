/**
 * Copyright (C) 2016 Matthieu Brouillard [http://oss.brouillard.fr/jgitver] (matthieu@brouillard.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.brouillard.oss.jgitver.strategy.maven.others;

import static fr.brouillard.oss.jgitver.impl.Lambdas.unchecked;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.jupiter.api.Test;

import fr.brouillard.oss.jgitver.Scenarios;
import fr.brouillard.oss.jgitver.Strategies;
import fr.brouillard.oss.jgitver.metadata.Metadatas;
import fr.brouillard.oss.jgitver.ScenarioTest;

public class Scenario14WithMaxVersionTest extends ScenarioTest {

    public Scenario14WithMaxVersionTest() {
        super(
                Scenarios::s14_with_merges,
                calculator -> calculator
                        .setStrategy(Strategies.MAVEN)
                        .setNonQualifierBranches("master,hotfix"));
    }

    @Test
    public void version_of_B_commit() {
        ObjectId cCommit = scenario.getCommits().get("B");

        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName(cCommit.name()).call());
        assertThat(versionCalculator.getVersion(), is("1.0.0-SNAPSHOT"));
    }

    @Test
    public void version_of_C_commit() {
        ObjectId cCommit = scenario.getCommits().get("C");

        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName(cCommit.name()).call());
        assertThat(versionCalculator.getVersion(), is("1.0.0-SNAPSHOT"));
    }

    @Test
    public void version_of_E_commit() {
        ObjectId cCommit = scenario.getCommits().get("E");

        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName(cCommit.name()).call());
        assertThat(versionCalculator.getVersion(), is("1.1.2-SNAPSHOT"));
    }

    @Test
    public void version_of_master() {
        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName("master").call());
        assertThat(versionCalculator.getVersion(), is("1.1.2-SNAPSHOT"));

        assertThat(versionCalculator.meta(Metadatas.NEXT_MAJOR_VERSION).get(), is("2.0.0"));
        assertThat(versionCalculator.meta(Metadatas.NEXT_MINOR_VERSION).get(), is("1.2.0"));
        assertThat(versionCalculator.meta(Metadatas.NEXT_PATCH_VERSION).get(), is("1.1.2"));
    }

    @Test
    public void version_of_branch_hotfix() {
        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName("hotfix").call());
        assertThat(versionCalculator.getVersion(), is("1.0.1"));
    }
}
